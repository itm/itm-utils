package de.uniluebeck.itm.tr.util;

import com.google.common.base.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CachingConvertingFileProviderTest {

	@Mock
	private File mockFile;

	@Mock
	private Function<File, String>  mockFunction;

	private CachingConvertingFileProvider<String> provider;

	@Before
	public void setUp() throws Exception {

		when(mockFile.exists()).thenReturn(true);
		when(mockFile.canRead()).thenReturn(true);

		provider = new CachingConvertingFileProvider<String>(mockFile, mockFunction);
	}

	@Test
	public void testIfFileProviderProvidesAtAll() throws Exception {

		String expected = "hello, world";
		when(mockFunction.apply(mockFile)).thenReturn(expected);

		assertEquals(expected, provider.get());
	}

	@Test
	public void testIfProviderCachesOldConversionResult() throws Exception {

		String expectedString = "hello, world";
		when(mockFunction.apply(mockFile)).thenReturn(expectedString);

		String expectedCachedConversionResult = provider.get();
		assertSame(expectedCachedConversionResult, expectedString);

		String actualCachedConversionResult = provider.get();
		assertSame(expectedCachedConversionResult, actualCachedConversionResult);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testIfProviderReloadsWhenFileTimestampChanged() throws Exception {

		when(mockFunction.apply(mockFile)).thenReturn(new String("hello, world"), new String("hello, world"));

		when(mockFile.lastModified()).thenReturn(1234567890L);
		String firstConversionResult = provider.get();

		when(mockFile.lastModified()).thenReturn(1234567891L);
		String secondConversionResult = provider.get();

		assertNotSame(firstConversionResult, secondConversionResult);
		assertEquals(firstConversionResult, secondConversionResult);
	}

	@Test(expected = RuntimeException.class)
	public void testIfProviderThrowsRuntimeExceptionWhenFileDoesNotExist() throws Exception {
		when(mockFile.exists()).thenReturn(false);
		provider.get();
	}

	@Test(expected = RuntimeException.class)
	public void testIfProviderThrowsRuntimeExceptionWhenFileIsNotReadable() throws Exception {
		when(mockFile.canRead()).thenReturn(false);
		provider.get();
	}
}
