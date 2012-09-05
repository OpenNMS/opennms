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

package org.opennms.acl.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.AuthorityView;

@Ignore("test database is not thread-safe, port to opennms temporary database code")
public class AuthoritiesNodeHelperTest {

    @Test
    public void constructor() {
        AuthorityDTO authority = createAuthority();

        List<String> items = new ArrayList<String>();
        items.add("1");

        authority.setItems(items);

        List<AuthorityDTO> authorities = new ArrayList<AuthorityDTO>();
        authorities.add(authority);

        AuthoritiesNodeHelper helper = new AuthoritiesNodeHelper(authorities);
        assertNotNull(helper);
    }

    @Test
    public void getAuthoritiesItems() {

        AuthorityDTO authority = createAuthority();

        List<String> items = new ArrayList<String>();
        items.add("1");

        authority.setItems(items);

        List<AuthorityDTO> authorities = new ArrayList<AuthorityDTO>();
        authorities.add(authority);

        AuthoritiesNodeHelper helper = new AuthoritiesNodeHelper(authorities);
        assertNotNull(helper);
        Set<AuthorityView> auths = new HashSet<AuthorityView>();
        auths.add(authority);

        assertTrue(helper.getAuthoritiesItems(auths).size() == 1);
        assertTrue(helper.getAuthorities().size() == 1);

    }

    @Test
    public void getAuthoritiesItemsNoDuplicated() {

        AuthorityDTO authority = createAuthority();

        List<String> items = new ArrayList<String>();
        items.add("1");
        items.add("2");
        items.add("3");

        authority.setItems(items);

        List<AuthorityDTO> authorities = new ArrayList<AuthorityDTO>();
        authorities.add(authority);
        authorities.add(authority);

        AuthoritiesNodeHelper helper = new AuthoritiesNodeHelper(authorities);
        assertNotNull(helper);
        Set<AuthorityView> auths = new HashSet<AuthorityView>();
        auths.add(authority);

        assertTrue(helper.getAuthorities().size() == 1);

    }

    @Test
    public void deleteItems() {

        AuthorityDTO authority = createAuthority();

        List<String> items = new ArrayList<String>();
        items.add("1");
        items.add("2");
        items.add("3");

        authority.setItems(items);

        List<AuthorityDTO> authorities = new ArrayList<AuthorityDTO>();
        authorities.add(authority);
        authorities.add(authority);

        AuthoritiesNodeHelper helper = new AuthoritiesNodeHelper(authorities);
        assertNotNull(helper);
        Set<AuthorityView> auths = new HashSet<AuthorityView>();
        auths.add(authority);

        assertTrue(helper.getAuthorities().size() == 1);

        Set<AuthorityView> authoritiesView = new HashSet<AuthorityView>();
        authoritiesView.add(authority);

        assertTrue(helper.deleteItem(1));
        assertTrue(helper.getAuthoritiesItems(authoritiesView).size() == 2);

        assertTrue(helper.deleteItem(2));
        assertTrue(helper.getAuthoritiesItems(authoritiesView).size() == 1);

        assertTrue(helper.deleteItem(3));
        assertTrue(helper.getAuthoritiesItems(authoritiesView).size() == 0);

    }

    private AuthorityDTO createAuthority() {
        AuthorityDTO authority = new AuthorityDTO();
        authority.setDescription("this is a description");
        authority.setId(12);
        authority.setName("ROLE_USER");
        return authority;
    }

}
