package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.utils.AlphaNumeric;
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

	public Set getIpInterfaces() {
		
		Set ifsForSnmpIface = new LinkedHashSet();
		for (Iterator it = getNode().getIpInterfaces().iterator(); it.hasNext();) {
			OnmsIpInterface	iface = (OnmsIpInterface) it.next();		
			if (getIfIndex().equals(iface.getIfIndex()))
				ifsForSnmpIface.add(iface);
		}
		return ifsForSnmpIface;
	}

	public CollectionType getCollectionType() {
		CollectionType maxCollType = CollectionType.NO_COLLECT;
		for (Iterator it = getIpInterfaces().iterator(); it.hasNext();) {
			OnmsIpInterface ipIface = (OnmsIpInterface) it.next();
			maxCollType = maxCollType.max(ipIface.getIsSnmpPrimary());
		}
		return maxCollType;		
	}

	public Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public String computePhysAddrForRRD() {
		/*
		 * In order to assure the uniqueness of the RRD file names
		 * we now append the MAC/physical address to the end of
		 * label if it is available.
		 */
		String physAddrForRRD = null;
		if (getPhysAddr() != null) {
			String parsedPhysAddr = AlphaNumeric.parseAndTrim(getPhysAddr());
			if (parsedPhysAddr.length() == 12) {
				physAddrForRRD = parsedPhysAddr;
			} else {
				Category log = log();
				if (log.isDebugEnabled()) {
					log.debug(
							"physAddrForRRD: physical address len "
							+ "is NOT 12, physAddr="
							+ parsedPhysAddr);
				}
			}
		}
		return physAddrForRRD;
	}

	public String computeNameForRRD() {
		/*
		 * Determine the label for this interface. The label will be
		 * used to create the RRD file name which holds SNMP data
		 * retreived from the remote agent. If available ifName is
		 * used to generate the label since it is guaranteed to be
		 * unique. Otherwise ifDescr is used. In either case, all
		 * non alpha numeric characters are converted to underscores
		 * to ensure that the resuling string will make a decent
		 * file name and that RRD won't have any problems using it
		 */
		String label = null;
		if (getIfName() != null) {
			label = AlphaNumeric.parseAndReplace(getIfName(), '_');
		} else if (getIfDescr() != null) {
			label = AlphaNumeric.parseAndReplace(getIfDescr(), '_');
		} else {
			log().warn("Interface ("+this+") has no ifName and no ifDescr...setting to label to 'no_ifLabel'.");
			label = "no_ifLabel";
		}
		return label;
	}

	public String computeLabelForRRD() {
		String name = computeNameForRRD();
		String physAddrForRRD = computePhysAddrForRRD();
		String label = (physAddrForRRD == null ? name : name + '-' + physAddrForRRD);
		return label;
	}

}
