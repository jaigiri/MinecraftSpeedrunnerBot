package com.daytrip.pathfinding;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PathfindingGrid {
	private static final List<Block> dangerousBlocks = new ArrayList<>();
	private final World world;

	public PathfindingGrid(World world) {
		this.world = world;
		if (dangerousBlocks.isEmpty()) {
			dangerousBlocks.add(Blocks.LAVA);
			dangerousBlocks.add(Blocks.MAGMA_BLOCK);
			dangerousBlocks.add(Blocks.COBWEB);
			dangerousBlocks.add(Blocks.FIRE);
			dangerousBlocks.add(Blocks.SOUL_FIRE);
			dangerousBlocks.add(Blocks.CAMPFIRE);
			dangerousBlocks.add(Blocks.SOUL_CAMPFIRE);
		}
	}

	private boolean isWalkable(BlockPos pos) {
		if (pos.getState(world).isSolid() || pos.up().getState(world).isSolid()) {
			return false;
		}
		int i = 0;
		BlockState bottomType;
		while (true) {
			i++;
			if (pos.down(i).getState(world).isSolid()) {
				bottomType = pos.down(i).getState(world);
				break;
			}
		}
		if (i > 5) {
			return false;
		}
		return !dangerousBlocks.contains(bottomType.getBlock());
	}

	public List<BlockPos> getNeighbors(BlockPos pos, boolean corners, BlockPos start) {
		List<BlockPos> positions = new ArrayList<>();
		BlockPos left = pos.add(-1, 0, 0);
		BlockPos right = pos.add(1, 0, 0);
		BlockPos up = pos.add(0, 0, 1);
		BlockPos down = pos.add(0, 0, -1);
		AxisAlignedBB boundingBox = new AxisAlignedBB(start).grow(300);
		if (boundingBox.contains(left.asVector()) && isWalkable(left)) positions.add(left);
		if (boundingBox.contains(right.asVector()) && isWalkable(right)) positions.add(right);
		if (boundingBox.contains(up.asVector()) && isWalkable(up)) positions.add(up);
		if (boundingBox.contains(down.asVector()) && isWalkable(down)) positions.add(down);
		if (corners) {
			BlockPos bottomLeft = pos.add(-1, 0, -1);
			BlockPos bottomRight = pos.add(1, 0, -1);
			BlockPos topLeft = pos.add(-1, 0, 1);
			BlockPos topRight = pos.add(1, 0, 1);
			if (boundingBox.contains(bottomLeft.asVector()) && isWalkable(bottomLeft)) positions.add(bottomLeft);
			if (boundingBox.contains(bottomRight.asVector()) && isWalkable(bottomRight)) positions.add(bottomRight);
			if (boundingBox.contains(topLeft.asVector()) && isWalkable(topLeft)) positions.add(topLeft);
			if (boundingBox.contains(topRight.asVector()) && isWalkable(topRight)) positions.add(topRight);
		}
		return positions;
	}
}
