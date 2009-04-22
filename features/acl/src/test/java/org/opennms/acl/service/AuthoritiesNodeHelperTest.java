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
