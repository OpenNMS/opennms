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
package org.opennnms.netmgt.collectd.prometheus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Map;

import org.junit.Rule;
import org.opennms.netmgt.collectd.prometheus.PrometheusCollector;
import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.prometheus.Collection;
import org.opennms.netmgt.config.prometheus.PrometheusDatacollectionConfig;
import org.opennms.netmgt.config.prometheus.Rrd;
import org.opennms.netmgt.dao.prometheus.PrometheusDataCollectionConfigDao;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;

public class PrometheusCollectorComplianceIT extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
            .withRootDirectory(Paths.get("src", "test", "resources").toString())
            .dynamicPort());

    public PrometheusCollectorComplianceIT() {
        super(PrometheusCollector.class, true);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
                .put("collection", COLLECTION)
                .put("url", String.format("http://127.0.0.1:%d/metrics", wireMockRule.port()))
                .build();
    }

    @Override
    public Map<String, Object> getRequiredBeans() {
        Collection collection = new Collection();
        Rrd rrd = new Rrd();
        collection.setRrd(rrd);

        PrometheusDatacollectionConfig dataCollectionConfig = new PrometheusDatacollectionConfig();
        dataCollectionConfig.setRrdRepository("target");
        dataCollectionConfig.getCollection().add(collection);

        PrometheusDataCollectionConfigDao prometheusCollectionDao = mock(PrometheusDataCollectionConfigDao.class);
        when(prometheusCollectionDao.getCollectionByName(COLLECTION)).thenReturn(collection);
        when(prometheusCollectionDao.getConfig()).thenReturn(dataCollectionConfig);

        return new ImmutableMap.Builder<String, Object>()
                .put("prometheusDataCollectionConfigDao", prometheusCollectionDao)
                .build();
    }
}
