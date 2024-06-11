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
