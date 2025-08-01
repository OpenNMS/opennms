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
package org.opennms.netmgt.jasper.measurement.remote;

import static org.opennms.netmgt.jasper.helper.MeasurementsHelper.marshal;
import static org.opennms.netmgt.jasper.helper.MeasurementsHelper.unmarshal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.opennms.netmgt.jasper.measurement.EmptyJRDataSource;
import org.opennms.netmgt.jasper.measurement.MeasurementDataSource;
import org.opennms.netmgt.jasper.measurement.MeasurementDataSourceWrapper;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;

/**
 * Makes Measurement API requests and wraps the request in an appropriate {@link JRRewindableDataSource}.
 */
public class RemoteMeasurementDataSourceWrapper implements MeasurementDataSourceWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteMeasurementDataSourceWrapper.class);

    private final MeasurementApiClient connector = new MeasurementApiClient();
    private final String url;
    private final String username;
    private final String password;
    private final boolean useSsl;

    public RemoteMeasurementDataSourceWrapper(boolean useSsl, String url, String username, String password) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.useSsl = useSsl;
    }

    @Override
    public JRRewindableDataSource createDataSource(String query) throws JRException {
        try {
            QueryRequest queryRequest = unmarshal(query);
            queryRequest.setRelaxed(true); // enforce relaxed mode
            query = marshal(queryRequest);
            Result result = connector.execute(useSsl, url, username, password, query);

            // All unauthorized requests may be forwarded to another page (e.g. login.jsp) which results in 200 OK, we
            // therefore do not allow redirection at all.
            if (result.wasRedirection()) {
                throw new IOException("Request was redirected. This is not supported.");
            }
            // for now we return an empty datasource, because we do not want exceptions to be thrown
            // if there is no data
            if (404 == result.getResponseCode()) {
                LOG.warn("Got a 404 (Not Found) response. This might be due to a wrong url or the resource does not exist. Requested URL was: '{}'", url);
                return new EmptyJRDataSource();
            }
            // OK
            if (result.wasSuccessful() && result.getInputStream() != null) {
                return new MeasurementDataSource(result.getInputStream());
            }
            // Error
            ByteArrayOutputStream errorMessageStream = new ByteArrayOutputStream();
            if (result.getErrorStream() != null) {
                ByteStreams.copy(result.getErrorStream(), errorMessageStream);
            }
            throw new JRException("Invalid request. Response was : "
                    + result.getResponseCode() + " (" + result.getResponseMessage() + ")\n"
                    + errorMessageStream.toString());
        } catch (IOException ioException) {
            throw new JRException(ioException);
        }
    }

    @Override
    public void close() {
        if (connector != null) {
            connector.disconnect();
        }
    }
}
