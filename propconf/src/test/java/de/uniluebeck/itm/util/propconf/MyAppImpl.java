package de.uniluebeck.itm.util.propconf;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import javax.annotation.Nullable;

public class MyAppImpl implements MyApp {

	private final MyCustomType myCustomType;

	@Nullable
	private final MyCustomType myCustomType2;

	@Inject
	public MyAppImpl(@Named(MyAppProperties.MYAPP_MYCUSTOMTYPE) final MyCustomType myCustomType,
					 @Nullable @Named(MyAppProperties.MYAPP_MYCUSTOMTYPE2) final MyCustomType myCustomType2) {
		this.myCustomType = myCustomType;
		this.myCustomType2 = myCustomType2;
	}

	@Override
	public void start() {
		System.out.println(
				"Starting with myCustomType=\"" + myCustomType + "\" and myCustomType2=\"" + myCustomType2 + "\""
		);
	}
}
