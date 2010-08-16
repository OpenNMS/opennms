package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.HttpVersion;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.core.utils.ParameterMap;

@Distributable
/**
 * <p>WebMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WebMonitor extends IPMonitor {

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
        HttpClient httpClient = new HttpClient();
        HostConfiguration hostConfig = new HostConfiguration();

        GetMethod  getMethod  = new GetMethod(ParameterMap.getKeyedString(map, "path", DEFAULT_PATH));
        httpClient.getParams().setParameter( HttpClientParams.SO_TIMEOUT, 
                                             ParameterMap.getKeyedInteger(map,"timeout", DEFAULT_TIMEOUT));
        httpClient.getParams().setParameter( HttpClientParams.USER_AGENT, 
                                             ParameterMap.getKeyedString(map,"user-agent",DEFAULT_USER_AGENT));
        hostConfig.setHost(svc.getAddress().getHostAddress(), 
                           ParameterMap.getKeyedInteger(map, "port", DEFAULT_PORT));
        hostConfig.getParams().setParameter(HttpClientParams.VIRTUAL_HOST,
                                            ParameterMap.getKeyedString(map,"virtual-host",null));

        if(ParameterMap.getKeyedBoolean(map, "http-1.0", false))
            httpClient.getParams().setParameter(HttpClientParams.PROTOCOL_VERSION,HttpVersion.HTTP_1_0);
        
        for(Object okey : map.keySet()) {
            String key = okey.toString();
            if(key.matches("header_[0-9]+$")){
                String headerName  = ParameterMap.getKeyedString(map,key,null);
                String headerValue = ParameterMap.getKeyedString(map,key + "_value",null);
                getMethod.setRequestHeader(headerName, headerValue);
            }
        }

        if(ParameterMap.getKeyedBoolean(map,"auth-enabled",false)){

            httpClient.getParams().setAuthenticationPreemptive(
                                                               ParameterMap.getKeyedBoolean(map, "auth-preemptive", true));
            httpClient.getState().setCredentials(AuthScope.ANY,
                                  new UsernamePasswordCredentials(
                                              ParameterMap.getKeyedString(map, "auth-user", DEFAULT_USER),
                                              ParameterMap.getKeyedString(map, "auth-password", DEFAULT_PASSWORD)));

        }     
        
        try {
            log().debug("httpClient request with the following parameters: " + httpClient);
            log().debug("hostConfig parameters: " + hostConfig);
            log().debug("getMethod parameters: " + getMethod);
            httpClient.executeMethod(hostConfig, getMethod);
            Integer statusCode = getMethod.getStatusCode();
            String  statusText = getMethod.getStatusText();
            String expectedText = ParameterMap.getKeyedString(map,"response-text",null);
            
            log().debug("returned results are:");
            
            if(!inRange(ParameterMap.getKeyedString(map, "response-range", DEFAULT_HTTP_STATUS_RANGE),statusCode)){
                pollStatus = PollStatus.unavailable(statusText);
            }
            else {
                pollStatus = PollStatus.available();
            }
            
            if (expectedText!=null){
                String responseText = getMethod.getResponseBodyAsString(); 
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

        } catch (HttpException e) {
            log().info(e.getMessage());
        } catch (IOException e) {
            log().info(e.getMessage());
        } finally{
            getMethod.releaseConnection();   
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
