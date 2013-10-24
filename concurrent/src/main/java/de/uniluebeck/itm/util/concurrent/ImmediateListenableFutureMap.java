package de.uniluebeck.itm.util.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.util.concurrent.Futures.immediateFuture;

public class ImmediateListenableFutureMap<K, V> extends SettableFutureMap<K, V> {

	protected ImmediateListenableFutureMap(final SettableFutureMap<K, V> map) {
		super(map);
	}

	public static <K, V> ImmediateListenableFutureMap<K, V> of(K key, V value) {
		return new ImmediateListenableFutureMap<K, V>(SettableFutureMap.of(key, immediateFuture(value)));
	}

	public static <K, V> ImmediateListenableFutureMap<K, V> of(Map<K, V> map) {
		final Map<K, ListenableFuture<V>> m = newHashMapWithExpectedSize(map.size());
		for (Entry<K, V> entry : map.entrySet()) {
			m.put(entry.getKey(), immediateFuture(entry.getValue()));
		}
		return new ImmediateListenableFutureMap<K, V>(new SettableFutureMap<K, V>(m));
	}

	public static <K, V> ImmediateListenableFutureMap<K, V> of(Set<K> keySet, V value) {
		final Map<K, ListenableFuture<V>> m = newHashMapWithExpectedSize(keySet.size());
		for (K key : keySet) {
			m.put(key, immediateFuture(value));
		}
		return new ImmediateListenableFutureMap<K, V>(new SettableFutureMap<K, V>(m));
	}
}
