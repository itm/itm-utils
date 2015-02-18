package de.uniluebeck.itm.util.concurrent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Helper class providing static methods to fork() and join() asynchronous calls using {@link
 * java.util.concurrent.ExecutorService} and {@link java.util.concurrent.Callable}s.
 */
public class ForkJoinHelper {

	/**
	 * Fork a number of {@link java.util.concurrent.Callable} instances, i.e. execute them in parallel.
	 *
	 * @param callables	   the callables to be executed in parallel
	 * @param executorService the {@link java.util.concurrent.ExecutorService} to be used for execution
	 * @param <V>             the type of the calls result
	 *
	 * @return a {@link com.google.common.collect.BiMap} instance that maps the callable to the {@link
	 *         java.util.concurrent.Future}s representing the result
	 */
	@SuppressWarnings("unchecked")
	public static <V> BiMap<? extends Callable<V>, Future<V>> fork(
			final Collection<? extends Callable<V>> callables,
			final ExecutorService executorService) {

		final BiMap map = HashBiMap.create();
		for (Callable<V> callable : callables) {
			//noinspection unchecked
			map.put(callable, executorService.submit(callable));
		}
		//noinspection unchecked
		return map;
	}

	/**
	 * Same as calling {@link ForkJoinHelper#join(com.google.common.collect.BiMap, boolean)} with {@code
	 * omitFailures=false}.
	 */
	public static <V> BiMap<Callable<V>, V> join(final BiMap<? extends Callable<V>, Future<V>> calledCallables) {
		return join(calledCallables, false);
	}

	/**
	 * Joins the {@link Future} instances and returns a mapping between the {@link Callable}s that were submitted to an
	 * {@link ExecutorService} and the results of the {@link Future} instances.
	 *
	 * @param calledCallables the {@link Callable} instances which have already been started executing
	 * @param omitFailures	{@code true} if failed {@link Future}s (i.e. futures that return an Exception) should be
	 *                        omitted (i.e. ignored) when building the resulting map, or {@code false} if a {@link
	 *                        RuntimeException} should be thrown if one future failed
	 * @param <V>             the type of the results
	 *
	 * @return a map between the {@link Callable}s executed and the results of the execution
	 */
	@SuppressWarnings("unchecked")
	public static <V> BiMap<Callable<V>, V> join(final BiMap<? extends Callable<V>, Future<V>> calledCallables,
												 boolean omitFailures) {

		final BiMap map = HashBiMap.create();

		for (Map.Entry<? extends Callable<V>, Future<V>> entry : calledCallables.entrySet()) {

			try {
				map.put(entry.getKey(), entry.getValue().get());
			} catch (Exception e) {
				if (!omitFailures) {
					throw new RuntimeException(e);
				}
			}
		}

		return map;
	}

}
