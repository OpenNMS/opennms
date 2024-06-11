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
package org.opennms.protocols.wmi;

import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;

/**
 * <p>IWmiClient interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface IWmiClient {

    /**
     * <p>performExecQuery</p>
     *
     * @param strQuery a {@link java.lang.String} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObjectSet performExecQuery(String strQuery) throws WmiException;

    /**
     * <p>performExecQuery</p>
     *
     * @param strQuery a {@link java.lang.String} object.
     * @param strQueryLanguage a {@link java.lang.String} object.
     * @param flags a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObjectSet performExecQuery(String strQuery,String strQueryLanguage,Integer flags) throws WmiException;

    /**
     * <p>performInstanceOf</p>
     *
     * @param wmiClass a {@link java.lang.String} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemObjectSet performInstanceOf(String wmiClass) throws WmiException;

    /**
     * <p>connect</p>
     *
     * @param domain a {@link java.lang.String} object.
     * @param username a {@link java.lang.String} object.
     * @param password a {@link java.lang.String} object.
     * @param namespace a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public void connect(String domain, String username, String password, String namespace) throws WmiException;
	
	/**
	 * <p>disconnect</p>
	 *
	 * @throws org.opennms.protocols.wmi.WmiException if any.
	 */
    public void disconnect() throws WmiException;

}

