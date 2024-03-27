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

import org.opennms.protocols.wmi.WmiException;

/**
 * <p>OnmsWbemObjectPath interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface OnmsWbemObjectPath {

    /**
     * <p>getWmiAuthority</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiAuthority() throws WmiException;

    /**
     * <p>getWmiClass</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiClass() throws WmiException;

    /**
     * <p>getWmiDisplayName</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiDisplayName() throws WmiException;

    /**
     * <p>getWmiLocale</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiLocale() throws WmiException;

    /**
     * <p>getWmiNamespace</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiNamespace() throws WmiException;

    /**
     * <p>getWmiParentNamespace</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiParentNamespace() throws WmiException;

    /**
     * <p>getWmiPath</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiPath() throws WmiException;

    /**
     * <p>getWmiRelPath</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiRelPath() throws WmiException;

    /**
     * <p>getWmiServer</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public String getWmiServer() throws WmiException;

    /**
     * <p>getWmiIsClass</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Boolean getWmiIsClass() throws WmiException;

    /**
     * <p>getWmiIsSingleton</p>
     *
     * @return a {@link java.lang.Boolean} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Boolean getWmiIsSingleton() throws WmiException;
}
