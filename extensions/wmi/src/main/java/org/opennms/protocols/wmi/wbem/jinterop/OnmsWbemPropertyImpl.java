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
