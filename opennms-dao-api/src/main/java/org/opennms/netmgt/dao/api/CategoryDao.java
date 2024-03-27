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
package org.opennms.netmgt.dao.api;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.opennms.netmgt.model.OnmsCategory;

/**
 * <p>CategoryDao interface.</p>
 */
public interface CategoryDao extends OnmsDao<OnmsCategory, Integer> {
	
    /**
     * <p>findByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCategory} object.
     */
    OnmsCategory findByName(String name);
    
    /**
     * <p>findByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param useCached a boolean.
     * @return a {@link org.opennms.netmgt.model.OnmsCategory} object.
     */
    OnmsCategory findByName(String name, boolean useCached);
    
    /**
     * <p>getAllCategoryNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getAllCategoryNames();
    
    /**
     * <p>getCriterionForCategorySetsUnion</p>
     *
     * @param categories an array of {@link java.lang.String} objects.
     * @return a {@link java.util.List} object.
     */
    List<Criterion> getCriterionForCategorySetsUnion(String[]... categories);
    
    /**
     * <p>getCategoriesWithAuthorizedGroup</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<OnmsCategory> getCategoriesWithAuthorizedGroup(String groupName);
}
