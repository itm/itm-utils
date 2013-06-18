package de.uniluebeck.itm.util.propconf;

import com.google.inject.AbstractModule;
import org.nnsoft.guice.rocoto.configuration.ConfigurationModule;
import org.nnsoft.guice.rocoto.converters.AbstractConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.newHashMap;

public class PropConfModule extends AbstractModule {

	private final Class<? extends PropConfProperties> propertiesClass;

	private final Properties properties;

	public PropConfModule(final Class<? extends PropConfProperties> propertiesClass,
						  final Properties properties) {
		this.propertiesClass = propertiesClass;
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
			if (!properties.containsKey(entry.getKey())) {
				String msg = "Configuration property \"" + entry.getKey() + "\""
						+ " (Usage: a" + entry.getValue().usage() + ","
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
				install(converter);
			}
		}
	}

	private Map<String, PropConf> getDeclaredAnnotations() {
		final Map<String, PropConf> map = newHashMap();
		for (Field field : propertiesClass.getDeclaredFields()) {
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
		install(new ConfigurationModule() {
			@Override
			protected void bindConfigurations() {
				bindProperties(properties);
			}
		}
		);
	}
}
