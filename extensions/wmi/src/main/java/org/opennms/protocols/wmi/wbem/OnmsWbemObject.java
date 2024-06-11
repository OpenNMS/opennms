/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
