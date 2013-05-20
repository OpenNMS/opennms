/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.wmi.test.stubs;

import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.opennms.protocols.wmi.WmiException;

public class OnmsWbemPropSetBiosStub implements OnmsWbemPropertySet {
    public OnmsWbemProperty releaseDate;
    public OnmsWbemPropSetBiosStub(OnmsWbemProperty prop) {
        releaseDate = prop;
    }
    @Override
    public Integer count() throws WmiException {
        return null;
    }

    @Override
    public OnmsWbemProperty get(Integer idx) throws WmiException {
        return null;
    }

    @Override
    public OnmsWbemProperty getByName(String name) throws WmiException {      
        if(name.equals("ReleaseDate")) return releaseDate;
        throw new WmiException("Failed to perform WMI operation: Unknown name. [0x80020006]");
    }
}
