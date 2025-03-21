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
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.threshd.Expression;

/**
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ExpressionConfigWrapperTest {
    
    private final String FORMULA = "ifSpeed > 0 and ifSpeed < 100000000 ? ((ifInOctets * 8 / ifSpeed) * ${requisition:testMultiplier|0}) : (ifHighSpeed > 0 ? (((ifHCInOctets * 8) / (ifHighSpeed * 1000000)) * ${requisition:testMultiplier|0}) : 0)";

    private ExpressionConfigWrapper wrapper;
    
    private final Scope scope = mock(Scope.class);

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE"); 
        Expression exp = new Expression();
        exp.setExpression(FORMULA);
        when(scope.get(new ContextKey("requisition", "testMultiplier"))).thenReturn(Optional.of(new Scope.ScopeValue(Scope.ScopeName.DEFAULT, "100")));
        wrapper = new ExpressionConfigWrapper(exp);
        Assert.assertEquals(4, wrapper.getRequiredDatasources().size());
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testSlowInterface() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifInOctets", 200000.0);
        values.put("ifSpeed", 10000000.0);
        double value = wrapper.interpolateAndEvaluate(values, scope).value;
        Assert.assertTrue(value == 16.0);
    }

    @Test
    public void testFastInterface() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifHCInOctets", 20000000.0);
        values.put("ifSpeed", 100000000.0);
        values.put("ifHighSpeed", 1000.0);
        double value = wrapper.interpolateAndEvaluate(values, scope).value;
        Assert.assertTrue(value == 16.0);
    }

    @Test
    public void testDivisionByZeroA() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifHCInOctets", 20000000.0);
        values.put("ifSpeed", 100000000.0);
        values.put("ifHighSpeed", 1000.0);
        double value = wrapper.interpolateAndEvaluate(values, scope).value;
        Assert.assertTrue(value == 16.0);
    }

    @Test
    public void testDivisionByZeroB() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifInOctets", 200000.0);
        values.put("ifSpeed", 0.0);
        values.put("ifHighSpeed", 0.0);
        double value = wrapper.interpolateAndEvaluate(values, scope).value;
        Assert.assertTrue(value == 0.0);
    }

    @Test
    public void testDivisionByZeroC() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifHCInOctets", 20000000.0);
        values.put("ifSpeed", 0.0);
        values.put("ifHighSpeed", 0.0);
        double value = wrapper.interpolateAndEvaluate(values, scope).value;
        Assert.assertTrue(value == 0.0);
    }

    @Test
    public void testMath() throws Exception {
        Expression exp = new Expression();
        exp.setExpression("math.max(data, 5)");
        wrapper = new ExpressionConfigWrapper(exp);
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("data", 10.0);
        Assert.assertTrue(10.0 == wrapper.interpolateAndEvaluate(values, scope).value);
    }

    /* See NMS-5014 */
    @Test
    public void testComplexExpression() throws Exception {
        Expression exp = new Expression();
        exp.setExpression("jnxOperatingState == 2.0 || jnxOperatingState == 3.0 || jnxOperatingState == 7.0 ? 1.0 : 0.0");
        ExpressionConfigWrapper wrapper = new ExpressionConfigWrapper(exp);
        Assert.assertEquals(1, wrapper.getRequiredDatasources().size());
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("jnxOperatingState", 1.0);
        Assert.assertEquals(0.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
        values.put("jnxOperatingState", 2.0);
        Assert.assertEquals(1.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
        values.put("jnxOperatingState", 3.0);
        Assert.assertEquals(1.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
        values.put("jnxOperatingState", 4.0);
        Assert.assertEquals(0.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
        values.put("jnxOperatingState", 5.0);
        Assert.assertEquals(0.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
        values.put("jnxOperatingState", 6.0);
        Assert.assertEquals(0.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
        values.put("jnxOperatingState", 7.0);
        Assert.assertEquals(1.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
    }

    /* See NMS-5019 */
    @Test
    public void testHandleInvalidDsNames() throws Exception {
        Expression exp = new Expression();
        exp.setExpression("datasources['ns-dskTotal'] - datasources['ns-dskUsed']");
        ExpressionConfigWrapper wrapper = new ExpressionConfigWrapper(exp);
        Assert.assertEquals(2, wrapper.getRequiredDatasources().size());
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ns-dskTotal", 100.0);
        values.put("ns-dskUsed", 40.0);
        Assert.assertEquals(60.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
    }

    @Test
    public void testFunctions() throws Exception {
        Expression exp = new Expression();
        exp.setExpression("math.max((ifInOctets*8/ifSpeed), (ifOutOctets*8/ifSpeed))");
        ExpressionConfigWrapper wrapper = new ExpressionConfigWrapper(exp);
        Assert.assertEquals(3, wrapper.getRequiredDatasources().size());
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifInOctets", 100.0);
        values.put("ifOutOctets", 200.0);
        values.put("ifSpeed", 10.0);
        Assert.assertEquals(160.0, wrapper.interpolateAndEvaluate(values, scope).value, 0.0);
    }
}
