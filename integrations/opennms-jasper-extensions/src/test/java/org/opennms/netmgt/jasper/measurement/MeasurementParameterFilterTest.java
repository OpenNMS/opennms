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
package org.opennms.netmgt.jasper.measurement;

import java.util.Arrays;
import java.util.Collection;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MeasurementParameterFilterTest {

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {true, false, createParameter(Parameters.PASSWORD)},
                {true, false, createParameter(Parameters.USERNAME)},
                {true, false, createParameter(Parameters.URL)},
                {true, true, createParameter("someRandomParameter")},
                {false, true, createParameter(Parameters.PASSWORD)},
                {false, true, createParameter(Parameters.USERNAME)},
                {false, true, createParameter(Parameters.URL)},
                {false, true, createParameter("someRandomParameter")},

        });
    }

    private static JRParameter createParameter(String name) {
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setName(name);
        return parameter;
    }
    private boolean jvmMode;

    private boolean apply;

    private JRParameter parameter;

    public MeasurementParameterFilterTest(boolean jvmMode, boolean apply, JRParameter parameter) {
        this.jvmMode = jvmMode;
        this.apply = apply;
        this.parameter = parameter;
    }

    @Test
    public void test() {
        MeasurementParameterFilter filter = new MeasurementParameterFilter(new MeasurementParameterFilter.JvmDetector() {
            @Override
            public boolean isRunInOpennmsJvm() {
                return jvmMode;
            }
        });

        boolean result = filter.apply(parameter);
        Assert.assertEquals(apply, result);
    }

}
