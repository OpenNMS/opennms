package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="servicemap"
 *     
*/
public class OnmsServiceMap implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6550492519192174055L;
    
    private Integer id;

    /** identifier field */
    private String ipaddr;

    /** identifier field */
    private String servicemapname;

    /** full constructor */
    public OnmsServiceMap(String ipaddr, String servicemapname) {
        this.ipaddr = ipaddr;
        this.servicemapname = servicemapname;
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
     *                 column="servicemapname"
     *                 length="32"
     *             
     */
    public String getServicemapname() {
        return this.servicemapname;
    }

    public void setServicemapname(String servicemapname) {
        this.servicemapname = servicemapname;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipaddr", getIpaddr())
            .append("servicemapname", getServicemapname())
            .toString();
    }

}
