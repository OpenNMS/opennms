/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

/**
 * This is a data class to store the argument information for a Command
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
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
    @Override
    public String toString() {
        return m_switch + "(" + m_substitution + ") " + m_value;
    }
}
