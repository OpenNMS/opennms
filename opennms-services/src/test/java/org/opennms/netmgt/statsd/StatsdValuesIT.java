/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.statsd;

import com.google.common.collect.Iterables;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.dao.support.RrdStatisticAttributeVisitor;
import org.opennms.netmgt.dao.support.TopNAttributeStatisticVisitor;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties = {
        "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy",
        "org.opennms.timeseries.strategy=rrd",
        "rrd.base.dir=src/test/resources/share/jrb",
        "rrd.binary=rrdtool"
})
@JUnitTemporaryDatabase
public class StatsdValuesIT implements InitializingBean {

    @Autowired
    protected MonitoringLocationDao m_locationDao;

    @Autowired
    protected NodeDao m_nodeDao;

    @Autowired
    protected ResourceDao m_resourceDao;

    @Autowired
    protected RrdDao m_rrdDao;

    @Autowired
    protected MeasurementFetchStrategy m_fetchStrategy;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    @Transactional
    public void testValue() throws Exception {
        final OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
        node.setId(1);
        m_nodeDao.save(node);
        m_nodeDao.flush();

        final OnmsResource resource = Iterables.getOnlyElement(m_resourceDao.getResourceForNode(node)
                                                                            .getChildResources());

        final OnmsAttribute attribute = resource.getRrdGraphAttributes().get("ifInOctets");

        final double statistic = m_rrdDao.getPrintValue(attribute,
                                                       "AVERAGE",
                                                       1414602000000L,
                                                       1417046400000L);

        final TopNAttributeStatisticVisitor result = new TopNAttributeStatisticVisitor();
        result.setCount(1);

        final RrdStatisticAttributeVisitor visitor = new RrdStatisticAttributeVisitor();
        visitor.setFetchStrategy(m_fetchStrategy);
        visitor.setConsolidationFunction("AVERAGE");
        visitor.setStartTime(1414602000000L);
        visitor.setEndTime(1417046400000L);
        visitor.setStatisticVisitor(result);
        visitor.afterPropertiesSet();

        visitor.visit(attribute);

        Assert.assertNotNull(result.getResults());
        Assert.assertEquals(1, result.getResults().size());
        Assert.assertNotNull(result.getResults().first());
        Assert.assertEquals(attribute, result.getResults().first().getAttribute());
        Assert.assertEquals(statistic, result.getResults().first().getStatistic(), 0.5);
    }
}
