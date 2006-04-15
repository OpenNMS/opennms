package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="servicemap"
 *     
*/
public class OnmsServiceMap extends OnmsEntity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6550492519192174055L;
    
    private Integer m_id;

    /** identifier field */
    private String m_ipAddr;

    /** identifier field */
    private String m_serviceMapName;

    /** full constructor */
    public OnmsServiceMap(String ipAddr, String serviceMapName) {
        this.m_ipAddr = ipAddr;
        this.m_serviceMapName = serviceMapName;
    }

    /** default constructor */
    public OnmsServiceMap() {
    }
    

    /**
     * 
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="svcMapNxtId"
     *         
     */
public Integer getId() {
        return m_id;
    }
    
    public void setId(Integer id) {
        this.m_id = id;
    }

    /** 
     *                @hibernate.property
     *                 column="ipAddr"
     *                 length="16"
     *             
     */
    public String getIpAddr() {
        return this.m_ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.m_ipAddr = ipAddr;
    }

    /** 
     *                @hibernate.property
     *                 column="serviceMapName"
     *                 length="32"
     *             
     */
    public String getServiceMapName() {
        return this.m_serviceMapName;
    }

    public void setServiceMapName(String serviceMapName) {
        this.m_serviceMapName = serviceMapName;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipAddr", getIpAddr())
            .append("serviceMapName", getServiceMapName())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		// TODO Auto-generated method stub
		throw new RuntimeException("visitor method not implemented");
		
	}

}
