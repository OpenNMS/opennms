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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ServiceEnabledTest {

    @Test
    public void nullEnabledDefaultsToTrue() {
        final Service s = service(null);
        assertEquals(Boolean.TRUE, s.isEnabled());
    }

    @Test
    public void emptyEnabledDefaultsToTrue() {
        assertEquals(Boolean.TRUE, service("").isEnabled());
        assertEquals(Boolean.TRUE, service("   ").isEnabled());
    }

    @Test
    public void literalTrueAndFalse() {
        assertEquals(Boolean.TRUE, service("true").isEnabled());
        assertEquals(Boolean.FALSE, service("false").isEnabled());
    }

    @Test
    public void caseInsensitive() {
        assertEquals(Boolean.TRUE, service("TRUE").isEnabled());
        assertEquals(Boolean.TRUE, service("True").isEnabled());
        assertEquals(Boolean.FALSE, service("FALSE").isEnabled());
        assertEquals(Boolean.FALSE, service("False").isEnabled());
    }

    @Test
    public void whitespaceIsTrimmed() {
        assertEquals(Boolean.TRUE, service(" true ").isEnabled());
        assertEquals(Boolean.FALSE, service("\tfalse\n").isEnabled());
    }

    @Test
    public void unresolvedPlaceholderUsesEmbeddedDefault() {
        // If interpolation never ran (e.g. raw unmarshal in a migrator), fall back to
        // the |default literal in the placeholder rather than silently disabling.
        assertEquals(Boolean.TRUE, service("${env:CORE_SERVICE_ALARMD_ENABLED|true}").isEnabled());
        assertEquals(Boolean.FALSE, service("${env:CORE_SERVICE_SYSLOGD_ENABLED|false}").isEnabled());
    }

    @Test
    public void unresolvedPlaceholderDefaultIsCaseInsensitive() {
        assertEquals(Boolean.TRUE, service("${env:X|TRUE}").isEnabled());
        assertEquals(Boolean.FALSE, service("${env:X|False}").isEnabled());
    }

    @Test
    public void unresolvedPlaceholderWithoutDefaultIsDisabled() {
        // No |default segment — we cannot guess; treat as disabled (logged WARN).
        assertEquals(Boolean.FALSE, service("${env:CORE_SERVICE_X_ENABLED}").isEnabled());
    }

    @Test
    public void unresolvedPlaceholderWithGarbageDefaultIsDisabled() {
        assertEquals(Boolean.FALSE, service("${env:X|yes}").isEnabled());
        assertEquals(Boolean.FALSE, service("${env:X|1}").isEnabled());
    }

    @Test
    public void garbageValueIsDisabled() {
        // A typo such as "ture" must not silently enable; before this change
        // Boolean.parseBoolean would have returned false anyway, but now it logs.
        assertEquals(Boolean.FALSE, service("ture").isEnabled());
        assertEquals(Boolean.FALSE, service("yes").isEnabled());
        assertEquals(Boolean.FALSE, service("1").isEnabled());
    }

    @Test
    public void setEnabledBooleanRoundTrip() {
        final Service s = new Service();
        s.setName("OpenNMS:Name=Test");
        s.setEnabled(Boolean.TRUE);
        assertEquals("true", s.getRawEnabled());
        assertEquals(Boolean.TRUE, s.isEnabled());

        s.setEnabled(Boolean.FALSE);
        assertEquals("false", s.getRawEnabled());
        assertEquals(Boolean.FALSE, s.isEnabled());

        s.setEnabled((Boolean) null);
        assertEquals(null, s.getRawEnabled());
        assertEquals(Boolean.TRUE, s.isEnabled());
    }

    private static Service service(final String enabled) {
        final Service s = new Service();
        s.setName("OpenNMS:Name=Test");
        s.setEnabled(enabled);
        return s;
    }
}
