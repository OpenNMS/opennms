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
package org.opennms.netmgt.config.mock;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;

public class MockUserManager extends UserManager {

    String m_xmlString;
    boolean updateNeeded = true;
    private long m_lastModified;
    private long m_fileSize;
    
    public MockUserManager(GroupManager groupManager, String xmlString) {
        super(groupManager);
        m_xmlString = xmlString;
        parseXML();
    }

    private void parseXML() {
        InputStream in = new ByteArrayInputStream(m_xmlString.getBytes());
        parseXML(in);
        updateNeeded = false;
        m_lastModified = System.currentTimeMillis();
        m_fileSize = m_xmlString.getBytes().length;
    }

    @Override
    protected void saveXML(String writerString) throws IOException {
        m_xmlString = writerString;
        updateNeeded = true;
    }

    @Override
    protected void doUpdate() throws IOException, FileNotFoundException {
        if (updateNeeded) {
            parseXML();
        }
    }

    @Override
    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    @Override
    public long getLastModified() {
        return m_lastModified;
    }

    @Override
    public long getFileSize() {
        return m_fileSize;
    }

    @Override
    public void reload() throws IOException, FileNotFoundException {
        parseXML();
    }

}
