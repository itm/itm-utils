package de.uniluebeck.itm.util.serialization;


import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * This class provides the ability to serialize different classes into byte arrays and vice versa.
 * <p/>
 * The serialization of an object according to this helper is a byte array containing a type byte at the first position and the byte serialization of the object given by the serializer functions afterwards.
 * <p/>
 * Due to the fact, that the multiple map iterator uses may yield in different order of the contained values,
 * it's not sufficient to generate the "type byte" <-> "class type" map at initialization time of this helper because previously serialized objects might not be deserializable any more. Hence the constructor of this class expects the mapping as a bidirectional map.
 * Furthermore this class provides functions for persisting, loading and creating the bidirectional class-byte-mapping.
 * <p/>
 * So in general there are two possibilities of handling the map:
 * <ol>
 * <li>store and load it with the helper functions of this class</li>
 * <li>hard code the byte map</li>
 * </ol>
 *
 * @param <T> The parent type of objects to be serialized by this helper can be provided if only subtypes of an application specific class type should be handled by this instance.
 *            Otherwise <code>Object</code> should be used. This parameter simplifies usage of the serialize and deserialize methods
 */

@SuppressWarnings({"UnusedDeclaration", "ResultOfMethodCallIgnored"})
public class MultiClassSerializationHelper<T> {
    public static final int MAX_NUMBER_OF_SERIALIZERS = (int) Byte.MAX_VALUE - (int) Byte.MIN_VALUE;
    private static Logger log = LoggerFactory.
            getLogger(MultiClassSerializationHelper.class);

    private Map<Class<? extends T>, Function<? extends T, byte[]>> serializers;
    private Map<Byte, Function<byte[], ? extends T>> deserializers;
    private final BiMap<Class<? extends T>, Byte> mapping;


    /**
     * Constructor for a new serialization helper
     *
     * @param serializers   a map from classes to functions used to serialize objects of the given class type
     * @param deserializers a map from classes to functions used to deserialize objects of the given class type
     * @param classByteMap  a map specifying which object type is matched to which byte during serialization
     * @throws IllegalArgumentException if <code>serializers</code>, <code>deserializers</code> and <code>classByteMap</code> did not have equal size or if a deserializer or serializer cannot be found for a class in the <code>classByteMap</code>
     */
    public MultiClassSerializationHelper(final Map<Class<? extends T>, Function<? extends T, byte[]>> serializers,
                                         Map<Class<? extends T>, Function<byte[], ? extends T>> deserializers, final BiMap<Class<? extends T>, Byte> classByteMap) throws IllegalArgumentException {


        if (classByteMap.size() > serializers.size() || classByteMap.size() > deserializers.size()) {
            throw new IllegalArgumentException("There are fewer entries in the serializer or deserializer maps than in the classToByteMap. Please add a serializer and deserializer for each class listed in the mapping file.");
        }

        this.serializers = new HashMap<Class<? extends T>, Function<? extends T, byte[]>>(serializers.size());
        this.deserializers = new HashMap<Byte, Function<byte[], ? extends T>>(deserializers.size());
        this.mapping = classByteMap;

        byte maxByte = Byte.MIN_VALUE;

        for (Map.Entry<Class<? extends T>, Byte> pairs : mapping.entrySet()) {
            if (pairs.getValue() > maxByte) {
                maxByte = pairs.getValue();
            }
            // Adding serializer
            Function<? extends T, byte[]> serializer = serializers.remove(pairs.getKey());
            if (serializer == null) {
                throw new IllegalArgumentException("No serializer found for class " + pairs.getKey().getName());
            }
            this.serializers.put(pairs.getKey(), serializer);

            // Adding deserializer
            Function<byte[], ? extends T> deserializer = deserializers.remove(pairs.getKey());
            if (deserializer == null) {
                throw new IllegalArgumentException("No deserializer found for class " + pairs.getKey().getName());
            }
            this.deserializers.put(pairs.getValue(), deserializer);
        }

        if (serializers.size() > 0 && deserializers.size() > 0 && serializers.size() == deserializers.size()) {
            for (Map.Entry<Class<? extends T>, Function<? extends T, byte[]>> serializerEntry : serializers.entrySet()) {
                Function<byte[], ? extends T> deserializer = deserializers.get(serializerEntry.getKey());
                if (deserializer != null) {
                    this.mapping.put(serializerEntry.getKey(), ++maxByte);
                    this.serializers.put(serializerEntry.getKey(), serializerEntry.getValue());
                    this.deserializers.put(maxByte, deserializer);
                } else {
                    throw new IllegalArgumentException("No deserializer found for class " + serializerEntry.getKey().getName());
                }
            }
        }

        if (this.serializers.size() != this.deserializers.size() || this.serializers.size() != this.mapping.size()) {
            throw new IllegalArgumentException("serializer mapping, deserializer mapping and byte to class mapping must have the same size! Check classByteMap for duplicate entries!");
        }

    }


    /**
     * This function builds a new class  byte mapping for the given serializers and deserializers
     * <p/>
     * <strong>Note: </strong> the returned map is not persisted by this method but you should persist it, if you'd like to deserialize serializations after a program restart hence the serializers and deserializers may have a different order.
     * <strong>Warning: </strong> multiple calls to this method may lead to different mappings.
     *
     * @param serializers   map from class types to serializer functions able to serialize instances of the class.
     * @param deserializers map from class types to deserializer functions able to deserialize byte arrays representing instances of the class.
     * @param <T>           the common parent type
     * @return a bidirectional mapping between classes and bytes
     * @throws IllegalArgumentException if <code>serializers.size()</code> and <code>deserializer.size()</code> differ or if these maps are bigger than <code>MAX_NUMBER_OF_SERIALIZERS</code>
     * @see de.uniluebeck.itm.util.serialization.MultiClassSerializationHelper#storeClassByteMap(java.io.File, com.google.common.collect.BiMap)  for mapping persistance
     * @see de.uniluebeck.itm.util.serialization.MultiClassSerializationHelper#loadClassByteMap(java.io.File) to get a persisted mapping from a file
     */
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

    /**
     * This methods loads the mapping from a file if it exists or creates a builds a new map and stores it at the file location otherwise.
     *
     * @param serializers   map from class types to serializer functions able to serialize instances of the class.
     * @param deserializers map from class types to deserializer functions able to deserialize byte arrays representing instances of the class.
     * @param mappingFile   the file used for mapping persistence
     * @param <T>           the parent type
     * @return the loaded mapping if existing, a newly created mapping otherwise
     * @throws IllegalArgumentException if the file points to a directory
     * @throws IOException              if another error occurs while reading or writing the mapping file
     * @see de.uniluebeck.itm.util.serialization.MultiClassSerializationHelper#buildClassByteMap(java.util.Map, java.util.Map)
     * @see de.uniluebeck.itm.util.serialization.MultiClassSerializationHelper#storeClassByteMap(java.io.File, com.google.common.collect.BiMap)
     * @see de.uniluebeck.itm.util.serialization.MultiClassSerializationHelper#loadClassByteMap(java.io.File)
     */
    public static <T> BiMap<Class<? extends T>, Byte> loadOrCreateClassByteMap(final Map<Class<? extends T>, Function<? extends T, byte[]>> serializers,
                                                                               Map<Class<? extends T>, Function<byte[], ? extends T>> deserializers, File mappingFile) throws IllegalArgumentException, IOException {
        BiMap<Class<? extends T>, Byte> mapping;
        try {
            mapping = loadClassByteMap(mappingFile);
        } catch (Exception e) {
            //failed to load mapping from file
            mapping = buildClassByteMap(serializers, deserializers);
            storeClassByteMap(mappingFile, mapping);
        }

        return mapping;

    }

    /**
     * This method creates an new MultiClassSerializationHelper after loading the class-byte-mapping from the specified file.
     *
     * @param serializers   map from class types to serializer functions able to serialize instances of the class.
     * @param deserializers map from class types to deserializer functions able to deserialize byte arrays representing instances of the class.
     * @param mappingFile   the file which contains the class-byte-mapping
     * @param <T>           the parent type of objects serialized by this helper
     * @return a new instance of this class
     * @throws IOException              if an error occurs while reading the mapping file
     * @throws ClassNotFoundException   if one or multiple classes specified in the mapping file cannot be found while loading the map
     * @throws IllegalArgumentException if the file points to a directory
     * @see de.uniluebeck.itm.util.serialization.MultiClassSerializationHelper#loadClassByteMap(java.io.File)
     */
    public static <T> MultiClassSerializationHelper<T> buildHelperWithMappingFile(final Map<Class<? extends T>, Function<? extends T, byte[]>> serializers,
                                                                                  Map<Class<? extends T>, Function<byte[], ? extends T>> deserializers, File mappingFile) throws IOException, ClassNotFoundException, IllegalArgumentException {
        BiMap<Class<? extends T>, Byte> map = loadClassByteMap(mappingFile);
        return new MultiClassSerializationHelper<T>(serializers, deserializers, map);
    }


    /**
     * Loading the class-byte-mapping from the provided file
     * <p/>
     * The file must be a simple text file containing one map entry per line. Class name and type byte are comma seperated (e.g. "java.lang.String,10")
     *
     * @param mappingFile the file containing the mapping
     * @param <T>         the parent type
     * @return the loaded class-byte-mapping
     * @throws java.io.FileNotFoundException if the mapping file cannot be found
     * @throws IOException                   if an error occurs while reading the mapping file
     * @throws ClassNotFoundException        if the mapping file specifies one or many non existing class(es)
     * @throws IllegalArgumentException      if the file points to a directory
     * @see de.uniluebeck.itm.util.serialization.MultiClassSerializationHelper#storeClassByteMap(java.io.File, com.google.common.collect.BiMap)
     */

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

    /**
     * Writes the provided class-byte-mapping to the provided file
     *
     * @param mappingFile the mapping file
     * @param mapping     the class-byte-map
     * @param <T>         the common parent type of all classes in the mapping
     * @throws IOException              if an error occurs while writing the mapping to the file
     * @throws IllegalArgumentException if the mapping file is a directory
     */
    public static <T> void storeClassByteMap(File mappingFile, BiMap<Class<? extends T>, Byte> mapping) throws IOException, IllegalArgumentException {
        if (!mappingFile.exists() && !mappingFile.isDirectory()) {
            Files.createParentDirs(mappingFile);
            mappingFile.createNewFile();
        }

        if (mappingFile.isDirectory()) {
            throw new IllegalArgumentException("Mapping file must be a file and not a directory (" + mappingFile.getAbsolutePath() + ")");
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(mappingFile));
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
            throw new IOException("Error while closing file. Persisted mapping file may be invalid!", e);
        }
    }

    public BiMap<Class<? extends T>, Byte> getClassByteMapping() {
        return mapping;
    }

    /**
     * Method for serializing an object.
     * <p/>
     * This method uses the objects getClass-method to get the serializer type
     *
     * @param object the object to serialize
     * @return the serialization of the object
     * @throws NotSerializableException if something went wrong during serialization
     */
    public byte[] serialize(T object) throws NotSerializableException {
        @java.lang.SuppressWarnings("unchecked") Class<T> c = (Class<T>) object.getClass();
        return serialize(object, c);
    }

    /**
     * Method for serializing an object using a specific serializer type.
     * <p/>
     * This method is useful, if there exists a serializer for a super type which should be used here.
     *
     * @param object the object to serialize
     * @param type   the serializer type to use
     * @return the serialized version of the object
     * @throws NotSerializableException if the serializer for the given <code>type</code> isn't able to serialize the object or if the serializer returns <code>null</code>.
     */
    public byte[] serialize(T object, final Class<? extends T> type) throws NotSerializableException {
        try {

            if (!serializers.containsKey(type)) {
                throw new NotSerializableException("Can't find a serializer for type " + type.getName());
            }

            Function serializer = serializers.get(type);
            @SuppressWarnings("unchecked") byte[] serialized = (byte[]) serializer.apply(object);

            if (serialized == null) {
                throw new RuntimeException("Serialization result for object " + object.toString() + " is null");
            }

            byte typeByte = mapping.get(type);
            byte[] finalSerialization = new byte[serialized.length + 1];
            finalSerialization[0] = typeByte;
            System.arraycopy(serialized, 0, finalSerialization, 1, serialized.length);

            return finalSerialization;

        } catch (ClassCastException e) {
            throw new NotSerializableException("Failed to apply serializer function to " + object);
        }
    }


    /**
     * Method for deserializing a byte array conforming to the serialization format used by this class (type byte|object serialization)
     *
     * @param serialization the serialization
     * @return the deserialized version
     * @throws IllegalArgumentException if no deserializer was found for the first byte (type byte) of the serialization or if the serialization is an empty array
     */
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
