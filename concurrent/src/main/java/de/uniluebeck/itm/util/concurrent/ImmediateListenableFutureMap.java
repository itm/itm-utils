package de.uniluebeck.itm.util.concurrent;

import static com.google.common.util.concurrent.Futures.immediateFuture;

public class ImmediateListenableFutureMap<K, V> extends SettableFutureMap<K, V> {

	protected ImmediateListenableFutureMap(final SettableFutureMap<K, V> map) {
		super(map);
	}

	public static <K, V> ImmediateListenableFutureMap<K, V> of(K key, V value) {
		return new ImmediateListenableFutureMap<K,V>(SettableFutureMap.of(key, immediateFuture(value)));
	}
}
