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
package org.opennms.features.alarms.history.elastic.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheConfig;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentDTO;
import org.opennms.features.alarms.history.elastic.dto.NodeDocumentDTO;
import org.opennms.netmgt.model.OnmsAlarm;

import com.google.common.cache.CacheLoader;

public class MapStructDocumentImplTest {

    MapStructDocumentImpl mapper;

    @Before
    public void setUp() {
        final CacheConfig config = new CacheConfig("test");
        @SuppressWarnings("unchecked")
        final CacheLoader<Integer, Optional<NodeDocumentDTO>> loader = mock(CacheLoader.class);
        Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache = new Cache<>(config, loader);
        mapper = new MapStructDocumentImpl(nodeInfoCache, () -> 0L);
    }

    @Test
    public void canMapIpAddress() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setIpAddr(InetAddressUtils.ONE_TWENTY_SEVEN);
        AlarmDocumentDTO documentDTO = mapper.apply(alarm);
        assertThat(documentDTO.getIpAddress(), equalTo("127.0.0.1"));
    }
}
