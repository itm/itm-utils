package de.uniluebeck.itm.util.propconf;

import com.google.inject.Guice;

import java.util.Properties;

public class MyMain {

	public static void main(String[] args) {

		final Properties properties = new Properties();
		//properties.put(MyAppProperties.MYAPP_MYCUSTOMTYPE, "mycustomvalue");
		properties.put(MyAppProperties.MYAPP_MYCUSTOMTYPE2, "mycustomvalue2");

		final MyAppModule myAppModule = new MyAppModule(properties);
		Guice.createInjector(myAppModule).getInstance(MyApp.class).start();
	}

}
