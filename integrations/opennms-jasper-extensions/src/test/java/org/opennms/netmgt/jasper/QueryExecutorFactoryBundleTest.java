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
package org.opennms.netmgt.jasper;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.jasper.measurement.MeasurementExecutorFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;

public class QueryExecutorFactoryBundleTest {

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
        final QueryExecutorFactoryBundle executorBundle = new QueryExecutorFactoryBundle();
        final QueryExecuterFactory factory = executorBundle.getQueryExecuterFactory(language);
        Assert.assertEquals(supported, factory != null);
        if (supported) {
            Assert.assertEquals(expectedFactoryClass, factory.getClass());
        }
    }
}
