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
package org.opennms.web.springframework.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoginModuleUtilsTest {
    @Test
    public void testEmptyUrlsAreInvalid() {
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl(null));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl(""));
    }

    @Test
    public void testAssetUrlsAreInvalid() {
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/assets/opennms-theme.css"));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/ui-components/assets/index.js"));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/assets/fonts/opensans.ttf"));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/ASSETS/OPENNMS-THEME.CSS"));
    }

    @Test
    public void testRestAndApiUrlsAreInvalid() {
        // NMS-20174: a REST XHR captured in the request cache must never become
        // the post-login redirect target (navigating there downloads a file).
        // Servlets mapped at /rest/* and /api/v2/* report those prefixes as
        // their servlet path, so both the bare prefix and subpaths must match.
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/rest"));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/rest/menu"));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/api"));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/api/v2"));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/api/v2/nodes"));
        assertTrue(LoginModuleUtils.isInvalidSavedRequestUrl("/REST/menu"));
    }

    @Test
    public void testPageUrlsAreValid() {
        assertFalse(LoginModuleUtils.isInvalidSavedRequestUrl("/index.jsp"));
        assertFalse(LoginModuleUtils.isInvalidSavedRequestUrl("/element/node.jsp?node=1"));
        assertFalse(LoginModuleUtils.isInvalidSavedRequestUrl("/alarm/detail.htm"));
        // prefix matching must not reject pages that merely start with "rest"/"api"
        assertFalse(LoginModuleUtils.isInvalidSavedRequestUrl("/restrictedPage.jsp"));
        assertFalse(LoginModuleUtils.isInvalidSavedRequestUrl("/apidocs.jsp"));
    }
}
