package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collection;
import java.util.List;


public class AliasedResource extends CollectionResource {
    
    private IfInfo m_ifInfo;
    private String m_ifAliasComment;
    private String m_domain;

    public AliasedResource(String domain, IfInfo ifInfo, String ifAliasComment) {
        m_domain = domain;
        m_ifInfo = ifInfo;
        m_ifAliasComment = ifAliasComment;
    }
    
    public IfInfo getIfInfo() {
        return m_ifInfo;
    }

    public Collection getAttributeList() {
        return m_ifInfo.getAttributeList();
    }

    String getAliasDir() {
        return getIfInfo().getAliasDir(m_ifAliasComment);
    }

    public String getDomain() {
        return m_domain;
    }

    protected File getResourceDir(File rrdBaseDir) {
        File domainDir = new File(rrdBaseDir, getDomain());
        File aliasDir = new File(domainDir, getAliasDir());
        return aliasDir;
    }

    public CollectionAgent getCollectionAgent() {
        return getIfInfo().getCollectionAgent();
    }

    public String toString() {
        return getDomain()+'/'+getAliasDir()+" ["+m_ifInfo+']';
    }

    protected SNMPCollectorEntry getEntry() {
        return getIfInfo().getEntry();
    }

    void checkForAliasChanged(ForceRescanState forceRescanState) {
        getIfInfo().checkForChangedIfAlias(forceRescanState);
    }

    public boolean isScheduledForCollection() {
        return getIfInfo().isScheduledForCollection();
    }

    public boolean shouldPersist(ServiceParameters serviceParameters) {
        return (serviceParameters.aliasesEnabled() && getAliasDir() != null) && (isScheduledForCollection() || serviceParameters.forceStoreByAlias(getAliasDir()));
    }

}
