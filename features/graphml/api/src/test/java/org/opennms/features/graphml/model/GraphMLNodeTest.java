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