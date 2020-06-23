/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.graphml.model;

import org.junit.Assert;
import org.junit.Test;

public class GraphMLNodeTest {

    @Test
    public void verifyEqualsAndHashCode() {
        GraphMLNode node = new GraphMLNode();
        node.setProperty("id", "some-id");
        node.setProperty("label", "some-label");

        GraphMLNode copy = new GraphMLNode();
        copy.setProperty("id", "some-id");
        copy.setProperty("label", "some-label");

        Assert.assertEquals(node.hashCode(), node.hashCode());
        Assert.assertEquals(node, node);
        Assert.assertEquals(node.hashCode(), copy.hashCode());
        Assert.assertEquals(node, copy);

    }

}