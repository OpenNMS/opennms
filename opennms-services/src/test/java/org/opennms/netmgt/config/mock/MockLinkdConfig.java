package org.opennms.netmgt.config.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.LinkdConfig;
import org.opennms.netmgt.config.LinkdConfigManager;
import org.opennms.netmgt.config.linkd.LinkdConfiguration;
import org.opennms.netmgt.config.linkd.Package;

public class MockLinkdConfig implements LinkdConfig {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();

    @Override
    public List<InetAddress> getIpList(final Package pkg) {
        return Collections.emptyList();
    }

    @Override
    public boolean isInterfaceInPackage(final InetAddress iface, final Package pkg) {
        return false;
    }

    @Override
    public boolean isInterfaceInPackageRange(final InetAddress iface, final Package pkg) {
        return false;
    }

    @Override
    public Package getFirstPackageMatch(final InetAddress ipaddr) {
        return null;
    }

    @Override
    public List<String> getAllPackageMatches(final InetAddress ipAddr) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAutoDiscoveryEnabled() {
        return false;
    }

    @Override
    public boolean isVlanDiscoveryEnabled() {
        return false;
    }

    @Override
    public Enumeration<Package> enumeratePackage() {
        final List<Package> list = Collections.emptyList();
        return Collections.enumeration(list);
    }

    @Override
    public Package getPackage(final String pkgName) {
        return null;
    }

    @Override
    public int getThreads() {
        return 1;
    }

    @Override
    public boolean enableDiscoveryDownload() {
        return false;
    }

    @Override
    public boolean useIpRouteDiscovery() {
        return false;
    }

    @Override
    public boolean forceIpRouteDiscoveryOnEthernet() {
        return false;
    }

    @Override
    public boolean saveRouteTable() {
        return false;
    }

    @Override
    public boolean useCdpDiscovery() {
        return false;
    }

    @Override
    public boolean useBridgeDiscovery() {
        return false;
    }

    @Override
    public boolean saveStpNodeTable() {
        return false;
    }

    @Override
    public boolean saveStpInterfaceTable() {
        return false;
    }

    @Override
    public long getInitialSleepTime() {
        return 3000;
    }

    @Override
    public long getSnmpPollInterval() {
        return 3000;
    }

    @Override
    public long getDiscoveryLinkInterval() {
        return 3000;
    }

    @Override
    public void update() {
    }

    @Override
    public void save() throws MarshalException, IOException, ValidationException {
    }

    @Override
    public LinkdConfiguration getConfiguration() {
        return new LinkdConfiguration();
    }

    @Override
    public String getVlanClassName(final String sysoid) {
        return null;
    }

    @Override
    public boolean hasClassName(final String sysoid) {
        return false;
    }

    @Override
    public Lock getReadLock() {
        return m_readLock;
    }

    @Override
    public Lock getWriteLock() {
        return m_writeLock;
    }

    @Override
    public boolean hasIpRouteClassName(final String sysoid) {
        return false;
    }

    @Override
    public String getIpRouteClassName(final String sysoid) {
        return LinkdConfigManager.DEFAULT_IP_ROUTE_CLASS_NAME;
    }

    @Override
    public String getDefaultIpRouteClassName() {
        return LinkdConfigManager.DEFAULT_IP_ROUTE_CLASS_NAME;
    }

    @Override
    public void reload() throws IOException, MarshalException,
            ValidationException {        
    }

}
