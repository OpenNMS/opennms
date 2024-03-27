/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
public class ServletInitializerIT {

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
