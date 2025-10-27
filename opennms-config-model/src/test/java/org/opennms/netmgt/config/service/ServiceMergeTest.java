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
package org.opennms.netmgt.config.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ServiceMergeTest {

    @Test
    public void testMergeWithBothNull() {
        Service result = Service.merge(null, null);
        assertNull(result);
    }

    @Test
    public void testMergeWithUserNullReturnsDefault() {
        Service defaultService = createService("TestService", "com.example.Test", true);
        Service result = Service.merge(null, defaultService);
        assertEquals(defaultService, result);
    }

    @Test
    public void testMergeWithDefaultNullReturnsUser() {
        Service userService = createService("TestService", null, null);
        Service result = Service.merge(userService, null);
        assertEquals(userService, result);
    }

    @Test
    public void testMergeUserEnabledTakesPrecedence() {
        Service userService = createService("TestService", null, false);
        Service defaultService = createService("TestService", "com.example.Test", true);

        Service result = Service.merge(userService, defaultService);

        assertEquals("TestService", result.getName());
        assertEquals(Boolean.FALSE, result.isEnabled());
        assertEquals("com.example.Test", result.getClassName());
    }

    @Test
    public void testMergeUserEnabledNullDefaultsToTrue() {
        Service userService = createService("TestService", null, null);
        Service defaultService = createService("TestService", "com.example.Test", false);

        Service result = Service.merge(userService, defaultService);

        assertEquals("TestService", result.getName());
        assertEquals(Boolean.TRUE, result.isEnabled()); // Should default to true
        assertEquals("com.example.Test", result.getClassName());
    }

    @Test
    public void testMergeUserExplicitlyEnabledTrue() {
        Service userService = createService("TestService", null, true);
        Service defaultService = createService("TestService", "com.example.Test", false);

        Service result = Service.merge(userService, defaultService);

        assertEquals("TestService", result.getName());
        assertEquals(Boolean.TRUE, result.isEnabled());
        assertEquals("com.example.Test", result.getClassName());
    }

    @Test
    public void testMergeUserAttributesTakePrecedence() {
        Service userService = createService("TestService", null, null);
        List<Attribute> userAttributes = new ArrayList<>();
        userAttributes.add(new Attribute("UserAttr", "java.lang.String", "UserValue"));
        userService.setAttributes(userAttributes);

        Service defaultService = createService("TestService", "com.example.Test", true);
        List<Attribute> defaultAttributes = new ArrayList<>();
        defaultAttributes.add(new Attribute("DefaultAttr", "java.lang.String", "DefaultValue"));
        defaultService.setAttributes(defaultAttributes);

        Service result = Service.merge(userService, defaultService);

        assertEquals(1, result.getAttributes().size());
        assertEquals("UserAttr", result.getAttributes().get(0).getName());
        assertEquals("UserValue", result.getAttributes().get(0).getValue().getContent());
    }

    @Test
    public void testMergeUserEmptyAttributesUsesDefaults() {
        Service userService = createService("TestService", null, null);
        userService.setAttributes(new ArrayList<>());

        Service defaultService = createService("TestService", "com.example.Test", true);
        List<Attribute> defaultAttributes = new ArrayList<>();
        defaultAttributes.add(new Attribute("DefaultAttr", "java.lang.String", "DefaultValue"));
        defaultService.setAttributes(defaultAttributes);

        Service result = Service.merge(userService, defaultService);

        assertEquals(1, result.getAttributes().size());
        assertEquals("DefaultAttr", result.getAttributes().get(0).getName());
    }


    @Test
    public void testMergeUserEmptyInvokesUsesDefaults() {
        Service userService = createService("TestService", null, null);
        userService.setInvokes(new ArrayList<>());

        Service defaultService = createService("TestService", "com.example.Test", true);
        List<Invoke> defaultInvokes = new ArrayList<>();
        defaultInvokes.add(new Invoke(InvokeAtType.START, 0, "defaultMethod", null));
        defaultService.setInvokes(defaultInvokes);

        Service result = Service.merge(userService, defaultService);

        assertEquals(1, result.getInvokes().size());
        assertEquals("defaultMethod", result.getInvokes().get(0).getMethod());
    }

    @Test
    public void testServiceConfigurationMergeWithDefaults() {
        // Create user config with only 2 services
        ServiceConfiguration userConfig = new ServiceConfiguration();
        userConfig.addService(createService("Service1", null, true));
        userConfig.addService(createService("Service2", null, null)); // No enabled attribute

        // Create defaults with 3 services
        ServiceConfiguration defaults = new ServiceConfiguration();
        defaults.addService(createService("Service1", "com.example.Service1", true));
        defaults.addService(createService("Service2", "com.example.Service2", false));
        defaults.addService(createService("Service3", "com.example.Service3", true));


        ServiceConfiguration merged = new ServiceConfiguration();
        for (Service userService : userConfig.getServices()) {
            Service defaultService = null;
            for (Service ds : defaults.getServices()) {
                if (ds.getName().equals(userService.getName())) {
                    defaultService = ds;
                    break;
                }
            }
            merged.addService(Service.merge(userService, defaultService));
        }

        // Verify merged config
        assertEquals(2, merged.getServices().size()); // Only user services should be in merged config

        Service service1 = merged.getServices().get(0);
        assertEquals("Service1", service1.getName());
        assertEquals(Boolean.TRUE, service1.isEnabled());
        assertEquals("com.example.Service1", service1.getClassName());

        Service service2 = merged.getServices().get(1);
        assertEquals("Service2", service2.getName());
        assertEquals(Boolean.TRUE, service2.isEnabled()); // Should default to true since user didn't specify
        assertEquals("com.example.Service2", service2.getClassName());
    }

    @Test
    public void testRealWorldScenarioCorrelatorDisabled() {
        // User wants to enable Correlator that's disabled by default
        Service userService = createService("OpenNMS:Name=Correlator", null, null);
        Service defaultService = createService("OpenNMS:Name=Correlator",
                "org.opennms.netmgt.correlation.jmx.Correlator", false);

        Service result = Service.merge(userService, defaultService);

        assertEquals("OpenNMS:Name=Correlator", result.getName());
        assertEquals(Boolean.TRUE, result.isEnabled()); // User included it, so it should be enabled
        assertEquals("org.opennms.netmgt.correlation.jmx.Correlator", result.getClassName());
    }

    @Test
    public void testRealWorldScenarioExplicitlyDisableService() {
        // User wants to explicitly disable a service
        Service userService = createService("OpenNMS:Name=Alarmd", null, false);
        Service defaultService = createService("OpenNMS:Name=Alarmd",
                "org.opennms.netmgt.daemon.SimpleSpringContextJmxServiceDaemon", true);

        Service result = Service.merge(userService, defaultService);

        assertEquals("OpenNMS:Name=Alarmd", result.getName());
        assertEquals(Boolean.FALSE, result.isEnabled());
        assertEquals("org.opennms.netmgt.daemon.SimpleSpringContextJmxServiceDaemon", result.getClassName());
    }

    @Test
    public void testRealWorldScenarioUserRemovesService() {
        // If user removes a service from config, it shouldn't be in merged config
        ServiceConfiguration userConfig = new ServiceConfiguration();
        userConfig.addService(createService("OpenNMS:Name=Manager", null, true));
        // User intentionally did NOT include Syslogd

        ServiceConfiguration defaults = new ServiceConfiguration();
        defaults.addService(createService("OpenNMS:Name=Manager", "org.opennms.netmgt.vmmgr.Manager", true));
        defaults.addService(createService("OpenNMS:Name=Syslogd", "org.opennms.netmgt.syslogd.jmx.Syslogd", false));

        // Simulate merge
        ServiceConfiguration merged = new ServiceConfiguration();
        for (Service userService : userConfig.getServices()) {
            Service defaultService = null;
            for (Service ds : defaults.getServices()) {
                if (ds.getName().equals(userService.getName())) {
                    defaultService = ds;
                    break;
                }
            }
            merged.addService(Service.merge(userService, defaultService));
        }

        assertEquals(1, merged.getServices().size());
        assertEquals("OpenNMS:Name=Manager", merged.getServices().get(0).getName());
        // Syslogd should NOT be in merged config
    }

    // Helper method to create services
    private Service createService(String name, String className, Boolean enabled) {
        Service service = new Service();
        service.setName(name);
        if (className != null) {
            service.setClassName(className);
        }
        if (enabled != null) {
            service.setEnabled(enabled);
        }
        return service;
    }
}
