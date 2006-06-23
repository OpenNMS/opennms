//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.eventconf.bobject;

/**
 * This is a data class for storing snmp event configuration information as
 * parsed from the eventconf.xml file
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 * @deprecated Replaced by a Castor-generated implementation.
 * 
 * @see org.opennms.netmgt.xml.eventconf.Autoaction
 * 
 */
public class AutoAction implements Cloneable {
    /**
     * A list of values for autoaction states. If this array changes please
     * update any of the AUTO_ACTION_STATE_DEFAULT_INDEX members if needed.
     */
    public static final String AUTO_ACTION_STATES[] = { "on", "off" };

    /**
     * The index into the AUTO_ACTION_STATES array indicating the default state
     * of an auto action. If the values array changes please update this index
     * if needed.
     */
    public static final int AUTO_ACTION_STATE_DEFAULT_INDEX = 0;

    /**
     */
    private String m_autoAction;

    /**
     */
    private String m_state;

    /**
     * Default constructor, intializes the member variables.
     */
    public AutoAction() {
        m_state = AUTO_ACTION_STATES[AUTO_ACTION_STATE_DEFAULT_INDEX];
    }

    /**
     */
    public Object clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }

        AutoAction newAction = new AutoAction();

        newAction.setAutoAction(m_autoAction);
        newAction.setState(m_state);

        return newAction;
    }

    /**
     */
    public void setAutoAction(String action) {
        m_autoAction = action;
    }

    /**
     */
    public String getAutoAction() {
        return m_autoAction;
    }

    /**
     */
    public void setState(String state) {
        /*
         * if (index < 0 || index > AUTO_ACTION_STATE_VALUES.length) throw new
         * InvalidParameterException("The auto action state index("+index+")
         * must be >= 0 and <= " + CORRELATION_STATE_VALUES.length);
         */
        m_state = state;
    }

    /**
     */
    public String getState() {
        return m_state;
    }
}
