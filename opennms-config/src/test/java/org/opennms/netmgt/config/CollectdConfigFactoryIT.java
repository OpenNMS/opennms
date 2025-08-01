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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.OpenNMSConfigurationExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class
})
@JUnitConfigurationEnvironment
public class CollectdConfigFactoryIT {
    private CollectdConfigFactory m_factory;

    @Before
    public void setUp() throws Exception {
        try (InputStream in = ConfigurationTestUtils.getInputStreamForResource(this, "collectd-testdata.xml")) {
            m_factory = new CollectdConfigFactory(in);
        }
    }

    @Test
    public void testGetName() {
        String pkgName = "example1";
        Package wpkg = m_factory.getPackage(pkgName);
        assertNotNull(wpkg);
        assertEquals(pkgName, wpkg.getName());
    }

}
