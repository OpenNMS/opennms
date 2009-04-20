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
package org.opennms.acl.factory;

import org.opennms.acl.domain.Authority;
import org.opennms.acl.service.AclItemService;
import org.opennms.acl.service.AuthorityService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author Massimiliano Dessi (desmax74@yahoo.it)
 * @since jdk 1.5.0
 */
@Service("authorityFactory")
public class AuthorityFactoryImpl implements AutorityFactory, InitializingBean {

    public Authority getAuthority(Integer id) {
        return new Authority(authorityService.getAuthority(id), authorityService, aclItemService);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.state(authorityService != null, "authorityService property must be set and cannot be null");
        Assert.state(aclItemService != null, "aclItemService property must be set and cannot be null");
    }

    @Autowired
    private AuthorityService authorityService;
    @Autowired
    @Qualifier("categoryNodesItemsService")
    private AclItemService aclItemService;
}
