/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.correlation.ncs;

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
