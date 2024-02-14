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
