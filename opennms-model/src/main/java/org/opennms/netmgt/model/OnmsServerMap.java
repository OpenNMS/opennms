package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="servermap"
 *     
*/
public class OnmsServerMap implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -858347716282069343L;

    /** identifier field */
    private String ipaddr;

    /** identifier field */
    private String servername;

    private Integer id;

    /** full constructor */
    public OnmsServerMap(String ipaddr, String servername) {
        this.ipaddr = ipaddr;
        this.servername = servername;
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
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    /** 
     *                @hibernate.property
     *                 column="ipaddr"
     *                 length="16"
     *             
     */
    public String getIpaddr() {
        return this.ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    /** 
     *                @hibernate.property
     *                 column="servername"
     *                 length="64"
     *             
     */
    public String getServername() {
        return this.servername;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipaddr", getIpaddr())
            .append("servername", getServername())
            .toString();
    }

}
