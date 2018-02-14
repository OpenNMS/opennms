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
