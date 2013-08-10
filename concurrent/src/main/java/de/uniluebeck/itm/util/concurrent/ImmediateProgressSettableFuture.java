package de.uniluebeck.itm.util.concurrent;

public class ImmediateProgressSettableFuture<V> extends ProgressSettableFuture<V> {

	public static <V> ImmediateProgressSettableFuture<V> of(V value) {
		ImmediateProgressSettableFuture<V> future = new ImmediateProgressSettableFuture<V>();
		future.set(value);
		return future;
	}
}
