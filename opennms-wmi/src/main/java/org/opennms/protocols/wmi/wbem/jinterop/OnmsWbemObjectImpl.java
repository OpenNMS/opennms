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

import java.util.List;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.impls.JIObjectFactory;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath;
import org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet;

/**
 * <p>OnmsWbemObjectImpl class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsWbemObjectImpl implements OnmsWbemObject {
    private IJIDispatch wbemObjectDispatch;

    /**
     * <p>Constructor for OnmsWbemObjectImpl.</p>
     *
     * @param wbemObjectDispatch a {@link org.jinterop.dcom.impls.automation.IJIDispatch} object.
     */
    public OnmsWbemObjectImpl(final IJIDispatch wbemObjectDispatch) {
        this.wbemObjectDispatch = wbemObjectDispatch;
    }

    /** {@inheritDoc} */
    @Override
    public OnmsWbemObjectImpl wmiExecMethod(String methodName, List<?> params, List<?> namedValueSet) {
        return null; // TODO IMPLEMENT THIS METHOD
    }

    /**
     * <p>wmiInstances</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<String> wmiInstances() {
        return null; // TODO IMPLEMENT THIS METHOD
    }

    /**
     * <p>wmiPut</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String wmiPut() {
        return ""; // TODO IMPLEMENT THIS METHOD
    }

    /**
     * <p>getWmiMethods</p>
     *
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemMethodSet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public OnmsWbemMethodSet getWmiMethods() throws WmiException {
        try {
            // Get the WbemMethodSet dispatcher.
            final IJIComObject methodComObject = wbemObjectDispatch.get("Methods_").getObjectAsComObject();
            final IJIDispatch methodsSet_dispatch = (IJIDispatch) JIObjectFactory.narrowObject(methodComObject);

            return new OnmsWbemMethodSetImpl(methodsSet_dispatch);
        } catch (final JIException e) {
            throw new WmiException("Failed to retrieve list of methods: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiPath</p>
     *
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemObjectPath} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public OnmsWbemObjectPath getWmiPath() throws WmiException {
        try {
            // Get the WbemMethodSet dispatcher.
            final IJIComObject pathComObject = wbemObjectDispatch.get("Path_").getObjectAsComObject();
            final IJIDispatch path_dispatch = (IJIDispatch) JIObjectFactory.narrowObject(pathComObject);

            return new OnmsWbemObjectPathImpl(path_dispatch);
        } catch (final JIException e) {
            throw new WmiException("Failed to retrieve object path: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiObjectText</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public String getWmiObjectText() throws WmiException {
        try {
            return (wbemObjectDispatch.callMethodA("GetObjectText_", new Object[]{1}))[0].getObjectAsString2();
        } catch (final JIException e) {
            throw new WmiException("Unable to retrieve WbemObjectPath GetObjectText_ attribute: " + e.getMessage(), e);
        }
    }

    /**
     * <p>getWmiProperties</p>
     *
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemPropertySet} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    @Override
    public OnmsWbemPropertySet getWmiProperties() throws WmiException {
        try {
            // Get the WbemMethodSet dispatcher.
            final IJIComObject propsSetComObject = wbemObjectDispatch.get("Properties_").getObjectAsComObject();
            final IJIDispatch propSet_dispatch = (IJIDispatch) JIObjectFactory.narrowObject(propsSetComObject);

            return new OnmsWbemPropertySetImpl(propSet_dispatch);
        } catch (final JIException e) {
            throw new WmiException("Failed to retrieve object property set: " + e.getMessage(), e);
        }
    }

}
