/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.protocols.wmi.wbem;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

/**
 * Created by IntelliJ IDEA.
 * User: CE136452
 * Date: Oct 20, 2008
 * Time: 2:33:48 PM
 * To change this template use File | Settings | File Templates.
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

    private static final Map<Integer, OnmsWbemCimTypeEnum> lookup = new HashMap<Integer, OnmsWbemCimTypeEnum>();
    private int cimValue;
    private String cimName;

    static {
        for (OnmsWbemCimTypeEnum s : EnumSet.allOf(OnmsWbemCimTypeEnum.class))
            lookup.put(s.getCimValue(), s);
    }

    OnmsWbemCimTypeEnum(int cimValue, String cimName) {
        this.cimValue = cimValue;
        this.cimName = cimName;
    }

    public int getCimValue() { return cimValue; }
    public String getCimName() { return cimName; }

    public static OnmsWbemCimTypeEnum get(int cimValue) {
          return lookup.get(cimValue);
     }
}
