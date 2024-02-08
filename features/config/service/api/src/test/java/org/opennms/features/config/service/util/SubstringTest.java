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

import static org.junit.Assert.*;

import org.junit.Test;

public class SubstringTest {

    @Test
    public void shouldDoBeforeLastIndex() {
        assertEquals("abc-def", new Substring("abc-def-ghi").getBeforeLast("-").toString());
        assertEquals("-abc-def", new Substring("-abc-def-ghi").getBeforeLast("-").toString());
        assertEquals("abc", new Substring("abc").getBeforeLast("-").toString());
        assertEquals("", new Substring("").getBeforeLast("-").toString());
    }

    @Test
    public void shouldDoAfterLastIndex() {
        assertEquals("def", new Substring("abc-def").getAfterLast("-").toString());
        assertEquals("", new Substring("-abc-def-ghi-").getAfterLast("-").toString());
        assertEquals("", new Substring("abc").getAfterLast("-").toString());
        assertEquals("", new Substring("").getAfterLast("-").toString());
    }

    @Test
    public void shouldDoAfter() {
        assertEquals("def-ghi", new Substring("abc-def-ghi").getAfter("-").toString());
        assertEquals("abc-def-ghi-", new Substring("-abc-def-ghi-").getAfter("-").toString());
        assertEquals("", new Substring("abc").getAfter("-").toString());
        assertEquals("", new Substring("").getAfterLast("-").toString());
    }

}