/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.opennms.core.spring.FileReloadCallback;
import org.opennms.core.test.MockLogAppender;
import org.springframework.core.io.Resource;

public class DirectoryWatcherTest {

	private FileSystemBuilder m_bldr;
	private DirectoryWatcher<String> m_watcher;

	@Before
	public void setUp() throws Exception {
		MockLogAppender.setupLogging();
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
	public void tearDown() throws Exception {
		m_bldr.cleanup();
		m_watcher.stop();
		MockLogAppender.assertNoWarningsOrGreater();
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

	@Test
	public void testFilUpdated() throws Exception {
	    assertEquals("file2Contents", m_watcher.getContents("file2.xml"));

	    m_bldr.file("file2.xml", "updated-content");

	    assertEquals("updated-content", m_watcher.getContents("file2.xml"));
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
		assertEquals(Collections.emptySet(), m_watcher.getBaseNamesWithExtension(".txt"));
	}
	
	
	private static Set<String> set(String... items) {
		Set<String> set = new LinkedHashSet<>();
		Collections.addAll(set, items);
		return set;
	}

}
