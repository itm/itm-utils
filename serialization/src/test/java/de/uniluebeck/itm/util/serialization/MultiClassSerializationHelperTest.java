package de.uniluebeck.itm.util.serialization;


import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import javax.annotation.Nullable;
import java.io.File;
import java.io.NotSerializableException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@RunWith(BlockJUnit4ClassRunner.class)
public class MultiClassSerializationHelperTest extends TestCase {

    private MultiClassSerializationHelper serializationHelper;

    @Before
    public void setUp() throws Exception {
        serializationHelper = buildSerializationHelper();

    }

    private MultiClassSerializationHelper buildSerializationHelper() throws Exception {
        Map<Class<?>, Function<?, byte[]>> serializers = new HashMap<Class<?>, Function<?, byte[]>>();
        serializers.put(String.class, new Function<String, byte[]>() {
                    @Override
                    public byte[] apply(String string) {
                        return string.getBytes();
                    }

                    @Override
                    public String toString() {
                        return "String -> byte[]";
                    }
                }
        );
        Map<Class<?>, Function<byte[], ?>> deserializers = new HashMap<Class<?>, Function<byte[], ?>>();
        deserializers.put(String.class, new Function<byte[], String>() {
                    @Override
                    public String apply(byte[] bytes) {
                        try {
                            return new String(bytes, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            return null;
                        }
                    }

                    @Override
                    public String toString() {
                        return "byte[] -> String";
                    }
                }
        );

        serializers.put(BigInteger.class, new Function<BigInteger, byte[]>() {
                    @Override
                    public byte[] apply(BigInteger o) {
                        return o.toByteArray();
                    }

                    @Override
                    public String toString() {
                        return "BigInteger -> byte[]";
                    }
                }
        );

        deserializers.put(BigInteger.class, new Function<byte[], BigInteger>() {
                    @Override
                    public BigInteger apply(byte[] bytes) {
                        return new BigInteger(bytes);
                    }

                    @Override
                    public String toString() {
                        return "byte[] -> BigInteger";
                    }
                }
        );

        serializers.put(java.lang.Byte.class, new Function<Byte, byte[]>() {
            @Nullable
            @Override
            public byte[] apply(@Nullable Byte o) {
                byte[] array = new byte[1];
                array[0] = o;
                return array;
            }
        });

        deserializers.put(Byte.class, new Function<byte[], Byte>() {
            @Nullable
            @Override
            public Byte apply(@Nullable byte[] bytes) {
                return bytes[0];
            }
        });

        String path = System.getProperty("java.io.tmpdir") + "/MultiClassSerializationHelper.mapping";

        BiMap<Class<?>, Byte> mapping = MultiClassSerializationHelper.loadOrCreateClassByteMap(serializers, deserializers, new File(path));

        return new MultiClassSerializationHelper(serializers, deserializers, mapping);
    }

    @Test
    public void testValidSerializationDeserialization() throws Exception {
        final String test = "Test";
        byte[] bytes = serializationHelper.serialize(test);

        assertNotNull(bytes);
        assertEquals("Serialization has unexpected size", test.getBytes().length + 1, bytes.length);

        String deserialized = (String) serializationHelper.deserialize(bytes);
        assertEquals(test, deserialized);

    }

    @Test(expected = NotSerializableException.class)
    public void testUnknownObjectSerialization() throws Exception {
        Object obj = new Object();
        serializationHelper.serialize(obj);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteArrayDeserializationFails() throws Exception {
        byte[] bytes = new byte[10];
        bytes[0] = Byte.MAX_VALUE;
        Object obj = serializationHelper.deserialize(bytes);
    }

    @Test
    public void testSerializationHelperWorksAfterRestart() throws Exception {
        String test = "Test";
        byte[] s1 = serializationHelper.serialize(test);
        for (int i = 0; i < 10000; i++) {
            MultiClassSerializationHelper secondHelper = buildSerializationHelper();
            assertEquals(test, secondHelper.deserialize(s1));
            Assert.assertArrayEquals(s1, secondHelper.serialize(test));
        }
    }
}
