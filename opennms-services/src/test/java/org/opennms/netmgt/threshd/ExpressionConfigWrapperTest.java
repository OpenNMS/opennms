/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.opennms.netmgt.config.threshd.Expression;

/**
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class ExpressionConfigWrapperTest {
    
    /* See NMS-5014 */
    @Test
    public void testComplexExpression() throws Exception {
        Expression exp = new Expression();
        exp.setExpression("jnxOperatingState == 2.0 || jnxOperatingState == 3.0 || jnxOperatingState == 7.0 ? 1.0 : 0.0");
        ExpressionConfigWrapper wrapper = new ExpressionConfigWrapper(exp);
        Assert.assertEquals(1, wrapper.getRequiredDatasources().size());
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("jnxOperatingState", 1.0);
        Assert.assertEquals(0.0, wrapper.evaluate(values));
        values.put("jnxOperatingState", 2.0);
        Assert.assertEquals(1.0, wrapper.evaluate(values));
        values.put("jnxOperatingState", 3.0);
        Assert.assertEquals(1.0, wrapper.evaluate(values));
        values.put("jnxOperatingState", 4.0);
        Assert.assertEquals(0.0, wrapper.evaluate(values));
        values.put("jnxOperatingState", 5.0);
        Assert.assertEquals(0.0, wrapper.evaluate(values));
        values.put("jnxOperatingState", 6.0);
        Assert.assertEquals(0.0, wrapper.evaluate(values));
        values.put("jnxOperatingState", 7.0);
        Assert.assertEquals(1.0, wrapper.evaluate(values));
    }

    /* See NMS-5019 */
    @Test
    public void testHandleInvalidDsNames() throws Exception {
        Expression exp = new Expression();
        exp.setExpression("datasources['ns-dskTotal'] - datasources['ns-dskUsed']");
        ExpressionConfigWrapper wrapper = new ExpressionConfigWrapper(exp);
        Assert.assertEquals(1, wrapper.getRequiredDatasources().size());
        Map<String, Double> values = new HashMap<String,Double>();
        values.put("ns-dskTotal", 100.0);
        values.put("ns-dskUsed", 40.0);
        Assert.assertEquals(60.0, wrapper.evaluate(values));
    }

}
