package org.opennms.netmgt.threshd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.nfunk.jep.JEP;
import org.opennms.netmgt.config.threshd.Expression;

public class ExpressionConfigWrapper extends BaseThresholdDefConfigWrapper {

    private Expression m_expression;
    private Collection<String> m_datasources;
    private JEP m_parser;
    public ExpressionConfigWrapper(Expression expression) throws ThresholdExpressionException {
        super(expression);
        m_expression=expression;
        m_datasources=new ArrayList<String>();
        m_parser = new JEP();
        m_parser.setAllowUndeclared(true); //This is critical - we allow undelared vars, then ask the parser for what vars are used
        m_parser.parseExpression(m_expression.getExpression());
        if(m_parser.hasError()) {
            throw new ThresholdExpressionException("Could not parse threshold expression:"+m_parser.getErrorInfo());
        }
        m_datasources.addAll(m_parser.getSymbolTable().keySet());
    }
    
    @Override
    public String getDatasourceExpression() {
        return m_expression.getExpression();
    }
    @Override
    public Collection<String> getRequiredDatasources() {
       return m_datasources;
    }

    @Override
    public double evaluate(Map<String, Double> values) throws ThresholdExpressionException {
        for(String valueName : values.keySet()) {
            m_parser.addVariable(valueName, values.get(valueName));
        }
        double result=m_parser.getValue();
        if(m_parser.hasError()) {
            throw new ThresholdExpressionException("Error while evaluating expression "+m_expression.getExpression()+": "+m_parser.getErrorInfo());
        }
        return result;
    }

}
