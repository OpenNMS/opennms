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
package org.opennms.protocols.wmi.wbem;

import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.common.JIException;
import org.opennms.protocols.wmi.WmiException;

import java.util.List;
import java.util.ArrayList;

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
    public OnmsWbemObject wmiExecMethod(String methodName, List params, List namedValueSet);

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
