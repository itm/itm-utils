package de.uniluebeck.itm.util;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProgressSettableFutureTest {

	@Test
	public void testIfProgressListenersAreCalledIfProgressIsSet() throws Exception {

		ProgressSettableFuture<Void> future = ProgressSettableFuture.create();

		final Runnable mock1 = mock(Runnable.class);
		final Runnable mock2 = mock(Runnable.class);

		future.addProgressListener(mock1, MoreExecutors.sameThreadExecutor());
		future.addProgressListener(mock2, MoreExecutors.sameThreadExecutor());

		future.setProgress(0.1f);
		verify(mock1, times(1)).run();
		verify(mock2, times(1)).run();

		future.setProgress(0.2f);
		verify(mock1, times(2)).run();
		verify(mock2, times(2)).run();

		future.setProgress(0.3f);
		verify(mock1, times(3)).run();
		verify(mock2, times(3)).run();
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
		ProgressSettableFuture.<Void>create().addProgressListener(mock(Runnable.class), null);
	}

	@Test
	public void testThatProgressCantBeModifiedAfterFutureIsComplete() throws Exception {
		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();
		future.set(null);
		assertFalse(future.setProgress(0.9f));
	}

	@Test
	public void testThatProgressListenersAreCalledWhenFutureIsCompletingWithoutError() throws Exception {

		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();

		final Runnable mock1 = mock(Runnable.class);
		final Runnable mock2 = mock(Runnable.class);

		future.addProgressListener(mock1, MoreExecutors.sameThreadExecutor());
		future.addProgressListener(mock2, MoreExecutors.sameThreadExecutor());

		assertTrue(future.set(null));

		verify(mock1, times(1)).run();
		verify(mock2, times(1)).run();
	}

	@Test
	public void testThatProgressListenersAreCalledWhenFutureIsCompletingWithError() throws Exception {

		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();

		final Runnable mock1 = mock(Runnable.class);
		final Runnable mock2 = mock(Runnable.class);

		future.addProgressListener(mock1, MoreExecutors.sameThreadExecutor());
		future.addProgressListener(mock2, MoreExecutors.sameThreadExecutor());

		assertTrue(future.setException(mock(Throwable.class)));

		verify(mock1, times(1)).run();
		verify(mock2, times(1)).run();
	}

	@Test
	public void testThatProgressIsOneIfFutureIsCompletedWithoutError() throws Exception {
		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();
		assertTrue(future.set(null));
		assertEquals(1f, future.getProgress(), 0f);
	}

	@Test
	public void testThatProgressIsOneIfFutureIsCompletedWithError() throws Exception {
		final ProgressSettableFuture<Void> future = ProgressSettableFuture.create();
		assertTrue(future.set(null));
		assertEquals(1f, future.getProgress(), 0f);
	}
}
