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

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

public class ApplicationCriteriaTest {
    @Test
    public void testEqualsAndHashCode() {
        ApplicationCriteria criteria = new ApplicationCriteria("dummy-id");
        Assert.assertEquals(false, criteria.equals(null));
        Assert.assertEquals(true, criteria.equals(criteria));
        Assert.assertEquals(false, criteria.equals("RANDOM STRING"));
        Assert.assertEquals(true, criteria.equals(new ApplicationCriteria("dummy-id")));

        Assert.assertEquals(criteria.hashCode(), criteria.hashCode());
        Assert.assertEquals(criteria.hashCode(), new ApplicationCriteria("dummy-id").hashCode());
        Assert.assertEquals(false, Objects.equals(criteria.hashCode(), new ApplicationCriteria("bla").hashCode()));
    }

}
