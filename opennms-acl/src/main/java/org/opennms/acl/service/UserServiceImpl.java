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
//       Massimiliano Dess“
//       desmax74@yahoo.it
//
//
//-----------------------------------------------------------------------------
// OpenNMS Network Management System is Copyright by The OpenNMS Group, Inc.
//============================================================================
package org.opennms.acl.service;

import java.util.List;

import org.opennms.acl.model.Pager;
import org.opennms.acl.model.UserAuthoritiesDTO;
import org.opennms.acl.model.UserDTO;
import org.opennms.acl.model.UserDTOLight;
import org.opennms.acl.model.UserView;
import org.opennms.acl.repository.UserRepository;
import org.opennms.acl.util.Cripto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Massimiliano Dessi (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    public UserDTO getUserCredentials(String id) {
        return userRepository.getUserCredentials(id);
    }

    public boolean save(UserDTO user) {
        return user.isNew() ? userRepository.insertUser(user) > 0 : updatePassword(user);
    }

    public boolean save(UserAuthoritiesDTO user) {
        return userRepository.save(user);
    }

    public boolean updatePassword(UserDTO user) {
        user.setOldPassword(user.getPassword());
        user.setPassword(Cripto.stringToSHA(new StringBuilder(user.getOldPassword()).toString()));
        return userRepository.updatePassword(user) == 1;
    }

    public UserAuthoritiesDTO getUserWithAuthorities(String username) {
        return userRepository.getUserWithAuthorities(username);
    }

    public UserAuthoritiesDTO getUserWithAuthoritiesByID(Integer sid) {
        return userRepository.getUserWithAuthoritiesByID(sid);
    }

    public UserView getUser(String id) {
        return userRepository.getUser(id);
    }

    public Boolean disableUser(String id) {
        return userRepository.disableUser(id);
    }

    public Object getIdUser(String username) {
        return userRepository.getIdUser(username);
    }

    public Integer getUsersNumber() {
        return userRepository.getUsersNumber();
    }

    public Integer getTotalItemsNumber() {
        return userRepository.getUsersNumber();
    }

    public List<UserDTOLight> getDisabledUsers(Pager pager) {
        return userRepository.getDisabledUsers(pager);
    }

    public List<UserDTOLight> getEnabledUsers(Pager pager) {
        return userRepository.getEnabledUsers(pager);
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private UserRepository userRepository;
}
