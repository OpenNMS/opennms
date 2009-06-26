package org.opennms.web.rest;

import java.beans.PropertyEditorSupport;
import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * PropertyEditor suitable for use by BeanWrapperImpl, so that we can accept xsd:datetime formatted dates
 * in query strings.
 * Also handles "epoch" style dates, if they exist.  Could be extended to guess the date format and do something
 * useful with it
 * @author miskellc
 *
 */
public class ISO8601DateEditor extends PropertyEditorSupport {
    static final DateTimeFormatter m_formatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
    
    public ISO8601DateEditor() {
        super();
        
    }
	@Override
	public String getAsText() {
		Date date=(Date)super.getValue();
		return m_formatter.print(date.getTime());
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		Date date;
		try {
			int epoch=Integer.parseInt(text);
			date=new Date(epoch);
		} catch (NumberFormatException e) {
		    date = new Date(m_formatter.parseMillis(text));
		}
		super.setValue(date);
	}

	/**
	 * No, we don't do GUIs.  Sod off
	 */
	@Override
	public boolean isPaintable() {
		return false;
	}

}
