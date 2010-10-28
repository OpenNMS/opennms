package org.opennms.netmgt.provision.persist;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>StringXmlCalendarPropertyEditor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class StringXmlCalendarPropertyEditor extends PropertyEditorSupport implements PropertyEditor {
    
    /** {@inheritDoc} */
    public void setAsText(String text) throws IllegalArgumentException {
        try {
            setValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(text));
        } catch (DatatypeConfigurationException e) {
            throw new IllegalArgumentException("Unable to convert " + text + " to and XMLCalender");
        }
    }

    /**
     * <p>getAsText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAsText() {
        return ((XMLGregorianCalendar)getValue()).toXMLFormat();
    } 
}
