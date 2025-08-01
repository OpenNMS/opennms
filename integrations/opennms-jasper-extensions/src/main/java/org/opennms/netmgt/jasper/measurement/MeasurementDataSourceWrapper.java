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

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;

public interface MeasurementDataSourceWrapper {

    /**
     * Creates a {@link JRRewindableDataSource} according to the provided query.
     *
     * @param query The query to execute. Should be a OpenNMS Measurement API parsable {@link org.opennms.netmgt.measurements.model.QueryRequest}. It may be null, but not empty.
     * @return The DataSource.
     * @throws JRException In any error situation. RuntimeException are not catched and may be thrown in addition.
     */
    JRRewindableDataSource createDataSource(String query) throws JRException;

    void close();
}
