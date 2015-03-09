package de.uniluebeck.itm.util.propconf.converters;

import com.google.inject.TypeLiteral;
import org.nnsoft.guice.rocoto.converters.AbstractConverter;

public class ClassTypeConverter extends AbstractConverter<Class<?>> {

    @Override
    public Object convert(String value, TypeLiteral<?> toType) {
        try {
            return Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
