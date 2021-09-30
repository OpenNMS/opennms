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
