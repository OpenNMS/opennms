package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.FileReloadCallback;
import org.springframework.core.io.Resource;

public class DirectoryWatcherTest {

	private FileSystemBuilder m_bldr;
	private DirectoryWatcher<String> m_watcher;

	@Before
	public void setUp() throws IOException {
		
		m_bldr = new FileSystemBuilder("target", "DirectoryWatcherTest");
		m_bldr.file("file1.xml", "file1Contents").file("file2.xml", "file2Contents");
		
		File dir = m_bldr.getCurrentDir();

		FileReloadCallback<String> loader = new FileReloadCallback<String>() {

			@Override
			public String reload(String object, Resource resource) throws IOException {
				return FileUtils.readFileToString(resource.getFile());
			}
		};
		
		m_watcher = new DirectoryWatcher<String>(dir, loader);
	}
	
	@After
	public void tearDown() throws IOException {
		m_bldr.cleanup();
	}

	@Test
	public void testGetContents() throws FileNotFoundException {
		assertEquals("file1Contents", m_watcher.getContents("file1.xml"));
		assertEquals("file2Contents", m_watcher.getContents("file2.xml"));
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testFileDoesntExist() throws FileNotFoundException {
		m_watcher.getContents("doesnotexist.xml");
	}
	
	@Test
	public void testFileAdded() throws IOException {
		assertEquals("file2Contents", m_watcher.getContents("file2.xml"));
		
		m_bldr.file("file3.xml", "file3Contents");
		
		assertEquals("file3Contents", m_watcher.getContents("file3.xml"));
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testFileDeleted() throws IOException {
		assertEquals("file2Contents", m_watcher.getContents("file2.xml"));
		
		File file2 = new File(m_bldr.getCurrentDir(), "file2.xml");
		
		file2.delete();
		
		// expect this to throw a file not found
		m_watcher.getContents("file2.xml");
		
		fail("This should not get here!");
		
	}
	
	@Test
	public void testGetFilesNames() {
		assertEquals(set("file1.xml", "file2.xml"), m_watcher.getFileNames());
		assertEquals(set("file1", "file2"), m_watcher.getBaseNamesWithExtension(".xml"));
		assertEquals(set(), m_watcher.getBaseNamesWithExtension(".txt"));
	}
	
	
	public <T> Set<T> set(T... items) {
		Set<T> set = new LinkedHashSet<T>();
		Collections.addAll(set, items);
		return set;
	}

}
