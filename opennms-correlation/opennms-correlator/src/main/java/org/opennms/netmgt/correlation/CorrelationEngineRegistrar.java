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
package org.opennms.netmgt.correlation;

import java.util.Collection;

/**
 * <p>CorrelationEngineRegistrar interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface CorrelationEngineRegistrar {

    /**
     * <p>addCorrelationEngine</p>
     *
     * @param engine a {@link org.opennms.netmgt.correlation.CorrelationEngine} object.
     */
    void addCorrelationEngine(CorrelationEngine engine);
    
    /**
     * <p>addCorrelationEngine</p>
     *
     * @param engine a {@link org.opennms.netmgt.correlation.CorrelationEngine} object.
     */
    void addCorrelationEngines(CorrelationEngine... engines);
    
    /**
     * <p>getEngines</p>
     *
     * @return a {@link java.util.List} object.
     */
    Collection<CorrelationEngine> getEngines();
    
    /**
     * <p>findEngineByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.correlation.CorrelationEngine} object.
     */
    CorrelationEngine findEngineByName(String name);

    void removeCorrelationEngine(String name);
}
