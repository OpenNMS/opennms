/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;



// This might actually be usable in the remote poller with some work
@Distributable(DistributionContext.DAEMON)

/**
 * <P>
 * This <code>ServiceMonitor</code> is designed to enable the evaluation
 * or execution of user-supplied scripts via the Bean Scripting Framework
 * (BSF).  Scripts should indicate a status whose string value is one of:
 * 
 * "OK" (service is available),
 * "UNK" (service status unknown),
 * "UNR" (service is unresponsive), or
 * "NOK" (service is unavailable).
 * 
 * These strings map into the status values defined in @PollStatus and are
 * indicated differently depending on the run-type of the script in question
 * (see below for details).
 *
 * Use cases:
 *
 * a) Evaluate an expression from a file in the filesystem.  The result of the
 *    evaluation carries the status indication in this mode.  As a special case
 *    for backward compatibility, a status code outside the set described above
 *    will be taken to convey that the service is unavailable, and the status code
 *    itself will be set as the reason code.
 *    
 *    If the scripting engine in use supports bean manipulation during an
 *    evaluation, then any entries put into the "times" bean will be returned
 *    for optional thresholding and/or persisting.  If no entries exist in this
 *    bean upon the evaluation's return, then a single value describing the time
 *    window (in milliseconds) from just before the evaluation began until just
 *    after it returned will be substituted.
 *    
 *    This mode is the default if no "run-type" parameter is
 *    specified in the service definition or if this parameter's value is "eval".
 *    
 * b) Execute a self-contained script from a file in the filesystem.  The script
 *    must put an entry into the "results" HashMap bean with key "status" and a
 *    value from the above list of service status indications.  If the script puts
 *    one or more entries into the "times" bean, then these key-value pairs will
 *    be returned for optional thresholding and/or persisting.  If no entry exists
 *    in this bean with a key of "response-time", the overall response time will
 *    be substituted as the time from just before the script's execution to just
 *    after its completion.
 *    
 *    This mode is used if the service's definition contains a "run-type" parameter
 *    with a value of "exec".
 *    
 * The following beans are declared in the script's execution context:
 * 
 * map: A @Map<String,Object> allowing direct access to the list of parameters
 *      configured for the service at hand
 * ip_addr: A @String representing the IPv4 or IPv6 address of the interface
 *          on which the polled service resides
 * node_id: An int containing the unique identifying number from the OpenNMS
 *          configuration database of the node on whose interface the
 *          monitored service resides
 * node_label: A @String containing the textual node label of the node on whose
 *             interface the monitored service resides
 * svc_name: A @String containing the textual name of the monitored service
 * bsf_monitor: The singleton instance of the @BSFMonitor class, useful primarily
 *              for purposes of logging via its
 *              log(String sev, String fmt, Object... args) method.  The severity
 *              must be one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL.  The format
 *              is a printf-style format string, and the args fill in the tokens.
 * results: A @HashMap<String,String> that the script may use to pass its results
 *          back to the @BSFMonitor. A status indication should be set into the
 *          entry with key "status", and for status indications other than "OK"
 *          a reason code should be set into the entry with key "reason".
 * times: A @LinkedHashMap<String,Number> that the script may use to pass one
 *        or more response times back to the @BSFMonitor.
 * </P>
 *
 * @author <A HREF="mailto:jay@opennms.org">Jason Aras</A>
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org">OpenNMS</A>
 */

public class BSFMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(BSFMonitor.class);

    private static final String STATUS_UNKNOWN = "UNK";
    private static final String STATUS_UNRESPONSIVE = "UNR";
    private static final String STATUS_AVAILABLE = "OK";
    private static final String STATUS_UNAVAILABLE = "NOK";
    
    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String,Object> map) {
        BSFManager bsfManager = new BSFManager();
        PollStatus pollStatus = PollStatus.unavailable();
        String fileName = ParameterMap.getKeyedString(map,"file-name", null);
        String lang = ParameterMap.getKeyedString(map, "lang-class", null);
        String langEngine = ParameterMap.getKeyedString(map, "bsf-engine", null);
        String langExtensions[] = ParameterMap.getKeyedString(map, "file-extensions", "").split(",");
        String runType = ParameterMap.getKeyedString(map, "run-type", "eval");
        File file = new File(fileName);

        try {
           
            if(lang==null)
                lang = BSFManager.getLangFromFilename(fileName);
                
            if(langEngine!=null && lang!=null && langExtensions.length > 0 ){
                BSFManager.registerScriptingEngine(lang,langEngine,langExtensions);
            }
            
            if(file.exists() && file.canRead()){   
                    String code = IOUtils.getStringFromReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                    HashMap<String,String> results = new HashMap<String,String>();
                    LinkedHashMap<String,Number> times = new LinkedHashMap<String,Number>();
                    
                    // Declare some beans that can be used inside the script
                    bsfManager.declareBean("map", map, Map.class);
                    bsfManager.declareBean("ip_addr",svc.getIpAddr(),String.class);
                    bsfManager.declareBean("node_id",svc.getNodeId(),int.class );
                    bsfManager.declareBean("node_label", svc.getNodeLabel(), String.class);
                    bsfManager.declareBean("svc_name", svc.getSvcName(), String.class);
                    bsfManager.declareBean("bsf_monitor", this, BSFMonitor.class);
                    bsfManager.declareBean("results", results, HashMap.class);
                    bsfManager.declareBean("times", times, LinkedHashMap.class);

                    for (final Entry<String, Object> entry : map.entrySet()) {
                        bsfManager.declareBean(entry.getKey(),entry.getValue(),String.class);
                    }
                    
                    pollStatus = PollStatus.unknown("The script did not update the service status");
                    
                    long startTime = System.currentTimeMillis();
                    if ("eval".equals(runType)) {
                        results.put("status", bsfManager.eval(lang, "BSFMonitor", 0, 0, code).toString());
                    } else if ("exec".equals(runType)) {
                        bsfManager.exec(lang, "BSFMonitor", 0, 0, code);
                    } else {
                        LOG.warn("Invalid run-type parameter value '{}' for service '{}'. Only 'eval' and 'exec' are supported.", runType, svc.getSvcName());
                        throw new RuntimeException("Invalid run-type '" + runType + "'");
                    }
                    long endTime = System.currentTimeMillis();
                    if (!times.containsKey("response-time")) {
                        times.put("response-time", endTime - startTime);
                    }
                    
                    if (STATUS_UNKNOWN.equals(results.get("status"))) {
                        pollStatus = PollStatus.unknown(results.get("reason"));
                    } else if (STATUS_UNRESPONSIVE.equals(results.get("status"))) {
                        pollStatus = PollStatus.unresponsive(results.get("reason"));
                    } else if (STATUS_AVAILABLE.equals(results.get("status"))){
                        pollStatus = PollStatus.available();
                    } else if (STATUS_UNAVAILABLE.equals(results.get("status"))) {
                        pollStatus = PollStatus.unavailable(results.get("reason"));
                    } else {
                        // Fall through to the old default of treating any other non-OK
                        // code as meaning unavailable and also carrying the reason code
                        pollStatus = PollStatus.unavailable(results.get("status"));
                    }
                    
                    LOG.debug("Setting {} times for service '{}'", times.size(), svc.getSvcName());
                    pollStatus.setProperties(times);
                    
                    if ("exec".equals(runType) && !results.containsKey("status")) {
                        LOG.warn("The exec script '{}' for service '{}' never put a 'status' entry in the 'results' bean. Exec scripts should put this entry with a value of 'OK' for up.", fileName, svc.getSvcName());
                    }
            } else {
                LOG.warn("Cannot locate or read BSF script file '{}'. Marking service '{}' down.", fileName, svc.getSvcName());
                pollStatus = PollStatus.unavailable("Cannot locate or read BSF script file: " + fileName);
            }            

        } catch (BSFException e) {
            LOG.warn("BSFMonitor poll for service '{}' failed with BSFException: {}", svc.getSvcName(), e.getMessage(), e);
            pollStatus = PollStatus.unavailable(e.getMessage());
        } catch (FileNotFoundException e){
            LOG.warn("Could not find BSF script file '{}'. Marking service '{}' down.", fileName, svc.getSvcName());
            pollStatus = PollStatus.unavailable("Could not find BSF script file: " + fileName);
        } catch (IOException e) {
            pollStatus = PollStatus.unavailable(e.getMessage());
            LOG.warn("BSFMonitor poll for service '{}' failed with IOException: {}", svc.getSvcName(), e.getMessage(), e);
        } catch (Throwable e) {
            // Catch any RuntimeException throws
            pollStatus = PollStatus.unavailable(e.getMessage());
            LOG.warn("BSFMonitor poll for service '{}' failed with unexpected throwable: {}", svc.getSvcName(), e.getMessage(), e);
        } finally { 
            bsfManager.terminate();
        }

        return pollStatus;
    }
    
    public void log(String level, String format, Object... args) {
        if ("TRACE".equals(level)) LOG.trace(format, args);
        if ("DEBUG".equals(level)) LOG.debug(format, args);
        if ("INFO".equals(level)) LOG.info(format, args);
        if ("WARN".equals(level)) LOG.warn(format, args);
        if ("ERROR".equals(level)) LOG.error(format, args);
        if ("FATAL".equals(level)) LOG.error(format, args);
    }
}
