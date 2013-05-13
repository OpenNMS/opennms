/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.dao.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.categories.Categories;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.web.svclayer.dao.CategoryConfigDao;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * <p>DefaultCategoryConfigDao class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jason.aras@fastsearch.com">Jason Ayers</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jason.aras@fastsearch.com">Jason Ayers</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultCategoryConfigDao implements CategoryConfigDao {
	
	/**
	 * <p>Constructor for DefaultCategoryConfigDao.</p>
	 */
	public DefaultCategoryConfigDao() {
		try {
			CategoryFactory.init();
		} catch (MarshalException e) {
			throw new DataRetrievalFailureException("Syntax error in categories file", e);
		} catch (ValidationException e) {
			throw new DataRetrievalFailureException("Validation error in categories file", e);
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
		
		List<Category> catList = new ArrayList<Category>();
		Catinfo catInfo = CategoryFactory.getInstance().getConfig();
		List<Categorygroup> catGroupList = catInfo.getCategorygroupCollection();
		if (catGroupList != null) {
			Iterator<Categorygroup> catIter = catGroupList.iterator();
			while(catIter.hasNext()){
				Categorygroup cg = catIter.next();
				Categories cats = cg.getCategories();
				Category[] categories = cats.getCategory();
				int i = 0;
				for (i = 0; i < categories.length; i++) {
					catList.add(categories[i]);					
				}
			}
		}
		return catList;				
	}

}
	
	
	
	

