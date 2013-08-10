package de.uniluebeck.itm.util.concurrent;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ImmediateProgressSettableFutureMap<K, V> extends ProgressSettableFutureMap<K, V> {

	protected ImmediateProgressSettableFutureMap(final Map<K, ProgressListenableFuture<V>> of) {
		super(of);
	}

	public static <K, V> ImmediateProgressSettableFutureMap<K, V> of(K key, V value) {
		return new ImmediateProgressSettableFutureMap<K, V>(
				ImmutableMap.<K, ProgressListenableFuture<V>>of(
						key,
						ImmediateProgressSettableFuture.of(value)
				)
		);
	}

}
