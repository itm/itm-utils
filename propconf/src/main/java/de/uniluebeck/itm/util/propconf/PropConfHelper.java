package de.uniluebeck.itm.util.propconf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

class PropConfHelper {

	public static Map<Field, PropConf> getDeclaredAnnotations(final Class<?>... configClasses) {
		final Map<Field, PropConf> map = newHashMap();
		for (Class<?> configClass : configClasses) {
			for (Field field : configClass.getDeclaredFields()) {
				for (Annotation annotation : field.getDeclaredAnnotations()) {
					if (PropConf.class.isInstance(annotation)) {
						try {
							map.put(field, (PropConf) annotation);
						} catch (ClassCastException e) {
							throw new RuntimeException(
									PropConf.class.getSimpleName() +
											" annotations are only allowed on constant String fields!"
							);
						}
					}
				}
			}
		}
		return map;
	}

}
