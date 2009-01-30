package org.opennms.netmgt.provision.persist;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class StringXmlCalendarPropertyEditor extends PropertyEditorSupport implements PropertyEditor {
    
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(XMLGregorianCalendarImpl.parse(text));
    }

    public String getAsText() {
        return ((XMLGregorianCalendar)getValue()).toXMLFormat();
    } 
}
