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
package org.opennms.features.topology.app.internal;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

public class CategoryDaoProvider implements CategoryProvider {
    private final CategoryDao categoryDao;
    private final NodeDao nodeDao;

    public CategoryDaoProvider(CategoryDao categoryDao, NodeDao nodeDao) {
        this.categoryDao = Objects.requireNonNull(categoryDao);
        this.nodeDao = Objects.requireNonNull(nodeDao);
    }

    @Override
    public Collection<OnmsCategory> getAllCategories() {
        return categoryDao.findAll();
    }

    @Override
    public OnmsCategory findCategoryByName(String categoryName) {
        return categoryDao.findByName(categoryName);
    }
    @Override
    public List<OnmsNode> findNodesForCategory(OnmsCategory category) {
        return nodeDao.findByCategory(category);
    }
}
