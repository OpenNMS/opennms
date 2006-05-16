package org.opennms.netmgt.dao.jdbc.snmpif;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class LazySnmpInterface extends OnmsSnmpInterface {
	
	private boolean m_loaded = false;
	private DataSource m_dataSource;
	private boolean m_dirty;
	
	public LazySnmpInterface(DataSource dataSource) {
		m_dataSource = dataSource;
	}

    private void load() {
        if (!m_loaded) {
            SnmpInterfaceId id = new SnmpInterfaceId(this);
            FindById.get(m_dataSource, id).find(id);
        }
    }

    public void setLoaded(boolean loaded) {
        m_loaded = loaded;
    }
    
    public boolean isLoaded() {
        return m_loaded;
    }

    public boolean isDirty() {
        return m_dirty;
    }
    
    public void setDirty(boolean dirty) {
        m_dirty = dirty;
    }

    
    /*Override*/
    public Integer getIfAdminStatus() {
        load();
        return super.getIfAdminStatus();
    }

    /*Override*/
    public String getIfAlias() {
        load();
        return super.getIfAlias();
    }

    /*Override*/
    public String getIfDescr() {
        load();
        return super.getIfDescr();
    }

    /*Override*/
    public String getIfName() {
        load();
        return super.getIfName();
    }

    /*Override*/
    public Integer getIfOperStatus() {
        load();
        return super.getIfOperStatus();
    }

    /*Override*/
    public Long getIfSpeed() {
        load();
        return super.getIfSpeed();
    }

    /*Override*/
    public Integer getIfType() {
        load();
        return super.getIfType();
    }

    /*Override*/
    public String getNetMask() {
        load();
        return super.getNetMask();
    }

    /*Override*/
    public String getPhysAddr() {
        load();
        return super.getPhysAddr();
    }

    /*Override*/
    public void setIfAdminStatus(Integer snmpifadminstatus) {
        load();
        setDirty(true);
        super.setIfAdminStatus(snmpifadminstatus);
    }

    /*Override*/
    public void setIfAlias(String snmpifalias) {
        load();
        setDirty(true);
        super.setIfAlias(snmpifalias);
    }

    /*Override*/
    public void setIfDescr(String snmpifdescr) {
        load();
        setDirty(true);
        super.setIfDescr(snmpifdescr);
    }

    /*Override*/
    public void setIfName(String snmpifname) {
        load();
        setDirty(true);
        super.setIfName(snmpifname);
    }

    /*Override*/
    public void setIfOperStatus(Integer snmpifoperstatus) {
        load();
        setDirty(true);
        super.setIfOperStatus(snmpifoperstatus);
    }

    /*Override*/
    public void setIfSpeed(Long snmpifspeed) {
        load();
        setDirty(true);
        super.setIfSpeed(snmpifspeed);
    }

    /*Override*/
    public void setIfType(Integer snmpiftype) {
        load();
        setDirty(true);
        super.setIfType(snmpiftype);
    }

    /*Override*/
    public void setNetMask(String snmpipadentnetmask) {
        load();
        setDirty(true);
        super.setNetMask(snmpipadentnetmask);
    }

    /*Override*/
    public void setPhysAddr(String snmpphysaddr) {
        load();
        setDirty(true);
        super.setPhysAddr(snmpphysaddr);
    }

    /*Override*/
    public String toString() {
        load();
        return super.toString();
    }

}
