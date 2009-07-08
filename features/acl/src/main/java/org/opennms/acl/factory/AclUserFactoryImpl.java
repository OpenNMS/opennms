/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.acl.factory;

import org.opennms.acl.domain.GenericUser;
import org.opennms.acl.model.UserAuthoritiesDTO;
import org.opennms.acl.service.AuthorityService;
import org.opennms.acl.service.GroupService;
import org.opennms.acl.service.UserService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Acl user factory, retrieve users by id or username
 * 
 * @author Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Service("aclUserFactory")
public class AclUserFactoryImpl implements AclUserFactory, InitializingBean {

    public GenericUser getAclUserByUsername(String username) {
        UserAuthoritiesDTO userAuthoritiesDTO = userService.getUserWithAuthorities(username);
        return new GenericUser(userAuthoritiesDTO, userService, groupService);
    }

    public GenericUser getAclUser(Integer sid) {
        UserAuthoritiesDTO userAuthoritiesDTO = userService.getUserWithAuthoritiesByID(sid);
        return new GenericUser(userAuthoritiesDTO, userService, groupService);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(authorityService != null, "authorityService property must be set and cannot be null");
        Assert.state(userService != null, "userService property must be set and cannot be null");
    }

    @Autowired
    private UserService userService;
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private GroupService groupService;
}
