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
