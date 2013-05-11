/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl.repository.ibatis;

import java.util.ArrayList;
import java.util.List;

import org.opennms.acl.model.NodeONMSDTO;
import org.opennms.acl.repository.ItemAclRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.stereotype.Repository;

import com.ibatis.sqlmap.client.SqlMapClient;
/**
 * <p>CategoryNodeAclRepositoryIbatis class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Repository("categoryNodeRepository")
public class CategoryNodeAclRepositoryIbatis extends SqlMapClientTemplate implements ItemAclRepository {

    /** {@inheritDoc} */
    @Autowired
    @Override
    public void setSqlMapClient(@Qualifier("onmsSqlMapClient") SqlMapClient sqlMapClient) {
        super.setSqlMapClient(sqlMapClient);
    }

    /**
     * <p>getItems</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<?> getItems() {
        return queryForList("selectCategoryNodes");
    }

    /** {@inheritDoc} */
    @Override
    public List<?> getAuthorityItems(List<Integer> items) {
        return items.size() > 0 ? queryForList("selectAuthorityCategories", items) : new ArrayList<NodeONMSDTO>(0);
    }

    /** {@inheritDoc} */
    @Override
    public List<?> getFreeItems(List<Integer> items) {
        return items.size() > 0 ? queryForList("selectFreeCategoryNodes", items) : queryForList("selectCategoryNodes");
    }
}
