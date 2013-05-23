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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.acl.model.GroupDTO;
import org.opennms.acl.model.Pager;
import org.opennms.acl.repository.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ibatis.SqlMapClientCallback;
import org.springframework.orm.ibatis.SqlMapClientTemplate;
import org.springframework.stereotype.Repository;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapExecutor;

/**
 * <p>GroupRepositoryIbatis class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Repository("groupRepository")
public class GroupRepositoryIbatis extends SqlMapClientTemplate implements GroupRepository {

    /** {@inheritDoc} */
    @Autowired
    @Override
    public void setSqlMapClient(@Qualifier("sqlMapClient") SqlMapClient sqlMapClient) {
        super.setSqlMapClient(sqlMapClient);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List<GroupDTO> getUserGroupsWithAutorities(String username) {
        return queryForList("getUserGroupsComplete", username);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean hasUsers(Integer id) {
        return queryForList("getGroupMembers", id).size() > 0;
    }

    /**
     * <p>getGroupUsernames</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<String> getGroupUsernames(Integer id) {
        return queryForList("getGroupMembers", id);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean deleteUserGroups(String username) {
        return delete("deleteUserGroups", username) > 0;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List<GroupDTO> getFreeGroups(String username) {
        return queryForList("getFreeGroups", username);
    }

    /** {@inheritDoc} */
    @Override
    public GroupDTO getGroup(Integer id) {
        return (GroupDTO) queryForObject("getGroup", id);
    }

    /**
     * <p>getGroups</p>
     *
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<GroupDTO> getGroups() {
        return queryForList("getAllGroups");
    }

    /** {@inheritDoc} */
    @Override
    public Boolean saveGroups(final String username, final List<Integer> groups) {

        return execute(new SqlMapClientCallback<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            public Boolean doInSqlMapClient(SqlMapExecutor executor) {
                int ris = 0;
                try {

                    executor.startBatch();
                    Iterator<Integer> iter = groups.iterator();

                    while (iter.hasNext()) {
                        Map params = new HashMap();
                        params.put("username", username);
                        params.put("id", iter.next());
                        executor.insert("insertGroupUser", params);
                    }
                    ris = executor.executeBatch();
                } catch (SQLException e) {
                    Logger log = LoggerFactory.getLogger(this.getClass());
                    StringBuffer sb = new StringBuffer("saveGroups failed \n").append("num groups batch:").append(groups.size()).append("\n").append(" username:").append(username).append("\n")
                            .append(e.getNextException());
                    log.error(sb.toString());
                }
                return ris > 0;
            }
        });
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List<GroupDTO> getGroups(Pager pager) {
        Map params = new HashMap();
        params.put("limit", pager.getItemsNumberOnPage());
        params.put("offset", pager.getPage() * pager.getItemsNumberOnPage());
        return queryForList("getGroups", params);
    }

    /**
     * <p>getGroupsNumber</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer getGroupsNumber() {
        return (Integer) queryForObject("getGroupsNumber");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public List<GroupDTO> getUserGroups(String username) {
        return queryForList("getUserGroups", username);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean removeGroup(Integer id) {
        return delete("deleteGroup", id) == 1;
    }

    /** {@inheritDoc} */
    @Override
    public Boolean save(GroupDTO group) {
        return group.isNew() ? add(group) : update(group);
    }

    private Boolean add(GroupDTO group) {
        return insert("insertGroup", group) != null;
    }

    private int deleteAuthorityGroup(Integer id) {
        return update("updateAuthorityToGroupToHidden", id);
    }

    private Boolean update(GroupDTO group) {
        deleteAuthorityGroup(group.getId());
        if (group.getAuthorities() != null && group.getAuthorities().size() > 0) {
            saveAuthorities(group.getId(), group.getAuthorities());
        }
        return updateGroupName(group);
    }

    private Boolean updateGroupName(GroupDTO group) {
        return update("updateGroupName", group) == 1;
    }

    private Boolean saveAuthorities(final Integer group, final List<?> authorities) {

        return execute(new SqlMapClientCallback<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            public Boolean doInSqlMapClient(SqlMapExecutor executor) {
                int ris = 0;
                try {
                    executor.startBatch();
                    Iterator<Integer> iter = (Iterator<Integer>) authorities.iterator();

                    while (iter.hasNext()) {
                        Map params = new HashMap();
                        params.put("id", iter.next());
                        params.put("groupId", group);
                        executor.update("updateGroupAuthority", params);
                    }
                    ris = executor.executeBatch();
                } catch (SQLException e) {
                    Logger log = LoggerFactory.getLogger(this.getClass());
                    StringBuffer sb = new StringBuffer("saveAuthorities failed \n").append("num authorities batch:").append(authorities.size()).append("\n").append(" group:").append(group).append(
                            "\n").append(e.getNextException());
                    log.error(sb.toString());
                }
                return ris > 0;
            }
        });
    }

}
