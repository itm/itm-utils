package de.uniluebeck.itm.util.propconf;

import org.nnsoft.guice.rocoto.converters.AbstractConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface PropConf {

	String usage() default "";

	String example() default "";

	boolean required() default false;

	String defaultValue() default "";

	Class<? extends AbstractConverter> typeConverter() default AbstractConverter.class;
}
