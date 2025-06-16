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
package org.opennms.netmgt.collectd;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcRrd;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.ImmutableMap;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class,reuseDatabase=true)
public class JdbcCollectorComplianceIT extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    public JdbcCollectorComplianceIT() {
        super(JdbcCollector.class, true);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
            .put("collection", COLLECTION)
            .put("data-source", "opennms")
            .build();
    }

    @Override
    public Map<String, Object> getRequiredBeans() {
        JdbcDataCollection collection = new JdbcDataCollection();
        collection.setJdbcRrd(new JdbcRrd());
        JdbcDataCollectionConfigDao jdbcCollectionDao = mock(JdbcDataCollectionConfigDao.class, RETURNS_DEEP_STUBS);
        when(jdbcCollectionDao.getDataCollectionByName(COLLECTION)).thenReturn(collection);
        when(jdbcCollectionDao.getConfig().buildRrdRepository(COLLECTION)).thenReturn(new RrdRepository());

        ResourceTypesDao resourceTypesDao = mock(ResourceTypesDao.class);
        return new ImmutableMap.Builder<String, Object>()
                .put("jdbcDataCollectionConfigDao", jdbcCollectionDao)
                .put("resourceTypesDao", resourceTypesDao)
                .build();
    }

}
