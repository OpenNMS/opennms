/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StatusType implements Comparable<StatusType>, Serializable {
    private static final long serialVersionUID = -4784344871599250528L;
    private static final char[] s_order = {'A', 'N', 'D', 'U', 'B', 'G'};
    private char m_statusType;

    private static final Map<Character, String> statusMap = new HashMap<>();

    static {
        statusMap.put('A', "Active");
        statusMap.put('U', "Unknown");
        statusMap.put('D', "Deleted");
        statusMap.put('N', "Not Active");
        statusMap.put('B', "Bad");
        statusMap.put('G', "Good");
    }

    @SuppressWarnings("unused")
    private StatusType() {
    }

    public StatusType(char statusType) {
        m_statusType = statusType;
    }

    public char getCharCode() {
        return m_statusType;
    }

    public void setCharCode(char statusType) {
        m_statusType = statusType;
    }

    @Override
    public int compareTo(StatusType o) {
        return getIndex(m_statusType) - getIndex(o.m_statusType);
    }

    private static int getIndex(char code) {
        for (int i = 0; i < s_order.length; i++) {
            if (s_order[i] == code) {
                return i;
            }
        }
        throw new IllegalArgumentException("illegal statusType code '" + code + "'");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StatusType) {
            return m_statusType == ((StatusType) o).m_statusType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(m_statusType);
    }

    public static StatusType get(char code) {
        switch (code) {
            case 'A':
                return ACTIVE;
            case 'N':
                return INACTIVE;
            case 'D':
                return DELETED;
            case 'U':
                return UNKNOWN;
            case 'B':
                return BAD;
            case 'G':
                return GOOD;
            default:
                throw new IllegalArgumentException("Cannot create statusType from code " + code);
        }
    }

    /**
     * <p>getStatusString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    /**
     */
    public static String getStatusString(char code) {
        if (statusMap.containsKey(code))
            return statusMap.get(code);
        return null;
    }

    public static StatusType get(String code) {
        if (code == null)
            return UNKNOWN;
        code = code.trim();
        if (code.length() < 1)
            return UNKNOWN;
        else if (code.length() > 1)
            throw new IllegalArgumentException("Cannot convert string " + code + " to a StatusType");
        else
            return get(code.charAt(0));
    }

    public static final StatusType ACTIVE = new StatusType('A');
    public static final StatusType INACTIVE = new StatusType('N');
    public static final StatusType DELETED = new StatusType('D');
    public static final StatusType UNKNOWN = new StatusType('U');
    public static final StatusType BAD = new StatusType('B');
    public static final StatusType GOOD = new StatusType('G');


}
