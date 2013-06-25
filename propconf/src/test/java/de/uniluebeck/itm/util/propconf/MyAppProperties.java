package de.uniluebeck.itm.util.propconf;

public interface MyAppProperties {

	@PropConf(
			usage = "myUsage",
			example = "myExample",
			typeConverter = MyCustomTypeConverter.class
	)
	String MYAPP_MYCUSTOMTYPE = "myapp.mycustomtype";

	@PropConf(
			usage = "myUsage2",
			example = "myExample2",
			typeConverter = MyCustomTypeConverter.class
	)
	String MYAPP_MYCUSTOMTYPE2 = "myapp.mycustomtype2";
}
