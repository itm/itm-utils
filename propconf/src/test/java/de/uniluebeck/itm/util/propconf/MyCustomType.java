package de.uniluebeck.itm.util.propconf;

public class MyCustomType {

	private final String value;

	public MyCustomType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "MyCustomType{" +
				"value='" + value + '\'' +
				"}";
	}
}
