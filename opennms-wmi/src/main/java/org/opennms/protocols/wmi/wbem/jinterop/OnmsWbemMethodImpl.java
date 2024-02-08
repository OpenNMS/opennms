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
