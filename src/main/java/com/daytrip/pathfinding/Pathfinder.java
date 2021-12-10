package com.daytrip.pathfinding;

import net.minecraft.dispenser.IPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Pathfinder {
	public static class PathPoint extends BlockPos {
		@Nullable
		public PathPoint previous;

		public PathPoint(int x, int y, int z) {
			super(x, y, z);
		}

		public PathPoint(double x, double y, double z) {
			super(x, y, z);
		}

		public PathPoint(Vector3d vec) {
			super(vec);
		}

		public PathPoint(IPosition position) {
			super(position);
		}

		public PathPoint(Vector3i source) {
			super(source);
		}

		public static PathPoint create(BlockPos pos, PathPoint previous) {
			PathPoint pathPoint = new PathPoint(pos.asVector());
			pathPoint.previous = previous;
			return pathPoint;
		}

		public PathPoint offset(int dx, int dy, int dz) {
			return new PathPoint(getX() + dx, getY() + dy, getZ() + dz);
		}
	}

	private static boolean walkable(@Nonnull World world, @Nonnull PathPoint point) {
		return !world.getBlockState(point).isSolid();
	}

	private static List<PathPoint> FindNeighbors(@Nonnull World world, @Nonnull PathPoint point) {
		List<PathPoint> neighbors = new ArrayList<>();
		PathPoint front = point.offset(0, 0,  1);
		PathPoint back = point.offset(0, 0,  -1);
		PathPoint left = point.offset(-1, 0, 0);
		PathPoint right = point.offset(1, 0, 0);
		front.previous = point;
		back.previous = point;
		left.previous = point;
		right.previous = point;
		if (walkable(world, front)) neighbors.add(front);
		if (walkable(world, back)) neighbors.add(back);
		if (walkable(world, left)) neighbors.add(left);
		if (walkable(world, right)) neighbors.add(right);
		return neighbors;
	}

	@Nullable
	public static List<PathPoint> findPath(@Nonnull World world, @Nonnull BlockPos startPoint, @Nonnull BlockPos endPoint) {
		boolean finished = false;
		PathPoint start = PathPoint.create(startPoint, null);
		PathPoint end = PathPoint.create(endPoint, null);
		List<PathPoint> used = new ArrayList<>();
		used.add(start);

		while (!finished) {
			List<PathPoint> newOpen = new ArrayList<>();
			for(int i = 0; i < used.size(); ++i){
				PathPoint point = used.get(i);
				for (PathPoint neighbor : FindNeighbors(world, point)) {
					if (!used.contains(neighbor) && !newOpen.contains(neighbor)) {
						newOpen.add(neighbor);
					}
				}
			}

			for(PathPoint point : newOpen) {
				used.add(point);
				if (end.equals(point)) {
					finished = true;
					break;
				}
			}

			if (!finished && newOpen.isEmpty())
				return null;
		}

		List<PathPoint> path = new ArrayList<>();
		PathPoint point = used.get(used.size() - 1);
		while(point.previous != null) {
			path.add(0, point);
			point = point.previous;
		}
		return path;
	}
}
