package de.uniluebeck.itm.tr.util;

import com.google.common.util.concurrent.AbstractFuture;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkArgument;

public class ProgressSettableFuture<V> extends AbstractFuture<V> {

	public static <V> ProgressSettableFuture<V> create() {
		return new ProgressSettableFuture<V>();
	}

	private final ReExecutableExecutionList progressExecutionList = new ReExecutableExecutionList();

	private float progress = 0;

	private ProgressSettableFuture() {
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

		checkArgument(progress >= 0f && progress <= 1f, "Progress value must be between 0f and 1f!");

		if (isDone()) {
			return false;
		}

		return setProgressInternal(progress);
	}

	public float getProgress() {
		return this.progress;
	}

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
