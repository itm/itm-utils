package de.uniluebeck.itm.util.propconf;

public class MyAppProperties {

	@PropConf(
			usage = "myUsage",
			example = "myExample",
			typeConverter = MyCustomTypeConverter.class
	)
	public static final String MYAPP_MYCUSTOMTYPE = "myapp.mycustomtype";

	@PropConf(
			usage = "myUsage2",
			example = "myExample2",
			defaultValue = "myDefaultValue2",
			typeConverter = MyCustomTypeConverter.class
	)
	public static final String MYAPP_MYCUSTOMTYPE2 = "myapp.mycustomtype2";
}
