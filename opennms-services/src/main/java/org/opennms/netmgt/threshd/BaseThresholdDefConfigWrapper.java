/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.threshd;

import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.Threshold;

/**
 * <p>Abstract BaseThresholdDefConfigWrapper class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @version $Id: $
 */
public abstract class BaseThresholdDefConfigWrapper {
    Basethresholddef m_baseDef;
    
    /**
     * <p>Constructor for BaseThresholdDefConfigWrapper.</p>
     *
     * @param baseDef a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     */
    protected BaseThresholdDefConfigWrapper(Basethresholddef baseDef) {
        m_baseDef=baseDef;
    }
    
    /**
     * <p>getConfigWrapper</p>
     *
     * @param baseDef a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     * @return a {@link org.opennms.netmgt.threshd.BaseThresholdDefConfigWrapper} object.
     * @throws org.opennms.netmgt.threshd.ThresholdExpressionException if any.
     */
    public static BaseThresholdDefConfigWrapper getConfigWrapper(Basethresholddef baseDef) throws ThresholdExpressionException {
        if(baseDef instanceof Threshold) {
            return new ThresholdConfigWrapper((Threshold)baseDef);
        } else if(baseDef instanceof Expression) {
            return new ExpressionConfigWrapper((Expression)baseDef);
        }
        return null;
    }
    
    /**
     * <p>getDatasourceExpression</p>
     *
     * @return a descriptive string for the data source - typically either a data source name, or an expression of data source names
     */
    public abstract String getDatasourceExpression();
    
    /**
     * Returns the names of the datasources required to evaluate this threshold
     *
     * @return Collection of the names of datasources
     */
    public abstract Collection<String> getRequiredDatasources();
    
    /**
     * Evaluate the threshold expression/datasource in terms of the named values supplied, and return that value
     *
     * @param values named values to use in evaluating the expression/data source
     * @return the value of the evaluated expression
     * @throws org.opennms.netmgt.threshd.ThresholdExpressionException if any.
     */
    public abstract double evaluate(Map<String, Double> values)  throws ThresholdExpressionException;
    
    /**
     * <p>getDsType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDsType() {
        return m_baseDef.getDsType();
    }
    
    /**
     * <p>getDsLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDsLabel() {
        return m_baseDef.getDsLabel();
    }
    
    /**
     * <p>getRearm</p>
     *
     * @return a double.
     */
    public double getRearm() {
        return m_baseDef.getRearm();
    }
    
    /**
     * <p>getTrigger</p>
     *
     * @return a int.
     */
    public int getTrigger() {
        return m_baseDef.getTrigger();
    }
    
    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_baseDef.getType();
    }
    
    /**
     * <p>getValue</p>
     *
     * @return a double.
     */
    public double getValue() {
        return m_baseDef.getValue();
    }
    
    /**
     * <p>hasRearm</p>
     *
     * @return a boolean.
     */
    public boolean hasRearm() {
        return m_baseDef.hasRearm();
    }
    
    /**
     * <p>hasTrigger</p>
     *
     * @return a boolean.
     */
    public boolean hasTrigger() {
        return m_baseDef.hasTrigger();
    }
    
    /**
     * <p>hasValue</p>
     *
     * @return a boolean.
     */
    public boolean hasValue() {
        return m_baseDef.hasValue();
    }
    
    /**
     * <p>getTriggeredUEI</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTriggeredUEI() {
        return m_baseDef.getTriggeredUEI();
    }
    
    /**
     * <p>getRearmedUEI</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRearmedUEI() {
        return m_baseDef.getRearmedUEI();
    }
    
    /**
     * <p>getBasethresholddef</p>
     *
     * @return a {@link org.opennms.netmgt.config.threshd.Basethresholddef} object.
     */
    public Basethresholddef getBasethresholddef() {
        return m_baseDef;
    }
}

