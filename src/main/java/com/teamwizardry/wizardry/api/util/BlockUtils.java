package com.teamwizardry.wizardry.api.util;

import com.mojang.authlib.GameProfile;
import com.teamwizardry.wizardry.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Demoniaque.
 */
public final class BlockUtils {

	private final static UUID uuid = UUID.randomUUID();
	private final static GameProfile breaker = new GameProfile(uuid, "Wizardry Block Breaker");
	private final static GameProfile placer = new GameProfile(uuid, "Wizardry Block Placer");

	private BlockUtils() {
	}

	public static boolean isAnyAir(IBlockState state) {
		return state.getBlock() == Blocks.AIR || state.getBlock() == ModBlocks.FAKE_AIR;
	}

	public static boolean isAnyAir(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getBlock() == Blocks.AIR || state.getBlock() == ModBlocks.FAKE_AIR;
	}

	public static boolean isAnyAir(Block block) {
		return block == Blocks.AIR || block == ModBlocks.FAKE_AIR;
	}

	/**
	 * Tries placing a block safely and fires an event for it.
	 *
	 * @return Whether the block was successfully placed
	 */
	public static boolean placeBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable EntityPlayerMP player) {
		if (!world.isBlockLoaded(pos)) return false;

		EntityPlayerMP playerMP;
		if (player == null) {
			playerMP = new FakePlayer((WorldServer) world, placer);
			playerMP.setPosition(pos.getX(), pos.getY(), pos.getZ());
			playerMP.setSneaking(true);
		} else playerMP = player;

		if (!hasEditPermission(pos, playerMP)) return false;

		BlockEvent.PlaceEvent event = new BlockEvent.PlaceEvent(BlockSnapshot.getBlockSnapshot(world, pos), Blocks.AIR.getDefaultState(), playerMP, playerMP.getActiveHand());
		MinecraftForge.EVENT_BUS.post(event);
		if (!event.isCanceled()) {
			world.setBlockState(pos, state);
			world.notifyBlockUpdate(pos, state, state, 3);
			return true;
		}
		return false;
	}

	public static boolean placeBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing facing, @Nonnull ItemStack stack) {
		if (!world.isBlockLoaded(pos)) return false;

		FakePlayer player = new FakePlayer((WorldServer) world, placer);
		player.setPosition(pos.getX(), pos.getY(), pos.getZ());
		player.setHeldItem(EnumHand.MAIN_HAND, stack);
		player.setSneaking(true);

		if (!hasEditPermission(pos, player)) return false;
		if (!world.isBlockLoaded(pos)) return false;

		if (world.getTileEntity(pos) == null) {
			if (facing == null) {
				for (EnumFacing enumFacing : EnumFacing.VALUES) {
					EnumActionResult result = player.interactionManager.processRightClickBlock(
							player, world, stack, EnumHand.MAIN_HAND,
							pos, enumFacing, 0, 0, 0);
					if (result != EnumActionResult.FAIL) return true;
				}
			} else {
				EnumActionResult result = player.interactionManager.processRightClickBlock(
						player, world, stack, EnumHand.MAIN_HAND,
						pos, facing, 0, 0, 0);
				return result != EnumActionResult.FAIL;
			}
		}

		return false;
	}

	/**
	 * Tries breaking a block safely and fires an event for it.
	 *
	 * @return Whether the block was successfully broken
	 */
	public static boolean breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nullable IBlockState oldState, @Nullable EntityPlayerMP player, boolean drop) {
		if (!world.isBlockLoaded(pos)) return false;

		EntityPlayerMP playerMP;
		if (player == null) {
			playerMP = new FakePlayer((WorldServer) world, breaker);
			playerMP.setPosition(pos.getX(), pos.getY(), pos.getZ());
		} else playerMP = player;

		if (player != null && !(player instanceof FakePlayer))
			if (!hasBreakPermission(world, pos, playerMP)) return false;

		if (oldState == null) oldState = world.getBlockState(pos);

		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, oldState, playerMP);

		MinecraftForge.EVENT_BUS.post(event);
		return !event.isCanceled() && (drop ? world.destroyBlock(pos, true) : world.setBlockToAir(pos));
	}

	public static boolean hasBreakPermission(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayerMP player) {
		return hasEditPermission(pos, player) && !(ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos) == -1);
	}

	public static boolean hasEditPermission(@Nonnull BlockPos pos, @Nonnull EntityPlayerMP player) {
		if (FMLCommonHandler.instance().getMinecraftServerInstance().isBlockProtected(player.getEntityWorld(), pos, player))
			return false;
		IBlockState block = player.getEntityWorld().getBlockState(pos);
		if(block.getBlockHardness(player.getEntityWorld(), pos) == -1.0F && !player.capabilities.isCreativeMode) return false;
		for (EnumFacing e : EnumFacing.VALUES)
			if (!player.canPlayerEdit(pos, e, player.getHeldItemMainhand()))
				return false;
		return true;
	}

	public static Set<BlockPos> blocksInSquare(BlockPos center, Axis axis, int maxBlocks, int maxRange, Predicate<BlockPos> ignore)
	{	
		Set<BlockPos> blocks = new HashSet<>();
		if (ignore.test(center)) return blocks;
		blocks.add(center);
		if (blocks.size() >= maxBlocks) return blocks;
		
		Queue<BlockPos> blockQueue = new LinkedList<>();
		blockQueue.add(center);
		
		while (!blockQueue.isEmpty())
		{
			BlockPos pos = blockQueue.remove();
			
			for (EnumFacing facing : EnumFacing.VALUES)
			{
				if (facing.getAxis() == axis)
					continue;
				BlockPos shift = pos.offset(facing);
				if (shift.getX() - center.getX() > maxRange || center.getX() - shift.getX() > maxRange)
					continue;
				if (shift.getY() - center.getY() > maxRange || center.getY() - shift.getY() > maxRange)
					continue;
				if (shift.getZ() - center.getZ() > maxRange || center.getZ() - shift.getZ() > maxRange)
					continue;
				if (blocks.contains(shift))
					continue;
				if (ignore.test(shift))
					continue;
				blocks.add(shift);
				blockQueue.add(shift);
				if (blocks.size() >= maxBlocks)
					break;
			}
			
			if (blocks.size() >= maxBlocks)
				break;
		}
		
		return blocks;
	}
	
	public static Set<BlockPos> blocksInSquare(BlockPos center, EnumFacing facing, int maxBlocks, int maxRange, Predicate<BlockPos> ignore)
	{
		return blocksInSquare(center, facing.getAxis(), maxBlocks, maxRange, ignore);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends TileEntity> T getTileEntity(IBlockAccess world, BlockPos pos, Class<T> clazz) {
		TileEntity te = world.getTileEntity(pos);
		if( !clazz.isInstance(te) )
			return null;
		return (T)te;
	}

}
