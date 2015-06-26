/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml"
        // Can't use minimal-conf here
        //"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties = {"org.opennms.snmp.dataCollectionConfig.reloadCheckInterval=60000"})
@JUnitTemporaryDatabase(dirtiesContext=false)
public class DataCollectionConfigDaoTest {

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
