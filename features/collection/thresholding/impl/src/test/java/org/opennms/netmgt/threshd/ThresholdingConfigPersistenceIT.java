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
package org.opennms.netmgt.threshd;

import static org.awaitility.Awaitility.await;

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
