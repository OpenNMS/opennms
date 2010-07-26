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

import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.AuthorityView;
import org.opennms.acl.model.Pager;
import org.opennms.acl.repository.AuthorityRepository;
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
 * <p>AuthorityRepositoryIbatis class.</p>
 *
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 * @version $Id: $
 */
@Repository("authorityRepository")
public class AuthorityRepositoryIbatis extends SqlMapClientTemplate implements AuthorityRepository {

    /** {@inheritDoc} */
    @Autowired
    @Override
    public void setSqlMapClient(@Qualifier("sqlMapClient") SqlMapClient sqlMapClient) {
        super.setSqlMapClient(sqlMapClient);
    }

    /** {@inheritDoc} */
    public Boolean removeGroupFromAuthorities(Integer id) {
        return update("updateAuthorityToGroupToHidden", id) > 0;
    }

    /**
     * <p>getAuthorities</p>
     *
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<AuthorityDTO> getAuthorities() {
        return queryForList("getAuthorities");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<AuthorityDTO> getUserAuthorities(String username) {
        return queryForList("getUserAuthorities", username);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<AuthorityDTO> getFreeAuthorities(String username) {
        return queryForList("getFreeAuthorities", username);
    }

    /**
     * <p>getFreeAuthoritiesForGroup</p>
     *
     * @return a {@link java.util.List} object.
     */
    @SuppressWarnings("unchecked")
    public List<AuthorityDTO> getFreeAuthoritiesForGroup() {
        return queryForList("getFreeAuthoritiesForGroup");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<Integer> getIdItemsAuthority(Integer id) {
        return queryForList("getAuthorityItemsIds", id);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<AuthorityDTO> getGroupAuthorities(Integer id) {
        return queryForList("getGroupAuthorities", id);
    }

    /** {@inheritDoc} */
    public Boolean saveAuthorities(final Integer groupId, final List<Integer> authorities) {

        return execute(new SqlMapClientCallback<Boolean>() {
            @SuppressWarnings("unchecked")
            public Boolean doInSqlMapClient(SqlMapExecutor executor) {
                int ris = 0;
                try {

                    executor.startBatch();
                    Iterator<Integer> iter = authorities.iterator();

                    while (iter.hasNext()) {
                        Map params = new HashMap();
                        params.put("group_id", groupId);
                        params.put("id", iter.next());
                        executor.insert("updateGroupAuthority", params);
                    }
                    ris = executor.executeBatch();
                } catch (SQLException e) {
                    Logger log = LoggerFactory.getLogger(this.getClass());
                    StringBuffer sb = new StringBuffer("saveAuthorities failed \n").append("num items batch:").append(authorities.size()).append("\n").append(" groupId:").append(groupId).append("\n")
                            .append(e.getNextException());
                    log.error(sb.toString());
                }
                return ris > 0;
            }
        });
    }

    private Boolean saveItems(final Integer authority, final List<?> items) {

        return execute(new SqlMapClientCallback<Boolean>() {
            @SuppressWarnings("unchecked")
            public Boolean doInSqlMapClient(SqlMapExecutor executor) {
                int ris = 0;
                try {

                    executor.startBatch();
                    Iterator<Integer> iter = (Iterator<Integer>) items.iterator();

                    while (iter.hasNext()) {
                        Map params = new HashMap();
                        params.put("categoryId", iter.next());
                        params.put("authorityId", authority);
                        executor.insert("inserAuthorityItem", params);
                    }
                    ris = executor.executeBatch();
                } catch (SQLException e) {
                    Logger log = LoggerFactory.getLogger(this.getClass());
                    StringBuffer sb = new StringBuffer("saveItems failed \n").append("num items batch:").append(items.size()).append("\n").append(" idUser:").append(authority).append("\n").append(
                            e.getNextException());
                    log.error(sb.toString());
                }
                return ris > 0;
            }
        });
    }

    /**
     * <p>getAuthoritiesNumber</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getAuthoritiesNumber() {
        return (Integer) queryForObject("getAuthoritiesNumber");
    }

    /** {@inheritDoc} */
    public AuthorityDTO getAuthority(Integer id) {
        return (AuthorityDTO) queryForObject("getAuthority", id);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<AuthorityDTO> getAuthorities(Pager pager) {
        Map params = new HashMap();
        params.put("limit", pager.getItemsNumberOnPage());
        params.put("offset", pager.getPage() * pager.getItemsNumberOnPage());
        return queryForList("getAllAuthorities", params);
    }

    /** {@inheritDoc} */
    public Boolean removeAuthority(Integer id) {
        return delete("deleteAuthority", id) == 1 ? true : false;
    }

    /** {@inheritDoc} */
    public Boolean deleteUserGroups(String username) {
        return delete("deleteUserGroups", username) > 0;
    }

    /** {@inheritDoc} */
    public Boolean save(AuthorityDTO authority) {
        return authority.isNew() ? insert(authority) : update(authority);
    }

    private Boolean insert(AuthorityView authority) {
        return insert("insertAuthority", authority) != null;
    }

    private Boolean deleteAuthorityItems(Integer id) {
        return delete("deleteAuthorityItems", id) > 0;
    }

    private Boolean update(AuthorityView authority) {

        if (authority.getItems() != null && authority.getItems().size() > 0) {
            deleteAuthorityItems(authority.getId());
            saveItems(authority.getId(), authority.getItems());
        }
        return update("updateAuthority", authority) == 1;
    }
}
