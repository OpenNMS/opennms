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
package org.opennms.core.soa;

/**
 * RegistrationListener
 *
 * @author brozow
 * @version $Id: $
 */
public interface RegistrationListener<T> {
    
    /**
     * <p>providerRegistered</p>
     *
     * @param registration a {@link org.opennms.core.soa.Registration} object.
     * @param provider a T object.
     * @param <T> a T object.
     */
    public void providerRegistered(Registration registration, T provider);
    
    /**
     * <p>providerUnregistered</p>
     *
     * @param registration a {@link org.opennms.core.soa.Registration} object.
     * @param provider a T object.
     */
    public void providerUnregistered(Registration registration, T provider);

}
