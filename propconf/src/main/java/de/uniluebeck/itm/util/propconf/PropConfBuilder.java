package de.uniluebeck.itm.util.propconf;

import com.google.inject.Guice;

import java.util.Properties;

public abstract class PropConfBuilder {

	public static <T> T buildConfig(final Class<? extends T> configClass, final Properties properties) {
		return Guice.createInjector(new PropConfModule(properties, configClass)).getInstance(configClass);
	}
}
