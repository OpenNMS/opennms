package org.opennms.netmgt.poller.monitors;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.junit.runner.SeleniumComputer;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;

public class AjaxPageSequenceMonitor extends AbstractServiceMonitor {
	private static final int DEFAULT_SEQUENCE_RETRY = 0;
	private static final int DEFAULT_TIMEOUT = 3000;
    
	@Override
	public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) 
	{
		PollStatus serviceStatus = PollStatus.unavailable("Poll not completed yet");
		TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_SEQUENCE_RETRY, DEFAULT_TIMEOUT);
	    
		for(tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
    		try {
    	        
                Map<String, Number> responseTimes = new HashMap<String, Number>();
                responseTimes.put("response-time", Double.NaN);
                
                tracker.startAttempt();
                Result result = runTest( getBaseUrl(parameters, svc), createGroovyClass( getGroovyFilename( parameters ) ) );
                double responseTime = tracker.elapsedTimeInMillis();
                responseTimes.put("response-time", responseTime);
                
                if(result.wasSuccessful()) {
                    serviceStatus = PollStatus.available();
                    serviceStatus.setProperties(responseTimes);
                }else {
                    serviceStatus = PollStatus.unavailable( getFailureMessage( result, svc ));
                }
            } catch (CompilationFailedException e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on:" + svc.getIpAddr() + " failed : " + e.getMessage());
            } catch (IOException e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on " + svc.getIpAddr() + " failed: " + e.getMessage());
            } catch (Exception e) {
                serviceStatus = PollStatus.unavailable("Selenium page sequence attempt on " + svc.getIpAddr() + " failed: " + e.getMessage());
            }
		}
	    
		return serviceStatus;
	}

    private String getBaseUrl(Map<String, Object> parameters, MonitoredService svc)
    {
        if(parameters.containsKey("base-url")) {
            String baseUrl = (String) parameters.get("base-url");
            if(baseUrl.contains("${ipAddr}")) {;
                return "http://" + svc.getIpAddr();
            }
            
            return (String) parameters.get("base-url");
        }else {
            return null;
        }
        
    }

    private String getFailureMessage(Result result, MonitoredService svc)
    {
        StringBuffer stringBuilder = new StringBuffer();
        stringBuilder.append("Failed page sequence attempt on " + svc.getIpAddr() + " for node: " + svc.getNodeLabel() + " with service: " + svc.getSvcName());
        for(Failure failure : result.getFailures()) { 
            stringBuilder.append(failure.getMessage() + "\n");
        }
        return stringBuilder.toString();
    }

    private Result runTest(String baseUrl, Class<?> clazz)
    {
        return JUnitCore.runClasses(new SeleniumComputer(baseUrl), clazz);
    }

    private String  getGroovyFilename(Map<String, Object> parameters) 
    {
        if(parameters.containsKey("selenium-test")) {
            return (String) parameters.get("selenium-test");
        }else {
            return "";
        }
        
    }

    private Class<?> createGroovyClass(String string) throws CompilationFailedException, IOException 
    {
        GroovyClassLoader gcl = new GroovyClassLoader();
        
        return gcl.parseClass( new File( string ) );
    }

}
