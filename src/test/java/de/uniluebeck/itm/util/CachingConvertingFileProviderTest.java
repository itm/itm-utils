package de.uniluebeck.itm.util;

import com.google.common.base.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.*;
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

		Mockito.when(mockFile.exists()).thenReturn(true);
		Mockito.when(mockFile.canRead()).thenReturn(true);

		provider = new CachingConvertingFileProvider<String>(mockFile, mockFunction);
	}

	@Test
	public void testIfFileProviderProvidesAtAll() throws Exception {

		String expected = "hello, world";
		Mockito.when(mockFunction.apply(mockFile)).thenReturn(expected);

		Assert.assertEquals(expected, provider.get());
	}

	@Test
	public void testIfProviderCachesOldConversionResult() throws Exception {

		String expectedString = "hello, world";
		Mockito.when(mockFunction.apply(mockFile)).thenReturn(expectedString);

		String expectedCachedConversionResult = provider.get();
		Assert.assertSame(expectedCachedConversionResult, expectedString);

		String actualCachedConversionResult = provider.get();
		Assert.assertSame(expectedCachedConversionResult, actualCachedConversionResult);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testIfProviderReloadsWhenFileTimestampChanged() throws Exception {

		Mockito.when(mockFunction.apply(mockFile)).thenReturn(new String("hello, world"), new String("hello, world"));

		Mockito.when(mockFile.lastModified()).thenReturn(1234567890L);
		String firstConversionResult = provider.get();

		Mockito.when(mockFile.lastModified()).thenReturn(1234567891L);
		String secondConversionResult = provider.get();

		Assert.assertNotSame(firstConversionResult, secondConversionResult);
		Assert.assertEquals(firstConversionResult, secondConversionResult);
	}

	@Test(expected = RuntimeException.class)
	public void testIfProviderThrowsRuntimeExceptionWhenFileDoesNotExist() throws Exception {
		Mockito.when(mockFile.exists()).thenReturn(false);
		provider.get();
	}

	@Test(expected = RuntimeException.class)
	public void testIfProviderThrowsRuntimeExceptionWhenFileIsNotReadable() throws Exception {
		Mockito.when(mockFile.canRead()).thenReturn(false);
		provider.get();
	}
}
