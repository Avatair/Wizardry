package com.teamwizardry.wizardry.api.capability.mana;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by Demoniaque.
 */
public final class CapManager {

	public static CapManagerBuilder forObject(@Nullable Entity entity) {
		return new CapManagerBuilder(entity);
	}

	public static CapManagerBuilder forObject(@Nullable ItemStack stack) {
		return new CapManagerBuilder(stack);
	}

	public static CapManagerBuilder forObject(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		return new CapManagerBuilder(world, pos, facing);
	}

	public static CapManagerBuilder forObject(@Nullable IWizardryCapability cap) {
		return new CapManagerBuilder(cap);
	}

	@Nullable
	private static IWizardryCapability getCap(@Nullable ItemStack stack) {
		return stack != null && !stack.isEmpty() ? WizardryCapabilityProvider.getCap(stack) : null;
	}

	@Nullable
	private static IWizardryCapability getCap(@Nullable Entity entity) {
		return entity != null ? WizardryCapabilityProvider.getCap(entity) : null;

	}

	@Nullable
	private static IWizardryCapability getCap(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		return world == null || pos == null ? null : world.isRemote ? null : WizardryCapabilityProvider.getCap(world, pos, facing);
	}

	public static double getMaxMana(@Nullable IWizardryCapability cap) {
		if (cap == null) return 0;
		return cap.getMaxMana();
	}

	public static double getMaxMana(@Nullable Entity entity) {
		IWizardryCapability cap = getCap(entity);
		if (cap == null) return 0;
		return cap.getMaxMana();
	}

	public static double getMaxMana(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		IWizardryCapability cap = getCap(world, pos, facing);
		if (cap == null) return 0;
		return cap.getMaxMana();
	}

	public static double getMaxMana(@Nullable ItemStack stack) {
		IWizardryCapability cap = getCap(stack);
		if (cap == null) return 0;
		return cap.getMaxMana();
	}

	public static double getMana(@Nullable IWizardryCapability cap) {
		if (cap == null) return 0;
		return cap.getMana();
	}

	public static double getMana(@Nullable Entity entity) {
		IWizardryCapability cap = getCap(entity);
		if (cap == null) return 0;
		return cap.getMana();
	}

	public static double getMana(@Nullable ItemStack stack) {
		IWizardryCapability cap = getCap(stack);
		if (cap == null) return 0;
		return cap.getMana();
	}

	public static double getMana(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		IWizardryCapability cap = getCap(world, pos, facing);
		if (cap == null) return 0;
		return cap.getMana();
	}

	public static double getBurnout(@Nullable IWizardryCapability cap) {
		if (cap == null) return 0;
		return cap.getBurnout();
	}

	public static double getBurnout(@Nullable ItemStack stack) {
		IWizardryCapability cap = getCap(stack);
		if (cap == null) return 0;
		return cap.getBurnout();
	}

	public static double getBurnout(@Nullable Entity entity) {
		IWizardryCapability cap = getCap(entity);
		if (cap == null) return 0;
		return cap.getBurnout();
	}

	public static double getBurnout(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		IWizardryCapability cap = getCap(world, pos, facing);
		if (cap == null) return 0;
		return cap.getBurnout();
	}

	public static double getMaxBurnout(@Nullable IWizardryCapability cap) {
		if (cap == null) return 0;
		return cap.getMaxBurnout();
	}

	public static double getMaxBurnout(@Nullable ItemStack stack) {
		IWizardryCapability cap = getCap(stack);
		if (cap == null) return 0;
		return cap.getMaxBurnout();
	}

	public static double getMaxBurnout(@Nullable Entity entity) {
		IWizardryCapability cap = getCap(entity);
		if (cap == null) return 0;
		return cap.getMaxBurnout();
	}

	public static double getMaxBurnout(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		IWizardryCapability cap = getCap(world, pos, facing);
		if (cap == null) return 0;
		return cap.getMaxBurnout();
	}

	public static boolean isManaFull(@Nullable IWizardryCapability cap) {
		return cap != null && cap.getMana() >= cap.getMaxMana();
	}

	public static boolean isManaFull(@Nullable Entity entity) {
		IWizardryCapability cap = getCap(entity);
		return cap != null && cap.getMana() >= cap.getMaxMana();
	}

	public static boolean isManaFull(@Nullable ItemStack stack) {
		IWizardryCapability cap = getCap(stack);
		return cap != null && cap.getMana() >= cap.getMaxMana();
	}

	public static boolean isManaFull(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		IWizardryCapability cap = getCap(world, pos, facing);
		return cap != null && cap.getMana() >= cap.getMaxMana();
	}

	public static boolean isBurnoutFull(@Nullable ItemStack stack) {
		IWizardryCapability cap = getCap(stack);
		return cap != null && cap.getBurnout() >= cap.getMaxBurnout();
	}

	public static boolean isBurnoutFull(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		IWizardryCapability cap = getCap(world, pos, facing);
		return cap != null && cap.getBurnout() >= cap.getMaxBurnout();
	}

	public static boolean isManaEmpty(@Nullable IWizardryCapability cap) {
		return cap != null && cap.getMana() <= 0;
	}

	public static boolean isManaEmpty(@Nullable ItemStack stack) {
		IWizardryCapability cap = getCap(stack);
		return cap != null && cap.getMana() <= 0;
	}

	public static boolean isManaEmpty(@Nullable Entity entity) {
		IWizardryCapability cap = getCap(entity);
		return cap != null && cap.getMana() <= 0;
	}

	public static boolean isManaEmpty(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		IWizardryCapability cap = getCap(world, pos, facing);
		return cap != null && cap.getMana() <= 0;
	}

	public static boolean isBurnoutEmpty(@Nullable IWizardryCapability cap) {
		return cap != null && cap.getBurnout() <= 0;
	}

	public static boolean isBurnoutEmpty(@Nullable Entity entity) {
		IWizardryCapability cap = getCap(entity);
		return cap != null && cap.getBurnout() <= 0;
	}

	public static boolean isBurnoutEmpty(@Nullable ItemStack stack) {
		IWizardryCapability cap = getCap(stack);
		return cap != null && cap.getBurnout() <= 0;
	}

	public static boolean isBurnoutEmpty(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
		IWizardryCapability cap = getCap(world, pos, facing);
		return cap != null && cap.getBurnout() <= 0;
	}

	public static class CapManagerBuilder implements AutoCloseable {

		@Nullable
		private IWizardryCapability cap = null;
		@Nullable
		private Entity entity = null;

		private boolean somethingChanged = false;

		CapManagerBuilder(@Nullable Entity entity) {
			this.entity = entity;
			if (entity != null) {
				cap = WizardryCapabilityProvider.getCap(entity);
			}
		}

		CapManagerBuilder(@Nullable ItemStack stack) {
			if (stack != null && !stack.isEmpty()) {
				cap = WizardryCapabilityProvider.getCap(stack);
			}
		}

		CapManagerBuilder(@Nullable IWizardryCapability cap) {
			this.cap = cap;
		}

		CapManagerBuilder(@Nullable World world, @Nullable BlockPos pos, @Nullable EnumFacing facing) {
			if (world == null || pos == null) return;
			if (world.isRemote) return;

			cap = WizardryCapabilityProvider.getCap(world, pos, facing);
		}

		public CapManagerBuilder addMana(double mana) {
			setMana(getMana() + mana);
			return this;
		}

		public CapManagerBuilder removeMana(double mana) {
			setMana(getMana() - mana);
			return this;
		}

		public CapManagerBuilder addBurnout(double burnout) {
			setBurnout(getBurnout() + burnout);
			return this;
		}

		public CapManagerBuilder removeBurnout(double burnout) {
			setBurnout(getBurnout() - burnout);
			return this;
		}

		public double getMaxMana() {
			if (cap == null) return 0;
			return cap.getMaxMana();
		}

		public CapManagerBuilder setMaxMana(double mana) {
			if (cap == null) return this;

			if (getMaxMana() != mana) {
				cap.setMaxMana(mana);
				somethingChanged = true;
			}
			if (getMana() > mana) {
				cap.setMana(mana);
				somethingChanged = true;
			}
			return this;
		}

		public double getMana() {
			if (cap == null) return 0;
			return cap.getMana();
		}

		public CapManagerBuilder setMana(double mana) {
			if (cap == null) return this;
			double clamped = MathHelper.clamp(mana, 0, getMaxMana());

			if (cap.getMana() != clamped) {
				cap.setMana(clamped);
				somethingChanged = true;
			}
			return this;
		}

		public double getBurnout() {
			if (cap == null) return 0;
			return cap.getBurnout();
		}

		public CapManagerBuilder setBurnout(double burnout) {
			if (cap == null) return this;
			double clamped = MathHelper.clamp(burnout, 0, getMaxBurnout());

			if (cap.getBurnout() != clamped) {
				cap.setBurnout(clamped);
				somethingChanged = true;
			}
			return this;
		}

		public double getMaxBurnout() {
			if (cap == null) return 0;
			return cap.getMaxBurnout();
		}

		public CapManagerBuilder setMaxBurnout(double burnout) {
			if (cap == null) return this;

			if (getMaxBurnout() != burnout) {
				cap.setMaxBurnout(burnout);
				somethingChanged = true;
			}
			if (getBurnout() > burnout) {
				cap.setBurnout(burnout);
				somethingChanged = true;
			}
			return this;
		}

		public boolean isManaFull() {
			return cap != null && cap.getMana() >= cap.getMaxMana();
		}

		public boolean isBurnoutFull() {
			return cap != null && cap.getBurnout() >= cap.getMaxBurnout();
		}

		public boolean isManaEmpty() {
			return cap != null && cap.getMana() <= 0;
		}

		public boolean isBurnoutEmpty() {
			return cap != null && cap.getBurnout() <= 0;
		}

		@Override
		public void close() {
			if (somethingChanged && cap != null && entity != null && !entity.world.isRemote) {
				cap.dataChanged(entity);
			}
		}
	}
}
