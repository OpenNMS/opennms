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

import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.common.JIException;
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
    public String getWmiAuthority() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("Authority");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath Authority attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiClass</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiClass() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("Class");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath Class attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiDisplayName</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiDisplayName() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("DisplayName");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath DisplayName attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiLocale</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiLocale() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("Locale");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath Locale attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiNamespace</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiNamespace() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("Namespace");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath Namespace attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiParentNamespace</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiParentNamespace() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("ParentNamespace");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath ParentNamespace attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiPath</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiPath() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("Path");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath Path attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiRelPath</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiRelPath() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("RelPath");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath RelPath attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiServer</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiServer() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("Server");

            return variant.getObjectAsString2();
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath Server attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiIsClass</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Boolean getWmiIsClass() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("IsClass");

            return new Boolean(variant.getObjectAsBoolean());
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath IsClass attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiIsSingleton</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Boolean getWmiIsSingleton() throws WmiException {
        try {
            JIVariant variant = wbemObjectPathDispatch.get("IsSingleton");

            return new Boolean(variant.getObjectAsBoolean());
        } catch (JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath IsSingleton attribute: " + e.getMessage(), e);
        }
    }
}
