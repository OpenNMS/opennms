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
