package de.uniluebeck.itm.util.serialization;


import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class MultiClassSerializationHelper<T> {
    public static final int MAX_NUMBER_OF_SERIALIZERS = (int) Byte.MAX_VALUE - (int) Byte.MIN_VALUE;
    private static Logger log = LoggerFactory.
            getLogger(MultiClassSerializationHelper.class);

    private Map<Class<? extends T>, Function<? extends T, byte[]>> serializers;
    private Map<Byte, Function<byte[], ? extends T>> deserializers;
    private final BiMap<Class<? extends T>, Byte> mapping;


    public MultiClassSerializationHelper(final Map<Class<? extends T>, Function<? extends T, byte[]>> serializers,
                                         Map<Class<? extends T>, Function<byte[], ? extends T>> deserializers, final BiMap<Class<? extends T>, Byte> classByteMap) throws IllegalArgumentException {
        if (serializers.size() != deserializers.size() || serializers.size() != classByteMap.size()) {
            throw new IllegalArgumentException("serializer mapping, deserializer mapping and byte to class mapping must have the same size!");
        }

        this.serializers = new HashMap<Class<? extends T>, Function<? extends T, byte[]>>(serializers.size());
        this.deserializers = new HashMap<Byte, Function<byte[], ? extends T>>(deserializers.size());
        this.mapping = classByteMap;

        for (Map.Entry<Class<? extends T>, Byte> pairs : mapping.entrySet()) {
            // Adding serializer
            Function<? extends T, byte[]> serializer = serializers.get(pairs.getKey());
            if (serializer == null) {
                throw new IllegalArgumentException("Not serializer found for class " + pairs.getKey().getName());
            }
            this.serializers.put(pairs.getKey(), serializer);

            // Adding deserializer
            Function<byte[], ? extends T> deserializer = deserializers.get(pairs.getKey());
            if (deserializer == null) {
                throw new IllegalArgumentException("Not deserializer found for class " + pairs.getKey().getName());
            }
            this.deserializers.put(pairs.getValue(), deserializer);
        }

        if (this.serializers.size() != this.deserializers.size() || this.serializers.size() != this.mapping.size()) {
            throw new IllegalArgumentException("serializer mapping, deserializer mapping and byte to class mapping must have the same size! Check classByteMap for duplicate entries!");
        }

    }

    public static <T> BiMap<Class<? extends T>, Byte> buildClassByteMap(final Map<Class<? extends T>, Function<? extends T, byte[]>> serializers,
                                                       Map<Class<? extends T>, Function<byte[], ? extends T>> deserializers) throws IllegalArgumentException {

        if (serializers.size() != deserializers.size()) {
            throw new IllegalArgumentException("Size of serializers and deserializers must be equal!");
        }
        if (serializers.size() > MAX_NUMBER_OF_SERIALIZERS) {
            throw new IllegalArgumentException("serializers map or deserializers map is too big! Max size is " + MAX_NUMBER_OF_SERIALIZERS);
        }

        BiMap<Class<? extends T>, Byte> mapping = HashBiMap.create(serializers.size());
        Byte id = Byte.MIN_VALUE;
        for (Class<? extends T> clazz : serializers.keySet()) {
            mapping.put(clazz, id);
            id = (byte) (id + 1);
        }
        return mapping;

    }

    public static <T> BiMap<Class<? extends T>, Byte> loadOrCreateClassByteMap(final Map<Class<? extends T>, Function<? extends T, byte[]>> serializers,
                                                                               Map<Class<? extends T>, Function<byte[], ? extends T>> deserializers, File mappingFile) throws IllegalArgumentException, IOException {
        BiMap<Class<? extends T>, Byte> mapping = null;
        try {
            mapping = loadClassByteMap(mappingFile);
        } catch (Exception e) {
            //failed to load mapping from file
            mapping = buildClassByteMap(serializers, deserializers);
            storeClassByteMap(mappingFile, mapping);
        }

        return mapping;

    }

    public static <T> MultiClassSerializationHelper<T> buildHelperWithMappingFile(final Map<Class<? extends T>, Function<? extends T, byte[]>> serializers,
                                                                           Map<Class<? extends T>, Function<byte[], ? extends T>> deserializers, File mappingFile) throws IOException, ClassNotFoundException, IllegalArgumentException {
        BiMap<Class<? extends T>, Byte> map = loadClassByteMap(mappingFile);
        return new MultiClassSerializationHelper<T>(serializers, deserializers, map);
    }

    public static <T> BiMap<Class<? extends T>, Byte> loadClassByteMap(File mappingFile) throws IOException, ClassNotFoundException, IllegalArgumentException {
        if (!mappingFile.exists()) {
            throw new FileNotFoundException("Can't load mapping from non existing file (" + mappingFile.getAbsolutePath() + ")");
        }
        if (mappingFile.isDirectory()) {
            throw new IllegalArgumentException("Mapping file must be a file and not a directory (" + mappingFile.getAbsolutePath() + ")");
        }
        BiMap<Class<? extends T>, Byte> mapping = HashBiMap.create();

        // Build mapping for existing types
        List<String> notFoundClasses = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(mappingFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] components = line.split(",");
                if (components.length != 2) {
                    log.warn("Invalid line in mapping file: {}", line);
                    continue;
                }
                String className = components[0].trim();
                try {
                    @SuppressWarnings("unchecked") Class<? extends T> clazz = (Class<T>) Class.forName(className);
                    mapping.put(clazz, Byte.parseByte(components[1]));
                } catch (ClassNotFoundException e) {
                    notFoundClasses.add(className);
                }
            }
        } catch (IOException e) {
            log.error("Failed to read line from CSV mapping file.", e);
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (notFoundClasses.size() > 0) {
            throw new ClassNotFoundException("Unknown classes in event store mapping file. Check the mapping file and fix the class names if appropriate.\nUnknown Classes: " + notFoundClasses);
        }
        return mapping;
    }

    public static <T> void storeClassByteMap(File mappingFile, BiMap<Class<? extends T>, Byte> mapping) throws IOException, IllegalArgumentException {
        if (!mappingFile.exists() && !mappingFile.isDirectory()) {
            Files.createParentDirs(mappingFile);
            mappingFile.createNewFile();
        }

        if (mappingFile.isDirectory()) {
            throw new IllegalArgumentException("Mapping file must be a file and not a directory (" + mappingFile.getAbsolutePath() + ")");
        }

        BufferedWriter bw = null;
        bw = new BufferedWriter(new FileWriter(mappingFile));
        Iterator<Map.Entry<Class<? extends T>, Byte>> it = mapping.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Class<? extends T>, Byte> entry = it.next();
            bw.write(entry.getKey().getName() + "," + entry.getValue());
            if (it.hasNext()) {
                bw.write("\n");
            }
        }
        try {
            bw.close();
        } catch (IOException e) {
            throw new IOException("Error while closing file. Persisted mapping file may be invalid!");
        }
    }

    public BiMap<Class<? extends T>, Byte> getClassByteMapping() {
        return mapping;
    }

    public byte[] serialize(T object) throws NotSerializableException {
        @java.lang.SuppressWarnings("unchecked") Class<T> c = (Class<T>) object.getClass();
        return serialize(object, c);
    }

    public byte[] serialize(T object, final Class<? extends T> type) throws NotSerializableException {
        try {
            Function serializer = serializers.get(type);
            @SuppressWarnings("unchecked") byte[] serialized = (byte[]) serializer.apply(object);
            byte typeByte = mapping.get(type);
            byte[] finalSerialization = new byte[serialized.length + 1];
            finalSerialization[0] = typeByte;
            System.arraycopy(serialized, 0, finalSerialization, 1, serialized.length);
            return finalSerialization;
        } catch (NullPointerException e) {
            throw new NotSerializableException("Can't find a serializer for type " + type.getName() + " or serialization failed!");
        } catch (ClassCastException e) {
            throw new NotSerializableException("Failed to apply serializer function to "+object);
        }
    }

    public T deserialize(byte[] serialization) throws IllegalArgumentException {
        if (serialization == null || serialization.length == 0) {
            throw new IllegalArgumentException("Can't deserialize empty byte array");
        }

       Function<byte[], ? extends T> deserializer = deserializers.get(serialization[0]);
        if (deserializer == null) {
            throw new IllegalArgumentException("The provided byte array is invalid. No matching serializer found!");
        }
        byte[] event = new byte[serialization.length - 1];
        System.arraycopy(serialization, 1, event, 0, serialization.length - 1);


        return deserializer.apply(event);
    }
}
