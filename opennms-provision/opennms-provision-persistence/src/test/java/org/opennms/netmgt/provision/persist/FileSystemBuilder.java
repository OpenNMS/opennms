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
import java.io.IOException;
import java.util.Stack;

import org.apache.commons.io.FileUtils;

class FileSystemBuilder {

	private File m_baseDir;
	private Stack<File> m_dirs = new Stack<>();

	public FileSystemBuilder(String dir, String name) {
		m_baseDir = new File(dir, name);
		m_baseDir.delete();
		m_baseDir.mkdirs();
		m_baseDir.deleteOnExit();
		m_dirs.push(m_baseDir);
	}
	
	public File getCurrentDir() {
		return m_dirs.peek();
	}
	
	public File pop() {
		return m_dirs.pop();
	}
	
	public FileSystemBuilder dir(String name) {
		File dir = new File(getCurrentDir(), name);
		dir.mkdirs();
		dir.deleteOnExit();
		m_dirs.push(dir);
		return this;
	}
	
	public FileSystemBuilder file(String name) throws IOException {
		return file(name, "");
	}
	
	public FileSystemBuilder file(String name, String contents) throws IOException {
		File file = new File(getCurrentDir(), name);
		FileUtils.writeStringToFile(file, contents);
		file.deleteOnExit();
		return this;
	}

	public void cleanup() throws IOException {
		m_dirs.clear();
		FileUtils.deleteDirectory(m_baseDir);
		
	}
	
	
}