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
import org.opennms.netmgt.graph.api.validation.exception.InvalidNamespaceException;

public class GenericVertexTest {

    @Test
    public void genericVertexMustHaveANamespaceAndId() {
        GenericVertex.builder().namespace("not-null").id("not-null"); // should throw no exception
        assertThrowsException(NullPointerException.class, ()-> GenericVertex.builder().namespace(null).id(null).build());
        assertThrowsException(NullPointerException.class, ()-> GenericVertex.builder().namespace("not-null").id(null).build());
        assertThrowsException(NullPointerException.class, ()-> GenericVertex.builder().namespace(null).id("not-null").build());
    }

    @Test
    public void verifyCannotSetInvalidNamespace() {
        assertThrowsException(InvalidNamespaceException.class, () -> GenericVertex.builder().namespace("$invalid$").build());
    }
}
