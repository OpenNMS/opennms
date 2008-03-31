/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 10, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.db;

import junit.framework.TestCase;

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
