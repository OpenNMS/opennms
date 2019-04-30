/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.alarms.history.elastic.mapping;

import static org.easymock.EasyMock.mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.cache.Cache;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.alarms.history.elastic.dto.AlarmDocumentDTO;
import org.opennms.features.alarms.history.elastic.dto.NodeDocumentDTO;
import org.opennms.netmgt.model.OnmsAlarm;

public class MapStructDocumentImplTest {

    MapStructDocumentImpl mapper;

    @Before
    public void setUp() {
        Cache<Integer, Optional<NodeDocumentDTO>> nodeInfoCache = mock(Cache.class);
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
