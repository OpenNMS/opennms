/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class StringReplaceOperationTest extends TestCase {
    
    public void testReplaceFirst() {
        String orig = "There was once a quick brown fox. There was once a lazy dog.";
        String expected = "The quick brown fox. There was once a lazy dog.";
        StringReplaceOperation op = new ReplaceFirstOperation("s/There was once a/The/");
        assertEquals(op.replace(orig), expected);
    }
    
    public void testReplaceAll() {
        String orig = "There was once a quick brown fox. There was once a lazy dog.";
        String expected = "There was twice a quick brown fox. There was twice a lazy dog.";
        StringReplaceOperation op = new ReplaceAllOperation("s/once/twice/");
        assertEquals(op.replace(orig), expected);
    }
    
    public void testFirstAllFirst() {
        String orig = "There was once a quick brown fox. There was once a lazy dog.";
        String expected = "On the roof was once one quick brown fox. In the basement was once one lazy dog.";
        
        List<StringReplaceOperation> ops = new ArrayList<>();
        ops.add(new ReplaceFirstOperation("s/There/On the roof/"));
        ops.add(new ReplaceAllOperation("s/ a / one /"));
        ops.add(new ReplaceFirstOperation("s/There/In the basement/"));
        String result = orig;
        for (StringReplaceOperation op : ops) {
            result = op.replace(result);
        }
        
        assertEquals(result, expected);
    }

}
