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
package org.opennms.netmgt.ha;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HaConfigSyncerTest {

    // -------------------------------------------------------------------------
    // JSON array parser
    // -------------------------------------------------------------------------

    @Test
    public void parsesSimpleArray() {
        List<String> result = HaConfigSyncer.parseJsonStringArray(
                "[\"events/foo.xml\",\"threshd-configuration.xml\"]");
        assertEquals(2, result.size());
        assertEquals("events/foo.xml", result.get(0));
        assertEquals("threshd-configuration.xml", result.get(1));
    }

    @Test
    public void parsesEmptyArray() {
        assertTrue(HaConfigSyncer.parseJsonStringArray("[]").isEmpty());
    }

    @Test
    public void parsesNullInput() {
        assertTrue(HaConfigSyncer.parseJsonStringArray(null).isEmpty());
    }

    @Test
    public void parsesArrayWithWhitespace() {
        List<String> result = HaConfigSyncer.parseJsonStringArray(
                "[ \"a.xml\" , \"b.xml\" ]");
        assertEquals(2, result.size());
        assertEquals("a.xml", result.get(0));
        assertEquals("b.xml", result.get(1));
    }

    @Test
    public void handlesMalformedInput() {
        assertTrue(HaConfigSyncer.parseJsonStringArray("not json").isEmpty());
        assertTrue(HaConfigSyncer.parseJsonStringArray("{\"key\":\"val\"}").isEmpty());
    }

    @Test
    public void parsesSingleElement() {
        List<String> result = HaConfigSyncer.parseJsonStringArray("[\"only.xml\"]");
        assertEquals(1, result.size());
        assertEquals("only.xml", result.get(0));
    }

    // -------------------------------------------------------------------------
    // SCV expression resolution (literal passthrough — no real keystore needed)
    // -------------------------------------------------------------------------

    @Test
    public void literalPasswordPassedThrough() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setSyncPassword("myPlainPassword");
        HaConfigSyncer syncer = new HaConfigSyncer(cfg);

        // A literal (non-SCV) password is returned as-is
        assertEquals("myPlainPassword", syncer.resolveScvExpression("myPlainPassword"));
    }

    @Test
    public void nullPasswordReturnsNull() {
        HaConfigSyncer syncer = new HaConfigSyncer(new HaConfiguration());
        assertNull(syncer.resolveScvExpression(null));
    }

    @Test
    public void scvExpressionWithMissingKeystoreReturnsNull() {
        // Point to a non-existent keystore; should log a warning and return null
        System.setProperty("opennms.home", "/tmp/no-such-dir-" + System.currentTimeMillis());
        try {
            HaConfigSyncer syncer = new HaConfigSyncer(new HaConfiguration());
            // Should not throw; should return null (or possibly the empty-keystore case)
            String result = syncer.resolveScvExpression("${scv:hasync:password}");
            // With a non-existent keystore file, JCEKSSecureCredentialsVault returns an empty vault
            // so getCredentials returns null → resolveScvExpression returns null
            assertNull(result);
        } finally {
            System.clearProperty("opennms.home");
        }
    }

    // -------------------------------------------------------------------------
    // Sync skips sync-disabled and missing partner URL configs
    // -------------------------------------------------------------------------

    @Test
    public void syncSkipsWhenDisabled() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setSyncEnabled(false);
        cfg.setPartnerRestUrl("http://partner:8980/opennms");
        // If sync is disabled this should be a no-op — just ensure no exception
        new HaConfigSyncer(cfg).sync();
    }

    @Test
    public void syncSkipsWhenNoPartnerUrl() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setSyncEnabled(true);
        cfg.setPartnerRestUrl(null);
        // Should log a warning and return without attempting HTTP
        new HaConfigSyncer(cfg).sync();
    }
}
