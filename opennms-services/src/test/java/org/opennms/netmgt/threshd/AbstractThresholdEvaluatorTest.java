/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.threshd;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

import junit.framework.TestCase;

/**
 * @author jeffg
 *
 */
public abstract class AbstractThresholdEvaluatorTest extends TestCase {
    protected static void parmPresentAndValueNonNull(Event event, String parmName) {
        boolean parmPresent = false;
        
        for (Parm parm : event.getParms().getParmCollection()) {
            if (parmName.equals(parm.getParmName())) {
                assertNotNull("Value content of parm '" + parmName + "'", parm.getValue().getContent());
                parmPresent = true;
            }
        }
        assertTrue("Parm '" + parmName + "' present", parmPresent);
    }
    
    protected static void parmPresentWithValue(Event event, String parmName, String expectedValue) {
        boolean parmPresent = false;
        
        for (Parm parm : event.getParms().getParmCollection()) {
            if (parmName.equals(parm.getParmName())) {
                parmPresent = true;
                if (expectedValue.equals(parm.getValue().getContent())) {
                    assertNotNull("Value content of parm '" + parmName + "'", parm.getValue().getContent());
                    assertEquals("Value content of parm '" + parmName + "' should be '" + expectedValue + "'", expectedValue, parm.getValue().getContent());
                    parmPresent = true;
                }
            }
        }
        assertTrue("Parm '" + parmName + "' present", parmPresent);
    }
}
