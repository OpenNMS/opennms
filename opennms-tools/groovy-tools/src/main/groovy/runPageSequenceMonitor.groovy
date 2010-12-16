#!/usr/bin/env groovy

import org.springframework.util.*;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.poller.*;
import org.opennms.netmgt.poller.monitors.*;

public class runPageSequenceMonitor implements MonitoredService {
    private int m_nodeId = 21;
    private String m_nodeLabel = "nodeLabel";
    private String m_ipAddr;
    private String m_svcName = "PSMTest";
    private InetAddress m_inetAddr;

    public void setSvcName(String svcName) {
        m_svcName = svcName;
    }

    public String getSvcName() {
        return m_svcName;
    }

    public String getIpAddr() {
        return m_ipAddr;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public NetworkInterface getNetInterface() {
        return new IPv4NetworkInterface(getAddress());
    }

    public InetAddress getAddress() {
        return m_inetAddr;
    }

    public void setIpAddr(String ipAddr) throws UnknownHostException {
        m_ipAddr = ipAddr;
        m_inetAddr = InetAddress.getByName(ipAddr);
    }

    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }

    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }


    public static void main(String[] args) {
        
        if (args.length != 2) {
            System.err.println "usage runPageSequenceMonitor <ip-address> <page-sequence-file>"
            System.exit(1);
        }

        String ipAddr = args[0];
        println ipAddr;
        MonitoredService monSvc = new runPageSequenceMonitor(ipAddr:ipAddr);	  
        String pageSequenceConfig = new File(args[1]).getText();
        println pageSequenceConfig;
    
        Map parms = [retry:'1', timeout:'20000', 'page-sequence':pageSequenceConfig];
	  
        PageSequenceMonitor monitor = new PageSequenceMonitor();
	  
        monitor.initialize(parms);
        monitor.initialize(monSvc);
        PollStatus status = monitor.poll(monSvc, parms);
        println "${status} : ${status.reason}";
    }
}
