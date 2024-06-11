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
 * <p>OnmsWbemMethodSet interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface OnmsWbemMethodSet {
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public Integer getCount() throws WmiException;

    /**
     * <p>get</p>
     *
     * @param idx a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.protocols.wmi.wbem.OnmsWbemMethod} object.
     * @throws org.opennms.protocols.wmi.WmiException if any.
     */
    public OnmsWbemMethod get(Integer idx) throws WmiException;
}
