/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.threshd;

import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.Scope;
import org.opennms.netmgt.config.threshd.Expression;
import org.opennms.netmgt.config.threshd.ThresholdType;

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
    
    private final Scope scope = mock(Scope.class);
    
    @Override
    public void setUp() {
        expression=new Expression();
        expression.setType(ThresholdType.HIGH);
        expression.setDsType("node");
        expression.setValue("99.0");
        expression.setRearm("0.5");
        expression.setTrigger("1");
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
        
        //1MB free, hopefully
        assertEquals("Threshold Expression result", Double.valueOf(result), Double.valueOf(1024.0*1024.0));
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
        
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        
        double result=wrapper.interpolateAndEvaluate(values, scope).value;
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
        
        double result = wrapper.interpolateAndEvaluate(values, scope).value;
        assertEquals("Conditional Expression result", Double.valueOf(20.0), result);
        
        values.clear();
        values.put("variable", 25.0);
        
        result = wrapper.interpolateAndEvaluate(values, scope).value;
        assertEquals("Conditional Expression result", Double.valueOf(30.0), result);
    }
}
