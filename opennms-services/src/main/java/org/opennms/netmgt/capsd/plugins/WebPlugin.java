package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.jfree.util.Log;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.capsd.AbstractPlugin;

public class WebPlugin extends AbstractPlugin {

    static Integer DEFAULT_TIMEOUT = 3000;
    static Integer DEFAULT_PORT = 80;
    static String DEFAULT_USER_AGENT = "OpenNMS WebMonitor";
    static String DEFAULT_PATH = "/";
    static String DEFAULT_USER = "admin";
    static String DEFAULT_PASSWORD = "admin";
    static String DEFAULT_HTTP_STATUS_RANGE = "100-399";

    @Override
    public String getProtocolName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isProtocolSupported(InetAddress address) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> map) {

        boolean retval=false;
        HttpClient httpClient = new HttpClient();
        HostConfiguration hostConfig = new HostConfiguration();

        GetMethod  getMethod  = new GetMethod(ParameterMap.getKeyedString(map, "path", DEFAULT_PATH));
        httpClient.getParams().setParameter( HttpClientParams.SO_TIMEOUT, 
                                             ParameterMap.getKeyedInteger(map,"timeout", DEFAULT_TIMEOUT));
        httpClient.getParams().setParameter( HttpClientParams.USER_AGENT, 
                                             ParameterMap.getKeyedString(map,"user-agent",DEFAULT_USER_AGENT));
        hostConfig.setHost(address.getHostAddress(),
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

            httpClient.executeMethod(hostConfig, getMethod);
            Integer statusCode = getMethod.getStatusCode();
 
            String expectedText = ParameterMap.getKeyedString(map,"response-text",null);


            if(!inRange(ParameterMap.getKeyedString(map, "response-range", DEFAULT_HTTP_STATUS_RANGE),statusCode)){
                retval=false;
            }
            else {
                retval=true;
            }

            if (expectedText!=null){
                String responseText = getMethod.getResponseBodyAsString(); 
                if(expectedText.charAt(0)=='~'){
                    if(!responseText.matches(expectedText.substring(1)))
                        retval=false;
                    else 
                        retval=true;                
                }
                else {
                    
                    if(responseText.equals(expectedText)){
                        retval=true;
                    }
                    else
                        retval=false;
                }
                
                
            }

        } catch (HttpException e) {
            Log.info(e);
            retval = false;
        } catch (IOException e) {
            Log.info(e);
            retval = false;
        } finally{
            getMethod.releaseConnection();   
        }


        return retval;
    }


    private boolean inRange(String range,Integer val){
        String boundries[] = range.split("-");
        if(val < new Integer(boundries[0]) || val > new Integer(boundries[1]))
            return false;
        else
            return true;
    }

}
