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
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ThresholdEntity;
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
public class ReductionFunctionDaoIT {

    @Autowired
    private ReductionFunctionDao m_reductionFunctionDao;

    @Test
    public void canCreateReadUpdateAndDeleteReductionFunctions() {
        // Initially there should be no reduction functions
        assertEquals(0, m_reductionFunctionDao.countAll());

        // Create a reduction function
        ThresholdEntity threshold = new ThresholdEntity();
        threshold.setThreshold(0.75f);

        m_reductionFunctionDao.save(threshold);
        m_reductionFunctionDao.flush();

        // Read a reduction function
        assertEquals(threshold, m_reductionFunctionDao.get(threshold.getId()));

        // Update a reduction function
        threshold.setThreshold(0.5f);
        m_reductionFunctionDao.save(threshold);
        m_reductionFunctionDao.flush();

        ThresholdEntity otherThreshold = (ThresholdEntity)m_reductionFunctionDao.get(threshold.getId());
        assertEquals(threshold, otherThreshold);
        assertEquals(1, m_reductionFunctionDao.countAll());

        // Delete a reduction function
        m_reductionFunctionDao.delete(threshold);
        assertEquals(0, m_reductionFunctionDao.countAll()); 
    }
}
