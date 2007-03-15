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
