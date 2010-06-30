/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 27, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
	public Category getCategoryByLabel(String label) {
		return CategoryFactory.getInstance().getCategory(label);
	}
	
	/**
	 * <p>findAll</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<Category> findAll() {
		
		List<Category> catList = new ArrayList<Category>();
		Catinfo catInfo = CategoryFactory.getInstance().getConfig();
		List catGroupList = catInfo.getCategorygroupCollection();
		if (catGroupList != null) {
			Iterator catIter = catGroupList.iterator();
			while(catIter.hasNext()){
				Categorygroup cg = (Categorygroup)catIter.next();
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
	
	
	
	

