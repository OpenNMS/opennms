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
        assertEquals(Boolean.TRUE, service(null).isEnabled());
    }

    @Test
    public void literalTrueAndFalse() {
        assertEquals(Boolean.TRUE, service("true").isEnabled());
        assertEquals(Boolean.FALSE, service("false").isEnabled());
    }

    @Test
    public void caseInsensitive() {
        assertEquals(Boolean.TRUE, service("TRUE").isEnabled());
        assertEquals(Boolean.FALSE, service("FALSE").isEnabled());
    }

    @Test
    public void whitespaceFromEnvVarIsTrimmed() {
        // Env values can carry stray whitespace; ensure we don't silently disable.
        assertEquals(Boolean.TRUE, service(" true ").isEnabled());
        assertEquals(Boolean.FALSE, service("\tfalse\n").isEnabled());
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
