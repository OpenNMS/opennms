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
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
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
        assertEquals(1, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "dsname" }) {
            assertTrue(wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname",1000.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(100.0));
    }
    
    public void testEvaluateEvaluateSingleItemWithMultiply() throws Exception {
        expression.setExpression("dsname*10");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(1, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "dsname" }) {
            assertTrue(wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname",100.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(1000.0));
    }
    
    public void testEvaluateEvaluateSingleItemWithSubtraction() throws Exception {
        expression.setExpression("dsname-10");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(1, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "dsname" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname",100.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(90.0));
    }

    public void testEvaluateEvaluateSingleItemWithAddition() throws Exception {
        expression.setExpression("dsname+10");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(1, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "dsname" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname",100.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(110.0));
    }
    
    public void testEvaluateEvaluateMultipleItemsDivided() throws Exception {
        expression.setExpression("dsname1/dsname2");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(2, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "dsname1", "dsname2" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname1",100.0);
        values.put("dsname2",5.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(20.0));
    }
    
    public void testEvaluateEvaluateMultipleItemsMultiplied() throws Exception {
        expression.setExpression("dsname1*dsname2");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(2, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "dsname1", "dsname2" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname1",20.0);
        values.put("dsname2",5.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(100.0));
    }
    
    public void testEvaluateEvaluateMultipleItemsAdded() throws Exception {
        expression.setExpression("dsname1+dsname2");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(2, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "dsname1", "dsname2" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname1",20.0);
        values.put("dsname2",5.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(25.0));
    }
    
    public void testEvaluateEvaluateMultipleItemsSubtracted() throws Exception {
        expression.setExpression("dsname1-dsname2");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(2, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "dsname1", "dsname2" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("dsname1",20.0);
        values.put("dsname2",5.0);
        double result=wrapper.evaluate(values);
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(15.0));
    }
    
    /*
     * Modelled on an expression that could be required for hrStorageIndex, where
     * we want to threshold not on percentage full (which we get from "Used/Size"), but rather
     * when free space is lower than some number of bytes (KB, MB, GB, TB, whatever)
     */
    public void testSemiComplexExpression() throws Exception {
        expression.setExpression("(hrStorageSize-hrStorageUsed)*hrStorageAllocationUnits");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(3, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "hrStorageSize", "hrStorageUsed", "hrStorageAllocationUnits" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("hrStorageAllocationUnits",1024.0); //1K units
        values.put("hrStorageSize",2048.0); //2MB total size
        values.put("hrStorageUsed",1024.0); //1MB used
        double result=wrapper.evaluate(values);
        
        //1MB free, hopefully
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(1024.0*1024.0));
    }
    
    public void testThresholdEntityRequiredDataSources() throws Exception {
        ThresholdEntity entity=new ThresholdEntity();
        expression.setExpression("(hrStorageSize-hrStorageUsed)*hrStorageAllocationUnits");
        BaseThresholdDefConfigWrapper wrapper=BaseThresholdDefConfigWrapper.getConfigWrapper(expression);
        assertEquals(3, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "hrStorageSize", "hrStorageUsed", "hrStorageAllocationUnits" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }

        entity.addThreshold(wrapper);
        Collection<String> dataSources=entity.getRequiredDatasources();
        StringBuffer dsStringBuffer = new StringBuffer();
        for (String dataSource : dataSources) {
            dsStringBuffer.append(dataSource).append(" ");
        }
        String dsString = dsStringBuffer.toString().trim();
        
        assertTrue("Required data sources should contain hrStorageSize: " + dsString, dataSources.contains("hrStorageSize"));
        assertTrue("Required data sources should contain hrStorageUsed: " + dsString, dataSources.contains("hrStorageUsed"));
        assertTrue("Required data sources should contain hrStorageAllocationUnits: " + dsString, dataSources.contains("hrStorageAllocationUnits"));
    }

    public void testEvaluateConditionalFalse() throws Exception {
        // Doesn't work because the expression is actually being evaluated to sniff 
        // the variable names and trueval is never visited by the parser
        // expression.setExpression("a < b ? trueval : falseval");
        
        // Force trueval to be visited by the parser
        expression.setExpression("(trueval == trueval && a < b) ? trueval : falseval");
        
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(4, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "a", "b", "trueval", "falseval" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("a",20.0);
        values.put("b",5.0);
        values.put("trueval",3.0);
        values.put("falseval",7.0);
        
        double result=wrapper.evaluate(values);
        assertEquals("Conditional Expression result", Double.valueOf(result), Double.valueOf(7.0));
    }
    
    public void testEvaluateConditionalTrue() throws Exception {
        // Doesn't work because the expression is actually being evaluated to sniff 
        // the variable names and trueval is never visited by the parser
        // expression.setExpression("a < b ? trueval : falseval");
        
        // Force trueval to be visited by the parser
        expression.setExpression("(trueval == trueval && a < b) ? trueval : falseval");
        
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(4, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "a", "b", "trueval", "falseval" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("a",2.0);
        values.put("b",5.0);
        values.put("trueval",3.0);
        values.put("falseval",7.0);
        
        double result=wrapper.evaluate(values);
        assertEquals("Conditional Expression result", Double.valueOf(result), Double.valueOf(3.0));
    }
    
    public void testAbsoluteValues() throws Exception {
        expression.setExpression("math.abs(variable + 5)");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        assertEquals(1, wrapper.getRequiredDatasources().size());
        for (String ds : new String[] { "variable" }) {
            assertTrue("Could not find expected variable: " + ds, wrapper.getRequiredDatasources().contains(ds));
        }
        
        Map<String,Double> values=new HashMap<String,Double>();
        values.put("variable", -25.0);
        
        double result = wrapper.evaluate(values);
        assertEquals("Conditional Expression result", Double.valueOf(20.0), result);
        
        values.clear();
        values.put("variable", 25.0);
        
        result = wrapper.evaluate(values);
        assertEquals("Conditional Expression result", Double.valueOf(30.0), result);
    }
}
