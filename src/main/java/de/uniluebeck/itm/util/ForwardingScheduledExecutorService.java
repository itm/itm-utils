/**********************************************************************************************************************
 * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                 *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote*
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package de.uniluebeck.itm.util;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * A {@link ScheduledExecutorService} implementation that wraps a {@link ScheduledExecutorService} and a {@link
 * ExecutorService}. The {@link ScheduledExecutorService} instance is only used for scheduling, the actual work is
 * forwarded to the {@link ExecutorService} instance for immediate execution when the scheduled time arrives.
 *
 * @author Daniel Bimschas
 */
public class ForwardingScheduledExecutorService implements ScheduledExecutorService {

	private class ForwardingCallable<V> implements Callable<Future<V>> {

		private final Callable<V> callable;

		private ForwardingCallable(final Callable<V> callable) {
			this.callable = callable;
		}

		@Override
		public Future<V> call() throws Exception {
			return executorService.submit(callable);
		}
	}

	private class ForwardingRunnable implements Runnable {

		private final Runnable runnable;

		public ForwardingRunnable(final Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			executorService.execute(runnable);
		}
	}

	private ScheduledExecutorService scheduledExecutorService;

	private ExecutorService executorService;

	public ForwardingScheduledExecutorService(final ScheduledExecutorService scheduledExecutorService,
											  final ExecutorService executorService) {
		this.scheduledExecutorService = scheduledExecutorService;
		this.executorService = executorService;
	}

	@Override
	public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
		return scheduledExecutorService.schedule(new ForwardingCallable(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
		return scheduledExecutorService.schedule(new ForwardingRunnable(command), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period,
												  final TimeUnit unit) {
		return scheduledExecutorService
				.scheduleAtFixedRate(new ForwardingRunnable(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay,
													 final TimeUnit unit) {
		return scheduledExecutorService
				.scheduleWithFixedDelay(new ForwardingRunnable(command), initialDelay, delay, unit);
	}

	@Override
	public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
		final boolean schedulerTerminated = scheduledExecutorService.awaitTermination(timeout, unit);
		final boolean executorTerminated = executorService.awaitTermination(timeout, unit);
		return schedulerTerminated && executorTerminated;
	}

	@Override
	public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return executorService.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout,
										 final TimeUnit unit) throws InterruptedException {
		return executorService.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return executorService.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return executorService.invokeAny(tasks, timeout, unit);
	}

	@Override
	public boolean isShutdown() {
		final boolean schedulerShutDown = scheduledExecutorService.isShutdown();
		final boolean executorShutDown = executorService.isShutdown();
		return schedulerShutDown && executorShutDown;
	}

	@Override
	public boolean isTerminated() {
		final boolean schedulerTerminated = scheduledExecutorService.isTerminated();
		final boolean executorTerminated = executorService.isTerminated();
		return schedulerTerminated && executorTerminated;
	}

	@Override
	public void shutdown() {
		scheduledExecutorService.shutdown();
		executorService.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		final List<Runnable> failed = Lists.newArrayList();
		failed.addAll(scheduledExecutorService.shutdownNow());
		failed.addAll(executorService.shutdownNow());
		return failed;
	}

	@Override
	public <T> Future<T> submit(final Callable<T> task) {
		return executorService.submit(task);
	}

	@Override
	public Future<?> submit(final Runnable task) {
		return executorService.submit(task);
	}

	@Override
	public <T> Future<T> submit(final Runnable task, final T result) {
		return executorService.submit(task, result);
	}

	@Override
	public void execute(final Runnable command) {
		executorService.execute(command);
	}
}
