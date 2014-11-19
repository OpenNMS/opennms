/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.threshd.Expression;

/**
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ExpressionConfigWrapperTest {
    
    private final String FORMULA = "ifSpeed > 0 and ifSpeed < 100000000 ? ((ifInOctets * 8 / ifSpeed) * 100) : (ifHighSpeed > 0 ? (((ifHCInOctets * 8) / (ifHighSpeed * 1000000)) * 100) : 0)";

    private ExpressionConfigWrapper wrapper;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE"); 
        Expression exp = new Expression();
        exp.setExpression(FORMULA);
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
        double value = wrapper.evaluate(values);
        Assert.assertTrue(value == 16.0);
    }

    @Test
    public void testFastInterface() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifHCInOctets", 20000000.0);
        values.put("ifSpeed", 100000000.0);
        values.put("ifHighSpeed", 1000.0);
        double value = wrapper.evaluate(values);
        Assert.assertTrue(value == 16.0);
    }

    @Test
    public void testDivisionByZeroA() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifHCInOctets", 20000000.0);
        values.put("ifSpeed", 100000000.0);
        values.put("ifHighSpeed", 1000.0);
        double value = wrapper.evaluate(values);
        Assert.assertTrue(value == 16.0);
    }

    @Test
    public void testDivisionByZeroB() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifInOctets", 200000.0);
        values.put("ifSpeed", 0.0);
        values.put("ifHighSpeed", 0.0);
        double value = wrapper.evaluate(values);
        Assert.assertTrue(value == 0.0);
    }

    @Test
    public void testDivisionByZeroC() throws Exception {
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ifHCInOctets", 20000000.0);
        values.put("ifSpeed", 0.0);
        values.put("ifHighSpeed", 0.0);
        double value = wrapper.evaluate(values);
        Assert.assertTrue(value == 0.0);
    }

    @Test
    public void testMath() throws Exception {
        Expression exp = new Expression();
        exp.setExpression("math.max(data, 5)");
        wrapper = new ExpressionConfigWrapper(exp);
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("data", 10.0);
        Assert.assertTrue(10.0 == wrapper.evaluate(values));
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
        Assert.assertEquals(0.0, wrapper.evaluate(values), 0.0);
        values.put("jnxOperatingState", 2.0);
        Assert.assertEquals(1.0, wrapper.evaluate(values), 0.0);
        values.put("jnxOperatingState", 3.0);
        Assert.assertEquals(1.0, wrapper.evaluate(values), 0.0);
        values.put("jnxOperatingState", 4.0);
        Assert.assertEquals(0.0, wrapper.evaluate(values), 0.0);
        values.put("jnxOperatingState", 5.0);
        Assert.assertEquals(0.0, wrapper.evaluate(values), 0.0);
        values.put("jnxOperatingState", 6.0);
        Assert.assertEquals(0.0, wrapper.evaluate(values), 0.0);
        values.put("jnxOperatingState", 7.0);
        Assert.assertEquals(1.0, wrapper.evaluate(values), 0.0);
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
        Assert.assertEquals(60.0, wrapper.evaluate(values), 0.0);
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
        Assert.assertEquals(160.0, wrapper.evaluate(values), 0.0);
    }
}
