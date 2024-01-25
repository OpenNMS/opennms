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
package org.opennms.install;

import java.sql.SQLException;

import junit.framework.TestCase;

public class InstallerJniTest extends TestCase {

    private Installer m_installer;

    protected void setUp() throws SQLException {
        m_installer = new Installer();
    }

    public void testBogus() {
    }
    
    public void XXXtestPingLocalhost() throws Exception {
        System.setProperty("opennms.library.jicmp", "/Users/dgregor/opennms/trunk/opennms/target/opennms-1.3.3-SNAPSHOT/lib/libjicmp.jnilib");
        m_installer.pingLocalhost();
    }
}
