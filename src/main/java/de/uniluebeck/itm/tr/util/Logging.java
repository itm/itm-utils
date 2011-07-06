/**********************************************************************************************************************
 * Copyright (c) 2010, Institute of Telematics, University of Luebeck                                                 *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or        *
 *   promote products derived from this software without specific prior written permission.                           *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package de.uniluebeck.itm.tr.util;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import org.apache.log4j.*;

import java.io.File;
import java.util.Properties;

/**
 * Helper class to set logging defaults for log4j in a running JVM.
 */
@SuppressWarnings("unused")
public class Logging {

	/**
	 * The default {@link PatternLayout}
	 */
	private static final String DEFAULT_PATTERN_LAYOUT =
			"%-23d{yyyy-MM-dd HH:mm:ss,SSS} | %-30.30t | %-30.30c{1} | %-5p | %m%n";

	/**
	 * Same as calling {@link Logging#setLoggingDefaults(org.apache.log4j.Level, org.apache.log4j.Layout)} with level
	 * {@link Level#INFO} and layout {@link Logging#DEFAULT_PATTERN_LAYOUT}.
	 */
	public static void setLoggingDefaults() {
		setLoggingDefaults(Level.INFO, new PatternLayout(DEFAULT_PATTERN_LAYOUT));
	}

	/**
	 * Same as calling {@link Logging#setLoggingDefaults(org.apache.log4j.Level, org.apache.log4j.Layout)} with level
	 * {@link Level#DEBUG} and layout {@link Logging#DEFAULT_PATTERN_LAYOUT}.
	 */
	public static void setDebugLoggingDefaults() {
		setLoggingDefaults(Level.DEBUG, new PatternLayout(DEFAULT_PATTERN_LAYOUT));
	}

	/**
	 * Same as calling {@link Logging#setLoggingDefaults(org.apache.log4j.Level, org.apache.log4j.Layout)} with level
	 * {@code level} and layout {@link Logging#DEFAULT_PATTERN_LAYOUT}.
	 *
	 * @param level the log level to set on the root logger
	 */
	public static void setLoggingDefaults(final Level level) {
		setLoggingDefaults(level, new PatternLayout(DEFAULT_PATTERN_LAYOUT));
	}

	/**
	 * Sets the logging defaults according to the following algorithm:
	 * <p/>
	 * <ol> <li>If the system property "log4j.configuration" is found and it points to a valid readable file the file is
	 * passed to log4j as a configuration file.</li> <li>If the first does not apply and a file with the name
	 * "log4j.properties" is found in the JVMs classpath this file is passed to log4j as a configuration file.</li> <li>If
	 * neither the first or the second apply log4j is configured to log level {@code level} and layout {@code layout}.</li>
	 * </ol>
	 *
	 * @param level  the log level to set on the root logger
	 * @param layout the layout to set on the root logger
	 */
	public static void setLoggingDefaults(final Level level, final Layout layout) {

		if (System.getProperty("log4j.configuration") != null) {

			File configurationFile = new File(System.getProperty("log4j.configuration"));
			if (configurationFile.exists() && !configurationFile.isDirectory() && configurationFile.canRead()) {
				try {
					PropertyConfigurator.configure(configurationFile.getAbsolutePath());
					return;
				} catch (Exception e) {
					System.err.println(
							"Tried to load log4j configuration from classpath, resulting in the following exception: {}" + e
					);
					System.err.println("Using default logging configuration.");
				}
			}

		}

		ClassPathFactory factory = new ClassPathFactory();
		ClassPath classPath = factory.createFromJVM();
		if (classPath.isResource("log4j.properties")) {
			try {
				Properties properties = new Properties();
				properties.load(classPath.getResourceAsStream("log4j.properties"));
				PropertyConfigurator.configure(properties);
				return;
			} catch (Exception e) {
				System.err.println(
						"Tried to load log4j configuration from classpath, resulting in the following exception: {}" + e
				);
				System.err.println("Using default logging configuration.");
			}
		}


		Appender appender = new ConsoleAppender(layout);

		Logger.getRootLogger().addAppender(appender);
		Logger.getRootLogger().setLevel(level);
	}
}
