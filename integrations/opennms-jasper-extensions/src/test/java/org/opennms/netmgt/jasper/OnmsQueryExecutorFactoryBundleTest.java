/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuterFactory;

import org.junit.Test;
import org.opennms.netmgt.jasper.jrobin.JRobinQueryExecutorFactory;
import org.opennms.netmgt.jasper.resource.ResourceQueryExecuterFactory;
import org.opennms.netmgt.jasper.rrdtool.RrdtoolQueryExecutorFactory;

public class OnmsQueryExecutorFactoryBundleTest {

    @Test
    public void testPickCorrectStrategy() throws JRException {
        OnmsQueryExecutorFactoryBundle executorBundle = new OnmsQueryExecutorFactoryBundle();
        JRQueryExecuterFactory factory = executorBundle.getQueryExecuterFactory("jrobin");
        assertTrue(JRobinQueryExecutorFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("rrdtool");
        assertTrue(RrdtoolQueryExecutorFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("resourceQuery");
        assertTrue(ResourceQueryExecuterFactory.class == factory.getClass());
        
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy");
        factory = executorBundle.getQueryExecuterFactory("jrobin");
        assertTrue(RrdtoolQueryExecutorFactory.class == factory.getClass());
        factory = executorBundle.getQueryExecuterFactory("rrdtool");
        assertTrue(RrdtoolQueryExecutorFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("resourceQuery");
        assertTrue(ResourceQueryExecuterFactory.class == factory.getClass());
        
        System.setProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
        factory = executorBundle.getQueryExecuterFactory("jrobin");
        assertTrue(JRobinQueryExecutorFactory.class == factory.getClass());
        factory = executorBundle.getQueryExecuterFactory("rrdtool");
        assertTrue(JRobinQueryExecutorFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("resourceQuery");
        assertTrue(ResourceQueryExecuterFactory.class == factory.getClass());
        
        factory = executorBundle.getQueryExecuterFactory("sql");
        assertNull(factory);
    }

}
