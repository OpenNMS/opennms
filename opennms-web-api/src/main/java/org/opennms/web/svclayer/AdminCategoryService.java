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
package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.CategoryAndMemberNodes;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.EditModel;
import org.opennms.web.svclayer.support.DefaultAdminCategoryService.NodeEditModel;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>AdminCategoryService interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly = true)
public interface AdminCategoryService {
    /**
     * <p>getCategory</p>
     *
     * @param categoryIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminCategoryService.CategoryAndMemberNodes} object.
     */
    public CategoryAndMemberNodes getCategory(String categoryIdString);

    /**
     * <p>findAllNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsNode> findAllNodes();
    
    /**
     * <p>findCategoryAndAllNodes</p>
     *
     * @param categoryIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminCategoryService.EditModel} object.
     */
    public EditModel findCategoryAndAllNodes(String categoryIdString);

    /**
     * <p>performEdit</p>
     *
     * @param editAction a {@link java.lang.String} object.
     * @param editAction2 a {@link java.lang.String} object.
     * @param toAdd an array of {@link java.lang.String} objects.
     * @param toDelete an array of {@link java.lang.String} objects.
     */
    @Transactional(readOnly = false)
    public void performEdit(String editAction, String editAction2, String[] toAdd, String[] toDelete);

    /**
     * <p>addNewCategory</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCategory} object.
     */
    @Transactional(readOnly = false)
    public OnmsCategory addNewCategory(String name);

    /**
     * <p>getCategoryWithName</p>
     *
     * @param newCategoryName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsCategory} object.
     */
    public OnmsCategory getCategoryWithName(String newCategoryName);

    /**
     * <p>findAllCategories</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsCategory> findAllCategories();

    /**
     * <p>removeCategory</p>
     *
     * @param categoryIdString a {@link java.lang.String} object.
     */
    @Transactional(readOnly = false)
    public void removeCategory(String categoryIdString);

    /**
     * <p>findByNode</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsCategory> findByNode(int nodeId);

    /**
     * <p>findNodeCategories</p>
     *
     * @param nodeIdString a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultAdminCategoryService.NodeEditModel} object.
     */
    public NodeEditModel findNodeCategories(String nodeIdString);

    /**
     * <p>performNodeEdit</p>
     *
     * @param nodeIdString a {@link java.lang.String} object.
     * @param editAction a {@link java.lang.String} object.
     * @param toAdd an array of {@link java.lang.String} objects.
     * @param toDelete an array of {@link java.lang.String} objects.
     */
    @Transactional(readOnly = false)
    public void performNodeEdit(String nodeIdString, String editAction, String[] toAdd, String[] toDelete);

}
