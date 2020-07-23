/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.api.validation;

import java.util.List;

import org.junit.Test;
import org.opennms.core.test.OnmsAssert;
import org.opennms.netmgt.graph.api.validation.exception.InvalidNamespaceException;

import com.google.common.collect.Lists;

public class NamespaceValidatorTest {

    @Test
    public void verifyIsValid() {
        final NamespaceValidator validator = new NamespaceValidator();
        final List<String> acceptableNamespaces = Lists.newArrayList(
                "nodes",
                "application",
                "bsm",
                "test",
                "vmware",
                "acme:markets",
                "acme:regions",
                "nodes.ldp",
                "nodes.xyz",
                "test-1",
                "test-2",
                "test-1.1",
                "test_1.1",
                "test_1.1-2:hello-world2017"
        );
        for (String eachNamespace : acceptableNamespaces) {
            validator.validate(eachNamespace);
        }
    }

    @Test
    public void verifyIsNotValid() {
        final NamespaceValidator validator = new NamespaceValidator();
        final List<String> invalidNamespaces = Lists.newArrayList(
                "",
                null,
                "acme markets",
                "acme+markets",
                "+",
                "-",
                "_",
                "?",
                "$",
                "nodes$bridge"
        );
        for (String invalidNamespace : invalidNamespaces) {
            OnmsAssert.assertThrowsException(InvalidNamespaceException.class, () -> validator.validate(invalidNamespace));
        }
    }

}