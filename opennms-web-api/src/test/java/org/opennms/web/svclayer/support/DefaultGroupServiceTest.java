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
package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.test.util.ReflectionTestUtils;

public class DefaultGroupServiceTest {

    private CategoryDao m_categoryDao;
    private GroupDao m_groupDao;
    private DefaultGroupService m_service;
    private OnmsCategory m_first;
    private OnmsCategory m_second;

    @Before
    public void setUp() {
        m_categoryDao = mock(CategoryDao.class);
        m_groupDao = mock(GroupDao.class);
        m_service = new DefaultGroupService();
        ReflectionTestUtils.setField(m_service, "m_categoryDao", m_categoryDao);
        ReflectionTestUtils.setField(m_service, "m_groupDao", m_groupDao);
        m_first = new OnmsCategory("First");
        m_second = new OnmsCategory("Second");
        m_first.getAuthorizedGroups().add("old");
        m_second.getAuthorizedGroups().add("old");
        when(m_categoryDao.getCategoriesWithAuthorizedGroup("old")).thenReturn(Arrays.asList(m_first, m_second));
    }

    @Test
    public void testMoveCategoryAuthorizations() {
        m_service.moveCategoryAuthorizations("old", "new");

        for (final OnmsCategory category : Arrays.asList(m_first, m_second)) {
            assertTrue(category.getAuthorizedGroups().contains("new"));
            assertFalse(category.getAuthorizedGroups().contains("old"));
        }
        verifyNoInteractions(m_groupDao);
    }

    @Test
    public void testMoveCategoryAuthorizationsReplacesTheTargetsLeftovers() {
        final OnmsCategory leftover = new OnmsCategory("Leftover");
        leftover.getAuthorizedGroups().add("new");
        leftover.getAuthorizedGroups().add("other");
        // a category that has both names ends up with the new name only
        m_second.getAuthorizedGroups().add("new");
        when(m_categoryDao.getCategoriesWithAuthorizedGroup("new")).thenReturn(Arrays.asList(leftover, m_second));

        m_service.moveCategoryAuthorizations("old", "new");

        assertFalse(leftover.getAuthorizedGroups().contains("new"));
        assertTrue(leftover.getAuthorizedGroups().contains("other"));
        assertTrue(m_first.getAuthorizedGroups().contains("new"));
        assertTrue(m_second.getAuthorizedGroups().contains("new"));
        assertFalse(m_first.getAuthorizedGroups().contains("old"));
        assertFalse(m_second.getAuthorizedGroups().contains("old"));
    }

    @Test
    public void testClearCategoryAuthorizations() {
        m_service.clearCategoryAuthorizations("old");

        assertTrue(m_first.getAuthorizedGroups().isEmpty());
        assertTrue(m_second.getAuthorizedGroups().isEmpty());
        verifyNoInteractions(m_groupDao);
    }
}
