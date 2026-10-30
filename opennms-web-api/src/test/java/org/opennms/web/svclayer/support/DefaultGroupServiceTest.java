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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.UserMgmtManagers;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DefaultGroupServiceTest {

    private CategoryDao m_categoryDao;
    private GroupDao m_groupDao;
    private UserMgmtManagers m_managers;
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
        m_managers = mock(UserMgmtManagers.class);
        ReflectionTestUtils.setField(m_service, "m_managers", m_managers);
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
    public void testSetAuthorizedCategoriesReplacesTheSet() {
        final OnmsCategory third = new OnmsCategory("Third");
        third.getAuthorizedGroups().add("other");
        when(m_categoryDao.findByName("Third", false)).thenReturn(third);

        m_service.setAuthorizedCategories("old", Arrays.asList("First", "Third"));

        assertTrue(m_first.getAuthorizedGroups().contains("old"));
        assertFalse(m_second.getAuthorizedGroups().contains("old"));
        assertEquals(Set.of("old", "other"), third.getAuthorizedGroups());
        // a category the group already had is not looked up again
        verify(m_categoryDao, never()).findByName("First", false);
        verifyNoInteractions(m_groupDao);
        // outside a transaction, the managers are expired at once
        verify(m_managers).expireAll();
    }

    @Test
    public void testSetAuthorizedCategoriesWithTheSameSetChangesNothing() {
        m_service.setAuthorizedCategories("old", Arrays.asList("Second", "First"));

        assertEquals(Set.of("old"), m_first.getAuthorizedGroups());
        assertEquals(Set.of("old"), m_second.getAuthorizedGroups());
        verify(m_categoryDao, never()).findByName(anyString(), anyBoolean());
    }

    @Test
    public void testSetAuthorizedCategoriesWithAnEmptySetClearsThem() {
        m_service.setAuthorizedCategories("old", Collections.emptyList());

        assertTrue(m_first.getAuthorizedGroups().isEmpty());
        assertTrue(m_second.getAuthorizedGroups().isEmpty());
    }

    @Test
    public void testSetAuthorizedCategoriesIgnoresAnUnknownName() {
        // the caller validates the names; one that matches no category is skipped, not an error
        m_service.setAuthorizedCategories("old", Arrays.asList("First", "Gone"));

        assertTrue(m_first.getAuthorizedGroups().contains("old"));
        assertFalse(m_second.getAuthorizedGroups().contains("old"));
        verify(m_categoryDao).findByName("Gone", false);
    }

    @Test
    public void testCategoryChangesExpireTheManagersWhenTheTransactionCommits() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            m_service.moveCategoryAuthorizations("old", "new");
            m_service.clearCategoryAuthorizations("new");
            verify(m_managers, never()).expireAll();

            for (final TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }
            verify(m_managers, atLeastOnce()).expireAll();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void testClearCategoryAuthorizations() {
        m_service.clearCategoryAuthorizations("old");

        assertTrue(m_first.getAuthorizedGroups().isEmpty());
        assertTrue(m_second.getAuthorizedGroups().isEmpty());
        verifyNoInteractions(m_groupDao);
    }
}
