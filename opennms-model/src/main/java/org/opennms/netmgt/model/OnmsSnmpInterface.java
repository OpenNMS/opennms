package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="snmpinterface"
 *     
*/
public class OnmsSnmpInterface extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5396189389666285305L;
    
    private Integer m_id;

    /** identifier field */
    private String m_ipAddr;

    /** identifier field */
    private String m_netMask;

    /** identifier field */
    private String m_physAddr;

    /** identifier field */
    private Integer m_ifIndex;

    /** identifier field */
    private String m_ifDescr;

    /** identifier field */
    private Integer m_ifType;

    /** identifier field */
    private String m_ifName;

    /** identifier field */
    private Long m_ifSpeed;

    /** identifier field */
    private Integer m_ifAdminStatus;

    /** identifier field */
    private Integer m_ifOperStatus;

    /** identifier field */
    private String m_ifAlias;

    private OnmsNode m_node;

    public OnmsSnmpInterface(String ipAddr, int ifIndex, OnmsNode node) {
    	this(ipAddr, new Integer(ifIndex), node);
    }
    
    public OnmsSnmpInterface(String ipaddr, Integer ifIndex, OnmsNode node) {
        m_ipAddr = ipaddr;
        m_ifIndex = ifIndex;
        m_node = node;
        node.getSnmpInterfaces().add(this);
    }

    /** default constructor */
    public OnmsSnmpInterface() {
    }
    
    /**
     * Unique identifier for snmpInterface.
     * 
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="snmpIfNxtId"
     *         
     */
    public Integer getId() {
        return m_id;
    }
    
    public void setId(Integer id) {
        m_id = id;
    }


    /** 
     *                @hibernate.property
     *                 column="ipaddr"
     *                 length="16"
     *             
     */
    public String getIpAddress() {
        return m_ipAddr;
    }

    public void setIpAddress(String ipaddr) {
        m_ipAddr = ipaddr;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpipadentnetmask"
     *                 length="16"
     *             
     */
    public String getNetMask() {
        return m_netMask;
    }

    public void setNetMask(String snmpipadentnetmask) {
        m_netMask = snmpipadentnetmask;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpphysaddr"
     *                 length="12"
     *             
     */
    public String getPhysAddr() {
        return m_physAddr;
    }

    public void setPhysAddr(String snmpphysaddr) {
        m_physAddr = snmpphysaddr;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpifindex"
     *                 length="4"
     *             
     */
    public Integer getIfIndex() {
        return m_ifIndex;
    }

    public void setIfIndex(Integer snmpifindex) {
        m_ifIndex = snmpifindex;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpifdescr"
     *                 length="256"
     *             
     */
    public String getIfDescr() {
        return m_ifDescr;
    }

    public void setIfDescr(String snmpifdescr) {
        m_ifDescr = snmpifdescr;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpiftype"
     *                 length="4"
     *             
     */
    public Integer getIfType() {
        return m_ifType;
    }

    public void setIfType(Integer snmpiftype) {
        m_ifType = snmpiftype;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpifname"
     *                 length="32"
     *             
     */
    public String getIfName() {
        return m_ifName;
    }

    public void setIfName(String snmpifname) {
        m_ifName = snmpifname;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpifspeed"
     *                 length="4"
     *             
     */
    public Long getIfSpeed() {
        return m_ifSpeed;
    }

    public void setIfSpeed(Long snmpifspeed) {
        m_ifSpeed = snmpifspeed;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpifadminstatus"
     *                 length="4"
     *             
     */
    public Integer getIfAdminStatus() {
        return m_ifAdminStatus;
    }

    public void setIfAdminStatus(Integer snmpifadminstatus) {
        m_ifAdminStatus = snmpifadminstatus;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpifoperstatus"
     *                 length="4"
     *             
     */
    public Integer getIfOperStatus() {
        return m_ifOperStatus;
    }

    public void setIfOperStatus(Integer snmpifoperstatus) {
        m_ifOperStatus = snmpifoperstatus;
    }

    /** 
     *                @hibernate.property
     *                 column="snmpifalias"
     *                 length="256"
     *             
     */
    public String getIfAlias() {
        return m_ifAlias;
    }

    public void setIfAlias(String snmpifalias) {
        m_ifAlias = snmpifalias;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="nodeid"         
     *         
     */
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(OnmsNode node) {
        m_node = node;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipaddr", getIpAddress())
            .append("snmpipadentnetmask", getNetMask())
            .append("snmpphysaddr", getPhysAddr())
            .append("snmpifindex", getIfIndex())
            .append("snmpifdescr", getIfDescr())
            .append("snmpiftype", getIfType())
            .append("snmpifname", getIfName())
            .append("snmpifspeed", getIfSpeed())
            .append("snmpifadminstatus", getIfAdminStatus())
            .append("snmpifoperstatus", getIfOperStatus())
            .append("snmpifalias", getIfAlias())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		visitor.visitSnmpInterface(this);
		visitor.visitSnmpInterfaceComplete(this);
	}

}
