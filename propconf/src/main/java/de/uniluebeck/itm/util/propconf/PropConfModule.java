package de.uniluebeck.itm.util.propconf;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.nnsoft.guice.rocoto.configuration.ConfigurationModule;
import org.nnsoft.guice.rocoto.converters.AbstractConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;

public class PropConfModule<T> extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(PropConfModule.class);

	private final Class<? extends T> configClass;

	private final Properties properties;

	public PropConfModule(final Class<? extends T> configClass, final Properties properties) {
		this.configClass = configClass;
		this.properties = properties;
		assertRequiredPropertiesPresent();
	}

	@Override
	protected void configure() {
		bindDeclaredProperties();
		installDeclaredConverters();
	}

	private void assertRequiredPropertiesPresent() {
		for (Map.Entry<String, PropConf> entry : getDeclaredAnnotations().entrySet()) {
			if (entry.getValue().required() && !properties.containsKey(entry.getKey())) {
				String msg = "Configuration property \"" + entry.getKey() + "\""
						+ " (Usage: " + entry.getValue().usage() + ","
						+ " Example: " + entry.getKey() + "=" + entry.getValue().example() + ") is missing!";
				throw new IllegalArgumentException(msg);
			}
		}
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

	private Map<String, PropConf> getDeclaredAnnotations() {
		final Map<String, PropConf> map = newHashMap();
		for (Field field : configClass.getDeclaredFields()) {
			for (Annotation annotation : field.getDeclaredAnnotations()) {
				if (PropConf.class.isInstance(annotation)) {
					try {
						map.put((String) field.get(null), (PropConf) annotation);
					} catch (ClassCastException e) {
						throw new RuntimeException(
								PropConf.class.getSimpleName() +
										" annotations are only allowed on constant String fields!"
						);
					} catch (IllegalAccessException e) {
						throw propagate(e);
					}
				}
			}
		}
		return map;
	}

	private void bindDeclaredProperties() {

		final Map<String, PropConf> declaredAnnotations = getDeclaredAnnotations();
		final Set<String> declaredKeys = declaredAnnotations.keySet();
		final Set<String> boundKeys = newHashSet(transform(properties.keySet(), toStringFunction()));
		final Set<String> unboundKeys = difference(declaredKeys, boundKeys);

		// bind all constants that were included in the .properties file
		install(new ConfigurationModule() {
			@Override
			protected void bindConfigurations() {
				bindProperties(properties);
			}
		});

		// bind any non-specified properties to their default values
		for (String unboundKey : unboundKeys) {
			final String defaultValue = declaredAnnotations.get(unboundKey).defaultValue();
			log.trace("Binding default value ({}) for unspecified property \"{}\"", defaultValue, unboundKey);
			bindConstant().annotatedWith(Names.named(unboundKey)).to(defaultValue);
		}
	}
}
