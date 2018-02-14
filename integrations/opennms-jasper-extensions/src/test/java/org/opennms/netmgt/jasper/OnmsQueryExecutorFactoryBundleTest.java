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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.jasper.measurement.MeasurementExecutorFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;

public class OnmsQueryExecutorFactoryBundleTest {

    @Test
    public void verifyJrobinNotSupported() throws JRException {
        verifyLanguage("jrobin", false, null);
    }

    @Test
    public void verifyResourceQueryNotSupported() throws JRException {
        verifyLanguage("resourceQuery", false, null);
    }

    @Test
    public void verifyRrdtoolNotSupported() throws JRException {
        verifyLanguage("rrdtool", false, null);
    }

    @Test
    public void verifySqlNotSupported() throws JRException {
        verifyLanguage("sql", false, null);
    }

    @Test
    public void verifyMeasurementSupported() throws JRException {
        verifyLanguage("measurement", true, MeasurementExecutorFactory.class);
    }

    private static void verifyLanguage(String language, boolean supported, Class<?> expectedFactoryClass) throws JRException {
        final OnmsQueryExecutorFactoryBundle executorBundle = new OnmsQueryExecutorFactoryBundle();
        final QueryExecuterFactory factory = executorBundle.getQueryExecuterFactory(language);
        Assert.assertEquals(supported, factory != null);
        if (supported) {
            Assert.assertEquals(expectedFactoryClass, factory.getClass());
        }
    }
}
