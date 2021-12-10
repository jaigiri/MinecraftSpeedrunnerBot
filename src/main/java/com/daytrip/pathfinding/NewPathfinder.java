package com.daytrip.pathfinding;

import net.minecraft.util.math.BlockPos;

import java.util.*;

public class NewPathfinder {
	private static class Queue<T> {
		private final List<T> contents = new ArrayList<>();

		public T get() {
			T content = contents.get(contents.size() - 1);
			contents.remove(content);
			return content;
		}

		public void put(T content) {
			contents.add(content);
		}

		public boolean empty() {
			return contents.isEmpty();
		}
	}

	public static List<BlockPos> findPath(BlockPos start, BlockPos goal, PathfindingGrid grid) {
		Queue<BlockPos> frontier = new Queue<>();
		frontier.put(start);
		Map<BlockPos, BlockPos> came_from = new HashMap<>();
		came_from.put(start, null);
		System.out.println("hi 1");
		while (!frontier.empty()) {
			BlockPos current = frontier.get();
			if (current == goal) {
				break;
			}
			for (BlockPos next : grid.getNeighbors(current, true, start)) {
				if (!came_from.containsKey(next)) {
					System.out.println(next.toString());
					frontier.put(next);
					came_from.put(next, current);
				}
			}
		}

		System.out.println("hi 2");

		BlockPos current = goal;
		List<BlockPos> path = new ArrayList<>();
		while (current != start) {
			path.add(current);
			current = came_from.get(current);
		}
		Collections.reverse(path);
		System.out.println("hi 3");
		return path;
	}
}
