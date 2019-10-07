/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import static com.jayway.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThresholdingDao;
import org.opennms.netmgt.config.dao.thresholding.impl.AbstractThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.impl.AbstractThresholdingDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholdingConfigDaos.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass = MockDatabase.class)
public class ThresholdingConfigPersistenceIT {
    @Autowired
    private WriteableThreshdDao threshdDao;

    @Autowired
    private WriteableThresholdingDao thresholdingDao;

    @Autowired
    private JsonStore jsonStore;

    @Test
    public void canStoreInitialConfig() {
        await().atMost(10, TimeUnit.SECONDS).until(() ->
                jsonStore.get(AbstractThreshdDao.JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT).isPresent() &&
                        jsonStore.get(AbstractThresholdingDao.JSON_STORE_KEY,
                                ConfigDaoConstants.JSON_KEY_STORE_CONTEXT).isPresent());
    }
}
