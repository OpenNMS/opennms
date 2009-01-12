package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.snmpinterfacepoller.Package;

public interface SnmpInterfacePollerConfig {
    public int getThreads();
    public String getService();
    public boolean suppressAdminDownEvent();
    public String[] getCriticalServiceIds();
    public Enumeration<Package> enumeratePackage();
    public Iterable<Package> packages();
    public Package getPackage(String packageName);
    public Package getPackageForAddress(String ipaddr);
    public List<String> getAllPackageMatches(String ipaddr);
    public boolean interfaceInPackage(String iface, Package pkg);
    public void rebuildPackageIpListMap();
    public void update() throws IOException, MarshalException, ValidationException;
}
