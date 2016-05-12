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
