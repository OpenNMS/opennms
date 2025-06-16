/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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