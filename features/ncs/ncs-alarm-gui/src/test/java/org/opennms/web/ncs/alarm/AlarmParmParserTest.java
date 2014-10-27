/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.ncs.alarm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AlarmParmParserTest {

    @Test
    public void testParseParms() {
        String parms = "url=http://localhost:8980/opennms/rtc/post/Network+Interfaces(string,text);user=rtc(string,text);passwd=rtc(string,text);catlabel=Network Interfaces(string,text)";
        
        assertEquals("http://localhost:8980/opennms/rtc/post/Network+Interfaces", getParm(parms, "url"));
    }

    private String getParm(String eventParms, String parm) {
        String retVal = null;
        if(eventParms.contains(parm + "=")){
            String[] colonSplit = eventParms.split(";");
            for(int i = 0; i < colonSplit.length; i++) {
                if(colonSplit[i].contains(parm + "=")) {
                    String[] tempArr = colonSplit[i].split("=");
                    retVal = tempArr[tempArr.length - 1].replace("(string,text)", "");
                }
            }
            return retVal;
        }else {
            return null;
        }
        
    }

}
