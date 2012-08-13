package de.uniluebeck.itm.tr.util;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

public class ReExecutableExecutionList {

	private static final Logger log = LoggerFactory.getLogger(ReExecutableExecutionList.class);

	private final Lock runnablesLock = new ReentrantLock();

	private ImmutableList<RunnableExecutorPair> runnables = ImmutableList.of();

	public ReExecutableExecutionList() {
	}

	public void add(Runnable runnable, Executor executor) {

		checkNotNull(runnable, "Runnable was null.");
		checkNotNull(executor, "Executor was null.");

		runnablesLock.lock();
		try {
			runnables = ImmutableList.<RunnableExecutorPair>builder()
					.addAll(runnables)
					.add(new RunnableExecutorPair(runnable, executor))
					.build();
		} finally {
			runnablesLock.unlock();
		}
	}

	public void execute() {

		for (RunnableExecutorPair runnable : runnables) {
			runnable.execute();
		}
	}

	private static class RunnableExecutorPair {

		final Runnable runnable;

		final Executor executor;

		RunnableExecutorPair(Runnable runnable, Executor executor) {
			this.runnable = runnable;
			this.executor = executor;
		}

		void execute() {
			try {
				executor.execute(runnable);
			} catch (RuntimeException e) {
				log.error("RuntimeException while executing runnable " + runnable + " with executor " + executor, e);
			}
		}
	}
}
