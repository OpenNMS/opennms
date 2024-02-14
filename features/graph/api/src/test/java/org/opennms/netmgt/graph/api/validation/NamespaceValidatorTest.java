/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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