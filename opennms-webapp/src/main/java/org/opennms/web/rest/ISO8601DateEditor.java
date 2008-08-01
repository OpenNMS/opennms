package org.opennms.web.rest;

import java.beans.PropertyEditorSupport;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;

/**
 * PropertyEditor suitable for use by BeanWrapperImpl, so that we can accept xsd:datetime formatted dates
 * in query strings.
 * Also handles "epoch" style dates, if they exist.  Could be extended to guess the date format and do something
 * useful with it
 * @author miskellc
 *
 */
public class ISO8601DateEditor extends PropertyEditorSupport {

	@Override
	public String getAsText() {
		Date date=(Date)super.getValue();
		Calendar cal=new GregorianCalendar();
		cal.setTime(date);
		return DatatypeConverter.printDateTime(cal);
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		Date date;
		try {
			int epoch=Integer.parseInt(text);
			date=new Date(epoch);
		} catch (NumberFormatException e) {
			//Doesn't parse as an int (epoch); try as a proper xsd:datetime
			date=DatatypeConverter.parseDateTime(text).getTime();
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
