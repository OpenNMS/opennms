/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
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

//
// This fiOle is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.snmp.Range;

/**
 */
public class RangeComparatorTest {

    @Test
    public void testCompare() {
        List<Range> ranges = new ArrayList<Range>();

        Range addMe = new Range();
        addMe.setBegin("192.168.101.1");
        addMe.setEnd("192.168.101.254");
        ranges.add(addMe);

        addMe = new Range();
        addMe.setBegin("192.168.100.1");
        addMe.setEnd("192.168.100.254");
        ranges.add(addMe);

        addMe = new Range();
        addMe.setBegin("10.1.2.1");
        addMe.setEnd("10.1.2.100");
        ranges.add(addMe);

        addMe = new Range();
        addMe.setBegin("11.1.2.1");
        addMe.setEnd("11.1.2.100");
        ranges.add(addMe);

        addMe = new Range();
        addMe.setBegin("12.1.2.1");
        addMe.setEnd("12.1.2.100");
        ranges.add(addMe);

        Collections.sort(ranges, new RangeComparator());

        assertEquals("10.1.2.1", ranges.get(0).getBegin());
        assertEquals("10.1.2.100", ranges.get(0).getEnd());
        assertEquals("11.1.2.1", ranges.get(1).getBegin());
        assertEquals("11.1.2.100", ranges.get(1).getEnd());
        assertEquals("12.1.2.1", ranges.get(2).getBegin());
        assertEquals("12.1.2.100", ranges.get(2).getEnd());
        assertEquals("192.168.100.1", ranges.get(3).getBegin());
        assertEquals("192.168.100.254", ranges.get(3).getEnd());
        assertEquals("192.168.101.1", ranges.get(4).getBegin());
        assertEquals("192.168.101.254", ranges.get(4).getEnd());

        /*
        <?xml version="1.0"?>
        <snmp-config retry="3" timeout="800"
           read-community="public" write-community="private">
           <definition version="v2c">
               <specific>192.168.0.5</specific>
           </definition>

           <definition read-community="opennmsrules">
               <range begin="192.168.100.1" end="192.168.100.254"/>
               <range begin="192.168.101.1" end="192.168.101.254"/>
               <range begin="192.168.102.1" end="192.168.102.254"/>
               <range begin="192.168.103.1" end="192.168.103.254"/>
               <range begin="192.168.104.1" end="192.168.104.254"/>
               <range begin="192.168.105.1" end="192.168.105.254"/>
               <range begin="192.168.106.1" end="192.168.106.254"/>
               <range begin="192.168.107.1" end="192.168.107.254"/>
               <range begin="192.168.0.1" end="192.168.0.10"/>
           </definition>
           <definition version="v2c" read-community="splice-test">
               <range begin="10.1.2.1" end="10.1.2.100"/>
               <range begin="11.1.2.1" end="11.1.2.100"/>
               <range begin="12.1.2.1" end="12.1.2.100"/>
               <specific>10.1.1.1</specific>
               <specific>10.1.1.2</specific>
               <specific>10.1.1.3</specific>
               <specific>10.1.1.5</specific>
               <specific>10.1.1.6</specific>
               <specific>10.1.1.10</specific>
           </definition>
           <definition read-community="splice2-test">
               <range begin="10.1.1.11" end="10.1.1.100"/>
               <range begin="11.1.2.1" end="11.1.2.100"/>
               <range begin="12.1.2.1" end="12.1.2.100"/>
               <specific>10.1.1.10</specific>
           </definition>
           <definition read-community="splice3-test">
               <range begin="10.1.1.11" end="10.1.1.100"/>
               <range begin="11.1.2.1" end="11.1.2.1"/>
               <range begin="12.1.2.1" end="12.1.2.1"/>
               <specific>10.1.1.10</specific>
               <specific>10.1.1.12</specific>
           </definition>
        </snmp-config>
         */
    }
}
