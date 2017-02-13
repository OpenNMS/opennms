/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
