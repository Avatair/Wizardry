package com.teamwizardry.wizardry.api.spell.vm;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.annotation.ContextRing;
import com.teamwizardry.wizardry.api.spell.annotation.ContextSuper;
import com.teamwizardry.wizardry.api.spell.annotation.MagicScriptBuiltin;
import com.teamwizardry.wizardry.api.spell.annotation.ModuleOverride;
import com.teamwizardry.wizardry.api.spell.module.ModuleInitException;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstance;
import com.teamwizardry.wizardry.api.spell.module.ModuleOverrideSuper;
import com.teamwizardry.wizardry.lib.vm.Action;
import com.teamwizardry.wizardry.lib.vm.ActionProcessor;
import com.teamwizardry.wizardry.lib.vm.command.ICommandGenerator;
import com.teamwizardry.wizardry.lib.vm.command.operable.IMagicCommandOperable;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.MagicScriptBuilder;
import com.teamwizardry.wizardry.lib.vm.command.program.factory.ProgramSequence;
import com.teamwizardry.wizardry.lib.vm.command.utils.RunUtils;
import com.teamwizardry.wizardry.lib.vm.utils.parser.ScriptParserException;

public class SpellProgramHandler {
	private static final String GENERICS_SOURCE = "/assets/wizardry/modules/scripts/generics.mgs";

	private static final String ROUTINE_INITMAIN = "initMain";

	private static final String HOOK_RUNSPELL = "hooks.onCasted";

	private static ProgramSequence generics = null;
	
	private final ScriptKey configuration;
	private final Map<String, BuiltinPointer> builtins; 
	private WizardryOperable initialState = null;
	
	private ICommandGenerator initRoutine;
	private ICommandGenerator runRoutine;
	
	public SpellProgramHandler(SpellRing spellChain) {
		if( spellChain.getParentRing() != null )
			throw new IllegalArgumentException("passed spellRing is not a root.");
		
		// Should be only initialized within a SpellRing
		this.configuration = scanProgramConfiguration( spellChain );
		this.builtins = scanBuiltins( spellChain );
		
		initProgram();
	}

	private void initProgram() {
		// NOTE: If exceptions are thrown. Don't quit minecraft!

		initialState = null;

		initRoutine = null;
		runRoutine = null;
		
		// Load scripts
		try {
			ProgramSequence[] assemblies = loadSources();
			
			initRoutine = RunUtils.compileProgram(ROUTINE_INITMAIN, assemblies);
			
			// Call initialization
			initialState = new WizardryOperable(builtins);
			ActionProcessor proc = RunUtils.runProgram(initialState, initRoutine);	// TODO: Handle exceptions
			logExceptions(proc);
			
			// Retrieve hooks
			String hook = getValue_String(initialState, HOOK_RUNSPELL, null);
			if( hook != null ) {
				runRoutine = RunUtils.compileProgram(hook, assemblies);
			}
			
			// TODO: Add more routines here ...

		} catch (Exception e) {
			initialState = null;
			
			// TODO: Handle proper way!
			e.printStackTrace();
		}
	}
	
	public boolean runProgram(ExecutionPhase phase) {
		if( initialState == null )
			return false; // If something was unsuccessful when loading.
		
		ActionProcessor proc;
		if( ExecutionPhase.RUN_TICK.equals(phase) ) {
			proc = RunUtils.runProgram(initialState, runRoutine);
		}
		else
			throw new IllegalArgumentException("Unknown execution phase " + phase);
		logExceptions(proc);
		
		return true;
	}
	
	private ProgramSequence[] loadSources() throws IOException, ScriptParserException {
		List<String> scripts = configuration.getScripts();
		
		ProgramSequence generics = getGenericsSource();
		
		if( scripts.size() > 0 ) {
			ProgramSequence[] result = new ProgramSequence[scripts.size()];
			for( int i = 0; i < scripts.size(); i ++ ) {
				String scriptFile = scripts.get(i);
	// TODO:			ResourceLocation loc = new ResourceLocation(scriptFile);
				result[i] = MagicScriptBuilder.createFromResource(scriptFile).provideDependency("generics", generics).build();
			}
			return result;
		}
		else {
			return new ProgramSequence[] {generics};
		}
	}
	
	private static ProgramSequence getGenericsSource() throws ScriptParserException, IOException {
		if( generics == null ) {
			generics = MagicScriptBuilder.createFromResource(GENERICS_SOURCE).build();
		}
		return generics;
	}

	private static ScriptKey scanProgramConfiguration(SpellRing spellChain) {
		ScriptKey key = new ScriptKey();

		ISpellProgramOverrides overrides = spellChain.getOverrideHandler().getConsumerInterface(ISpellProgramOverrides.class);
		overrides.appendScript(key);
		
		return key;
	}
	
	private static Map<String, BuiltinPointer> scanBuiltins(SpellRing spellChain) {
		SpellRing cur = spellChain;
		HashMap<String, BuiltinPointer> builtinMethods = new HashMap<>();
		
		while( cur != null ) {
			for( Entry<String, BuiltinMethod> entry : cur.getModule().getFactory().getBuiltins().entrySet() ) {
				
				BuiltinPointer ptr = new BuiltinPointer(entry.getKey(), cur, entry.getValue());
				builtinMethods.put(ptr.getBuiltinName(), ptr);
			}
			
			cur = cur.getChildRing();
		}
		
		// TODO: Call override to get more builtins
		
		return builtinMethods;
	}
	
	// TODO: Move to utils, all below!
	
	private static void logExceptions(ActionProcessor proc) {
		for( Action failedAction : proc.getFailedActions() ) {
			Exception exc = failedAction.getException();
			
			exc.printStackTrace();  // TODO: Handle proper way!
		}
	}
	
	private static String getValue_String(IMagicCommandOperable operable, String key, String defaultValue) {
		Object value = operable.getValue(key);
		if( value == null )
			return defaultValue;
		return value.toString();
	}
	
	////////////
	
	/**
	 * Returns a list of native methods from a given type, implementing them.
	 * 
	 * @param clazz the type.
	 * @param hasContext if <code>true</code> then the method may contain context parameters.
	 * @return a collection, mapping override names to their according methods. 
	 * @throws ModuleInitException if a method exists having invalid argument types or if some issues with reflection occurred.
	 */
	public static HashMap<String, BuiltinMethod> getBuiltinMethodsFromClass(Class<?> clazz) throws ModuleInitException {
		HashMap<String, BuiltinMethod> builtinMethods = new HashMap<>();
		
		// Determine VM-builtin methods via reflection
		
		// TODO: REFACTORING: Combine with ModuleOverrideHandler.getOverrideMethodsFromClass(Class<?>, boolean) and move to utils.
		
		// FIXME: Separation of concerns: Builtin methods are not part of factory. Move them or rename factory class appropriately.
		
		for(Method method : clazz.getMethods()) {
			MagicScriptBuiltin builtin = method.getDeclaredAnnotation(MagicScriptBuiltin.class);
			if( builtin == null )
				continue;
			
			if( builtinMethods.containsKey(builtin.value()) )
				throw new ModuleInitException("Multiple methods exist in class '" + clazz + "' with same builting name '" + builtin.value() + "'.");
			
			try {
				method.setAccessible(true);
			}
			catch(SecurityException e) {
				throw new ModuleInitException("Failed to aquire reflection access to method '" + method.toString() + "', annotated by @MagicScriptBuiltin.", e);
			}
			
			// Search for context parameters
			int idxContextParamRing = -1;
			Parameter[] params = method.getParameters();
			for( int i = 0; i < params.length; i ++ ) {
				Parameter param = params[i];
				if( param.isAnnotationPresent(ContextRing.class) ) {
					if( idxContextParamRing >= 0 )
						throw new ModuleInitException("Method '" + method.toString() + "' has invalid @ContextRing annotated parameter. It is not allowed on multiple parameters.");
					idxContextParamRing = i;
				}
			}
			
			BuiltinMethod binMethod = new BuiltinMethod(method, idxContextParamRing);
			builtinMethods.put(builtin.value(), binMethod);
		}
		
		return builtinMethods;
	}
	
	
	////////////
	
	public static class BuiltinMethod {
		private final Method method;
		private final MethodHandle methodHandle;
		private final int idxContextParamRing;
		
		BuiltinMethod(Method method, int idxContextParamRing) throws ModuleInitException {
			super();
			try {
				this.method = method;
				this.methodHandle = MethodHandles.lookup().unreflect(method);
				
				this.idxContextParamRing = idxContextParamRing >= 0 ? idxContextParamRing : -2;
			} catch (Exception e) {
				throw new ModuleInitException("Couldn't initialize override method binding. See cause.", e);
			}
		}

		public Method getMethod() {
			return method;
		}

		public MethodHandle getMethodHandle() {
			return methodHandle;
		}
		
		/**
		 * Returns the index of the parameter for the referenced ring if existing.
		 * 
		 * @return the index of the parameter for the referenced ring or a negative value if no such parameter exists.
		 */
		int getIdxContextParamRing() {
			return idxContextParamRing;
		}
	}
	
	static class BuiltinPointer {
		private final String builtinName;
		private final SpellRing spellRingWithBuiltin;
		private final BuiltinMethod baseMethod;
		
		public BuiltinPointer(String builtinName, SpellRing spellRingWithBuiltin, BuiltinMethod baseMethod) {
			super();
			this.builtinName = builtinName;
			this.spellRingWithBuiltin = spellRingWithBuiltin;
			this.baseMethod = baseMethod;
		}

		public String getBuiltinName() {
			return builtinName;
		}

		public SpellRing getSpellRingWithBuiltin() {
			return spellRingWithBuiltin;
		}

		public BuiltinMethod getBaseMethod() {
			return baseMethod;
		}
		
		public int getArgumentCount() {
			int countExtra = 0;
			
			int idxContextParamRing = baseMethod.getIdxContextParamRing();
			if( idxContextParamRing >= 0 )
				countExtra ++;
			
			return baseMethod.getMethod().getParameterCount() - countExtra;
		}
		
		/**
		 * Returns the module implementing the override method in case this pointer points to a spell chain.
		 * 
		 * @return the module or <code>null</code> if the implementation is a default implementation. 
		 */
		ModuleInstance getModule() {
			if( spellRingWithBuiltin == null )
				return null;
			return spellRingWithBuiltin.getModule();
		}
		
		/**
		 * Invokes the builtin implementation.
		 * 
		 * @param args passed arguments
		 * @return the return value from the override call.
		 * @throws Throwable any occurred exception thrown by the override implementation or by the Java Method Handler. 
		 */
		Object invoke(Object[] args) throws Throwable {
			// TODO: REFACTORING: Combine with ModuleOverrideHandler.OverridePointer.invoke(Object[]) and move to utils.

			// TODO: Type check. Needed for stack.
			
			int idxContextParamRing = baseMethod.getIdxContextParamRing();
			
			Object passedArgs[] = args;
			int countExtra = 1;
			if( idxContextParamRing >= 0 )
				countExtra ++;
			
			// Add extra arguments like this pointer a.s.o.
			passedArgs = new Object[args.length + countExtra];
			int i = 0;
			int j = 0;
			while( i < passedArgs.length ) {
				if( i == 0 ) {
					passedArgs[i] = getModule().getModuleClass();
				}
				else if( i == idxContextParamRing + 1 ) {
					passedArgs[i] = spellRingWithBuiltin;
				}
				else {
					passedArgs[i] = args[j];
					j ++;
				}
				i ++;
			}
			
			try {
				return baseMethod.getMethodHandle().invokeWithArguments(passedArgs);
			}
			catch(WrongMethodTypeException | ClassCastException e) {
				// NOTE: If this happens, then correctness of checks like "areMethodsCompatible()" a.s.o. need to be debugged.
				throw new IllegalStateException("Couldn't invoke call. See cause.", e);
			}
		}
	}

}
