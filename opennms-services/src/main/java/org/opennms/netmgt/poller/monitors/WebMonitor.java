package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;

@Distributable
/**
 * <p>WebMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WebMonitor extends AbstractServiceMonitor {

    static Integer DEFAULT_TIMEOUT = 3000;
    static Integer DEFAULT_PORT = 80;
    static String DEFAULT_USER_AGENT = "OpenNMS WebMonitor";
    static String DEFAULT_PATH = "/";
    static String DEFAULT_USER = "admin";
    static String DEFAULT_PASSWORD = "admin";
    static String DEFAULT_HTTP_STATUS_RANGE = "100-399";

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public PollStatus poll(MonitoredService svc, Map map) {
        PollStatus pollStatus = PollStatus.unresponsive();
        DefaultHttpClient httpClient = new DefaultHttpClient();

        try {
            HttpGet getMethod = new HttpGet(new URI(
                                                    null, 
                                                    null, 
                                                    svc.getAddress().getHostAddress(), 
                                                    ParameterMap.getKeyedInteger(map, "port", DEFAULT_PORT), 
                                                    ParameterMap.getKeyedString(map, "path", DEFAULT_PATH), 
                                                    null, 
                                                    null
            ));
            httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, ParameterMap.getKeyedInteger(map, "timeout", DEFAULT_TIMEOUT));
            httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, ParameterMap.getKeyedInteger(map, "timeout", DEFAULT_TIMEOUT));
            httpClient.getParams().setParameter( CoreProtocolPNames.USER_AGENT, 
                                                 ParameterMap.getKeyedString(map,"user-agent",DEFAULT_USER_AGENT));
            httpClient.getParams().setParameter(ClientPNames.VIRTUAL_HOST,
                                                new HttpHost(
                                                             ParameterMap.getKeyedString(map,"virtual-host",null), 
                                                             ParameterMap.getKeyedInteger(map, "port", DEFAULT_PORT)
                                                )
            );

            if(ParameterMap.getKeyedBoolean(map, "http-1.0", false)) {
                httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
            }

            for(Object okey : map.keySet()) {
                String key = okey.toString();
                if(key.matches("header_[0-9]+$")){
                    String headerName  = ParameterMap.getKeyedString(map,key,null);
                    String headerValue = ParameterMap.getKeyedString(map,key + "_value",null);
                    getMethod.setHeader(headerName, headerValue);
                }
            }

            if(ParameterMap.getKeyedBoolean(map,"auth-enabled",false)){
                httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials( ParameterMap.getKeyedString(map, "auth-user", DEFAULT_USER), ParameterMap.getKeyedString(map, "auth-password", DEFAULT_PASSWORD)));
                if (ParameterMap.getKeyedBoolean(map, "auth-preemptive", true)) {
                    /**
                     * Add an HttpRequestInterceptor that will perform preemptive auth
                     * @see http://hc.apache.org/httpcomponents-client-4.0.1/tutorial/html/authentication.html
                     */
                    HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {

                        public void process(final HttpRequest request, final HttpContext context) throws IOException {

                            AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                            CredentialsProvider credsProvider = (CredentialsProvider)context.getAttribute(ClientContext.CREDS_PROVIDER);
                            HttpHost targetHost = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

                            // If not auth scheme has been initialized yet
                            if (authState.getAuthScheme() == null) {
                                AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
                                // Obtain credentials matching the target host
                                Credentials creds = credsProvider.getCredentials(authScope);
                                // If found, generate BasicScheme preemptively
                                if (creds != null) {
                                    authState.setAuthScheme(new BasicScheme());
                                    authState.setCredentials(creds);
                                }
                            }
                        }

                    };
                    httpClient.addRequestInterceptor(preemptiveAuth, 0);
                }
            }

            log().debug("httpClient request with the following parameters: " + httpClient);
            log().debug("getMethod parameters: " + getMethod);
            HttpResponse response = httpClient.execute(getMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            String statusText = response.getStatusLine().getReasonPhrase();
            String expectedText = ParameterMap.getKeyedString(map,"response-text",null);

            log().debug("returned results are:");

            if(!inRange(ParameterMap.getKeyedString(map, "response-range", DEFAULT_HTTP_STATUS_RANGE),statusCode)){
                pollStatus = PollStatus.unavailable(statusText);
            }
            else {
                pollStatus = PollStatus.available();
            }

            if (expectedText!=null){
                String responseText = EntityUtils.toString(response.getEntity()); 
                if(expectedText.charAt(0)=='~'){
                    if(!responseText.matches(expectedText.substring(1))){
                        pollStatus = PollStatus.unavailable("Regex Failed");
                    }
                    else 
                        pollStatus = PollStatus.available();
                }
                else {
                    if(expectedText.equals(responseText))
                        pollStatus = PollStatus.available();
                    else
                        pollStatus = PollStatus.unavailable("Did not find expected Text");
                }
            }

        } catch (IOException e) {
            log().info(e.getMessage());
        } catch (URISyntaxException e) {
            log().info(e.getMessage());
        } finally {
            // Do we need to do any cleanup?
            // getMethod.releaseConnection();
        }
        return pollStatus;
    }

    private boolean inRange(String range,Integer val){
        String boundries[] = range.split("-");
        if(val < new Integer(boundries[0]) || val > new Integer(boundries[1]))
            return false;
        else
            return true;
    }
}
