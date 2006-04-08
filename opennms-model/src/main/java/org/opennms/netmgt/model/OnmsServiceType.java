package org.opennms.netmgt.model;

import java.io.Serializable;

import org.springframework.core.style.ToStringCreator;


/** 
 * @hibernate.class table="service"
 *     
*/
public class OnmsServiceType implements Serializable {

    private static final long serialVersionUID = -459218937667452586L;

    /** identifier field */
    private Integer m_id;

    /** persistent field */
    private String m_name;

    /** full constructor */
    public OnmsServiceType(String servicename) {
        m_name = servicename;
    }

    /** default constructor */
    public OnmsServiceType() {
    }

    /** 
     * @hibernate.id generator-class="native" column="serviceid"
     * @hibernate.generator-param name="sequence" value="serviceNxtId"
     */
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer serviceid) {
        m_id = serviceid;
    }

    /** 
     * @hibernate.property column="servicename" unique="true" not-null="true"
     *         
     */
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("id", getId())
            .append("name", getName())
            .toString();
    }

    public boolean equals(Object obj) {
        if (obj instanceof OnmsServiceType) {
            OnmsServiceType t = (OnmsServiceType)obj;
            return m_id.equals(t.m_id);
        }
        return false;
    }

    public int hashCode() {
        return m_id.intValue();
    }

}
