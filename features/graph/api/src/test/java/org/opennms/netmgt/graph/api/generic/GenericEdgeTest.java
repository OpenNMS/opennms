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
package org.opennms.netmgt.graph.api.generic;

import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import org.junit.Test;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.validation.exception.InvalidNamespaceException;

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
        GenericEdge.builder().namespace(NAMESPACE).source(refWithOurNamespace1).target(refWithOurNamespace2).build(); // should throw no Exception
        GenericEdge.builder().namespace(NAMESPACE).source(refWithOurNamespace1).target(refWithForeignNamespace1); // should throw no Exception
        GenericEdge.builder().namespace(NAMESPACE).source(refWithForeignNamespace1).target(refWithOurNamespace1); // should throw no Exception
        assertThrowsException(IllegalArgumentException.class, () -> GenericEdge.builder().namespace(NAMESPACE).source(refWithForeignNamespace1).target(refWithForeignNamespace2).build());
    }

    @Test
    public void verifyCannotSetInvalidNamespace() {
        final String namespace = "$invalid$";
        assertThrowsException(InvalidNamespaceException.class, () -> GenericEdge.builder()
                .namespace(namespace)
                .source(new VertexRef(namespace,"v1"))
                .target(new VertexRef(namespace, "v2"))
                .build());
    }
}
