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

package org.opennms.core.utils;

/**
 * This is a data class to store the argument information for a Command
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 */
public class Argument {
    /**
     * The switch that the Notify class identifies with the value of this
     * argument
     */
    private String m_switch;

    /**
     * The substitution switch that should be used when using this argument with
     * the console command.
     */
    private String m_substitution;

    /**
     * The value of the argument
     */
    private String m_value;

    /**
     * A boolean indicating if this argument should be sent to the console
     * command via an input stream.
     */
    private boolean m_streamed;

    /**
     * Default constructor, initializes the members
     *
     * @param aSwitch a {@link java.lang.String} object.
     * @param sub a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     * @param streamed a boolean.
     */
    public Argument(String aSwitch, String sub, String value, boolean streamed) {
        m_switch = aSwitch;
        m_substitution = sub;
        m_value = value;
        m_streamed = streamed;
    }

    /**
     * Sets the switch member
     *
     * @param aValue
     *            the value of the switch
     */
    public void setSwitch(String aValue) {
        m_switch = aValue;
    }

    /**
     * Sets the substitution switch
     *
     * @param aValue
     *            the value of the substitution
     */
    public void setSubstitution(String aValue) {
        m_substitution = aValue;
    }

    /**
     * Sets the value of the argument
     *
     * @param aValue
     *            the value of the argument
     */
    public void setValue(String aValue) {
        m_value = aValue;
    }

    /**
     * Returns the switch
     *
     * @return String, the switch string
     */
    public String getSwitch() {
        return m_switch;
    }

    /**
     * Returns the substitution switch
     *
     * @return String, the substitution
     */
    public String getSubstitution() {
        return m_substitution;
    }

    /**
     * Returns the value of the argument
     *
     * @return String, the value of the argument
     */
    public String getValue() {
        return m_value;
    }

    /**
     * Sets the boolean indicating if this argument should be sent to an input
     * stream
     *
     * @param aBool
     *            true if the argument should be sent to a input stream, false
     *            otherwise
     */
    public void setStreamed(boolean aBool) {
        m_streamed = aBool;
    }

    /**
     * Returns the boolean indicating if this argument should be sent to an
     * input stream.
     *
     * @return true if the argument should be sent to a input stream, false
     *         otherwise
     */
    public boolean isStreamed() {
        return m_streamed;
    }

    /**
     * Returns a string representation of the argument, for purposes of
     * debugging
     *
     * @return String, a string representation
     */
    public String toString() {
        return m_switch + "(" + m_substitution + ") " + m_value;
    }
}
