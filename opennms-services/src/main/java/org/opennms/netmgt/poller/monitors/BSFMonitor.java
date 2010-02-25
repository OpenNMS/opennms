package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.bsf.util.IOUtils;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.core.utils.ParameterMap;


// this might actually be able to pushed out to the remote poller....  though prob not so easy out of the box
@Distributable(DistributionContext.DAEMON)

public class BSFMonitor extends IPv4Monitor {
    
    @SuppressWarnings("unchecked")
    
    public PollStatus poll(MonitoredService svc, Map map) {
        BSFManager bsfManager = new BSFManager();
        PollStatus pollStatus = PollStatus.unavailable();
        String fileName = ParameterMap.getKeyedString(map,"file-name", null);
        String lang = ParameterMap.getKeyedString(map, "lang-class", null);
        String langEngine = ParameterMap.getKeyedString(map, "bsf-engine", null);
        String langExtensions[] = ParameterMap.getKeyedString(map, "file-extensions", "").split(",");
        File file = new File(fileName);

        try {
           
            if(lang==null)
                lang = BSFManager.getLangFromFilename(fileName);
                
            if(langEngine!=null && lang!=null && langExtensions.length > 0 ){
                BSFManager.registerScriptingEngine(lang,langEngine,langExtensions);
            }
            
            if(file.exists() && file.canRead()){   
                    String code = IOUtils.getStringFromReader(new FileReader(file));
                    String status = new String();
                    
                    bsfManager.declareBean("map", map, Map.class);
                    bsfManager.declareBean("ip_addr",svc.getIpAddr(),String.class);
                    bsfManager.declareBean("node_id",svc.getNodeId(),int.class );
                    bsfManager.declareBean("node_label", svc.getNodeLabel(), String.class);
                    bsfManager.declareBean("svc_name", svc.getSvcName(), String.class);
        
                    for(Object obj : map.keySet()){
                        bsfManager.declareBean(obj.toString(),map.get(obj),String.class);
                    }
                    
                    status = bsfManager.eval(lang, "BSFMonitor", 0, 0, code).toString();
                    
                    if(status=="OK"){
                        pollStatus = PollStatus.available();
                    } else {
                        pollStatus = PollStatus.unavailable(status);
                    }
                   
            } else {
                pollStatus = PollStatus.unavailable("can not locate or read file: " + fileName);
            }            

        } catch (BSFException e) {
            pollStatus = PollStatus.unavailable(e.toString());
        } catch (FileNotFoundException e){
           pollStatus = PollStatus.unavailable("Could not find file: " + fileName);
        } catch (IOException e) {
            pollStatus = PollStatus.unavailable(e.toString());
        } finally { 
            bsfManager.terminate();
        }

        return pollStatus;
    }
    
}
