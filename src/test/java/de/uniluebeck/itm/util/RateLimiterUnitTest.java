package de.uniluebeck.itm.util;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RateLimiterUnitTest {

	private RateLimiter rateLimiter;

	private int slotLength = 2;

	private TimeUnit timeUnit = TimeUnit.SECONDS;

	@Test
	public void checkIfAllPassedObjectsSuccessfullyApprovedInOneSlot() {
		rateLimiter = new RateLimiterImpl(10, slotLength, timeUnit);
		for (int i = 0; i < 10; i++) {
			Assert.assertTrue(rateLimiter.checkIfInSlotAndCount());
			Assert.assertTrue(rateLimiter.approvedCount() == i + 1);
			Assert.assertTrue(rateLimiter.dismissedCount() == 0);
		}
		Assert.assertTrue(rateLimiter.approvedCount() == 10);
		Assert.assertTrue(rateLimiter.dismissedCount() == 0);
	}

	@Test
	public void checkIfAllPassedObjectsDismissesInOneSlot() {
		rateLimiter = new RateLimiterImpl(0, slotLength, timeUnit);
		for (int i = 0; i < 10; i++) {
			Assert.assertFalse(rateLimiter.checkIfInSlotAndCount());
			Assert.assertTrue(rateLimiter.dismissedCount() == i + 1);
			Assert.assertTrue(rateLimiter.approvedCount() == 0);
		}
		Assert.assertTrue(rateLimiter.dismissedCount() == 10);
		Assert.assertTrue(rateLimiter.approvedCount() == 0);
	}

	@Test
	public void checkIfAllPassedObjectsSuccessfullyApprovedForTwoSlots() throws InterruptedException {
		rateLimiter = new RateLimiterImpl(10, slotLength, timeUnit);
		for (int i = 0; i < 10; i++) {
			Assert.assertTrue(rateLimiter.checkIfInSlotAndCount());
			Assert.assertTrue(rateLimiter.approvedCount() == i + 1);
			Assert.assertTrue(rateLimiter.dismissedCount() == 0);
		}
		Assert.assertFalse(rateLimiter.checkIfInSlotAndCount());
		Assert.assertTrue(rateLimiter.dismissedCount() == 1);
		//move on to next slot
		rateLimiter.nextSlot();
		for (int i = 0; i < 10; i++) {
			Assert.assertTrue(rateLimiter.checkIfInSlotAndCount());
			Assert.assertTrue(rateLimiter.approvedCount() == i + 1);
			Assert.assertTrue(rateLimiter.dismissedCount() == 0);
		}
	}

}
