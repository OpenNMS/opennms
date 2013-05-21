/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        List<String> categories = new ArrayList<String>();
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
