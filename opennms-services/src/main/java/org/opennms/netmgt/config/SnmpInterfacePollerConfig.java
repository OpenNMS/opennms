package org.opennms.netmgt.config;

import java.io.IOException;

import java.util.List;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

public interface SnmpInterfacePollerConfig {
    public int getThreads();
    public String getService();
    public String[] getCriticalServiceIds();
    public List<String> getAllPackageMatches(String ipaddr);
    public String getPackageName(String ipaddr);
    public Set<String> getInterfaceOnPackage(String pkgName);
    public boolean getStatus(String pkgName,String pkgInterfaceName);
    public long getInterval(String pkgName,String pkgInterfaceName);
    public String getCriteria(String pkgName,String pkgInterfaceName);
    public boolean hasPort(String pkgName,String pkgInterfaceName);
    public int getPort(String pkgName,String pkgInterfaceName);
    public boolean hasTimeout(String pkgName,String pkgInterfaceName);
    public int getTimeout(String pkgName,String pkgInterfaceName);
    public boolean hasRetries(String pkgName,String pkgInterfaceName);
    public int getRetries(String pkgName,String pkgInterfaceName);
    public boolean hasMaxVarsPerPdu(String pkgName,String pkgInterfaceName);
    public int getMaxVarsPerPdu(String pkgName,String pkgInterfaceName);
    public void rebuildPackageIpListMap();
    public void update() throws IOException, MarshalException, ValidationException;
}
