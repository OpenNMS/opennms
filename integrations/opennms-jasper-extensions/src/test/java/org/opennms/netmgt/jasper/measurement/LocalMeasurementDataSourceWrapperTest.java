/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.measurement;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.jasper.helper.MeasurementsHelper;
import org.opennms.netmgt.jasper.helper.SpringHelper;
import org.opennms.netmgt.jasper.measurement.MeasurementDataSource;
import org.opennms.netmgt.jasper.measurement.local.LocalMeasurementDataSourceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import net.sf.jasperreports.engine.JRException;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {"/META-INF/opennms/applicationContext-measurements-NMS-8337.xml"}
)
public class LocalMeasurementDataSourceWrapperTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void before() {
        // init ApplicationContext
        BeanUtils.setStaticApplicationContext(applicationContext);

        // Ensure that we are running in jvm mode
        Assert.assertEquals(Boolean.TRUE,MeasurementsHelper.isRunInOpennmsJvm());
    }

    // See NMS-8337
    @Test
    public void verifyContainsResultEvenIfResourceOrAttributeDoNotExist() throws JRException {
        SpringHelper springHelper = MeasurementsHelper.getSpringHelper();
        MeasurementDataSource dataSource = (MeasurementDataSource) new LocalMeasurementDataSourceWrapper(springHelper.getMeasurementFetchStrategy(),
                springHelper.getExpressionEngine(),
                springHelper.getFilterEngine())
                .createDataSource(getQuery());
        Assert.assertEquals(2, dataSource.getRowCount());
        double[] data = new double[] {13, 17};
        while (dataSource.next()) {
            Assert.assertEquals(data[dataSource.getCurrentRow()], dataSource.getFieldValue("ifInErrors", dataSource.getCurrentRow()));
            Assert.assertEquals(Double.NaN, dataSource.getFieldValue("ifOutDiscards", dataSource.getCurrentRow()));
            Assert.assertEquals(Double.NaN, dataSource.getFieldValue("ifOutErrors", dataSource.getCurrentRow()));
        }
    }

    // The query contains parameters which are not substituted
    private String getQuery() {
        String query = "<query-request step=\"300000\" start=\"$P{startDateTime}\" end=\"$P{endDateTime}\" maxrows=\"5000\">\n" +
                "            <source aggregation=\"AVERAGE\" label=\"ifInErrors\" attribute=\"ifInErrors\" transient=\"false\" resourceId=\"node[1].interfaceSnmp[127.0.0.1]\"/>\n" +
                "            <source aggregation=\"AVERAGE\" label=\"ifOutDiscards\" attribute=\"ifOutDiscards\" transient=\"false\" resourceId=\"node[1].interfaceSnmp[127.0.0.1]]\"/>\n" +
                "            <source aggregation=\"AVERAGE\" label=\"ifOutErrors\" attribute=\"ifOutErrors\" transient=\"false\" resourceId=\"node[2].interfaceSnmp[127.0.0.1]\"/>\n" +
                "        </query-request>";
        query = query.replace("$P{endDateTime}", new Date().getTime() + "");
        query = query.replace("$P{startDateTime}", "" + (new Date().getTime() - 7 * 24 * 60 * 60 * 1000));
        return query;
    }
}
