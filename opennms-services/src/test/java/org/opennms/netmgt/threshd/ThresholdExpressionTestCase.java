package org.opennms.netmgt.threshd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.threshd.Expression;

import junit.framework.TestCase;

/**
 * A lot of these tests could be construed as checking that JEP is working properly (which we kind of assume
 * to be the case anyway).
 * Therefore on one hand, they are  "belt-and-britches" security (affirming that JEP works).  On the other
 * we also still need to test that our integration with JEP works as expected (e.g., that there are no oddities
 * with the intepretation of operators or the like)
 * 
 * @author Craig Miskell
 *
 */
public class ThresholdExpressionTestCase extends TestCase {
    
    Expression expression;
    
    public void setUp() {
        expression=new Expression();
        expression.setType("high");
        expression.setDsType("ds-type");
        expression.setValue(99.0);
        expression.setRearm(0.5);
        expression.setTrigger(1);
   }
    
    public void testEvaluateEvaluateSingleItemWithDivision() throws Exception {
        expression.setExpression("dsname/10");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname",1000.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", new Double(result), new Double(100.0));
    }
    
    public void testEvaluateEvaluateSingleItemWithMultiply() throws Exception {
        expression.setExpression("dsname*10");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname",100.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", new Double(result), new Double(1000.0));
    }
    
    public void testEvaluateEvaluateSingleItemWithSubtraction() throws Exception {
        expression.setExpression("dsname-10");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname",100.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", new Double(result), new Double(90.0));
    }

    public void testEvaluateEvaluateSingleItemWithAddition() throws Exception {
        expression.setExpression("dsname+10");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname",100.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", new Double(result), new Double(110.0));
    }
    
    public void testEvaluateEvaluateMultipleItemsDivided() throws Exception {
        expression.setExpression("dsname1/dsname2");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname1",100.0);
        values.put("dsname2",5.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", new Double(result), new Double(20.0));
    }
    
    public void testEvaluateEvaluateMultipleItemsMultiplied() throws Exception {
        expression.setExpression("dsname1*dsname2");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname1",20.0);
        values.put("dsname2",5.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", new Double(result), new Double(100.0));
    }
    
    public void testEvaluateEvaluateMultipleItemsAdded() throws Exception {
        expression.setExpression("dsname1+dsname2");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname1",20.0);
        values.put("dsname2",5.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", new Double(result), new Double(25.0));
    }
    
    public void testEvaluateEvaluateMultipleItemsSubtracted() throws Exception {
        expression.setExpression("dsname1-dsname2");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname1",20.0);
        values.put("dsname2",5.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", new Double(result), new Double(15.0));
    }
    
    /*
     * Modelled on an expression that could be required for hrStorageIndex, where
     * we want to threshold not on percentage full (which we get from "Used/Size"), but rather
     * when free space is lower than some number of bytes (KB, MB, GB, TB, whatever)
     */
    public void testSemiComplexExpression() throws Exception {
        expression.setExpression("(hrStorageSize-hrStorageUsed)*hrStorageAllocationUnits");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("hrStorageAllocationUnits",1024.0); //1K units
        values.put("hrStorageSize",2048.0); //2MB total size
        values.put("hrStorageUsed",1024.0); //1MB used
        double result=wrapper.evaluate(values);
        
        //1MB free, hopefully
        assertEquals("Threshold Expression result", new Double(result), new Double(1024.0*1024.0));
    }
    
    public void testThresholdEntityRequiredDataSources() throws Exception {
        ThresholdEntity entity=new ThresholdEntity();
        expression.setExpression("(hrStorageSize-hrStorageUsed)*hrStorageAllocationUnits");
        BaseThresholdDefConfigWrapper wrapper=BaseThresholdDefConfigWrapper.getConfigWrapper(expression);

        entity.addThreshold(wrapper);
        Collection<String> dataSources=entity.getRequiredDatasources();
        
        assertTrue("Required data sources contains name",dataSources.contains("hrStorageSize"));
        assertTrue("Required data sources contains name",dataSources.contains("hrStorageUsed"));
        assertTrue("Required data sources contains name",dataSources.contains("hrStorageAllocationUnits"));
    }
   
}
