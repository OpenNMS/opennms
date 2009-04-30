//============================================================================
//
// Copyright (c) 2009+ desmax74
// Copyright (c) 2009+ The OpenNMS Group, Inc.
// All rights reserved everywhere.
//
// This program was developed and is maintained by Rocco RIONERO
// ("the author") and is subject to dual-copyright according to
// the terms set in "The OpenNMS Project Contributor Agreement".
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
// USA.
//
// The author can be contacted at the following email address:
//
//       Massimiliano Dess&igrave;
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
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
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Repository("groupRepository")
public class GroupRepositoryIbatis extends SqlMapClientTemplate implements GroupRepository {

    @Autowired
    @Override
    public void setSqlMapClient(@Qualifier("sqlMapClient") SqlMapClient sqlMapClient) {
        super.setSqlMapClient(sqlMapClient);
    }

    @SuppressWarnings("unchecked")
    public List<GroupDTO> getUserGroupsWithAutorities(String username) {
        return queryForList("getUserGroupsComplete", username);
    }

    public Boolean hasUsers(Integer id) {
        return queryForList("getGroupMembers", id).size() > 0;
    }

    @SuppressWarnings("unchecked")
    public List<String> getGroupUsernames(Integer id) {
        return queryForList("getGroupMembers", id);
    }

    public Boolean deleteUserGroups(String username) {
        return delete("deleteUserGroups", username) > 0;
    }

    @SuppressWarnings("unchecked")
    public List<GroupDTO> getFreeGroups(String username) {
        return queryForList("getFreeGroups", username);
    }

    public GroupDTO getGroup(Integer id) {
        return (GroupDTO) queryForObject("getGroup", id);
    }

    @SuppressWarnings("unchecked")
    public List<GroupDTO> getGroups() {
        return queryForList("getAllGroups");
    }

    public Boolean saveGroups(final String username, final List<Integer> groups) {

        return (Boolean) execute(new SqlMapClientCallback() {
            @SuppressWarnings("unchecked")
            public Object doInSqlMapClient(SqlMapExecutor executor) {
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

    @SuppressWarnings("unchecked")
    public List<GroupDTO> getGroups(Pager pager) {
        Map params = new HashMap();
        params.put("limit", pager.getItemsNumberOnPage());
        params.put("offset", pager.getPage() * pager.getItemsNumberOnPage());
        return queryForList("getGroups", params);
    }

    public Integer getGroupsNumber() {
        return (Integer) queryForObject("getGroupsNumber");
    }

    @SuppressWarnings("unchecked")
    public List<GroupDTO> getUserGroups(String username) {
        return queryForList("getUserGroups", username);
    }

    public Boolean removeGroup(Integer id) {
        return delete("deleteGroup", id) == 1;
    }

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

        return (Boolean) execute(new SqlMapClientCallback() {
            @SuppressWarnings("unchecked")
            public Object doInSqlMapClient(SqlMapExecutor executor) {
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
