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

import net.sf.jasperreports.engine.JRParameter;
import org.opennms.netmgt.jasper.helper.MeasurementsHelper;
import org.opennms.reporting.jasperreports.filter.ParameterFilter;

public class MeasurementParameterFilter implements ParameterFilter {

    protected interface JvmDetector {
        boolean isRunInOpennmsJvm();
    }

    private final JvmDetector jvmDetector;

    public MeasurementParameterFilter() {
        this(new JvmDetector() {
            @Override
            public boolean isRunInOpennmsJvm() {
                return MeasurementsHelper.isRunInOpennmsJvm();
            }
        });
    }

    protected MeasurementParameterFilter(JvmDetector jvmDetector) {
        this.jvmDetector = jvmDetector;
    }

    @Override
    public boolean apply(JRParameter reportParameter) {
        if (jvmDetector.isRunInOpennmsJvm()) {
            // We have to filter MEASUREMENT_* parameters if we run within the OpenNMS JVM
            if (Parameters.URL.equals(reportParameter.getName())
                    || Parameters.USERNAME.equals(reportParameter.getName())
                    || Parameters.PASSWORD.equals(reportParameter.getName())) {
                return false;
            }
        }
        return true;
    }
}
