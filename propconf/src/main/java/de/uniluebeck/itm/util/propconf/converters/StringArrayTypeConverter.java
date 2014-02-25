package de.uniluebeck.itm.util.propconf.converters;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.inject.TypeLiteral;
import org.nnsoft.guice.rocoto.converters.AbstractConverter;

public class StringArrayTypeConverter extends AbstractConverter<String[]> {

	@Override
	public Object convert(final String value, final TypeLiteral<?> toType) {
		return Iterables.toArray(Splitter.on(",").trimResults().omitEmptyStrings().split(value), String.class);
	}
}
