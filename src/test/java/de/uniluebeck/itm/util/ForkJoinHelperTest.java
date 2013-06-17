package de.uniluebeck.itm.util;

import com.google.common.collect.BiMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ForkJoinHelperTest {

	private SuccessfulCallable<String> successful1 = new SuccessfulCallable<String>("hello1");

	private SuccessfulCallable<String> successful2 = new SuccessfulCallable<String>("hello2");

	private SuccessfulCallable<String> successful3 = new SuccessfulCallable<String>("hello3");

	private FailingCallable<String> failingCallable = new FailingCallable<String>();

	private static class SuccessfulCallable<V> implements Callable<V> {

		private V returnValue;

		private SuccessfulCallable(final V returnValue) {
			this.returnValue = returnValue;
		}

		@Override
		public V call() throws Exception {
			return returnValue;
		}
	}

	private static class FailingCallable<V> implements Callable<V> {

		@Override
		public V call() throws Exception {
			throw new Exception("This Callable instance must always throw an Exception!");
		}
	}

	private ExecutorService executorService;

	@Before
	public void setUp() throws Exception {
		executorService = Executors.newCachedThreadPool();
	}

	@After
	public void tearDown() throws Exception {
		final List<Runnable> runnables = executorService.shutdownNow();
		if (runnables.size() > 0) {
			throw new RuntimeException("There should be no runnables remaining after the execution of a unit test");
		}
	}

	@Test
	public void testSuccessfulForkJoin() throws Exception {

		final List<SuccessfulCallable<String>> callables = newArrayList(successful1, successful2, successful3);

		final BiMap<Callable<String>, String> resultMap = ForkJoinHelper
				.join(ForkJoinHelper.fork(callables, executorService));

		assertEquals(3, resultMap.size());
		assertEquals("hello1", resultMap.get(successful1));
		assertEquals("hello2", resultMap.get(successful2));
		assertEquals("hello3", resultMap.get(successful3));
	}

	@Test
	public void testFailingForkJoin() throws Exception {

		final List<Callable<String>> callables = newArrayList(successful1, failingCallable, successful2);

		try {
			final BiMap<Callable<String>, String> resultMap = ForkJoinHelper
					.join(ForkJoinHelper.fork(callables, executorService));
			fail("An exception should have been thrown!");
		} catch (Exception expected) {
		}
	}

	@Test
	public void testOmittedFailingForkJoin() throws Exception {

		final List<Callable<String>> callables = newArrayList(successful1, failingCallable, successful2);

		final BiMap<Callable<String>, String> resultMap = ForkJoinHelper
				.join(ForkJoinHelper.fork(callables, executorService), true);
		assertEquals(2, resultMap.size());
		assertEquals("hello1", resultMap.get(successful1));
		assertEquals("hello2", resultMap.get(successful2));
	}
}
