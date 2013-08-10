package de.uniluebeck.itm.util.concurrent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SettableFutureMap<K, V> implements ListenableFutureMap<K, V> {

	protected final ImmutableMap<K, ListenableFuture<V>> map;

	public static <K, V> SettableFutureMap<K, V> of(K key, ListenableFuture<V> valueFuture) {
		return new SettableFutureMap<K, V>(ImmutableMap.of(key, valueFuture));
	}

	public SettableFutureMap(final Map<K, ? extends ListenableFuture<V>> map) {
		this.map = ImmutableMap.copyOf(map);
	}

	public SettableFutureMap(final ImmutableMap<K, ListenableFuture<V>> map) {
		this.map = map;
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		boolean canceled = true;
		for (final ListenableFuture<V> future : map.values()) {
			canceled = canceled && future.cancel(mayInterruptIfRunning);
		}
		return canceled;
	}

	@Override
	public boolean isCancelled() {
		for (final ListenableFuture<V> future : map.values()) {
			if (!future.isCancelled()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isDone() {
		for (final ListenableFuture<V> future : map.values()) {
			if (!future.isDone()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Map<K, V> get()
			throws InterruptedException, ExecutionException {

		final HashMap<K, V> retMap = Maps.newHashMap();

		for (final Entry<K, ListenableFuture<V>> entry : map.entrySet()) {
			retMap.put(entry.getKey(), entry.getValue().get());
		}

		return retMap;
	}

	@Override
	public Map<K, V> get(final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {

		final HashMap<K, V> retMap = Maps.newHashMap();

		for (final Entry<K, ListenableFuture<V>> entry : map.entrySet()) {
			retMap.put(entry.getKey(), entry.getValue().get(timeout, unit));
		}

		return retMap;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(final Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return map.containsValue(value);
	}

	@Override
	public ListenableFuture<V> get(final Object key) {
		return map.get(key);
	}

	@Override
	public ListenableFuture<V> put(final K key,
								   final ListenableFuture<V> value) {
		throw new UnsupportedOperationException("Map is immutable!");
	}

	@Override
	public ListenableFuture<V> remove(final Object key) {
		throw new UnsupportedOperationException("Map is immutable!");
	}

	@Override
	public void putAll(
			final Map<? extends K, ? extends ListenableFuture<V>> m) {
		throw new UnsupportedOperationException("Map is immutable!");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Map is immutable!");
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<ListenableFuture<V>> values() {
		return map.values();
	}

	@Override
	public Set<Entry<K, ListenableFuture<V>>> entrySet() {
		return map.entrySet();
	}

	@Override
	public void addListener(final Runnable listener, final Executor executor) {

		if (map.isEmpty() || isDone()) {
			executor.execute(listener);
			return;
		}

		for (final ListenableFuture<V> future : map.values()) {
			future.addListener(
					new IndividualFutureListener(executor, listener),
					executor
			);
		}
	}

	private class IndividualFutureListener implements Runnable {

		private final Runnable listener;

		private final Executor executor;

		private IndividualFutureListener(final Executor executor,
										 final Runnable listener) {
			this.executor = executor;
			this.listener = listener;
		}

		@Override
		public void run() {
			for (ListenableFuture<V> future : map.values()) {
				if (!future.isDone()) {
					return;
				}
			}
			executor.execute(listener);
		}
	}
}
