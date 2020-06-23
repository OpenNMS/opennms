/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Collections;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/applicationContext-soa.xml" })
@JUnitConfigurationEnvironment
public class ServletInitializerTest {

    /**
     * Verifies that {@link ServletInitializer#init} does not override existing
     * system properties.
     *
     * @throws ServletException
     */
    @Test
    public void initShouldNotOverrideExistingSystemProperties() throws ServletException {
        // Set a system property to a known value
        System.setProperty("x", "x");

        // Now mock the context and mock the version.properties file to contain
        // two system properties
        String properties = "x = !x\n" + "y = y";
        ServletContext context = mock(ServletContext.class);
        when(context.getResourceAsStream("/WEB-INF/version.properties"))
                .thenReturn(new ByteArrayInputStream(properties.getBytes()));
        when(context.getInitParameterNames()).thenReturn(Collections.emptyEnumeration());

        // Init
        ServletInitializer.init(context);

        // "x" should retain it's original value
        assertEquals("x", System.getProperty("x"));
        // "y" should be set
        assertEquals("y", System.getProperty("y"));
    }
}
