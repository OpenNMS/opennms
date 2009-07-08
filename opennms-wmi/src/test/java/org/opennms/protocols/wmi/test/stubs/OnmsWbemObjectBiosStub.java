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

package org.opennms.protocols.wmi.test.stubs;

import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.WmiException;

import java.util.List;

public class OnmsWbemObjectBiosStub implements OnmsWbemObject {
    public OnmsWbemPropertySet props;
    public OnmsWbemObjectBiosStub(OnmsWbemPropertySet propset) {
        props = propset;
    }

    public OnmsWbemObject wmiExecMethod(String methodName, List params, List namedValueSet) {
        return null;
    }

    public List<String> wmiInstances() {
        return null;
    }

    public String wmiPut() {
        return null;
    }

    public OnmsWbemMethodSet getWmiMethods() throws WmiException {
        return null;
    }

    public OnmsWbemObjectPath getWmiPath() throws WmiException {
        return null;
    }

    public String getWmiObjectText() throws WmiException {
        return null;
    }

    public OnmsWbemPropertySet getWmiProperties() throws WmiException {
        return props;
    }
}
