package org.opennms.netmgt.provision.service.operations;

import java.net.InetAddress;

public class IfSnmpCollector {
    
    public interface SystemGroup {

        String getSysContact();

        String getSysDescr();

        String getSysLocation();

        String getSysObjectID();
        
    }

    public IfSnmpCollector(InetAddress byName) {
        throw new UnsupportedOperationException("This constructor for IfSnmpCollector is not yet implemented");
    }

    public void run() {
        throw new UnsupportedOperationException("IfSnmpCollector.run is not yet implemented");
    }

    public boolean hasSystemGroup() {
        throw new UnsupportedOperationException("IfSnmpCollector.hasSystemGroup is not yet implemented");
    }

    public SystemGroup getSystemGroup() {
        throw new UnsupportedOperationException("IfSnmpCollector.getSystemGroup is not yet implemented");
    }

    public boolean hasIfTable() {
        throw new UnsupportedOperationException("IfSnmpCollector.hasIfTable is not yet implemented");
    }

    public boolean hasIpAddrTable() {
        throw new UnsupportedOperationException("IfSnmpCollector.hasIpAddrTable is not yet implemented");
    }

    public int getIfIndex(InetAddress inetAddr) {
        throw new UnsupportedOperationException("IfSnmpCollector.getIfIndex is not yet implemented");
    }

    public String getIfAlias(int ifIndex) {
        throw new UnsupportedOperationException("IfSnmpCollector.getIfAlias is not yet implemented");
    }

    public String getIfName(int ifIndex) {
        throw new UnsupportedOperationException("IfSnmpCollector.getIfName is not yet implemented");
    }

    public String getIfDescr(int ifIndex) {
        throw new UnsupportedOperationException("IfSnmpCollector.getIfDescr is not yet implemented");
    }

    public Long getIfSpeed(int ifIndex) {
        throw new UnsupportedOperationException("IfSnmpCollector.getIfSpeed is not yet implemented");
    }

    public String getPhysAddr(int ifIndex) {
        throw new UnsupportedOperationException("IfSnmpCollector.getPhysAddr is not yet implemented");
    }

    public int getAdminStatus(int ifIndex) {
        throw new UnsupportedOperationException("IfSnmpCollector.getAdminStatus is not yet implemented");
    }

    public int getIfType(int ifIndex) {
        throw new UnsupportedOperationException("IfSnmpCollector.getIfType is not yet implemented");
    }

    public InetAddress[] getIfAddressAndMask(int ifIndex) {
        throw new UnsupportedOperationException("IfSnmpCollector.getIfAddressAndMask is not yet implemented");
    }

}
