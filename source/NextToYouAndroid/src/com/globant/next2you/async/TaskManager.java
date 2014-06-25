package com.globant.next2you.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class TaskManager {
	private final String TAG = "TaskManager";
	private ThreadPoolExecutor cpuPipe;
	private ThreadPoolExecutor netPipe;
	private ThreadPoolExecutor diskPipe;
	private Handler uiHandler;

	public TaskManager() {
		cpuPipe = new ThreadPoolExecutor(1, 2, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		netPipe = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		diskPipe = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		uiHandler = new Handler(Looper.getMainLooper());
	}

	public int getPendingTasksCount(Pipe pipe) {
		if (pipe.equals(Pipe.NET)) {
			return netPipe.getQueue().size();
		} else if (pipe.equals(Pipe.DISK)) {
			return diskPipe.getQueue().size();
		} else if (pipe.equals(Pipe.CPU)) {
			return cpuPipe.getQueue().size();
		}
		return -1;
	}
	
	public Future<Object> assignDisk(Callable<Object> task, Callback callback) {
		return assign(diskPipe, task, callback);
	}

	public Future<Object> assignDisk(Callable<Object> task) {
		return assign(diskPipe, task, null);
	}
	
	public Future<Object> assignNet(Callable<Object> task, Callback callback) {
		return assign(netPipe, task, callback);
	}

	public Future<Object> assignNet(Callable<Object> task) {
		return assign(netPipe, task, null);
	}
	
	public Future<Object> assignCpu(Callable<Object> task, Callback callback) {
		return assign(cpuPipe, task, callback);
	}

	public Future<Object> assignCpu(Callable<Object> task) {
		return assign(cpuPipe, task, null);
	}

	private Future<Object> assign(ThreadPoolExecutor pipe, final Callable<Object> task, final Callback callback) {
		return pipe.submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					final Object result = task.call();
					if (callback != null) {
						if (callback instanceof UICallback) {
							uiHandler.post(new Runnable() {
								@Override
								public void run() {
									try {
										callback.onResult(result);
									} catch (Exception e) {
										Log.e(TAG, "", e);
									}
								}
							});
						} else {
							callback.onResult(result);
						}
					}
					return result;
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
				return null;
			}
		});
	}
	
	public enum Pipe {
		NET, DISK, CPU;
	}
}
