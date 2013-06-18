package de.uniluebeck.itm.util.propconf;

public interface MyAppProperties extends PropConfProperties {

	@PropConf(
			usage = "myUsage",
			example = "myExample",
			required = true,
			typeConverter = MyCustomTypeConverter.class
	)
	String MYAPP_MYCUSTOMTYPE = "myapp.mycustomtype";

	@PropConf(
			usage = "myUsage2",
			example = "myExample2",
			required = false,
			typeConverter = MyCustomTypeConverter.class
	)
	String MYAPP_MYCUSTOMTYPE2 = "myapp.mycustomtype2";
}
