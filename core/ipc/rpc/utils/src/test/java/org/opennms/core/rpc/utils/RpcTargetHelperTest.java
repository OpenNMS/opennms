/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.utils;

import org.junit.Test;
import org.opennms.core.rpc.api.RpcTarget;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RpcTargetHelperTest {

    @Test
    public void usesNullLocationAndSystemIdWhenNothingIsSet() {
        final RpcTarget target = new RpcTargetHelper().target().build();
        assertThat(target.getLocation(), nullValue());
        assertThat(target.getSystemId(), nullValue());
    }

    @Test
    public void usesGivenLocationAndSystemId() {
        final RpcTarget target = new RpcTargetHelper().target()
                .withLocation("x")
                .withSystemId("y")
                .build();
        assertThat(target.getLocation(), equalTo("x"));
        assertThat(target.getSystemId(), equalTo("y"));
    }

    @Test
    public void overridesGivenLocationAndSystemIdUsingAttributes() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(RpcTargetHelper.LOCATION_KEY, "xx");
        attributes.put(RpcTargetHelper.SYSTEM_ID_KEY, "yy");

        final RpcTarget target = new RpcTargetHelper().target()
                .withLocation("x")
                .withSystemId("y")
                .withServiceAttributes(attributes)
                .build();
        assertThat(target.getLocation(), equalTo("xx"));
        assertThat(target.getSystemId(), equalTo("yy"));
    }

    @Test
    public void overridesGivenLocationUsingCallback() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(RpcTargetHelper.LOCATION_KEY, "xx");

        final RpcTarget target = new RpcTargetHelper().target()
                .withLocation("x")
                .withServiceAttributes(attributes)
                .withLocationOverride(l -> "xx".equals(l) ? "aa" : "bb")
                .build();
        assertThat(target.getLocation(), equalTo("aa"));
        assertThat(target.getSystemId(), nullValue());
    }

    @Test
    public void canUseForeignIdAsSystemId() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(RpcTargetHelper.SYSTEM_ID_KEY, "yy");
        attributes.put(RpcTargetHelper.USE_FOREIGN_ID_AS_SYSTEM_ID_KEY, Boolean.TRUE.toString());

        final OnmsNode node = mock(OnmsNode.class);
        when(node.getForeignId()).thenReturn("aa");
        final NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.get(1)).thenReturn(node);

        final RpcTarget target = new RpcTargetHelper().target()
                .withSystemId("y")
                .withServiceAttributes(attributes)
                .withNodeId(1)
                .withNodeDao(nodeDao)
                .build();
        assertThat(target.getLocation(), nullValue());
        assertThat(target.getSystemId(), equalTo("aa"));
    }
}
