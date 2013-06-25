package de.uniluebeck.itm.util.files;

import java.io.File;

@SuppressWarnings("unused")
public class FilePreconditions {

	public static String checkFileExists(final String fileName) {
		checkFileExists(new File(fileName));
		return fileName;
	}

	public static File checkFileExists(final File file) {
		if (!file.exists()) {
			throw new RuntimeException(
					getFileOrDirectoryString(file) + " \"" + file.getAbsolutePath() + "\" does not exist"
			);
		}
		return file;
	}

	public static String checkFileReadable(final String fileName) {
		checkFileReadable(new File(fileName));
		return fileName;
	}

	public static File checkFileReadable(final File file) {
		if (!file.canRead()) {
			throw new RuntimeException(
					getFileOrDirectoryString(file) + " \"" + file.getAbsolutePath() + "\" can't be read"
			);
		}
		return file;
	}

	public static String checkFileWritable(final String fileName) {
		checkFileWritable(new File(fileName));
		return fileName;
	}

	public static File checkFileWritable(final File file) {
		if (!file.canWrite()) {
			throw new RuntimeException(
					getFileOrDirectoryString(file) + "\"" + file.getAbsolutePath() + "\" can't be written"
			);
		}
		return file;
	}

	public static String checkFileExecutable(final String fileName) {
		checkFileExecutable(new File(fileName));
		return fileName;
	}

	public static File checkFileExecutable(final File file) {
		if (!file.canExecute()) {
			throw new RuntimeException(
					getFileOrDirectoryString(file) + " \"" + file.getAbsolutePath() + "\" can't be executed"
			);
		}
		return file;
	}

	private static String getFileOrDirectoryString(final File file) {
		return file.isDirectory() ? "Directory" : "File";
	}

}
