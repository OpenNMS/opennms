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

import java.util.UUID;

import org.junit.Test;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;

/**
 * Exercises the static interpolation helper on {@link ServiceConfigFactory}.
 *
 * <p>System.getenv() is read-only in-process, so these tests use unset env vars
 * (with a UUID-randomized name) to validate the |default fallback path, and
 * an env var that is virtually always present (PATH) to validate resolution.
 */
public class ServiceConfigFactoryInterpolationTest {

    @Test
    public void unsetEnvVarFallsBackToEmbeddedDefault() {
        final String unsetVar = "OPENNMS_TEST_UNSET_" + UUID.randomUUID().toString().replace("-", "_").toUpperCase();
        assertEquals(null, System.getenv(unsetVar));

        final ServiceConfiguration cfg = new ServiceConfiguration();
        cfg.addService(svc("OpenNMS:Name=DefaultsTrue", "${env:" + unsetVar + "|true}"));
        cfg.addService(svc("OpenNMS:Name=DefaultsFalse", "${env:" + unsetVar + "|false}"));

        ServiceConfigFactory.interpolateServiceAttributes(cfg);

        assertEquals(Boolean.TRUE, cfg.getServices().get(0).isEnabled());
        assertEquals(Boolean.FALSE, cfg.getServices().get(1).isEnabled());
    }

    @Test
    public void literalValuesArePreserved() {
        final ServiceConfiguration cfg = new ServiceConfiguration();
        cfg.addService(svc("OpenNMS:Name=LiteralTrue", "true"));
        cfg.addService(svc("OpenNMS:Name=LiteralFalse", "false"));

        ServiceConfigFactory.interpolateServiceAttributes(cfg);

        assertEquals(Boolean.TRUE, cfg.getServices().get(0).isEnabled());
        assertEquals(Boolean.FALSE, cfg.getServices().get(1).isEnabled());
    }

    @Test
    public void nullEnabledIsLeftAlone() {
        final ServiceConfiguration cfg = new ServiceConfiguration();
        final Service s = new Service();
        s.setName("OpenNMS:Name=NullEnabled");
        cfg.addService(s);

        ServiceConfigFactory.interpolateServiceAttributes(cfg);

        assertEquals(null, cfg.getServices().get(0).getRawEnabled());
        assertEquals(Boolean.TRUE, cfg.getServices().get(0).isEnabled());
    }

    @Test
    public void resolvesEnvVarFromCurrentProcessIfPresent() {
        final String path = System.getenv("PATH");
        assertNotNull("PATH must be set for this test to be meaningful", path);

        final ServiceConfiguration cfg = new ServiceConfiguration();
        cfg.addService(svc("OpenNMS:Name=EnvFromHost", "${env:PATH|fallback-not-used}"));

        ServiceConfigFactory.interpolateServiceAttributes(cfg);

        assertEquals(path, cfg.getServices().get(0).getRawEnabled());
    }

    private static Service svc(final String name, final String enabled) {
        final Service s = new Service();
        s.setName(name);
        s.setEnabled(enabled);
        return s;
    }
}
