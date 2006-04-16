package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="servermap"
 *     
*/
public class OnmsServerMap extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -858347716282069343L;

    /** identifier field */
    private String m_ipAddr;

    /** identifier field */
    private String m_serverName;

    private Integer m_id;

    /** full constructor */
    public OnmsServerMap(String ipAddr, String serverName) {
        m_ipAddr = ipAddr;
        m_serverName = serverName;
    }

    /** default constructor */
    public OnmsServerMap() {
    }

    /**
     * 
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="svrMapNxtId"
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
     *                 column="ipAddr"
     *                 length="16"
     *             
     */
    public String getIpAddr() {
        return m_ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        m_ipAddr = ipAddr;
    }

    /** 
     *                @hibernate.property
     *                 column="serverName"
     *                 length="64"
     *             
     */
    public String getServerName() {
        return m_serverName;
    }

    public void setServerName(String serverName) {
        m_serverName = serverName;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipAddr", getIpAddr())
            .append("serverName", getServerName())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");
	}

}
