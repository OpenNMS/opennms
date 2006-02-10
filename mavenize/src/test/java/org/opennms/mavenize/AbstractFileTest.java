package org.opennms.mavenize;

import java.io.File;

import junit.framework.TestCase;

public class AbstractFileTest extends TestCase {

	protected void assertDirectoryExists(String dirName) {
		assertDirectoryExists(new File(dirName));
	}

	protected void assertDirectoryExists(String baseDir, String path) {
		assertDirectoryExists(new File(baseDir, path));
	}

	protected void assertDirectoryExists(File dir) {
		assertTrue("Directory "+dir.getPath()+" does not exist.", dir.exists());
		assertTrue(dir.getPath()+" is not a directory", dir.isDirectory());
	}

	protected void assertFileExists(String baseDir, String path) {
		assertFileExists(new File(baseDir, path));
	}

	protected void assertFileExists(File file) {
		assertTrue("File "+file.getPath()+" does not exist.", file.exists());
	}

	protected void assertFileNotExists(String baseDir, String path) {
		assertFileNotExists(new File(baseDir, path));
	}

	protected void assertFileNotExists(File file) {
		assertFalse("File "+file.getPath()+" shoult NOT exist.", file.exists());
	}

}
