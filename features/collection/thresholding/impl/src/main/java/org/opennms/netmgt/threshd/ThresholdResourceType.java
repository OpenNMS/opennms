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
package org.opennms.netmgt.threshd;

import java.util.Map;
import java.util.Set;

/**
 * <p>ThresholdResourceType class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class ThresholdResourceType {
    
    private final String m_dsType;

    private Map<String, Set<ThresholdEntity>> m_thresholdMap;
    
    /**
     * <p>Constructor for ThresholdResourceType.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public ThresholdResourceType(final String type) {
        m_dsType = type.intern();
    }

    /**
     * <p>getDsType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDsType() {
        return m_dsType;
    }
    
    /**
     * <p>getThresholdMap</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Set<ThresholdEntity>> getThresholdMap() {
        return m_thresholdMap;
    }
    
    /**
     * <p>setThresholdMap</p>
     *
     * @param thresholdMap a {@link java.util.Map} object.
     */
    public void setThresholdMap(Map<String, Set<ThresholdEntity>> thresholdMap) {
    	m_thresholdMap = thresholdMap;
    }


}
