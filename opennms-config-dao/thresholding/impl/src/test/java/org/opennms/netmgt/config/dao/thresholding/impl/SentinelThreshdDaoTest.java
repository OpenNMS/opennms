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
package org.opennms.netmgt.config.dao.thresholding.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.opennms.core.xml.JacksonUtils;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.dao.common.api.ConfigDaoConstants;
import org.opennms.netmgt.config.dao.thresholding.api.ReadableThreshdDao;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;

public class SentinelThreshdDaoTest {
    @Test
    public void canReadAndReload() throws IOException {
        ThreshdConfiguration configToServe = new ThreshdConfiguration();
        configToServe.setThreads(5);

        JsonStore mockJsonStore = mock(JsonStore.class);
        when(mockJsonStore.getLastUpdated(AbstractThreshdDao.JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT))
                .thenReturn(OptionalLong.of(System.currentTimeMillis()));
        when(mockJsonStore.get(AbstractThreshdDao.JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT))
                .thenReturn(Optional.of(configToJson(configToServe)));

        ReadableThreshdDao threshdDao = new SentinelThreshdDao(mockJsonStore);
        assertThat(threshdDao.getReadOnlyConfig(), equalTo(configToServe));

        configToServe.setThreads(10);
        when(mockJsonStore.getLastUpdated(AbstractThreshdDao.JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT))
                .thenReturn(OptionalLong.of(System.currentTimeMillis()));
        when(mockJsonStore.get(AbstractThreshdDao.JSON_STORE_KEY, ConfigDaoConstants.JSON_KEY_STORE_CONTEXT))
                .thenReturn(Optional.of(configToJson(configToServe)));
        threshdDao.reload();
        assertThat(threshdDao.getReadOnlyConfig(), equalTo(configToServe));
    }

    private String configToJson(ThreshdConfiguration threshdConfiguration) throws IOException {
        ObjectMapper mapper = JacksonUtils.createDefaultObjectMapper();
        return mapper.writeValueAsString(threshdConfiguration);
    }
}
