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

import org.opennms.acl.model.Pager;
import org.opennms.acl.model.UserAuthoritiesDTO;
import org.opennms.acl.model.UserDTO;
import org.opennms.acl.model.UserDTOLight;
import org.opennms.acl.model.UserView;
import org.opennms.acl.repository.UserRepository;
import org.opennms.acl.util.Cripto;
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
@Repository("userRepository")
public class UserRepositoryIbatis extends SqlMapClientTemplate implements UserRepository {

    public UserDTO getUserCredentials(String id) {
        return (UserDTO) queryForObject("getUserCredentials", new Long(id));
    }

    @Autowired
    @Override
    public void setSqlMapClient(@Qualifier("sqlMapClient") SqlMapClient sqlMapClient) {
        super.setSqlMapClient(sqlMapClient);
    }

    public Long insertUser(UserDTO user) {
        user.setPassword(Cripto.stringToSHA(user.getPassword()));
        return (Long) insert("insertUser", user);
    }

    public Integer updatePassword(UserDTO user) {
        return update("updateUserPassword", user);
    }

    public UserAuthoritiesDTO getUserWithAuthorities(String username) {
        return (UserAuthoritiesDTO) queryForObject("getUserWithAuthorities", username);
    }

    public UserAuthoritiesDTO getUserWithAuthoritiesByID(Integer sid) {
        return (UserAuthoritiesDTO) queryForObject("getUserWithAuthoritiesById", sid);
    }

    public UserView getUser(String id) {
        return (UserView) queryForObject("getUser", Integer.valueOf(id));
    }

    public Integer getUsersNumber() {
        return (Integer) queryForObject("getUsersNumber");
    }

    public Boolean disableUser(String id) {
        return update("disableUser", new Long(id)) == 1 ? true : false;
    }

    public Object getIdUser(String username) {
        return queryForObject("getIdUser", username);
    }

    @SuppressWarnings("unchecked")
    public List<UserDTOLight> getDisabledUsers(Pager pager) {
        Map params = new HashMap();
        params.put("limit", pager.getItemsNumberOnPage());
        params.put("offset", pager.getPage() * pager.getItemsNumberOnPage());
        return queryForList("getDisabledUsers", params);
    }

    @SuppressWarnings("unchecked")
    public List<UserDTOLight> getEnabledUsers(Pager pager) {
        Map params = new HashMap();
        params.put("limit", pager.getItemsNumberOnPage());
        params.put("offset", pager.getPage() * pager.getItemsNumberOnPage());
        return queryForList("getEnabledUsers", params);
    }

    public Boolean save(UserAuthoritiesDTO user) {
        return user.isNew() ? insert(user) : update(user);
    }

    private Boolean update(UserAuthoritiesDTO user) {
        boolean result = deleteUserItems(user.getUsername());
        if (user.getItems() != null && user.getItems().size() > 0) {
            result = saveItems(user.getUsername(), user.getItems());
        }
        return result;
    }

    private Boolean insert(UserAuthoritiesDTO user) {
        return insert("insertUser", user) != null;
    }

    private Boolean deleteUserItems(String username) {
        return delete("deleteUserItems", username) > 0;
    }

    private Boolean saveItems(final String username, final List<?> items) {

        return (Boolean) execute(new SqlMapClientCallback() {
            @SuppressWarnings("unchecked")
            public Object doInSqlMapClient(SqlMapExecutor executor) {
                int ris = 0;
                try {

                    executor.startBatch();
                    Iterator<Integer> iter = (Iterator<Integer>) items.iterator();

                    while (iter.hasNext()) {
                        Map params = new HashMap();
                        params.put("id", iter.next());
                        params.put("username", username);
                        executor.insert("insertUserItem", params);
                    }
                    ris = executor.executeBatch();
                } catch (SQLException e) {
                    Logger log = LoggerFactory.getLogger(this.getClass());
                    StringBuffer sb = new StringBuffer("saveItems failed \n").append("num groups batch:").append(items.size()).append("\n").append(" username:").append(username).append("\n").append(
                            e.getNextException());
                    log.error(sb.toString());
                }
                return ris > 0;
            }
        });
    }

}
