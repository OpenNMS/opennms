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
package org.opennms.features.topology.app.internal.support;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

public class LayoutManagerTest {
    
    @Test
    public void testHash() {
        ArrayList<VertexRef> defaultVertexRefs = Lists.newArrayList(
                new DefaultVertexRef("namespace1", "id1", "Label 1"),
                new DefaultVertexRef("namespace2", "id2", "Label 2"),
                new DefaultVertexRef("namespace3", "id3", "Label 3"));

        String hash1 = LayoutManager.calculateHash(defaultVertexRefs);
        String hash2 = LayoutManager.calculateHash(defaultVertexRefs);
        Assert.assertEquals(hash1, hash2);
    }

}