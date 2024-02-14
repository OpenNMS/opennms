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
package org.opennms.netmgt.correlation.drools;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.correlation.CorrelationEngine;
import org.opennms.netmgt.correlation.CorrelationEngineRegistrar;

public class MockCorrelator implements CorrelationEngineRegistrar {
    
    private final Map<String,CorrelationEngine> m_engines = new HashMap<>();
    
    @Override
    public void addCorrelationEngine(CorrelationEngine engine) {
        m_engines.put(engine.getName(), engine);
    }
    
    @Override
    public void addCorrelationEngines(CorrelationEngine... engines) {
        Arrays.stream(engines).forEach(engine -> m_engines.put(engine.getName(), engine));
    }
    
    @Override
    public CorrelationEngine findEngineByName(String name) {
        return m_engines.get(name);
    }
    
    @Override
    public Collection<CorrelationEngine> getEngines() {
        return m_engines.values();
    }

    @Override
    public void removeCorrelationEngine(String name) {
        m_engines.remove(name);
    }
}
