package de.uniluebeck.itm.util;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

public interface ProgressListenableFuture<V> extends ListenableFuture<V> {

	float getProgress();

	void addProgressListener(Runnable runnable, Executor executor);
}
