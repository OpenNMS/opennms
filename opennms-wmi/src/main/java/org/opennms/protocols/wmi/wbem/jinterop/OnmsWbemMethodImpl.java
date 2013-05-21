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
import org.opennms.protocols.wmi.wbem.OnmsWbemMethod;

/**
 * <p>OnmsWbemMethodImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemMethodImpl implements OnmsWbemMethod {
        private IJIDispatch wbemMethodDispatch;

    /**
     * <p>Constructor for OnmsWbemMethodImpl.</p>
     *
     * @param wbemMethodDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemMethodImpl(IJIDispatch wbemMethodDispatch) {
        this.wbemMethodDispatch = wbemMethodDispatch;
    }

    /**
     * <p>getWmiName</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
        @Override
    public String getWmiName()throws WmiException {
        try {
            return wbemMethodDispatch.get("Name").getObjectAsString2();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemMethod Name attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiOrigin</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
        @Override
    public String getWmiOrigin() throws WmiException {
        try {
            return wbemMethodDispatch.get("Origin").getObjectAsString2();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemMethod Origin attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiOutParameters</p>
     */
        @Override
    public void getWmiOutParameters() {
        return; // TODO IMPLEEMNT THIS
    }

    /**
     * <p>getWmiInParameters</p>
     */
        @Override
    public void getWmiInParameters() {
        return; // TODO IMPLEEMNT THIS
    }
}
