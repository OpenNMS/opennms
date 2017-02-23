/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.criterion.Criterion;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;

public class MockCategoryDao extends AbstractMockDao<OnmsCategory, Integer> implements CategoryDao {
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsCategory cat) {
        cat.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsCategory cat) {
        return cat.getId();
    }

    @Override
    public Integer save(final OnmsCategory cat) {
        if (cat == null) return null;
        final String categoryName = cat.getName();
        if (categoryName == null) return null;
        final OnmsCategory existingCategory = findByName(categoryName);
        if (existingCategory == null) {
            return super.save(cat);
        } else {
            cat.setId(existingCategory.getId());
            cat.setDescription(existingCategory.getDescription());
            cat.setAuthorizedGroups(existingCategory.getAuthorizedGroups());
            return cat.getId();
        }
    }

    @Override
    public OnmsCategory findByName(final String name) {
        for (final OnmsCategory cat : findAll()) {
            if (name.equals(cat.getName())) {
                return cat;
            }
        }
        return null;
    }

    @Override
    public OnmsCategory findByName(final String name, final boolean useCached) {
        return findByName(name);
    }

    @Override
    public List<String> getAllCategoryNames() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<Criterion> getCriterionForCategorySetsUnion(final String[]... categories) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsCategory> getCategoriesWithAuthorizedGroup(final String groupName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
