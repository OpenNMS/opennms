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
package org.opennms.features.topology.app.internal.info;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;

public class ReadOnlyDaoWrapperTest {

    private NodeDao mockNodeDao;
    private ResourceDao mockResourceDao;
    private NodeDaoWrapper nodeWrapper;
    private ResourceDaoWrapper resourceWrapper;

    @Before
    public void setUp() {
        mockNodeDao = mock(NodeDao.class);
        mockResourceDao = mock(ResourceDao.class);
        nodeWrapper = new NodeDaoWrapper(mockNodeDao);
        resourceWrapper = new ResourceDaoWrapper(mockResourceDao);
    }

    // ====================================================================
    // Test 1: The wrapper does NOT expose any write/mutating methods
    // ====================================================================

    /** Names of methods from OnmsDao / NodeDao that must NOT appear. */
    private static final Set<String> FORBIDDEN_NODE_METHODS = Set.of(
            "save", "saveOrUpdate", "update", "delete",
            "flush", "clear", "lock", "initialize"
    );

    @Test
    public void nodeWrapper_doesNotExposeWriteMethods() {
        Set<String> wrapperMethodNames = Arrays.stream(
                        NodeDaoWrapper.class.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        for (String forbidden : FORBIDDEN_NODE_METHODS) {
            assertFalse(
                "ReadOnlyNodeDaoWrapper must not expose '" + forbidden + "'",
                wrapperMethodNames.contains(forbidden));
        }
    }

    @Test
    public void resourceWrapper_doesNotExposeDeleteMethod() {
        Set<String> wrapperMethodNames = Arrays.stream(
                        ResourceDaoWrapper.class.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertFalse(
            "ReadOnlyResourceDaoWrapper must not expose 'deleteResourceById'",
            wrapperMethodNames.contains("deleteResourceById"));
    }

    // ====================================================================
    // Test 2: Read methods correctly delegate to the real DAO
    // ====================================================================

    @Test
    public void nodeWrapper_getById_delegates() {
        OnmsNode expected = new OnmsNode();
        expected.setId(42);
        expected.setLabel("test-node");
        when(mockNodeDao.get(42)).thenReturn(expected);

        OnmsNode result = nodeWrapper.get(42);

        assertSame(expected, result);
        verify(mockNodeDao).get(42);
    }

    @Test
    public void nodeWrapper_getByCriteria_delegates() {
        OnmsNode expected = new OnmsNode();
        when(mockNodeDao.get("mySource:myForeignId")).thenReturn(expected);

        OnmsNode result = nodeWrapper.get("mySource:myForeignId");

        assertSame(expected, result);
        verify(mockNodeDao).get("mySource:myForeignId");
    }

    @Test
    public void nodeWrapper_getLabelForId_delegates() {
        when(mockNodeDao.getLabelForId(7)).thenReturn("router-7");

        assertEquals("router-7", nodeWrapper.getLabelForId(7));
        verify(mockNodeDao).getLabelForId(7);
    }

    @Test
    public void nodeWrapper_getAllLabelsById_delegates() {
        Map<Integer, String> labels = Map.of(1, "a", 2, "b");
        when(mockNodeDao.getAllLabelsById()).thenReturn(labels);

        assertSame(labels, nodeWrapper.getAllLabelsById());
    }

    @Test
    public void nodeWrapper_countAll_delegates() {
        when(mockNodeDao.countAll()).thenReturn(123);
        assertEquals(123, nodeWrapper.countAll());
    }

    @Test
    public void resourceWrapper_getResourceById_delegates() {
        ResourceId rid = ResourceId.get("node", "1");
        OnmsResource expected = mock(OnmsResource.class);
        when(mockResourceDao.getResourceById(rid)).thenReturn(expected);

        assertSame(expected, resourceWrapper.getResourceById(rid));
        verify(mockResourceDao).getResourceById(rid);
    }

    @Test
    public void resourceWrapper_getResourceForNode_delegates() {
        OnmsNode node = new OnmsNode();
        node.setId(5);
        OnmsResource expected = mock(OnmsResource.class);
        when(mockResourceDao.getResourceForNode(node)).thenReturn(expected);

        assertSame(expected, resourceWrapper.getResourceForNode(node));
        verify(mockResourceDao).getResourceForNode(node);
    }

    @Test
    public void resourceWrapper_findTopLevelResources_delegates() {
        when(mockResourceDao.findTopLevelResources())
                .thenReturn(Collections.emptyList());

        assertTrue(resourceWrapper.findTopLevelResources().isEmpty());
        verify(mockResourceDao).findTopLevelResources();
    }

    // ====================================================================
    // Test 3: Constructor rejects null
    // ====================================================================

    @Test(expected = NullPointerException.class)
    public void nodeWrapper_rejectsNull() {
        new NodeDaoWrapper(null);
    }

    @Test(expected = NullPointerException.class)
    public void resourceWrapper_rejectsNull() {
        new ResourceDaoWrapper(null);
    }

    // ====================================================================
    // Test 4: Jinjava reflection surface has no surprises
    //
    // This test introspects the wrapper the same way Jinjava does —
    // via Class.getMethods() on the concrete class — and asserts that
    // none of the forbidden method names are reachable.
    // ====================================================================

    @Test
    public void jinjavaCannotResolveWriteMethods_onNodeWrapper() {
        Set<String> publicMethodNames = Arrays.stream(
                        NodeDaoWrapper.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        for (String forbidden : FORBIDDEN_NODE_METHODS) {
            assertFalse(
                "Jinjava can resolve '" + forbidden + "' via getMethods()!",
                publicMethodNames.contains(forbidden));
        }
    }

    @Test
    public void jinjavaCannotResolveDeleteMethod_onResourceWrapper() {
        Set<String> publicMethodNames = Arrays.stream(
                        ResourceDaoWrapper.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertFalse(
            "Jinjava can resolve 'deleteResourceById' via getMethods()!",
            publicMethodNames.contains("deleteResourceById"));
    }
}
