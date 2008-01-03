package org.opennms.netmgt.threshd;

import java.util.Collection;
import java.util.Map;

import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.Threshold;

public abstract class BaseThresholdDefConfigWrapper {
    Basethresholddef m_baseDef;
    
    protected BaseThresholdDefConfigWrapper(Basethresholddef baseDef) {
        m_baseDef=baseDef;
    }
    
    public static BaseThresholdDefConfigWrapper getConfigWrapper(Basethresholddef baseDef) throws ThresholdExpressionException {
        if(baseDef instanceof Threshold) {
            return new ThresholdConfigWrapper((Threshold)baseDef);
        } else if(baseDef instanceof Expression) {
            return new ExpressionConfigWrapper((Expression)baseDef);
        }
        return null;
    }
    
    /**
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
     */
    public abstract double evaluate(Map<String, Double> values)  throws ThresholdExpressionException;
    
    public String getDsType() {
        return m_baseDef.getDsType();
    }
    
    public String getDsLabel() {
        return m_baseDef.getDsLabel();
    }
    
    public double getRearm() {
        return m_baseDef.getRearm();
    }
    
    public int getTrigger() {
        return m_baseDef.getTrigger();
    }
    
    public String getType() {
        return m_baseDef.getType();
    }
    
    public double getValue() {
        return m_baseDef.getValue();
    }
    
    public boolean hasRearm() {
        return m_baseDef.hasRearm();
    }
    
    public boolean hasTrigger() {
        return m_baseDef.hasTrigger();
    }
    
    public boolean hasValue() {
        return m_baseDef.hasValue();
    }
    
    public String getTriggeredUEI() {
        return m_baseDef.getTriggeredUEI();
    }
    
    public String getRearmedUEI() {
        return m_baseDef.getRearmedUEI();
    }
    
    public Basethresholddef getBasethresholddef() {
        return m_baseDef;
    }
}

