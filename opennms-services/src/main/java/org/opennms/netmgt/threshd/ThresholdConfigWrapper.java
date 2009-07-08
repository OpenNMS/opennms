/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.config.threshd.Threshold;

public class ThresholdConfigWrapper extends BaseThresholdDefConfigWrapper {

    private Threshold m_threshold;
    private Collection<String> m_dataSources;
    
    public ThresholdConfigWrapper(Threshold threshold) {
        super(threshold);
        m_threshold=threshold;
        m_dataSources=new ArrayList<String>(1);
        m_dataSources.add(m_threshold.getDsName());
    }

    @Override
    public String getDatasourceExpression() {
        return m_threshold.getDsName();
        
    }

    @Override
    public Collection<String> getRequiredDatasources() {
        return m_dataSources;
    }

    @Override
    public double evaluate(Map<String, Double> values)  throws ThresholdExpressionException {
        Double result=values.get(m_threshold.getDsName());
        if(result==null) {
            return 0.0;
        }
        return result.doubleValue();
    }
}
