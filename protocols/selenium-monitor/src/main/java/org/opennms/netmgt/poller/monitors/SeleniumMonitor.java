package org.opennms.netmgt.poller.monitors;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.junit.runner.SeleniumComputer;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;

public class SeleniumMonitor extends AbstractServiceMonitor {

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
                responseTimes.put("response-time", Double.NaN);
                
                tracker.startAttempt();
                Result result = runTest( getBaseUrl(parameters, svc), getTimeout(parameters), createGroovyClass( seleniumTestFilename ) );
                double responseTime = tracker.elapsedTimeInMillis();
                responseTimes.put("response-time", responseTime);
                
                if(result.wasSuccessful()) {
                    serviceStatus = PollStatus.available();
                    serviceStatus.setProperties(responseTimes);
                }else {
                    serviceStatus = PollStatus.unavailable( getFailureMessage( result, svc ));
                }
            } catch (CompilationFailedException e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on:" + svc.getIpAddr() + " failed : selenium-test compilation error " + e.getMessage());
                logDown(Level.DEBUG, "Selenium sequence failed: " + e.getMessage());
            } catch (IOException e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on " + svc.getIpAddr() + " failed: IOException occurred, failed to find selenium-test: " + seleniumTestFilename);
                logDown(Level.DEBUG, "Selenium sequence failed: " + e.getMessage());
            } catch (Exception e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on " + svc.getIpAddr() + " failed:\n" + e.getMessage());
                logDown(Level.DEBUG, "Selenium sequence failed: " + e.getMessage());
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
                return BaseUrlUtils.replaceIpAddr(baseUrl, svc.getIpAddr());
            }
            
            return baseUrl;
        }else {
            return null;
        }
        
    }

    private String getFailureMessage(Result result, MonitoredService svc)
    {
        StringBuffer stringBuilder = new StringBuffer();
        stringBuilder.append("Failed: ");
        for(Failure failure : result.getFailures()) { 
            stringBuilder.append(" " + failure.getMessage() + "\n");
        }
        
        logDown(Level.DEBUG, "Selenium sequence failed: " + stringBuilder.toString());
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
        GroovyClassLoader gcl = new GroovyClassLoader();
        
        String file = System.getProperty("opennms.selenium.test.dir") + "/selenium/test/groovy/" + filename;
        return gcl.parseClass( new File( file ) );
    }

}
