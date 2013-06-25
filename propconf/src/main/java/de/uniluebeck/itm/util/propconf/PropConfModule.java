package de.uniluebeck.itm.util.propconf;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.nnsoft.guice.rocoto.configuration.ConfigurationModule;
import org.nnsoft.guice.rocoto.converters.AbstractConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;

public class PropConfModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(PropConfModule.class);

	private final Class<?>[] configClasses;

	private final Properties properties;

	public PropConfModule(final Properties properties, final Class<?>... configClasses) {
		this.configClasses = configClasses;
		this.properties = properties;
	}

	@Override
	protected void configure() {
		bindDeclaredProperties();
		installDeclaredConverters();
	}

	private void installDeclaredConverters() {
		for (PropConf propConf : getDeclaredAnnotations().values()) {
			if (AbstractConverter.class != propConf.typeConverter()) {
				final AbstractConverter converter;
				try {
					converter = propConf.typeConverter().newInstance();
				} catch (Exception e) {
					throw propagate(e);
				}
				log.trace("Installing Guice type converter: {}", converter.getClass().getCanonicalName());
				install(converter);
			}
		}
	}

	private Map<Field, PropConf> getDeclaredAnnotations() {
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

	private void bindDeclaredProperties() {

		if (log.isInfoEnabled()) {
			log.info("Binding properties for {}", Arrays.toString(configClasses));
		}

		final Map<Field, PropConf> declaredAnnotations = getDeclaredAnnotations();
		final Set<String> declaredKeys =
				newHashSet(Iterables.transform(declaredAnnotations.keySet(), new Function<Field, String>() {
					@Override
					public String apply(final Field input) {
						try {
							return (String) input.get(null);
						} catch (IllegalAccessException e) {
							throw propagate(e);
						}
					}
				}
				)
				);
		final Set<String> boundKeys = newHashSet(transform(properties.keySet(), toStringFunction()));
		final Set<String> unboundKeys = difference(declaredKeys, boundKeys);

		// bind all constants that were included in the .properties file
		install(new ConfigurationModule() {
			@Override
			protected void bindConfigurations() {
				bindProperties(properties);
			}
		}
		);

		// bind any non-specified properties to their default values
		try {
			for (Field field : declaredAnnotations.keySet()) {
				final String key = (String) field.get(null);
				if (unboundKeys.contains(key)) {
					final String defaultValue = declaredAnnotations.get(field).defaultValue();
					log.info("Binding \"{}\" = \"{}\" (default value)", key, defaultValue);
					bindConstant().annotatedWith(Names.named(key)).to(defaultValue);
				} else if (boundKeys.contains(key)) {
					final String value = (String) properties.get(key);
					log.info("Binding \"{}\" = \"{}\"", key, value);
					bindConstant().annotatedWith(Names.named(key)).to(value);
				}
			}
		} catch (IllegalAccessException e) {
			throw propagate(e);
		}
	}
}
