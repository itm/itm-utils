package de.uniluebeck.itm.util.futures;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("FieldCanBeLocal")
public class SettableFutureMapTest {

	private static final Map<Object, ListenableFuture<Object>> EMPTY_MAP =
			ImmutableMap.of();

	private static final Object KEY_1 = new Object();

	private static final Object KEY_2 = new Object();

	private static final Object KEY_3 = new Object();

	private static final ListeningExecutorService STE = MoreExecutors.sameThreadExecutor();

	private Map<Object, ListenableFuture<Object>> mapWithOneCompleteEntry;

	private Map<Object, ListenableFuture<Object>> mapWithOneIncompleteEntry;

	private Map<Object, ListenableFuture<Object>> mapWithMultipleCompleteEntries;

	private Map<Object, ListenableFuture<Object>> mapWithMultipleIncompleteEntries;

	private Map<Object, ListenableFuture<Object>> mapWithMultipleEntriesAndOneIncompleteEntry;

	private Map<Object, ListenableFuture<Object>> mapWithMultipleEntriesAndTwoIncompleteEntries;

	private Object v1;

	private Object v2;

	private Object v3;

	private SettableFuture<Object> completeFuture1;

	private SettableFuture<Object> completeFuture2;

	private SettableFuture<Object> completeFuture3;

	private SettableFuture<Object> incompleteFuture1;

	private SettableFuture<Object> incompleteFuture2;

	private SettableFuture<Object> incompleteFuture3;

	@Mock
	private Runnable listener;

	@Before
	public void setUp() throws Exception {

		mapWithOneCompleteEntry = Maps.newHashMap();
		mapWithOneIncompleteEntry = Maps.newHashMap();
		mapWithMultipleCompleteEntries = Maps.newHashMap();
		mapWithMultipleIncompleteEntries = Maps.newHashMap();
		mapWithMultipleEntriesAndOneIncompleteEntry = Maps.newHashMap();
		mapWithMultipleEntriesAndTwoIncompleteEntries = Maps.newHashMap();

		completeFuture1 = SettableFuture.create();
		completeFuture2 = SettableFuture.create();
		completeFuture3 = SettableFuture.create();

		v1 = new Object();
		v2 = new Object();
		v3 = new Object();

		completeFuture1.set(v1);
		completeFuture2.set(v2);
		completeFuture3.set(v3);

		incompleteFuture1 = SettableFuture.create();
		incompleteFuture2 = SettableFuture.create();
		incompleteFuture3 = SettableFuture.create();

		mapWithOneCompleteEntry.put(KEY_1, completeFuture1);

		mapWithOneIncompleteEntry.put(KEY_1, incompleteFuture1);

		mapWithMultipleCompleteEntries.put(KEY_1, completeFuture1);
		mapWithMultipleCompleteEntries.put(KEY_2, completeFuture2);
		mapWithMultipleCompleteEntries.put(KEY_3, completeFuture3);

		mapWithMultipleIncompleteEntries.put(KEY_1, incompleteFuture1);
		mapWithMultipleIncompleteEntries.put(KEY_2, incompleteFuture2);
		mapWithMultipleIncompleteEntries.put(KEY_3, incompleteFuture3);

		mapWithMultipleEntriesAndOneIncompleteEntry.put(KEY_1, completeFuture1);
		mapWithMultipleEntriesAndOneIncompleteEntry.put(KEY_2, completeFuture2);
		mapWithMultipleEntriesAndOneIncompleteEntry.put(KEY_3, incompleteFuture3);

		mapWithMultipleEntriesAndTwoIncompleteEntries.put(KEY_1, completeFuture1);
		mapWithMultipleEntriesAndTwoIncompleteEntries.put(KEY_2, incompleteFuture2);
		mapWithMultipleEntriesAndTwoIncompleteEntries.put(KEY_3, incompleteFuture3);
	}

	@Test
	public void testEmptyMapIsDoneImmediately() throws Exception {
		assertTrue(map(EMPTY_MAP).isDone());
	}

	@Test
	public void testMapWithOneEntryIsDoneIfEntryIsDone()
			throws Exception {
		assertTrue(map(mapWithOneCompleteEntry).isDone());
	}

	@Test
	public void testMapWithOneEntryIsNotDoneIfEntryIsNotDone()
			throws Exception {
		assertFalse(map(mapWithOneIncompleteEntry).isDone());
	}

	@Test
	public void testMapWithMultipleEntriesIsDoneWhenAllAreDone()
			throws Exception {
		assertTrue(map(mapWithMultipleCompleteEntries).isDone());
	}

	@Test
	public void testMapWithMultipleEntriesIsNotDoneIfOneEntryIsNotDone()
			throws Exception {
		assertFalse(map(mapWithMultipleEntriesAndOneIncompleteEntry).isDone());
	}

	@Test
	public void testListenerOnEmptyMapImmediatelyCalled() throws Exception {
		map(EMPTY_MAP).addListener(listener, STE);
		verify(listener).run();
	}

	@Test
	public void testListenerOnMapWithOneEntryIsNotNotifiedIfEntryIsNotCompleted()
			throws Exception {
		map(mapWithOneIncompleteEntry).addListener(listener, STE);
		verify(listener, never()).run();
	}

	@Test
	public void testListenerOnMapWithOneCompleteEntryIsImmediatelyNotified()
			throws Exception {
		map(mapWithOneCompleteEntry).addListener(listener, STE);
		verify(listener).run();
	}

	@Test
	public void testListenerOnMapWithOneEntryIsNotifiedIfEntryCompletes()
			throws Exception {
		map(mapWithOneIncompleteEntry).addListener(listener, STE);
		verify(listener, never()).run();
		incompleteFuture1.set(new Object());
		verify(listener).run();
	}

	@Test
	public void testListenerOnMapWithMultipleEntriesIsNotNotifiedUtilAllEntriesAreDone()
			throws Exception {
		map(mapWithMultipleIncompleteEntries).addListener(listener, STE);
		verify(listener, never()).run();

		incompleteFuture1.set(new Object());
		verify(listener, never()).run();

		incompleteFuture2.set(new Object());
		verify(listener, never()).run();

		incompleteFuture3.set(new Object());
		verify(listener).run();
	}

	@Test
	public void testListenerOnMapWithMultipleEntriesIsImmediatelyNotifiedIfAllEntriesAreDone()
			throws Exception {
		map(mapWithMultipleCompleteEntries).addListener(listener, STE);
		verify(listener).run();
	}

	@Test
	public void testListenerOnMapWithSomeIncompleteEntriesIsNotNotifiedUntilAllAreDone() throws Exception {
		map(mapWithMultipleEntriesAndTwoIncompleteEntries).addListener(listener, STE);
		verify(listener, never()).run();

		incompleteFuture2.set(new Object());
		verify(listener, never()).run();

		incompleteFuture3.set(new Object());
		verify(listener).run();
	}

	@Test
	public void testListenerOnMapWithOneFailedEntryIsNotified() throws Exception {

		map(mapWithOneIncompleteEntry).addListener(listener, STE);
		verify(listener, never()).run();

		incompleteFuture1.setException(new Exception());
		verify(listener).run();
	}

	@Test
	public void testListenerOnMapWithMultipleFailedEntriesIsNotified() throws Exception {

		map(mapWithMultipleIncompleteEntries).addListener(listener, STE);
		verify(listener, never()).run();

		incompleteFuture1.setException(new Exception());
		verify(listener, never()).run();

		incompleteFuture2.setException(new Exception());
		verify(listener, never()).run();

		incompleteFuture3.setException(new Exception());
		verify(listener).run();
	}

	@Test
	public void testListenerOnMapWithSomeSuccessfulAndSomeFailedFuturesIsNotified() throws Exception {

		map(mapWithMultipleEntriesAndTwoIncompleteEntries).addListener(listener, STE);
		verify(listener, never()).run();

		incompleteFuture2.set(new Object());
		verify(listener, never()).run();

		incompleteFuture3.setException(new Exception());
		verify(listener).run();
	}

	@Test
	public void testMapReturnedContainsSameValuesAsEntryFutures() throws Exception {

		final SettableFutureMap<Object, Object> map = map(mapWithMultipleCompleteEntries);

		assertSame(map.get().get(KEY_1), v1);
		assertSame(map.get().get(KEY_2), v2);
		assertSame(map.get().get(KEY_3), v3);
	}

	private SettableFutureMap<Object, Object> map(
			final Map<Object, ListenableFuture<Object>> map) {
		return new SettableFutureMap<Object, Object>(
				map
		);
	}
}
