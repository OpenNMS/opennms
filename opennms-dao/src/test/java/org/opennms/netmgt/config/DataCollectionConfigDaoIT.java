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

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.mock.MockSnmpPeerFactory;
import org.opennms.netmgt.dao.api.SnmpConfigDao;
import org.opennms.netmgt.dao.jaxb.DefaultSnmpConfigDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml"
        // Can't use minimal-conf here
        //"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties = {"org.opennms.snmp.dataCollectionConfig.reloadCheckInterval=60000"})
@JUnitTemporaryDatabase(dirtiesContext=false)
public class DataCollectionConfigDaoIT {

   @Test
   public void testDefaultReloadInterval() {
       DataCollectionConfigDao config = DataCollectionConfigFactory.getInstance();
       Assert.assertNotNull(config);
       Assert.assertTrue(config instanceof DefaultDataCollectionConfigDao);
       Assert.assertEquals(new Long(60000), ((DefaultDataCollectionConfigDao) config).getReloadCheckInterval());
   }

   @Test
   public void testResourceTypes() {
       long start = System.nanoTime();
       DataCollectionConfigDao config = DataCollectionConfigFactory.getInstance();
       for (int i=0; i<1000; i++) {
           Assert.assertNotNull(config.getConfiguredResourceTypes().get("hrStorageIndex"));
       }
       long end = System.nanoTime();
       System.err.println("Test took " + ((end - start)/1000) + " us");
   }
}
