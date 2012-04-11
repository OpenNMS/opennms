package org.opennms.netmgt.xml.eventconf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.exolab.castor.xml.Unmarshaller;

/**
 * Object used to identify which alarm fields should be updated during Alarm reduction.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 */
@XmlRootElement(name="update-field")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateField {
    
    @XmlAttribute(name="field-name", required=true)
    private java.lang.String m_fieldName;
    
    @XmlAttribute(name="update-on-reduction", required=false)
    private java.lang.Boolean m_updateOnReduction = Boolean.TRUE;

    public boolean hasFieldName() {
        return m_fieldName != null ? true : false;
    }
    
    public String getFieldName() {
        return m_fieldName;
    }

    public void setFieldName(String fieldName) {
        m_fieldName = fieldName;
    }
    
    public boolean hasUpdateOnReduction() {
        return m_updateOnReduction != null ? true : false; 
    }
    
    public Boolean isUpdateOnReduction() {
        return m_updateOnReduction;
    }
    
    public void setUpdateOnReduction(Boolean update) {
        m_updateOnReduction = update;
    }
    
    /**
     * Method unmarshal.
     * 
     * @param reader
     * @throws org.exolab.castor.xml.MarshalException if object is
     * null or if any SAXException is thrown during marshaling
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     * @return the unmarshaled
     * org.opennms.netmgt.xml.eventconf.AlarmData
     */
    public static UpdateField unmarshal(final java.io.Reader reader) throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (UpdateField) Unmarshaller.unmarshal(UpdateField.class, reader);
    }

    /**
     * 
     * 
     * @throws org.exolab.castor.xml.ValidationException if this
     * object is an invalid instance according to the schema
     */
    public void validate() throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}