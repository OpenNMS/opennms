/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
