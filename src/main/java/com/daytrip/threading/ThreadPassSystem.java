package com.daytrip.threading;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreadPassSystem {
	private final Thread mainThread;
	private volatile List<Runnable> work = new CopyOnWriteArrayList<>();

	public ThreadPassSystem(Thread mainThread) {
		this.mainThread =  mainThread;
	}

	public boolean isOnMainThread() {
		return Thread.currentThread() == mainThread;
	}

	public void runOnMainThread(Runnable task) {
		if(isOnMainThread()) task.run();
		else work.add(task);
	}

	public void updateWork() {
		if(isOnMainThread()) {
			for(Runnable task : work) {
				task.run();
				work.remove(task);
			}
		}
	}
}
