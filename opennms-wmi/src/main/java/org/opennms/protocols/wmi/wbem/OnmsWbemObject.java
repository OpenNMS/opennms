/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.wmi.wbem;

import java.util.List;

import org.opennms.protocols.wmi.WmiException;

/**
 * <p>OnmsWbemObject interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface OnmsWbemObject {
    /**
     * Execute a named method on this WMI object.
     *
     * @param methodName The name of the method to execute on the remote side.
     * @param params The list of parameters for this method.
     * @param namedValueSet A list of parameter values.
     * @return The result of the method execution.
     */
    public OnmsWbemObject wmiExecMethod(String methodName, List<?> params, List<?> namedValueSet);

    /**
     * Returns a list of instances of this object (if it is a WMI class.)
     *
     * @return a list of instance names.
     */
    public List<String> wmiInstances();

    /**
     * Create or update a WMI object.
     *
     * @return the object path to the created/updated WMI object.
     */
    public String wmiPut();

    /**
     * Gets a list of the available methods on this object.
     *
     * @return a list of available methods on this object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemMethodSet getWmiMethods() throws WmiException;

     /**
      * <p>getWmiPath</p>
      *
      * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath} object.
      * @throws org.opennms.protocols.wmi.WmiException if any.
      */
     public OnmsWbemObjectPath getWmiPath() throws WmiException;

    /**
     * <p>getWmiObjectText</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiObjectText() throws WmiException;

    /**
     * <p>getWmiProperties</p>
     *
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemPropertySet getWmiProperties() throws WmiException;
}
