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
package org.opennms.install.db;

import junit.framework.TestCase;

import org.opennms.core.db.install.Column;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ColumnTest extends TestCase {
    public void testColumnParsePlain() throws Exception {
        Column column = new Column();
        column.parse("x733ProbableCause integer");
        assertEquals("column toString", "x733probablecause integer(4)", column.toString());
        assertFalse("column should not have 'NOT NULL'", column.isNotNull());
        assertNull("column should not have a DEFAULT value", column.getDefaultValue());
    }
    
    public void testColumnParseWithDefaultAndNotNull() throws Exception {
        Column column = new Column();
        column.parse("x733ProbableCause integer default 0 not null");
        assertEquals("column toString", "x733probablecause integer(4) DEFAULT 0 NOT NULL", column.toString());
        assertTrue("column should have 'NOT NULL'", column.isNotNull());
        assertEquals("column DEFAULT value", "0", column.getDefaultValue());
    }
    
    public void testColumnParseWithDefaultDifferent() throws Exception {
        Column oldColumn = new Column();
        oldColumn.parse("x733ProbableCause integer NOT NULL");

        Column newColumn = new Column();
        newColumn.parse("x733ProbableCause integer DEFAULT 0 NOT NULL");
        
        assertFalse("new column should not equal old column", newColumn.equals(oldColumn));
        assertFalse("old column should not equal new column", oldColumn.equals(newColumn));
    }
    
    public void testColumnParseWithDefaultDifferentSpellingWithNextValSequence() throws Exception {
        Column oldColumn = new Column();
        oldColumn.parse("x733ProbableCause integer DEFAULT nextval('opennmsnxtid') NOT NULL");

        Column newColumn = new Column();
        newColumn.parse("x733ProbableCause integer DEFAULT nextval('opennmsNxtId') NOT NULL");
        
        assertTrue("old column should equal new column", oldColumn.equals(newColumn));
    }
}
