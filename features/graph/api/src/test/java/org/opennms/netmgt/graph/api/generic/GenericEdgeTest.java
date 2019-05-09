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

package org.opennms.netmgt.graph.api.generic;

import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import org.junit.Test;
import org.opennms.netmgt.graph.api.VertexRef;

public class GenericEdgeTest {

    private final static String NAMESPACE = "our_namespace";

    /**
     * We test for the condition: "The namespace of at least one VertexRef must match the namespace of the Edge".
     */
    @Test
    public void namespaceShouldBeEnforced(){
        VertexRef refWithOurNamespace1 = new VertexRef(NAMESPACE, "v1");
        VertexRef refWithOurNamespace2 = new VertexRef(NAMESPACE, "v2");
        VertexRef refWithForeignNamespace1 = new VertexRef("I don't know you, namespace", "v3");
        VertexRef refWithForeignNamespace2 = new VertexRef("I don't know you either, namespace", "v4");
        new GenericEdge(NAMESPACE, refWithOurNamespace1, refWithOurNamespace2); // should throw no Exception
        new GenericEdge(NAMESPACE, refWithOurNamespace1, refWithForeignNamespace1); // should throw no Exception
        new GenericEdge(NAMESPACE, refWithForeignNamespace1, refWithOurNamespace1); // should throw no Exception
        assertThrowsException(IllegalArgumentException.class, () -> new GenericEdge(NAMESPACE, refWithForeignNamespace1, refWithForeignNamespace2));
    }
}
