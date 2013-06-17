package de.uniluebeck.itm.util;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ProgressSettableFutureTest {

	@Test
	public void testIfProgressListenersAreCalledIfProgressIsSet() throws Exception {

		ProgressSettableFuture<Void> future = ProgressSettableFuture.create();

		final Runnable mock1 = Mockito.mock(Runnable.class);
		final Runnable mock2 = Mockito.mock(Runnable.class);

		future.addProgressListener(mock1, MoreExecutors.sameThreadExecutor());
		future.addProgressListener(mock2, MoreExecutors.sameThreadExecutor());

		future.setProgress(0.1f);
		Mockito.verify(mock1, Mockito.times(1)).run();
		Mockito.verify(mock2, Mockito.times(1)).run();

		future.setProgress(0.2f);
		Mockito.verify(mock1, Mockito.times(2)).run();
		Mockito.verify(mock2, Mockito.times(2)).run();

		future.setProgress(0.3f);
		Mockito.verify(mock1, Mockito.times(3)).run();
		Mockito.verify(mock2, Mockito.times(3)).run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIfAnIllegalArgumentExceptionIsThrownForProgressBelowZero() throws Exception {
		ProgressSettableFuture.<Void>create().setProgress(-0.1f);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIfAnIllegalArgumentExceptionIsThrownForProgressAboveOne() throws Exception {
		ProgressSettableFuture.<Void>create().setProgress(1.1f);
	}

	@Test(expected = NullPointerException.class)
	public void testIfNullPointerExceptionIsThrownIfRunnableIsNullWhenProgressListenerIsAdded() throws Exception {
		ProgressSettableFuture.<Void>create().addProgressListener(null, MoreExecutors.sameThreadExecutor());
	}

	@Test(expected = NullPointerException.class)
	public void testIfNullPointerExceptionIsThrownIfExecutorIsNullWhenProgressListenerIsAdded() throws Exception {
		ProgressSettableFuture.<Void>create().addProgressListener(Mockito.mock(Runnable.class), null);
	}

	@Test
	public void testThatProgressCantBeModifiedAfterFutureIsComplete() throws Exception {
		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();
		future.set(null);
		Assert.assertFalse(future.setProgress(0.9f));
	}

	@Test
	public void testThatProgressListenersAreCalledWhenFutureIsCompletingWithoutError() throws Exception {

		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();

		final Runnable mock1 = Mockito.mock(Runnable.class);
		final Runnable mock2 = Mockito.mock(Runnable.class);

		future.addProgressListener(mock1, MoreExecutors.sameThreadExecutor());
		future.addProgressListener(mock2, MoreExecutors.sameThreadExecutor());

		Assert.assertTrue(future.set(null));

		Mockito.verify(mock1, Mockito.times(1)).run();
		Mockito.verify(mock2, Mockito.times(1)).run();
	}

	@Test
	public void testThatProgressListenersAreCalledWhenFutureIsCompletingWithError() throws Exception {

		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();

		final Runnable mock1 = Mockito.mock(Runnable.class);
		final Runnable mock2 = Mockito.mock(Runnable.class);

		future.addProgressListener(mock1, MoreExecutors.sameThreadExecutor());
		future.addProgressListener(mock2, MoreExecutors.sameThreadExecutor());

		Assert.assertTrue(future.setException(Mockito.mock(Throwable.class)));

		Mockito.verify(mock1, Mockito.times(1)).run();
		Mockito.verify(mock2, Mockito.times(1)).run();
	}

	@Test
	public void testThatProgressIsOneIfFutureIsCompletedWithoutError() throws Exception {
		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();
		Assert.assertTrue(future.set(null));
		assertEquals(1f, future.getProgress(), 0f);
	}

	@Test
	public void testThatProgressIsOneIfFutureIsCompletedWithError() throws Exception {
		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();
		Assert.assertTrue(future.set(null));
		assertEquals(1f, future.getProgress(), 0f);
	}
}
