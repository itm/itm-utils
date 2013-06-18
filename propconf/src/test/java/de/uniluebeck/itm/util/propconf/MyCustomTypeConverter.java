package de.uniluebeck.itm.util.propconf;

import com.google.inject.TypeLiteral;
import org.nnsoft.guice.rocoto.converters.AbstractConverter;

public class MyCustomTypeConverter extends AbstractConverter<MyCustomType> {

	@Override
	public Object convert(final String value, final TypeLiteral<?> toType) {
		return new MyCustomType(value);
	}
}
