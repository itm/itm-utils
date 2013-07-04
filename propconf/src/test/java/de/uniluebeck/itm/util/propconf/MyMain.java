package de.uniluebeck.itm.util.propconf;

import com.google.inject.Guice;
import de.uniluebeck.itm.util.logging.Logging;

import java.util.Properties;

public class MyMain {

	static {
		Logging.setLoggingDefaults();
	}

	public static void main(String[] args) {


		final Properties properties = new Properties();
		//properties.put(MyAppProperties.MYAPP_MYCUSTOMTYPE, "mycustomvalue");
		properties.put(MyAppProperties.MYAPP_MYCUSTOMTYPE2, "mycustomvalue2");

		if (args.length > 0 && "help".equals(args[0])) {
			PropConfBuilder.printDocumentation(System.out, MyAppProperties.class);
		} else {

			final MyAppModule myAppModule = new MyAppModule(properties);
			Guice.createInjector(myAppModule).getInstance(MyApp.class).start();
		}
	}

}
