package de.uniluebeck.itm.util.propconf;

import com.google.inject.AbstractModule;

import java.util.Properties;

public class MyAppModule extends AbstractModule {

	private final Properties properties;

	public MyAppModule(final Properties properties) {
		this.properties = properties;
	}

	@Override
	protected void configure() {
		install(new PropConfModule(properties, MyAppProperties.class));
		bind(MyApp.class).to(MyAppImpl.class);
	}
}
