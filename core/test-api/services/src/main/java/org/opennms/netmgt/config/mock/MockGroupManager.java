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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.opennms.netmgt.config.GroupManager;

public class MockGroupManager extends GroupManager {
    
    String m_xmlString;
    boolean updateNeeded = false;
    
    public MockGroupManager(String xmlString) throws IOException {
        m_xmlString = xmlString;
        parseXML();
    }

    private void parseXML() throws IOException {
        InputStream reader = new ByteArrayInputStream(m_xmlString.getBytes(StandardCharsets.UTF_8));
        parseXml(reader);
        updateNeeded = false;
    }

    @Override
    public void update() throws IOException {
        if (updateNeeded) {
            parseXML();
        }
    }

    @Override
    protected void saveXml(String data) throws IOException {
        m_xmlString = data;
        updateNeeded = true;
    }

}
