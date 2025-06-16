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
package org.opennms.netmgt.poller.monitors;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.junit.runner.SeleniumComputer;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(SeleniumMonitor.class);

    private final GroovyClassLoader m_gcl = new GroovyClassLoader();

    public static class BaseUrlUtils{
        private static Pattern s_ipAddrPattern = Pattern.compile("\\$\\{ipAddr\\}");
        
        
        public static String replaceIpAddr(String baseUrl, String monSvcIpAddr) {
            if(!baseUrl.contains("${ipAddr}")) {
                return baseUrl;
            }
            
            String finalUrl = "";
            Matcher matcher = s_ipAddrPattern.matcher(baseUrl); 
            finalUrl = matcher.replaceAll(monSvcIpAddr);
            
            return finalUrl;
        }
    }
    
    private static final int DEFAULT_SEQUENCE_RETRY = 0;
	private static final int DEFAULT_TIMEOUT = 3000;
	
	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) 
	{
		PollStatus serviceStatus = PollStatus.unavailable("Poll not completed yet");
		TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_SEQUENCE_RETRY, DEFAULT_TIMEOUT);
	    
		for(tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
		    String seleniumTestFilename = getGroovyFilename( parameters );
    		try {
    	        
                Map<String, Number> responseTimes = new HashMap<String, Number>();
                responseTimes.put(PollStatus.PROPERTY_RESPONSE_TIME, Double.NaN);
                
                tracker.startAttempt();
                Result result = runTest( getBaseUrl(parameters, svc), getTimeout(parameters), createGroovyClass( seleniumTestFilename ) );
                double responseTime = tracker.elapsedTimeInMillis();
                responseTimes.put(PollStatus.PROPERTY_RESPONSE_TIME, responseTime);
                
                if(result.wasSuccessful()) {
                    serviceStatus = PollStatus.available();
                    serviceStatus.setProperties(responseTimes);
                }else {
                    serviceStatus = PollStatus.unavailable( getFailureMessage( result, svc ));
                }
            } catch (CompilationFailedException e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on:" + svc.getIpAddr() + " failed : selenium-test compilation error " + e.getMessage());
                String reason = "Selenium sequence failed: CompilationFailedException" + e.getMessage();
                SeleniumMonitor.LOG.debug(reason, e);
                PollStatus.unavailable(reason);
            } catch (IOException e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on " + svc.getIpAddr() + " failed: IOException occurred, failed to find selenium-test: " + seleniumTestFilename);
                String reason = "Selenium sequence failed: IOException: " + e.getMessage();
                SeleniumMonitor.LOG.debug(reason, e);
                PollStatus.unavailable(reason);
            } catch (Exception e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on " + svc.getIpAddr() + " failed:\n" + e.getMessage());
                String reason = "Selenium sequence failed: Exception: " + e.getMessage();
                SeleniumMonitor.LOG.debug(reason, e);
                PollStatus.unavailable(reason);
            }
		}
	    
		return serviceStatus;
	}

    private int getTimeout(Map<String, Object> parameters) {
        if(parameters.containsKey("timeout")) {
            return Integer.parseInt("" + parameters.get("timeout"));
        }else {
            return 3;
        }
    }

    private String getBaseUrl(Map<String, Object> parameters, MonitoredService svc)
    {
        if(parameters.containsKey("base-url")) {
            String baseUrl = (String) parameters.get("base-url");
            
            if(!baseUrl.contains("http")) {
                baseUrl = "http://" + baseUrl;
            }
            
            if(baseUrl.contains("${ipAddr}")) {
                baseUrl = BaseUrlUtils.replaceIpAddr(baseUrl, svc.getIpAddr());
            }
            
            if(parameters.containsKey("port")) {
                String port = (String) parameters.get("port");
                baseUrl = baseUrl + ":" + port;
            }
            
            return baseUrl;
        }else {
            return null;
        }
        
    }

    private String getFailureMessage(Result result, MonitoredService svc)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Failed: ");
        for(Failure failure : result.getFailures()) { 
            stringBuilder.append(" " + failure.getMessage() + "\n");
        }
        String reason = "Selenium sequence failed: " + stringBuilder.toString();
        SeleniumMonitor.LOG.debug(reason);
        
        PollStatus.unavailable(reason);
        return stringBuilder.toString();
    }

    private Result runTest(String baseUrl, int timeoutInSeconds, Class<?> clazz)
    {
        return JUnitCore.runClasses(new SeleniumComputer(baseUrl, timeoutInSeconds), clazz);
    }

    private String  getGroovyFilename(Map<String, Object> parameters) 
    {
        if(parameters.containsKey("selenium-test")) {
            return (String) parameters.get("selenium-test");
        }else {
            return "";
        }
        
    }

    private Class<?> createGroovyClass(String filename) throws CompilationFailedException, IOException 
    {
        String file = System.getProperty("opennms.home") + "/etc/selenium/" + filename;
        System.err.println("File name: " + file);
        return m_gcl.parseClass( new File( file ) );
    }

}
