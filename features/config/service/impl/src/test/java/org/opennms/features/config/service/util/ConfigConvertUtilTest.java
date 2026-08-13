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
package org.opennms.features.config.service.util;

import org.junit.Assert;
import org.junit.Test;

public class ConfigConvertUtilTest {

    /**
     * Stand-in for a persisted config entity. Mirrors how real config classes are shaped for
     * the CM ObjectMapper: field visibility is ANY and property names are kebab-cased, so the
     * camelCase field {@code securityName} maps to the JSON key {@code security-name}.
     */
    public static class SampleConfig {
        private String securityName;

        public String getSecurityName() {
            return securityName;
        }

        public void setSecurityName(final String securityName) {
            this.securityName = securityName;
        }
    }

    /**
     * Regression test for NMS-19970 / NMS-19723.
     *
     * Config JSON persisted by a newer version can contain fields the running (older) code does
     * not yet know about (e.g. the {@code id} attribute added to snmpv3-user). Reading such JSON
     * must not fail — otherwise a downgrade, rollback, or rolling upgrade cannot start. Before the
     * fix this threw {@code ConfigConversionException} caused by Jackson's
     * {@code UnrecognizedPropertyException}.
     */
    @Test
    public void jsonToObjectIgnoresUnknownProperties() {
        final String json = "{\"security-name\":\"admin\",\"id\":\"abc-123\",\"future-field\":42}";

        final SampleConfig cfg = ConfigConvertUtil.jsonToObject(json, SampleConfig.class);

        Assert.assertNotNull(cfg);
        Assert.assertEquals("admin", cfg.getSecurityName());
    }

    /**
     * Sanity check that known, kebab-cased properties still deserialize. This guards against the
     * regression test above passing trivially (e.g. if property mapping silently broke).
     */
    @Test
    public void jsonToObjectReadsKnownProperties() {
        final String json = "{\"security-name\":\"admin\"}";

        final SampleConfig cfg = ConfigConvertUtil.jsonToObject(json, SampleConfig.class);

        Assert.assertEquals("admin", cfg.getSecurityName());
    }
}
