package org.opennms.web.command;

public class NodeListCommand {
    private String m_nodename = null;
    private String m_iplike = null;
    private String m_maclike = null;
    private Integer m_service = null;
    private String m_ifAlias = null;
    private String[] m_category1 = null;
    private String[] m_category2 = null;
    private String m_statusViewName = null;
    private String m_statusSite = null;
    private String m_statusRowLabel = null;
    private boolean m_nodesWithOutages = false;
    private boolean m_nodesWithDownAggregateStatus = false;
    private boolean m_listInterfaces = false;
    
    public void setNodename(String nodename) {
        m_nodename = nodename;
    }
    public String getNodename() {
        return m_nodename;
    }
    public boolean hasNodename() {
        return m_nodename != null;
    }
    
    public void setIplike(String iplike) {
        m_iplike = iplike;
    }
    public String getIplike() {
        return m_iplike;
    }
    public boolean hasIplike() {
        return m_iplike != null;
    }
    
    public void setMaclike(String maclike) {
        m_maclike = maclike;
    }
    public String getMaclike() {
        return m_maclike;
    }
    public boolean hasMaclike() {
        return m_maclike != null;
    }
    
    public void setService(Integer service) {
        m_service = service;
    }
    public Integer getService() {
        return m_service;
    }
    public boolean hasService() {
        return m_service != null;
    }
    
    public void setIfAlias(String ifAlias) {
        m_ifAlias = ifAlias;
    }
    public String getIfAlias() {
        return m_ifAlias;
    }
    public boolean hasIfAlias() {
        return m_ifAlias != null;
    }
        
    public void setCategory1(String[] category1) {
        m_category1 = category1;
    }
    public String[] getCategory1() {
        return m_category1;
    }
    public boolean hasCategory1() {
        return m_category1 != null && m_category1.length > 0;
    }
    
    public void setCategory2(String[] category2) {
        m_category2 = category2;
    }
    public String[] getCategory2() {
        return m_category2;
    }
    public boolean hasCategory2() {
        return m_category2 != null && m_category2.length > 0;
    }
    
    public void setStatusViewName(String statusViewName) {
        m_statusViewName = statusViewName;
    }
    public String getStatusViewName() {
        return m_statusViewName;
    }
    public boolean hasStatusViewName() {
        return m_statusViewName != null;
    }
    
    public void setStatusSite(String statusSite) {
        m_statusSite = statusSite;
    }
    public String getStatusSite() {
        return m_statusSite;
    }
    public boolean hasStatusSite() {
        return m_statusSite != null;
    }

    public void setStatusRowLabel(String statusRowLabel) {
        m_statusRowLabel = statusRowLabel;
    }
    public String getStatusRowLabel() {
        return m_statusRowLabel;
    }
    public boolean hasStatusRowLabel() {
        return m_statusRowLabel != null;
    }
    
    public void setNodesWithOutages(boolean nodesWithOutages) {
        m_nodesWithOutages = nodesWithOutages;
    }
    public boolean getNodesWithOutages() {
        return m_nodesWithOutages;
    }
    
    public void setNodesWithDownAggregateStatus(boolean nodesWithDownAggregateStatus) {
        m_nodesWithDownAggregateStatus = nodesWithDownAggregateStatus;
    }
    public boolean getNodesWithDownAggregateStatus() {
        return m_nodesWithDownAggregateStatus;
    }
    
    public void setListInterfaces(boolean listInterfaces) {
        m_listInterfaces = listInterfaces;
    }
    public boolean getListInterfaces() {
        return m_listInterfaces;
    }
}