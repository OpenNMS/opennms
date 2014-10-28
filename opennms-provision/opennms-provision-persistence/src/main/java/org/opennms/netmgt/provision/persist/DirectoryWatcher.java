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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.core.spring.FileReloadCallback;
import org.opennms.core.spring.FileReloadContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryWatcher<T> {
	
	private static final Logger LOG = LoggerFactory.getLogger(DirectoryWatcher.class);
	
	private File m_directory;
	private FileReloadCallback<T> m_loader;
	private ConcurrentHashMap<String, FileReloadContainer<T>> m_contents = new ConcurrentHashMap<String, FileReloadContainer<T>>();
	private AtomicReference<Set<String>> m_fileNames = new AtomicReference<Set<String>>();

	public DirectoryWatcher(File directory, FileReloadCallback<T> loader) {
		m_directory = directory;
		if (!m_directory.exists()) {
		    if(!m_directory.mkdirs()) {
		        LOG.warn("Could not make directory: {}",m_directory.getPath());
		    }
		}
		m_loader = loader;
	}
	
	public Set<String> getFileNames() {
		return checkFileChanges();
	}
	
	private Set<String> checkFileChanges() {
		while (true) {
			LinkedHashSet<String> fileNames = new LinkedHashSet<String>(Arrays.asList(m_directory.list()));
			Set<String> oldFileNames = m_fileNames.get();
			if (fileNames.equals(oldFileNames)) {
				return fileNames;
			} else if (m_fileNames.compareAndSet(oldFileNames, fileNames)) {
				m_contents.clear();
				return fileNames;
			}
		}
	}
	
	public Set<String> getBaseNamesWithExtension(String extension) {
		Set<String> fileNames = checkFileChanges();
		Set<String> basenames = new LinkedHashSet<String>(); 
		for(String fileName : fileNames) {
			if (fileName.endsWith(extension)) {
				String basename = fileName.substring(0, fileName.length()-extension.length());
				basenames.add(basename);
			}
		}
		return basenames;
	}

	public T getContents(String fileName) throws FileNotFoundException {
		checkFileChanges();
		
		File file = new File(m_directory, fileName);
		
		if (file.exists()) {
			FileReloadContainer<T> newContainer = new FileReloadContainer<T>(file, m_loader);
			newContainer.setReloadCheckInterval(0);
			FileReloadContainer<T> container = m_contents.putIfAbsent(file.getName(), newContainer);
			if (container == null) { container = newContainer; }
			return container.getObject();
		} else {
			m_contents.remove(fileName);
			throw new FileNotFoundException("there is no file " + fileName + " in directory " + m_directory);
		}
		
	}
		

}
