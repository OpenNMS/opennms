package org.opennms.netmgt.jasper.measurement;

import com.google.common.io.ByteStreams;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Makes Measurement API requests and wraps the request in an appropriate {@link JRRewindableDataSource}.
 */
public class RemoteMeasurementDataSourceWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementQueryExecutor.class);

    private final MeasurementApiConnector connector = new MeasurementApiConnector();
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

    /**
     * Creates a {@link JRRewindableDataSource} according to the provided query.
     *
     * @param query The query to execute. Should be a OpenNMS Measurement API parsable {@link org.opennms.netmgt.measurements.model.QueryRequest}. It may be null, but not empty.
     * @return The DataSource.
     * @throws JRException In any error situation. RuntimeException are not catched and may be thrown in addition.
     */
    public JRRewindableDataSource createDataSource(String query) throws JRException {
        try {
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

    public void disconnect() {
        if (connector != null) {
            connector.disconnect();
        }
    }
}
