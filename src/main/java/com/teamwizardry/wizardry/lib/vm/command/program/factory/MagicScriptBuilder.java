package com.teamwizardry.wizardry.lib.vm.command.program.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.teamwizardry.wizardry.lib.vm.command.program.UnsatisfiedLinkException;
import com.teamwizardry.wizardry.lib.vm.command.type.CallNativeCommand;
import com.teamwizardry.wizardry.lib.vm.command.type.EchoCommand;
import com.teamwizardry.wizardry.lib.vm.command.type.LoadCommand;
import com.teamwizardry.wizardry.lib.vm.command.type.PopValueCommand;
import com.teamwizardry.wizardry.lib.vm.command.type.PushValueCommand;
import com.teamwizardry.wizardry.lib.vm.command.type.SetValueCommand;
import com.teamwizardry.wizardry.lib.vm.command.type.StoreCommand;
import com.teamwizardry.wizardry.lib.vm.utils.parser.ParserUtils;
import com.teamwizardry.wizardry.lib.vm.utils.parser.ScriptParserException;
import com.teamwizardry.wizardry.lib.vm.utils.parser.StringTokenizer;
import com.teamwizardry.wizardry.lib.vm.utils.parser.StringTokenizer.Token;
import com.teamwizardry.wizardry.lib.vm.utils.parser.StringTokenizer.TokenType;


public class MagicScriptBuilder {
	private final String input;
	private final HashMap<String, ProgramSequence> dependencies = new HashMap<>();
	
	public MagicScriptBuilder(String input) {
		this.input = input;
	}
	
	public MagicScriptBuilder(InputStream input) throws IOException {
		this.input = ParserUtils.readFromStream(input);
	}
	
	public MagicScriptBuilder provideDependency(String withName, ProgramSequence toImport) {
		this.dependencies.put(withName, toImport);
		return this;
	}
	
	public ProgramSequence build() throws ScriptParserException {
		try {		
			ProgramSequence newPrg = new ProgramSequence();
			
			StringTokenizer tokenizer = new StringTokenizer(input);
			while(tokenizer.hasNextToken()) {
				Token operation = tokenizer.getNextToken();
				
				if( operation.isKeyword("import") ) {
					parseImport(newPrg, tokenizer);
				}
				else if( operation.isKeyword("map") ) {
					parseMapTo(newPrg, tokenizer);
				}
				else if( operation.isKeyword("proc") ) {
					Token frameName = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
					
					// Do some proc init
					newPrg.beginFrame(frameName.toString());
					
					parseProcedure(newPrg, tokenizer);
					
					newPrg.addReturn();
					newPrg.endFrame();
				}
				else if( operation.isKeyword("edit") ) {
					operation = tokenizer.getNextToken();
					if( operation.isKeyword("proc") ) {
						// scan first arguments
						Token frameName = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
											
						ProgramSequence targetSequence;
						String frameNameNode;
						
						if( frameName.getCountDomainNodes() > 1 ) {
							String targetSequenceName = frameName.getSubDomain(0, frameName.getCountDomainNodes() - 1);
							String targetName = frameName.getDomainNode(frameName.getCountDomainNodes() - 1);
							
							targetSequence = dependencies.get(targetSequenceName);
							if( targetSequence == null )
								throw new ScriptParserException("Script '" + targetSequenceName + "' is not existing.");
							
							frameNameNode = targetName;
						}
						else {
							targetSequence = newPrg;
							frameNameNode = frameName.getDomainNode(0);
						}
						
						// Do some proc init
						targetSequence.editFrame(frameNameNode);
						
						Token location = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
						
						if( location.isKeyword("before") ) {
							Token injectionLabel = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
							
							// seek to point
							targetSequence.seekTo(injectionLabel.toString(), 0);
						}
						else if( location.isKeyword("at") ) {
							Token injectionLabel = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
	
							targetSequence.seekTo(injectionLabel.toString(), 1);
						}
						else if( location.isKeyword("end") ) {
							targetSequence.seekTo(null, 0);
						}
						else
							throw new ScriptParserException("Invalid location '" + location + "': Must be 'at' or 'before', 'end'.");
						
						parseProcedure(targetSequence, tokenizer);
	
						targetSequence.endFrame();
					}
					else
						throw new ScriptParserException("Unexpected syntax " + operation);
				}
				else if( !operation.isSign(';') )
					throw new ScriptParserException("Unexpected syntax " + operation);
			}
		
			return newPrg;
		}
		catch(UnsatisfiedLinkException exc) {
			throw new ScriptParserException("Found an unsatisfied link. See cause.", exc);
		}
	}
	
	////
	
	private void parseImport(ProgramSequence newPrg, StringTokenizer tokenizer) throws ScriptParserException {
		Token srcScript = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
		tokenizer.getNextToken().expectSign(';');
		
		// Add import
		ProgramSequence other = dependencies.get(srcScript.toString());
		if( other == null )
			throw new ScriptParserException("Script '" + srcScript + "' not existing.");
		// TODO: Check for cyclic dependencies !!!
		newPrg.importProgram(other);
	}
	
	private void parseMapTo(ProgramSequence newPrg, StringTokenizer tokenizer) throws ScriptParserException {
		Token alias = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
		tokenizer.getNextToken().expectKeyword("to");
		Token asFrameName = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
		tokenizer.getNextToken().expectSign(';');
		
		// Add alias
		newPrg.addFrameAlias(alias.toString(), asFrameName.toString());
	}
	
	private void parseProcedure(ProgramSequence targetPrg, StringTokenizer tokenizer) throws ScriptParserException {
		int numGeneratedLabels = 0;
		
		tokenizer.getNextToken().expectSign('{');
		while(tokenizer.hasNextToken()) {
			Token token = tokenizer.getNextToken();
			if( token.isSign('}') ) {
				// Close procedure
				return;
			}
			
			if( token.isKeyword("call") ) {
				token = tokenizer.getNextToken();
				
				if( token.isKeyword("native") ) {
					Token identifier = tokenizer.getNextToken();
					identifier.expectCompatibleTypes(TokenType.KEYWORD);
					tokenizer.getNextToken().expectSign(';');
					
					// Add a call native command
					targetPrg.addCommand(new CallNativeCommand(identifier.toString()));
				}
				else {
					Token targetFrame = token;
					targetFrame.expectCompatibleTypes(TokenType.KEYWORD);
					
					token = tokenizer.getNextToken();
					
					LinkedList<String> branches = new LinkedList<>();
					if( token.isKeyword("branching") ) {
						parseListUntilSemicolon(branches, tokenizer);
					}
					else { 
						token.expectSign(';');
					}
					
					// Add an invoke command with optional branches
					targetPrg.addFrameCall(targetFrame.toString());
					if( !branches.isEmpty() )
						targetPrg.assignFork(branches.toArray(new String[branches.size()]));
				}
			}
			else if( token.isKeyword("set") ) {
				tokenizer.getNextToken().expectSign('$');
				Token variableName = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
				tokenizer.getNextToken().expectSign('=');
				
				Token value = tokenizer.getNextToken();
				boolean isVariable = false;
				if( value.isSign('$') ) {
					isVariable = true;
					value = tokenizer.getNextToken();
					value.expectCompatibleTypes(TokenType.KEYWORD);
				}
				else
					value.expectCompatibleTypes(TokenType.TEXT, TokenType.INTEGER, TokenType.KEYWORD);
				tokenizer.getNextToken().expectSign(';');
				
				// Add set command
				targetPrg.addCommand(new SetValueCommand(variableName.toString(), value.toValue(), isVariable));
			}
			else if( token.isKeyword("push") ) {
				Token value = tokenizer.getNextToken();
				boolean isVariable = false;
				if( value.isSign('$') ) {
					isVariable = true;
					value = tokenizer.getNextToken();
					value.expectCompatibleTypes(TokenType.KEYWORD);
				}
				else
					value.expectCompatibleTypes(TokenType.TEXT, TokenType.INTEGER, TokenType.KEYWORD);
				
				// Add push command
				targetPrg.addCommand(new PushValueCommand(value.toValue(), isVariable));
			}
			else if( token.isKeyword("pop") ) {
				token = tokenizer.getNextToken();
				Token variableName = null;
				if( token.isSign('$') ) {
					variableName = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
					token = tokenizer.getNextToken();
				}
				token.expectSign(';');
				
				// Add pop command
				targetPrg.addCommand(new PopValueCommand(variableName!= null ? variableName.toString() : null));
			}
			else if( token.isKeyword("jump") ) {
				Token label = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
				tokenizer.getNextToken().expectSign(';');
				
				// Add a fork command
				targetPrg.addForkTo(label.toString());
			}
			else if( token.isKeyword("fork") ) {
				// Subcommands for fork
				token = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);

				String genLabel = "@@GENERATED_LABEL" + (++ numGeneratedLabels);
				if( token.isKeyword("jump") ) {
					LinkedList<String> branches = new LinkedList<>();
					branches.add(genLabel);
					
					parseListUntilSemicolon(branches, tokenizer);
					
					// Add a fork command
					targetPrg.addForkTo(branches.toArray(new String[branches.size()]));
				}
				else if( token.isKeyword("call") ) {
					// TODO: Need a proper test. Also an extension is required, because no proper exit method exists for an executed stream.
					//       It will already fail if implementing Turtle example 3a on magic script.
					
					Token targetFrame = token;
					targetFrame.expectCompatibleTypes(TokenType.KEYWORD);
					
					token = tokenizer.getNextToken();
					
					LinkedList<String> branches = new LinkedList<>();
					if( token.isKeyword("branching") ) {
						parseListUntilSemicolon(branches, tokenizer);
					}
					else {
						token.expectSign(';');
					}
					
					// Add a fork command
					String genLabel2 = "@@GENERATED_LABEL" + (++ numGeneratedLabels);
					targetPrg.addForkTo(genLabel2, genLabel);
					targetPrg.addLabel(genLabel2);
					
					// Add an invoke command with optional branches
					targetPrg.addFrameCall(targetFrame.toString());
					if( !branches.isEmpty() )
						targetPrg.assignFork(branches.toArray(new String[branches.size()]));
					targetPrg.addStop(false);	// Kill forked command stream.
				}
				else	// NOTE: Change exception text if new commands were added.
					throw new ScriptParserException("Unknown fork command '" + token + "'. Only 'fork jump' and 'fork call' is supported.");

				// Add label afterwards
				targetPrg.addLabel(genLabel);
			}
			else if( token.isKeyword("stop") ) {
				tokenizer.getNextToken().expectSign(';');
				
				// TODO: Need a more friendly exit method than int3 as it sounds like a fault.
				
				// Add stop
				targetPrg.addStop(false);
			}
			else if( token.isKeyword("nop") ) {
				int countTicks = 1;
				
				token = tokenizer.getNextToken();
				if( token.isType(TokenType.INTEGER) ) {
					countTicks = token.toInteger();
					if( countTicks < 1 )
						throw new ScriptParserException("Illegal amount of ticks. Expected a positive number, but got " + countTicks);
					token = tokenizer.getNextToken();
				}
				token.expectSign(';');
				
				// Add NOP
				targetPrg.addNop(countTicks);
			}
			else if( token.isKeyword("int") ) {
				tokenizer.getNextToken().expectInteger(3);
				tokenizer.getNextToken().expectSign(';');
				
				// Add int3
				targetPrg.addStop(true);
			}
			else if( token.isKeyword("return") ) {
				token = tokenizer.getNextToken();
				if( token.isSign(';') ) {
					targetPrg.addReturn();
				}
				else if( token.isType(TokenType.INTEGER) ) {
					int toForkIdx = token.toInteger();
					targetPrg.addReturn(toForkIdx);
				}
				else
					throw new ScriptParserException("Unexpected token '" + token + "'");
			}
			else if( !parseCommand( targetPrg, token, tokenizer ) ) {
				if( token.isType(TokenType.KEYWORD) ) {
					Token labelName = token;
					tokenizer.getNextToken().expectSign(':');
					
					// Add label
					targetPrg.addLabel(labelName.toString());
				}
				else if( !token.isSign(';') )
					throw new ScriptParserException("Unexpected token '" + token + "'");
			}
		}
		
		throw new ScriptParserException("Unexpected end of procedure.");
	}
	
	private boolean parseCommand( ProgramSequence targetPrg, Token token, StringTokenizer tokenizer ) throws ScriptParserException {
		if( token.isKeyword("store") ) {
			String pointer = null;
			boolean isPointerVariable = false;
			
			Object value = null;
			boolean isValueVariable = false;
			
			Object arg1 = null;
			boolean isArg1Variable = false;
			
			token = tokenizer.getNextToken();
			if( token.isSign('$') ) {
				isArg1Variable = true;
				token = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
				arg1 = token.toValue();
			}
			else if( token.isType(TokenType.TEXT) ) {
				arg1 = token.toValue();
			}
			
			if( arg1 != null ) {
				token = tokenizer.getNextToken();
				if( token.isSign('=') ) {
					pointer = arg1.toString();
					isPointerVariable = isArg1Variable;
					
					token = tokenizer.getNextToken();
					if( token.isSign('$') ) {
						isValueVariable = true;
						token = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
						value = token.toString();
					}
					else {
						token.expectCompatibleTypes(TokenType.TEXT);
						value = token.toString();
					}
					
					token = tokenizer.getNextToken();
				}
				else {
					value = arg1;
					isValueVariable = isArg1Variable;
				}
			}

			token.expectSign(';');
			
			// Add store command
			targetPrg.addCommand(new StoreCommand(pointer, isPointerVariable, value, isValueVariable) );
		}
		else if( token.isKeyword("load") ) {
			String fromPointer = null;
			boolean isPointerVariable = false;
			String targetVariable = null;
			
			String arg1 = null;
			boolean isArg1Variable = false;
			
			token = tokenizer.getNextToken();
			if( token.isSign('$') ) {
				isArg1Variable = true;
				token = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
				arg1 = token.toString();
			}
			else if( token.isType(TokenType.TEXT) ) {
				arg1 = token.toString();
			}
			
			if( arg1 != null ) {
				token = tokenizer.getNextToken();
				if( token.isSign('=') ) {
					targetVariable = arg1;
					if( !isArg1Variable )
						throw new ScriptParserException("First argument is expected to be a variable.");
					
					token = tokenizer.getNextToken();
					if( token.isSign('$') ) {
						isPointerVariable = true;
						token = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
						fromPointer = token.toString();
					}
					else {
						token.expectCompatibleTypes(TokenType.TEXT);
						fromPointer = token.toString();
					}
					
					token = tokenizer.getNextToken();
				}
				else {
					fromPointer = arg1;
					isPointerVariable = isArg1Variable;
				}
			}

			token.expectSign(';');
			
			// Add load command
			targetPrg.addCommand(new LoadCommand(fromPointer, isPointerVariable, targetVariable));
		}
		else if( token.isKeyword("echo") ) {
			token = tokenizer.getNextToken();
			
			boolean isStdErr = false;
			if( token.isKeyword("stdout") ) {
				token = tokenizer.getNextToken();				
			}
			else if( token.isKeyword("stderr") ) {
				isStdErr = true;
				token = tokenizer.getNextToken();
			}
			
			boolean isVariable = false;
			Object outputWhat = null;
			if( token.isSign('$') ) {
				isVariable = true;
				token = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
				outputWhat = token.toValue();
				token = tokenizer.getNextToken();
			}
			else if( token.isType(TokenType.TEXT) ) {
				outputWhat = token.toString();
				token = tokenizer.getNextToken();
			}

			token.expectSign(';');

			// Add echo command
			targetPrg.addCommand(new EchoCommand(isStdErr, outputWhat, isVariable));
		}
		else
			return false;
		
		return true;
	}	
	
	private void parseListUntilSemicolon( List<String> outList, StringTokenizer tokenizer ) throws ScriptParserException {
		if( !tokenizer.hasNextToken() )
			throw new ScriptParserException("Expected a token.");
		
		while(tokenizer.hasNextToken()) {
			Token name = tokenizer.getNextToken().expectCompatibleTypes(TokenType.KEYWORD);
			outList.add(name.toString());
			
			Token token = tokenizer.getNextToken();
			if( token.isSign(',') )
				continue;
			else if( token.isSign(';') )
				break;
			else
				throw new ScriptParserException("Unexpected token '" + token + "'");
		}
	}
	
	////

	
	public static MagicScriptBuilder createFromResource(String resName) throws IOException {
		InputStream stream = MagicScriptBuilder.class.getResourceAsStream(resName);
		if( stream == null )
			throw new IOException("Couldn't open resource '" +resName + "'");
		try {
			return new MagicScriptBuilder(stream);
		}
		finally {
			try {
				stream.close();
			}
			catch(IOException exc) {
				// TODO: Log it!
				exc.printStackTrace();
			}
		}
	}
}
