/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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
 * Date: Oct 21, 2008
 * Time: 6:45:47 AM
 * To change this template use File | Settings | File Templates.
 */
public enum OnmsWbemFlagReturnEnum {
   
    wbemFlagForwardOnly(32, "wbemFlagForwardOnly"),
    wbemFlagBidirectional(0, "wbemFlagBidirectional"),
    wbemFlagReturnImmediately(16, "wbemFlagReturnImmediately"),
    wbemFlagReturnWhenComplete(0, "wbemFlagReturnWhenComplete"),
    wbemQueryFlagPrototype(2, "wbemQueryFlagPrototype"),
    wbemFlagUseAmendedQualifiers(131072, "wbemFlagUseAmendedQualifiers");

    private static final Map<Integer, OnmsWbemFlagReturnEnum> lookup = new HashMap<Integer, OnmsWbemFlagReturnEnum>();
    private int returnFlagValue;
    private String returnFlagName;

    static {
        for (OnmsWbemFlagReturnEnum s : EnumSet.allOf(OnmsWbemFlagReturnEnum.class))
            lookup.put(s.getReturnFlagValue(), s);
    }

    OnmsWbemFlagReturnEnum(int returnFlagValue, String returnFlagName) {
        this.returnFlagValue = returnFlagValue;
        this.returnFlagName = returnFlagName;
    }

    public int getReturnFlagValue() { return returnFlagValue; }
    public String getReturnFlagName() { return returnFlagName; }

    public static OnmsWbemFlagReturnEnum get(int returnFlagValue) {
          return lookup.get(returnFlagValue);
     }
}

