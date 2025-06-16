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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.categories.Category;
import org.opennms.web.svclayer.CategoryConfigService;
import org.opennms.web.svclayer.dao.CategoryConfigDao;

/**
 * <p>DefaultCategoryConfigService class.</p>
 *
 * @author <a href="mailto:johnathan@opennms.org">Jonathan Sartin</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultCategoryConfigService implements CategoryConfigService {

    private CategoryConfigDao m_categoryConfigDao;

    /**
     * <p>getCategories</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<Category> getCategories() {
        return m_categoryConfigDao.findAll();
    }
    
    /**
     * <p>getCategoriesList</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<String> getCategoriesList() {
        List<String> categories = new ArrayList<>();
        Collection<Category> catCollection = m_categoryConfigDao.findAll();;
        Iterator<Category> i = catCollection.iterator();
        while (i.hasNext()) {
            categories.add(i.next().getLabel());
        }
        return categories;
        
    }

    /**
     * <p>getCategoryConfigDao</p>
     *
     * @return a {@link org.opennms.web.svclayer.dao.CategoryConfigDao} object.
     */
    public CategoryConfigDao getCategoryConfigDao() {
        return m_categoryConfigDao;
    }

    /**
     * <p>setCategoryConfigDao</p>
     *
     * @param categoryConfigDao a {@link org.opennms.web.svclayer.dao.CategoryConfigDao} object.
     */
    public void setCategoryConfigDao(CategoryConfigDao categoryConfigDao) {
        m_categoryConfigDao = categoryConfigDao;
    }

}
