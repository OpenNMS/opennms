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
package org.opennms.netmgt.flows.rest.internal.classification;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class FilenameHelperTest {

    @Test
    public void shouldAllowOnlyValidFileNames() {
        FilenameHelper h = new FilenameHelper();
        assertTrue(h.isValidFileName("x"));
        assertTrue(h.isValidFileName("xY"));
        assertTrue(h.isValidFileName("xY"));
        assertTrue(h.isValidFileName("x_Y-"));
        assertTrue(h.isValidFileName("x_Y -"));
        assertTrue(h.isValidFileName("aa.csv"));
        assertTrue(h.isValidFileName("9"));
        assertTrue("Default Filename should be accepted", h.isValidFileName(h.createFilenameForGroupExport(1, null)));
        assertFalse(h.isValidFileName(" aa.csv"));
        assertFalse(h.isValidFileName("aa "));
        assertFalse(h.isValidFileName(null));
        assertFalse(h.isValidFileName(""));
        assertFalse(h.isValidFileName(" "));
        assertFalse(h.isValidFileName("          "));
        assertFalse(h.isValidFileName("ä"));
        assertFalse(h.isValidFileName("a%"));
        assertFalse(h.isValidFileName("%"));

    }

    @Test
    public void verifyDefaultFileNameForGroupExport(){
        FilenameHelper helper = new FilenameHelper();
        assertEquals("1_rules.csv", helper.createFilenameForGroupExport(1, null));
        assertEquals("1_rules.csv", helper.createFilenameForGroupExport(1, ""));
    }

    @Test
    public void verifyCustomFileNameForGroupExport(){
        FilenameHelper helper = new FilenameHelper();
        assertEquals("abc.csv", helper.createFilenameForGroupExport(1, "abc.csv"));
        assertEquals("hello bello.csv", helper.createFilenameForGroupExport(1, "hello bello.csv"));
    }
}