package de.uniluebeck.itm.util;

import java.io.File;

@SuppressWarnings("unused")
public class FilePreconditions {

	public static void checkFileExists(final String fileName) {
		checkFileExists(new File(fileName));
	}

	public static void checkFileExists(final File file) {
		if (!file.exists()) {
			throw new RuntimeException(
					getFileOrDirectoryString(file) + " \"" + file.getAbsolutePath() + "\" does not exist"
			);
		}
	}

	public static void checkFileReadable(final String fileName) {
		checkFileReadable(new File(fileName));
	}

	public static void checkFileReadable(final File file) {
		if (!file.canRead()) {
			throw new RuntimeException(
					getFileOrDirectoryString(file) + " \"" + file.getAbsolutePath() + "\" can't be read"
			);
		}
	}

	public static void checkFileWritable(final String fileName) {
		checkFileWritable(new File(fileName));
	}

	public static void checkFileWritable(final File file) {
		if (!file.canWrite()) {
			throw new RuntimeException(
					getFileOrDirectoryString(file) + "\"" + file.getAbsolutePath() + "\" can't be written"
			);
		}
	}

	public static void checkFileExecutable(final String fileName) {
		checkFileExecutable(new File(fileName));
	}

	public static void checkFileExecutable(final File file) {
		if (!file.canExecute()) {
			throw new RuntimeException(
					getFileOrDirectoryString(file) + " \"" + file.getAbsolutePath() + "\" can't be executed"
			);
		}
	}

	private static String getFileOrDirectoryString(final File file) {
		return file.isDirectory() ? "Directory" : "File";
	}

}
