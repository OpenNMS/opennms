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

import org.opennms.netmgt.config.NotifdConfigManager;

/**
 * @author David Hustace <david@opennms.org>
 */
public class MockNotifdConfigManager extends NotifdConfigManager {
        
    private String m_nextNotifIdSql;
    private String m_nextUserNotifIdSql;

    /*
     * init the mock config
     */
    
    
    /**
     * @param configString
     * @throws IOException
     */
    public MockNotifdConfigManager(String configString) throws IOException {
        InputStream reader = new ByteArrayInputStream(configString.getBytes(StandardCharsets.UTF_8));
        parseXml(reader);
        reader.close();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotifdConfigManager#update()
     */
    @Override
    protected void update() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotifdConfigManager#saveXml(java.lang.String)
     */
    @Override
    protected void saveXml(String xml) throws IOException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotifdConfigManager#getNextNotifIdSql()
     */
    @Override
    public String getNextNotifIdSql() throws IOException {
        return m_nextNotifIdSql;
    }
    
    public void setNextNotifIdSql(String sql) {
        m_nextNotifIdSql = sql;
    }

    @Override
    public String getNextUserNotifIdSql() throws IOException {
        // TODO Auto-generated method stub
        return m_nextUserNotifIdSql;
    }

    public void setNextUserNotifIdSql(String sql) {
        m_nextUserNotifIdSql = sql;
    }
    
}
