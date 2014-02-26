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

package de.uniluebeck.itm.util.logging;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import org.apache.log4j.*;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;

/**
 * Helper class to set logging defaults for log4j in a running JVM.
 */
@SuppressWarnings("unused")
public class Logging {

	/**
	 * All available log levels as an array.
	 */
	public final static LogLevel[] LOG_LEVELS =
			{LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR};

	private static final Map<LogLevel, Level> LOG_LEVEL_MAP;

	static {
		Map<LogLevel, Level> map = new HashMap<LogLevel, Level>();
		map.put(LogLevel.TRACE, Level.TRACE);
		map.put(LogLevel.DEBUG, Level.DEBUG);
		map.put(LogLevel.INFO, Level.INFO);
		map.put(LogLevel.WARN, Level.WARN);
		map.put(LogLevel.ERROR, Level.ERROR);
		LOG_LEVEL_MAP = Collections.unmodifiableMap(map);
	}

	/**
	 * The default {@link PatternLayout}
	 */
	public static final String DEFAULT_PATTERN_LAYOUT =
			"%-23d{yyyy-MM-dd HH:mm:ss,SSS} | %-30.30t | %-30.30c{1} | %-5p | %m%n";

	/**
	 * Same as calling {@link Logging#setLoggingDefaults(LogLevel, String)} with level {@link Level#INFO} and layout
	 * {@link Logging#DEFAULT_PATTERN_LAYOUT} set on a {@link ConsoleAppender}.
	 */
	public static void setLoggingDefaults() {
		setLoggingDefaults(LogLevel.INFO, DEFAULT_PATTERN_LAYOUT);
	}

	/**
	 * Same as calling {@link Logging#setLoggingDefaults(LogLevel, String)} with level {@link Level#DEBUG} and layout
	 * {@link Logging#DEFAULT_PATTERN_LAYOUT} set on a {@link ConsoleAppender}.
	 */
	public static void setDebugLoggingDefaults() {
		setLoggingDefaults(LogLevel.DEBUG, DEFAULT_PATTERN_LAYOUT);
	}

	/**
	 * Same as calling {@link Logging#setLoggingDefaults(LogLevel, String)} with level {@code level} and layout
	 * {@link Logging#DEFAULT_PATTERN_LAYOUT} set on a {@link ConsoleAppender}.
	 *
	 * @param logLevel
	 * 		the log level to set on the root logger
	 */
	public static void setLoggingDefaults(final LogLevel logLevel) {
		setLoggingDefaults(logLevel, DEFAULT_PATTERN_LAYOUT);
	}

	/**
	 * Sets the logging defaults according to the following algorithm:
	 * <p/>
	 * <ol> <li>If the system property "log4j.configuration" is found and it points to a valid readable file the file is
	 * passed to log4j as a configuration file.</li> <li>If the first does not apply and a file with the name
	 * "log4j.properties" is found in the JVMs classpath this file is passed to log4j as a configuration file.</li> <li>If
	 * neither the first or the second apply log4j is configured to log logLevel {@code logLevel} and layout {@code
	 * layout}.</li>
	 * </ol>
	 *
	 * @param logLevel
	 * 		the log logLevel to set on the root logger
	 * @param patternLayout
	 * 		{@link Appender} instances to add to the root logger
	 */
	public static void setLoggingDefaults(final LogLevel logLevel, final String patternLayout) {

		final Appender appender = new ConsoleAppender(new PatternLayout(patternLayout));

		Logger.getRootLogger().removeAllAppenders();

		if (tryToLoadFromSystemProperty()) {
			return;
		}

		if (tryToLoadFromClassPath()) {
			return;
		}

		Logger.getRootLogger().addAppender(appender);
		Logger.getRootLogger().setLevel(LOG_LEVEL_MAP.get(logLevel));

		java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		for (final Handler handler : handlers) {
			rootLogger.removeHandler(handler);
		}
		SLF4JBridgeHandler.install();
	}

	public static void setRootLogLevel(final LogLevel logLevel) {
		Logger.getRootLogger().setLevel(LOG_LEVEL_MAP.get(logLevel));
	}

	public static void setLogLevel(final LogLevel logLevel, final String... packages) {
		for (String aPackage : packages) {
			org.apache.log4j.Logger.getLogger(aPackage).setLevel(LOG_LEVEL_MAP.get(logLevel));
		}
	}

	/**
	 * Adds a {@link FileAppender} to the logging configuration which appends all logged messages to a file using the
	 * specified {@link Logging#DEFAULT_PATTERN_LAYOUT}.<br/>
	 * Note that the the {@link FileAppender} will be removed if {@link Logging#setDebugLoggingDefaults} or
	 * {@link Logging#setLoggingDefaults} is called.
	 *
	 * @param pathToFile
	 * 		Path to the file used to store the logged messages
	 * @throws IOException
	 * 		Thrown if the file cannot be accessed properly
	 */
	public static void addFileAppender(String pathToFile) throws IOException {
		final Appender appender = new FileAppender(new PatternLayout(Logging.DEFAULT_PATTERN_LAYOUT), pathToFile, true);
		org.apache.log4j.Logger.getRootLogger().addAppender(appender);
	}

	/**
	 * Tries to load a file with the name "log4j.properties" if found on the classpath and loads the Log4J properties from
	 * the files' content.
	 *
	 * @return {@code true} if loading succeeded, {@code false} otherwise
	 */
	private static boolean tryToLoadFromClassPath() {
		ClassPathFactory factory = new ClassPathFactory();
		ClassPath classPath = factory.createFromJVM();
		if (classPath.isResource("log4j.properties")) {
			try {
				Properties properties = new Properties();
				properties.load(classPath.getResourceAsStream("log4j.properties"));
				PropertyConfigurator.configure(properties);
				return true;
			} catch (Exception e) {
				System.err.println(
						"Tried to load log4j configuration from classpath, resulting in the following exception: {}" + e
				);
				System.err.println("Using default logging configuration.");
			}
		}
		return false;
	}

	/**
	 * Tries to load a file from the system property "log4j.configuration" and loads the Log4J properties from the files'
	 * content.
	 *
	 * @return {@code true} if loading succeeded, {@code false} otherwise
	 */
	private static boolean tryToLoadFromSystemProperty() {
		if (System.getProperty("log4j.configuration") != null) {

			File configurationFile = new File(System.getProperty("log4j.configuration"));
			if (configurationFile.exists() && !configurationFile.isDirectory() && configurationFile.canRead()) {
				try {
					PropertyConfigurator.configure(configurationFile.getAbsolutePath());
					return true;
				} catch (Exception e) {
					System.err.println(
							"Tried to load log4j configuration from classpath, resulting in the following exception: {}" + e
					);
					System.err.println("Using default logging configuration.");
				}
			}

		}
		return false;
	}
}
