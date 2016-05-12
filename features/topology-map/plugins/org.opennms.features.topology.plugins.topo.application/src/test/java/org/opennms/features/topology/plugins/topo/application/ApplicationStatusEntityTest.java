/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.application;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.ApplicationStatusEntity;
import org.opennms.netmgt.model.OnmsSeverity;

public class ApplicationStatusEntityTest {
    @Test
    public void testKey() {
        ApplicationStatusEntity entity = new ApplicationStatusEntity(100, InetAddressUtils.getLocalHostAddress(), 20, new Date(), OnmsSeverity.NORMAL, 20L);
        ApplicationStatusEntity.Key key1 = entity.getKey();
        Assert.assertEquals(new ApplicationStatusEntity.Key("100", "20", InetAddressUtils.getLocalHostAddress().toString()), key1);
    }
}
