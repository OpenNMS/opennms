package org.opennms.netmgt.provision.persist;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * <p>StringXmlCalendarPropertyEditor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class StringXmlCalendarPropertyEditor extends PropertyEditorSupport implements PropertyEditor {
    
    /** {@inheritDoc} */
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(XMLGregorianCalendarImpl.parse(text));
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
