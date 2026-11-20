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
package org.opennms.netmgt.config.opennmsDataSources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConnectionPoolTest {

    @Test
    public void shouldParseIntegerValues() {
        final ConnectionPool pool = new ConnectionPool();
        pool.setMaxPool("7");
        pool.setMinPool(" 2 ");
        assertEquals(Integer.valueOf(7), pool.getMaxPool());
        assertEquals(Integer.valueOf(2), pool.getMinPool());
    }

    @Test
    public void shouldUseDefaultsWhenAbsentOrBlank() {
        final ConnectionPool pool = new ConnectionPool();
        assertEquals(Integer.valueOf(50), pool.getMaxPool());

        // What "${env:VAR|50}" resolves to when VAR is set but empty
        pool.setIdleTimeout("");
        pool.setLoginTimeout("  ");
        pool.setMinPool("");
        pool.setMaxPool("");
        pool.setMaxSize("");
        assertEquals(Integer.valueOf(600), pool.getIdleTimeout());
        assertEquals(Integer.valueOf(3), pool.getLoginTimeout());
        assertEquals(Integer.valueOf(10), pool.getMinPool());
        assertEquals(Integer.valueOf(50), pool.getMaxPool());
        assertEquals(Integer.valueOf(500), pool.getMaxSize());
    }

    @Test
    public void shouldUseTheCallersDefaultWhenAbsentOrBlank() {
        final ConnectionPool pool = new ConnectionPool();
        assertEquals(Integer.valueOf(42), pool.getMinPool(42));
        pool.setMinPool(" ");
        assertEquals(Integer.valueOf(42), pool.getMinPool(42));
        pool.setMinPool("7");
        assertEquals(Integer.valueOf(7), pool.getMinPool(42));
    }

    @Test
    public void shouldUseFallbackOfUnsetEnvironmentVariable() {
        final ConnectionPool pool = new ConnectionPool();
        pool.setMaxPool("${env:NMS_20620_UNSET_VARIABLE|12}");
        assertEquals(Integer.valueOf(12), pool.getMaxPool());
    }

    @Test
    public void shouldNameTheAttributeOfAnInvalidValue() {
        final ConnectionPool pool = new ConnectionPool();
        pool.setMaxPool("abc");
        final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, pool::getMaxPool);
        assertTrue(e.getMessage(), e.getMessage().contains("maxPool"));
        assertTrue(e.getMessage(), e.getMessage().contains("'abc'"));
    }
}
