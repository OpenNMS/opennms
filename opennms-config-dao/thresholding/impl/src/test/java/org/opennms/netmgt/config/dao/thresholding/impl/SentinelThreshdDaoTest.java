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
