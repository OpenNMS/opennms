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
        expression.setExpression("a < b ? trueval : falseval");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("a",20.0);
        values.put("b",5.0);
        values.put("trueval",3.0);
        values.put("falseval",7.0);
        
        double result=wrapper.evaluate(values);
        assertEquals("Conditional Expression result", new Double(result), new Double(7.0));
    }
    
    public void testEvaluateConditionalTrue() throws Exception {
        expression.setExpression("a < b ? trueval : falseval");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String, Double> values=new HashMap<String,Double>();
        values.put("a",2.0);
        values.put("b",5.0);
        values.put("trueval",3.0);
        values.put("falseval",7.0);
        
        double result=wrapper.evaluate(values);
        assertEquals("Conditional Expression result", new Double(result), new Double(3.0));
    }
    
    public void testAbsoluteValues() throws Exception {
        expression.setExpression("math.abs(variable + 5)");
        ExpressionConfigWrapper wrapper=new ExpressionConfigWrapper(expression);
        
        Map<String,Double> values=new HashMap<String,Double>();
        values.put("variable", -25.0);
        
        double result = wrapper.evaluate(values);
        assertEquals("Conditional Expression result", new Double(20.0), result);
        
        values.clear();
        values.put("variable", 25.0);
        
        result = wrapper.evaluate(values);
        assertEquals("Conditional Expression result", new Double(30.0), result);
    }
}
