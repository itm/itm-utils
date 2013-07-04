package de.uniluebeck.itm.util.propconf;

import com.google.inject.Guice;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.*;

import static com.google.common.base.Throwables.propagate;
import static de.uniluebeck.itm.util.propconf.PropConfHelper.getDeclaredAnnotations;

public abstract class PropConfBuilder {

	@SuppressWarnings("unused")
	public static <T> T buildConfig(final Class<? extends T> configClass, final Properties properties) {
		return Guice.createInjector(new PropConfModule(properties, configClass)).getInstance(configClass);
	}

	@SuppressWarnings("unused")
	public static String getDocumentationString(final Class<?>... configClasses) {
		final StringWriter stringWriter = new StringWriter();
		printDocumentation(stringWriter, configClasses);
		return stringWriter.toString();
	}

	@SuppressWarnings("unused")
	public static OutputStream printDocumentation(final OutputStream outputStream, final Class<?>... configClasses) {
		printDocumentation(new PrintWriter(outputStream), configClasses);
		return outputStream;
	}

	@SuppressWarnings("unused")
	public static Writer printDocumentation(final Writer writer, final Class<?>... configClasses) {
		try {

			final SortedMap<Field, PropConf> sortedMap = new TreeMap<Field, PropConf>(new Comparator<Field>() {
				@Override
				public int compare(final Field o1, final Field o2) {
					try {
						return ((String) o1.get(null)).compareTo(((String) o2.get(null)));
					} catch (IllegalAccessException e) {
						throw propagate(e);
					}
				}
			}
			);
			sortedMap.putAll(getDeclaredAnnotations(configClasses));

			for (Map.Entry<Field, PropConf> entry : sortedMap.entrySet()) {


				final String usage = entry.getValue().usage();
				final String example = entry.getValue().example();
				final String defaultValue = entry.getValue().defaultValue();
				final String key = (String) entry.getKey().get(null);

				writer.write("# Usage: ");
				writer.write(usage);
				writer.write("\n");

				if (!example.isEmpty()) {
					writer.write("# Example: ");
					writer.write(example);
					writer.write("\n");
				}

				if (!defaultValue.isEmpty()) {
					writer.write("# Default value: ");
					writer.write(defaultValue);
					writer.write("\n");
				}

				if (!defaultValue.isEmpty()) {
					writer.write("#");
				}
				writer.write(String.format("%s = %s", key, defaultValue));
				writer.write("\n");
				writer.write("\n");
				writer.flush();
			}

		} catch (Exception e) {
			throw propagate(e);
		}

		return writer;
	}
}
