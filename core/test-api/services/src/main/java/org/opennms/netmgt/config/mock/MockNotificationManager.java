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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.sql.DataSource;

import org.opennms.netmgt.config.NotifdConfigManager;
import org.opennms.netmgt.config.NotificationManager;

/**
 * @author david
 */
public class MockNotificationManager extends NotificationManager {

    @SuppressWarnings("deprecation")
    public MockNotificationManager(NotifdConfigManager configManager, DataSource db, String mgrString) {
        super(configManager, db);
        Reader reader = new StringReader(mgrString);
        parseXML(reader);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#saveXML(java.lang.String)
     */
    @Override
    protected void saveXML(String xmlString) throws IOException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#update()
     */
    @Override
    public void update() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.config.NotificationManager#getInterfaceFilter(java.lang.String)
     */
    protected String getInterfaceFilter(String rule) {
        return "SELECT DISTINCT ipaddr, servicename, nodeid FROM ifservices, service WHERE ifservices.serviceid = service.serviceid";
    }
}
