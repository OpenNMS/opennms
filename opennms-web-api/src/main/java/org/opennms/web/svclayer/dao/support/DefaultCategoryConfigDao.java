/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.dao.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.CategoryGroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.web.svclayer.dao.CategoryConfigDao;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * <p>DefaultCategoryConfigDao class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jason.aras@fastsearch.com">Jason Ayers</a>
 */
public class DefaultCategoryConfigDao implements CategoryConfigDao {
	
	/**
	 * <p>Constructor for DefaultCategoryConfigDao.</p>
	 */
	public DefaultCategoryConfigDao() {
		try {
			CategoryFactory.init();
		} catch (FileNotFoundException e) {
			throw new DataRetrievalFailureException("Unable to locate categories file", e);
		} catch (IOException e) {
			throw new DataRetrievalFailureException("Error load categories file", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Category getCategoryByLabel(String label) {
		return CategoryFactory.getInstance().getCategory(label);
	}
	
	/**
	 * <p>findAll</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	@Override
	public Collection<Category> findAll() {
		
		List<Category> catList = new ArrayList<>();
		Catinfo catInfo = CategoryFactory.getInstance().getConfig();
		List<CategoryGroup> catGroupList = catInfo.getCategoryGroups();
		if (catGroupList != null) {
		    for (final CategoryGroup cg : catGroupList) {
		        catList.addAll(cg.getCategories());
		    }
		}
		return catList;
	}
}
