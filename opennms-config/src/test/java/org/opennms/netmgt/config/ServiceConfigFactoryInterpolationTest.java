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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;

/**
 * Exercises the static interpolation helper on {@link ServiceConfigFactory}.
 *
 * <p>System.getenv() is read-only in-process, so these tests focus on the
 * default-fallback path (env var unset → use embedded default) and the
 * literal pass-through path. The Service.isEnabled() contract for unresolved
 * placeholders is covered separately in ServiceEnabledTest.
 */
public class ServiceConfigFactoryInterpolationTest {

    @Test
    public void unsetEnvVarFallsBackToEmbeddedDefault() {
        final String unsetVar = "OPENNMS_TEST_UNSET_" + UUID.randomUUID().toString().replace("-", "_").toUpperCase();
        // Sanity: ensure the test variable really isn't set in this JVM's env.
        assertEquals(null, System.getenv(unsetVar));

        final ServiceConfiguration cfg = new ServiceConfiguration();
        cfg.addService(svc("OpenNMS:Name=DefaultsTrue", "${env:" + unsetVar + "|true}"));
        cfg.addService(svc("OpenNMS:Name=DefaultsFalse", "${env:" + unsetVar + "|false}"));

        ServiceConfigFactory.interpolateServiceAttributes(cfg);

        assertEquals("true", cfg.getServices().get(0).getRawEnabled());
        assertEquals("false", cfg.getServices().get(1).getRawEnabled());
        assertEquals(Boolean.TRUE, cfg.getServices().get(0).isEnabled());
        assertEquals(Boolean.FALSE, cfg.getServices().get(1).isEnabled());
    }

    @Test
    public void literalValuesArePreserved() {
        final ServiceConfiguration cfg = new ServiceConfiguration();
        cfg.addService(svc("OpenNMS:Name=LiteralTrue", "true"));
        cfg.addService(svc("OpenNMS:Name=LiteralFalse", "false"));

        ServiceConfigFactory.interpolateServiceAttributes(cfg);

        assertEquals("true", cfg.getServices().get(0).getRawEnabled());
        assertEquals("false", cfg.getServices().get(1).getRawEnabled());
    }

    @Test
    public void nullEnabledIsLeftAlone() {
        final ServiceConfiguration cfg = new ServiceConfiguration();
        final Service s = new Service();
        s.setName("OpenNMS:Name=NullEnabled");
        cfg.addService(s);

        ServiceConfigFactory.interpolateServiceAttributes(cfg);

        assertEquals(null, cfg.getServices().get(0).getRawEnabled());
        // Null still defaults to enabled.
        assertEquals(Boolean.TRUE, cfg.getServices().get(0).isEnabled());
    }

    @Test
    public void resolvesEnvVarFromCurrentProcessIfPresent() {
        // We can't set env vars from Java, but we can pick any variable that's
        // virtually always present (PATH on every supported platform).
        final String path = System.getenv("PATH");
        assertNotNull("PATH must be set for this test to be meaningful", path);

        final ServiceConfiguration cfg = new ServiceConfiguration();
        cfg.addService(svc("OpenNMS:Name=EnvFromHost", "${env:PATH|fallback-not-used}"));

        ServiceConfigFactory.interpolateServiceAttributes(cfg);

        // After interpolation the raw value is whatever PATH contains, which
        // won't parse as a boolean — but that proves the env var resolved.
        assertEquals(path, cfg.getServices().get(0).getRawEnabled());
        assertTrue("interpolated value should match PATH",
                cfg.getServices().get(0).getRawEnabled().contains(":") || !path.isEmpty());
    }

    private static Service svc(final String name, final String enabled) {
        final Service s = new Service();
        s.setName(name);
        s.setEnabled(enabled);
        return s;
    }
}
