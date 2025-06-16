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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.opennms.netmgt.collection.test.api.CollectorComplianceTest;
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.Rrd;
import org.opennms.netmgt.rrd.RrdRepository;

import com.google.common.collect.ImmutableMap;

public class HttpCollectorComplianceTest extends CollectorComplianceTest {

    private static final String COLLECTION = "default";

    private HttpCollectionConfigFactory configFactory;
    
    public HttpCollectorComplianceTest() {
        super(HttpCollector.class, true);

        HttpCollection collection = new HttpCollection();
        collection.setName(COLLECTION);
        collection.setRrd(new Rrd(1, "RRA:AVERAGE:0.5:1:2016"));
        configFactory = mock(HttpCollectionConfigFactory.class);
        when(configFactory.getHttpCollection(COLLECTION)).thenReturn(collection);
        when(configFactory.getRrdRepository(COLLECTION)).thenReturn(new RrdRepository());
        HttpCollectionConfigFactory.setInstance(configFactory);
    }

    @Override
    public String getCollectionName() {
        return COLLECTION;
    }

    @Override
    public Map<String, Object> getRequiredParameters() {
        return new ImmutableMap.Builder<String, Object>()
            .put("collection", COLLECTION)
            .build();
    }

    @Override
    public void beforeMinion() {
        HttpCollectionConfigFactory.setInstance(null);
    }

    @Override
    public void afterMinion() {
        HttpCollectionConfigFactory.setInstance(configFactory);
    }
}
