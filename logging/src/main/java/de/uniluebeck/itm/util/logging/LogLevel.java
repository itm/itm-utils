package de.uniluebeck.itm.util.logging;

public enum LogLevel {
	TRACE,
	DEBUG,
	INFO,
	WARN,
	ERROR;

	public static LogLevel toLevel(String level) {
		for (LogLevel logLevel : values()) {
			if (logLevel.toString().equalsIgnoreCase(level)) {
				return logLevel;
			}
		}
		throw new IllegalArgumentException(level + " is not a valid LogLevel!");
	}
}
