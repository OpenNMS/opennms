/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.config.threshd.Threshold;

/**
 * <p>ThresholdConfigWrapper class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ThresholdConfigWrapper extends BaseThresholdDefConfigWrapper {

    private Threshold m_threshold;
    private Collection<String> m_dataSources;
    
    /**
     * <p>Constructor for ThresholdConfigWrapper.</p>
     *
     * @param threshold a {@link org.opennms.netmgt.config.threshd.Threshold} object.
     */
    public ThresholdConfigWrapper(Threshold threshold) {
        super(threshold);
        m_threshold=threshold;
        m_dataSources=new ArrayList<String>(1);
        m_dataSources.add(m_threshold.getDsName() == null ? null : m_threshold.getDsName().intern());
    }

    /** {@inheritDoc} */
    @Override
    public String getDatasourceExpression() {
        return m_threshold.getDsName();
        
    }

    /** {@inheritDoc} */
    @Override
    public Collection<String> getRequiredDatasources() {
        return m_dataSources;
    }

    /** {@inheritDoc} */
    @Override
    public double evaluate(Map<String, Double> values)  throws ThresholdExpressionException {
        Double result=values.get(m_threshold.getDsName());
        if(result==null) {
            return 0.0;
        }
        return result.doubleValue();
    }
}
