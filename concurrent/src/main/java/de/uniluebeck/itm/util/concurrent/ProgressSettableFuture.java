package de.uniluebeck.itm.util.concurrent;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractFuture;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;

public class ProgressSettableFuture<V> extends AbstractFuture<V> implements ProgressListenableFuture<V> {

	public static <V> ProgressSettableFuture<V> create() {
		return new ProgressSettableFuture<V>();
	}

	private final ReExecutableExecutionList progressExecutionList = new ReExecutableExecutionList();

	private float progress = 0;

	protected ProgressSettableFuture() {
	}

	@Override
	public boolean set(@Nullable V value) {
		final boolean set = super.set(value);
		if (set) {
			setProgressInternal(1f);
		}
		return set;
	}

	@Override
	public boolean setException(Throwable throwable) {
		final boolean set = super.setException(throwable);
		if (set) {
			setProgressInternal(1f);
		}
		return set;
	}

	public boolean setProgress(float progress) {

		Preconditions.checkArgument(progress >= 0f && progress <= 1f, "Progress value must be between 0f and 1f!");

		if (isDone()) {
			return false;
		}

		return setProgressInternal(progress);
	}

	@Override
	public float getProgress() {
		return this.progress;
	}

	@Override
	public void addProgressListener(final Runnable runnable, final Executor executor) {

		if (isDone()) {
			executor.execute(runnable);
		}

		synchronized (this) {
			progressExecutionList.add(runnable, executor);
		}
	}

	private synchronized boolean setProgressInternal(final float progress) {

		this.progress = progress;
		progressExecutionList.execute();

		return true;
	}
}
