//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.protocols.wmi.wbem.jinterop;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath;

/**
 * <p>OnmsWbemObjectPathImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemObjectPathImpl implements OnmsWbemObjectPath {
    private IJIDispatch wbemObjectPathDispatch;

    /**
     * <p>Constructor for OnmsWbemObjectPathImpl.</p>
     *
     * @param wbemObjectPathDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemObjectPathImpl(IJIDispatch wbemObjectPathDispatch) {
        this.wbemObjectPathDispatch = wbemObjectPathDispatch;
    }

    /**
     * <p>getWmiAuthority</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    private String getWmiString(final String name) throws WmiException {
        try {
            return wbemObjectPathDispatch.get(name).getObjectAsString2();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath " + name + " attribute: " + e.getMessage(), e);
        }
    }

    private Boolean getWmiBoolean(final String name) throws WmiException {
        try {
            return wbemObjectPathDispatch.get(name).getObjectAsBoolean();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath " + name + " attribute: " + e.getMessage(), e);
        }
    }

    public String getWmiAuthority() throws WmiException {
        return getWmiString("Authority");
    }

    public String getWmiClass() throws WmiException {
        return getWmiString("Class");
    }

    public String getWmiDisplayName() throws WmiException {
        return getWmiString("DisplayName");
    }

    public String getWmiLocale() throws WmiException {
        return getWmiString("Locale");
    }

    /**
     * <p>getWmiNamespace</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiNamespace() throws WmiException {
        return getWmiString("Namespace");
    }

    /**
     * <p>getWmiParentNamespace</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiParentNamespace() throws WmiException {
        return getWmiString("ParentNamespace");
    }

    /**
     * <p>getWmiPath</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiPath() throws WmiException {
        return getWmiString("Path");
    }

    /**
     * <p>getWmiRelPath</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiRelPath() throws WmiException {
        return getWmiString("RelPath");
    }

    /**
     * <p>getWmiServer</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiServer() throws WmiException {
        return getWmiString("Server");
    }

    /**
     * <p>getWmiIsClass</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Boolean getWmiIsClass() throws WmiException {
        return getWmiBoolean("IsClass");
    }

    /**
     * <p>getWmiIsSingleton</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Boolean getWmiIsSingleton() throws WmiException {
        return getWmiBoolean("IsSingleton");
    }
}
