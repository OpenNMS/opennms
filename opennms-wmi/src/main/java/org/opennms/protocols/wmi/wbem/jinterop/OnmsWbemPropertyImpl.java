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
import org.opennms.protocols.wmi.WmiClient;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;

/**
 * <p>OnmsWbemPropertyImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemPropertyImpl implements OnmsWbemProperty {
    private IJIDispatch wbemPropertyDispatch;

    /**
     * <p>Constructor for OnmsWbemPropertyImpl.</p>
     *
     * @param wbemPropertyDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemPropertyImpl(final IJIDispatch wbemPropertyDispatch) {
        this.wbemPropertyDispatch = wbemPropertyDispatch;
    }

    /**
     * <p>getWmiName</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public String getWmiName() throws WmiException {
        return getWmiString("Name");
    }

    /**
     * <p>getWmiOrigin</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public String getWmiOrigin() throws WmiException {
        return getWmiString("Origin");
    }

    /**
     * <p>getWmiIsArray</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Boolean getWmiIsArray() throws WmiException {
        return getWmiBoolean("IsArray");
    }

    /**
     * <p>getWmiIsLocal</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Boolean getWmiIsLocal() throws WmiException {
        return getWmiBoolean("IsLocal");
    }

    /**
     * <p>getWmiValue</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Object getWmiValue() throws WmiException {
        try {
            return WmiClient.convertToNativeType(wbemPropertyDispatch.get("Value"));
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve or convert WbemProperty Value attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiCIMType</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public Integer getWmiCIMType() throws WmiException {
        try {
            return wbemPropertyDispatch.get("CIMType").getObjectAsInt();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemProperty CIMType attribute: " + e.getMessage(), e);
        }
    }

    private String getWmiString(final String name) throws WmiException {
        try {
            return wbemPropertyDispatch.get(name).getObjectAsString2();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemProperty " + name + " attribute: " + e.getMessage(), e);
        }
    }

    private Boolean getWmiBoolean(final String name) throws WmiException {
        try {
            return wbemPropertyDispatch.get(name).getObjectAsBoolean();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemProperty " + name + " attribute: " + e.getMessage(), e);
        }
    }

}
