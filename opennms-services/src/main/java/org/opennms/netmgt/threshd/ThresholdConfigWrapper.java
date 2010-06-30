/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 14, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
        m_dataSources.add(m_threshold.getDsName());
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
