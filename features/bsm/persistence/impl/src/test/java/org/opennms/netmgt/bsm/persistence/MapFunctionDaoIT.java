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
package org.opennms.netmgt.bsm.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionDao;
import org.opennms.netmgt.bsm.persistence.api.functions.map.SetToEntity;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class MapFunctionDaoIT {

    @Autowired
    private MapFunctionDao m_mapFunctionDao;

    @Test
    public void canCreateReadUpdateAndDeleteMapFunctions() {
        // Initially there should be no map functions
        assertEquals(0, m_mapFunctionDao.countAll());

        // Create a map function
        SetToEntity setTo = new SetToEntity();
        setTo.setSeverity(OnmsSeverity.CRITICAL);

        m_mapFunctionDao.save(setTo);
        m_mapFunctionDao.flush();

        // Read a map function
        assertEquals(setTo, m_mapFunctionDao.get(setTo.getId()));

        // Update a map function
        setTo.setSeverity(OnmsSeverity.MAJOR);
        m_mapFunctionDao.save(setTo);
        m_mapFunctionDao.flush();

        SetToEntity otherSetTo = (SetToEntity)m_mapFunctionDao.get(setTo.getId());
        assertEquals(setTo, otherSetTo);
        assertEquals(1, m_mapFunctionDao.countAll());

        // Delete a map function
        m_mapFunctionDao.delete(setTo);
        assertEquals(0, m_mapFunctionDao.countAll()); 
    }
}
