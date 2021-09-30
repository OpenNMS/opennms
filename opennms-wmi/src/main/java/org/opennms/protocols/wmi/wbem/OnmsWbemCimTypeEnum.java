/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.wmi.wbem;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: CE136452
 * Date: Oct 20, 2008
 * Time: 2:33:48 PM
 * To change this template use File | Settings | File Templates.
 *
 * @author ranger
 * @version $Id: $
 */
public enum OnmsWbemCimTypeEnum {
    wbemCimtypeSint16(2, "wbemCimtypeSint16"),
    wbemCimtypeSint32(3, "wbemCimtypeSint32"),
    wbemCimtypeReal32(4, "wbemCimtypeReal32"),
    wbemCimtypeReal64(5, "wbemCimtypeReal64"),
    wbemCimtypeString(8, "wbemCimtypeString"),
    wbemCimtypeBoolean(11, "wbemCimtypeBoolean"),
    wbemCimtypeObject(13, "wbemCimtypeObject"),
    wbemCimtypeSint8(16, "wbemCimtypeSint8"),
    wbemCimtypeUint8(17, "wbemCimtypeUint8"),
    wbemCimtypeUint16(18, "wbemCimtypeUint16"),
    wbemCimtypeUint32(19, "wbemCimtypeUint32"),
    wbemCimtypeSint64(20, "wbemCimtypeSint64"),
    wbemCimtypeUint64(21, "wbemCimtypeUint64"),
    wbemCimtypeDatetime(101, "wbemCimtypeDatetime"),
    wbemCimtypeReference(102, "wbemCimtypeReference"),
    wbemCimtypeChar16(103, "wbemCimtypeChar16");

    /** Constant <code>lookup</code> */
    private static final Map<Integer, OnmsWbemCimTypeEnum> lookup = new HashMap<Integer, OnmsWbemCimTypeEnum>();
    private int cimValue;
    private String cimName;

    static {
        for (final OnmsWbemCimTypeEnum s : EnumSet.allOf(OnmsWbemCimTypeEnum.class))
            lookup.put(s.getCimValue(), s);
    }

    OnmsWbemCimTypeEnum(final int cimValue, final String cimName) {
        this.cimValue = cimValue;
        this.cimName = cimName;
    }

    /**
     * <p>Getter for the field <code>cimValue</code>.</p>
     *
     * @return a int.
     */
    public int getCimValue() { return cimValue; }
    /**
     * <p>Getter for the field <code>cimName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCimName() { return cimName; }

    /**
     * <p>get</p>
     *
     * @param cimValue a int.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemCimTypeEnum} object.
     */
    public static OnmsWbemCimTypeEnum get(final int cimValue) {
          return lookup.get(cimValue);
     }
}
