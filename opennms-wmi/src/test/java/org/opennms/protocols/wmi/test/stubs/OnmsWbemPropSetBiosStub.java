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

package org.opennms.protocols.wmi.test.stubs;

import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.WmiException;

public class OnmsWbemPropSetBiosStub implements OnmsWbemPropertySet {
    public OnmsWbemProperty releaseDate;
    public OnmsWbemPropSetBiosStub(OnmsWbemProperty prop) {
        releaseDate = prop;
    }
    public Integer count() throws WmiException {
        return null;
    }

    public OnmsWbemProperty get(Integer idx) throws WmiException {
        return null;
    }

    public OnmsWbemProperty getByName(String name) throws WmiException {      
        if(name.equals("ReleaseDate")) return releaseDate;
        throw new WmiException("Failed to perform WMI operation: Unknown name. [0x80020006]");
    }
}
