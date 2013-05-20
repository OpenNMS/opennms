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

    @Override
    public String getWmiAuthority() throws WmiException {
        return getWmiString("Authority");
    }

    @Override
    public String getWmiClass() throws WmiException {
        return getWmiString("Class");
    }

    @Override
    public String getWmiDisplayName() throws WmiException {
        return getWmiString("DisplayName");
    }

    @Override
    public String getWmiLocale() throws WmiException {
        return getWmiString("Locale");
    }

    /**
     * <p>getWmiNamespace</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public String getWmiNamespace() throws WmiException {
        return getWmiString("Namespace");
    }

    /**
     * <p>getWmiParentNamespace</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public String getWmiParentNamespace() throws WmiException {
        return getWmiString("ParentNamespace");
    }

    /**
     * <p>getWmiPath</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public String getWmiPath() throws WmiException {
        return getWmiString("Path");
    }

    /**
     * <p>getWmiRelPath</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public String getWmiRelPath() throws WmiException {
        return getWmiString("RelPath");
    }

    /**
     * <p>getWmiServer</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public String getWmiServer() throws WmiException {
        return getWmiString("Server");
    }

    /**
     * <p>getWmiIsClass</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Boolean getWmiIsClass() throws WmiException {
        return getWmiBoolean("IsClass");
    }

    /**
     * <p>getWmiIsSingleton</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Boolean getWmiIsSingleton() throws WmiException {
        return getWmiBoolean("IsSingleton");
    }
}
