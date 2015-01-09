package org.opennms.web.controller;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.web.navigate.LocationBasedNavBarEntry;
import org.opennms.web.navigate.MenuDropdownNavBarEntry;
import org.opennms.web.navigate.NavBarEntry;
import org.springframework.mock.web.MockHttpServletRequest;

import com.google.common.collect.Lists;

public class NavBarControllerTest {
    NavBarController navBarController;

    @Before
    public void setUp() throws Exception {
        // Build a simple menu
        LocationBasedNavBarEntry alarms = new LocationBasedNavBarEntry();
        alarms.setName("Alarms");
        alarms.setUrl("alarm/index.htm");
        alarms.setLocationMatch("alarm");

        MenuDropdownNavBarEntry menu = new MenuDropdownNavBarEntry();
        menu.setName("Menu");
        menu.setEntries(Lists.newArrayList((NavBarEntry) alarms));

        List<NavBarEntry> navBarEntries = Lists.newArrayList((NavBarEntry) menu);

        // Instantiate the controller
        navBarController = new NavBarController();
        navBarController.setNavBarItems(navBarEntries);
        navBarController.afterPropertiesSet();
    }

    @Test
    public void canRenderHeaderHtml() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRemoteUser("admin");
        String headerHtml = navBarController.getHeaderHtml(request);
        assertTrue(headerHtml.contains("<nav"));
    }
}
