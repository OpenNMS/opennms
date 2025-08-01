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
package org.opennms.netmgt.config.dao.outages.impl;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;

import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.config.poller.outages.Node;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Outages;

public class SentinelPollOutagesDaoTest {
    @Test
    public void canReadAndReload() throws IOException {
        Outages configToServe = new Outages();
        Outage outageToAdd = new Outage();
        Node nodeToAdd = new Node();
        nodeToAdd.setId(1);
        outageToAdd.addNode(nodeToAdd);
        outageToAdd.setName("test");
        outageToAdd.setType("daily");
        configToServe.addOutage(outageToAdd);

        JsonStore mockJsonStore = mock(JsonStore.class);
        when(mockJsonStore.getLastUpdated(AbstractPollOutagesDao.JSON_STORE_KEY,
                ConfigDaoConstants.JSON_KEY_STORE_CONTEXT))
                .thenReturn(OptionalLong.of(System.currentTimeMillis()));
        when(mockJsonStore.get(AbstractPollOutagesDao.JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT))
                .thenReturn(Optional.of(configToJson(configToServe)));

        ReadablePollOutagesDao threshdDao = new SentinelPollOutagesDao(mockJsonStore);
        assertThat(threshdDao.getReadOnlyConfig(), CoreMatchers.equalTo(configToServe));

        configToServe.removeOutage(outageToAdd);
        when(mockJsonStore.getLastUpdated(AbstractPollOutagesDao.JSON_STORE_KEY,
                ConfigDaoConstants.JSON_KEY_STORE_CONTEXT))
                .thenReturn(OptionalLong.of(System.currentTimeMillis()));
        when(mockJsonStore.get(AbstractPollOutagesDao.JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT))
                .thenReturn(Optional.of(configToJson(configToServe)));
        threshdDao.reload();
        assertThat(threshdDao.getReadOnlyConfig(), CoreMatchers.equalTo(configToServe));
    }

    private String configToJson(Outages outageConfig) throws IOException {
        ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();
        return mapper.writeValueAsString(outageConfig);
    }
}
