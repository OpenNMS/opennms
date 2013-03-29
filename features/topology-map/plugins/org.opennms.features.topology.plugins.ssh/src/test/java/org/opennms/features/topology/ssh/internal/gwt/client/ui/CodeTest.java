/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.topology.ssh.internal.testframework.SudoKeyDownEvent;
import org.opennms.features.topology.ssh.internal.testframework.SudoKeyPressEvent;

@Ignore("This GWT client mode code cannot be run inside a vanilla JUnit test")
public class CodeTest {
    int testCharCodeFalse = 10; // The char code for the test code where all of the options are false
    int testKeyCodeFalse = 20; // The key code for the test code where all of the options are false
    int testCharCodeTrue = 30; // The key code for the test code where all of the options are true
    int testKeyCodeTrue = 40;// The key code for the test code where all of the options are true
    int testKeyCodeControlChar = 17; // A control character (>=16 && <=18) to test the isControlKey method
    
    Code keyDownCodeAllFalse = new Code(new SudoKeyDownEvent(testKeyCodeFalse, false, false, false)); // A keyDownEvent where all of the options are false
    Code keyPressCodeAllFalse = new Code(new SudoKeyPressEvent(testCharCodeFalse, false, false, false)); // A keyPressEvent where all of the options are false
    Code keyDownCodeAllTrue = new Code(new SudoKeyDownEvent(testKeyCodeTrue, true, true, true));  // A keyDownEvent where all of the options are true
    Code keyPressCodeAllTrue = new Code(new SudoKeyPressEvent(testCharCodeTrue, true, true, true)); // A keyDownEvent where all of the options are true
    
    Code keyDownCodeControlChar = new Code(new SudoKeyDownEvent(testKeyCodeControlChar, true, true, true)); // A control Code to test the isControlKey method
    
    @Test
    public void testGetCharCode() {
        assertEquals(testCharCodeFalse, keyPressCodeAllFalse.getCharCode());
        assertEquals(testCharCodeTrue, keyPressCodeAllTrue.getCharCode());
    }
    
    @Test 
    public void testGetKeyCode() {
        assertEquals(testKeyCodeFalse, keyDownCodeAllFalse.getKeyCode());
        assertEquals(testKeyCodeTrue, keyDownCodeAllTrue.getKeyCode());
    }
    
    @Test
    public void testIsCtrlDown() {
        assertFalse(keyDownCodeAllFalse.isCtrlDown());
        assertTrue(keyDownCodeAllTrue.isCtrlDown());
    }
    
    @Test
    public void testIsAltDown() {
        assertFalse(keyDownCodeAllFalse.isAltDown());
        assertTrue(keyDownCodeAllTrue.isAltDown());
    }
    
    @Test
    public void testIsShiftDown(){
        assertFalse(keyDownCodeAllFalse.isShiftDown());
        assertTrue(keyDownCodeAllTrue.isShiftDown());
    }
    
    @Test
    public void testIsFunctionKey(){
        assertFalse(keyDownCodeAllFalse.isFunctionKey());
        assertTrue(keyDownCodeAllTrue.isFunctionKey());
    }
    
    @Test 
    public void testIsControlKey () {
        assertFalse(keyDownCodeAllFalse.isControlKey());
        assertFalse(keyDownCodeAllTrue.isControlKey());
        assertTrue(keyDownCodeControlChar.isControlKey());
    }
}
