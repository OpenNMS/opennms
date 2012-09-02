/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
