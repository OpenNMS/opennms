/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
