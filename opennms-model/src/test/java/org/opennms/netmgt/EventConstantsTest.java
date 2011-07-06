/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

public class EventConstantsTest {
	private static final DateFormat ENGLISH = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, new Locale("en", "US"));
	private static final DateFormat ITALIAN = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, new Locale("it", "IT"));
	private static final DateFormat FRENCH = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, new Locale("fr", "FR"));
	private static final DateFormat GERMAN = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, new Locale("de", "DE"));

	final String englishTimeText = "Thursday, March 10, 2011 5:40:37 PM EST";
	final String italianTimeText = "Gioved“, 10 Marzo 2011 22:40:37 o'clock GMT";
	final String frenchTimeText = "Jeudi, 10 Mars 2011 22:40:37 o'clock GMT";
	final String germanTimeText = "Donnerstag, 10 MŠrz 2011 22:40:37 o'clock GMT";
	
	final String englishTimeTextf = "Thursday, March 10, 2011 11:40:37 PM CET";
	final String italianTimeTextf = "gioved“ 10 marzo 2011 23.40.37 CET";
	final String frenchTimeTextf = "jeudi 10 mars 2011 23 h 40 CET";
	final String germanTimeTextf = "Donnerstag, 10. MŠrz 2011 23:40 Uhr MEZ";
	
	final long sampleTimeEpoch = 1299796837 * 1000L;

    @Test
    public void testEventDateParse() throws Exception {
    	//Locale.setDefault(new Locale("en", "EN"));
    	//Locale.setDefault(new Locale("it", "IT"));
    	//Locale.setDefault(new Locale("fr", "FR"));
    	//Locale.setDefault(new Locale("de", "DE"));
        final Date date = EventConstants.parseToDate(getTimeText());
        assertEquals(sampleTimeEpoch, date.getTime());
        assertEquals(getTimeTextFormatted(), getDateFormat().format(date));
    }
    
    private String getTimeText() {
    	if (getLocaleString().equals("it_IT"))
    		return italianTimeText;
    	else if (getLocaleString().equals("fr_FR"))
    		return frenchTimeText;
    	else if (getLocaleString().equals("de_DE"))
    		return germanTimeText;
    	return englishTimeText;
    }
    
    private String getTimeTextFormatted() {
    	if (getLocaleString().equals("it_IT"))
    		return italianTimeTextf;
    	else if (getLocaleString().equals("fr_FR"))
    		return frenchTimeTextf;
    	else if (getLocaleString().equals("de_DE"))
    		return germanTimeTextf;
    	return englishTimeTextf;
    }
    

    private DateFormat getDateFormat() {
    	if (getLocaleString().equals("it_IT"))
    		return ITALIAN;
    	else if (getLocaleString().equals("fr_FR"))
    		return FRENCH;
    	else if (getLocaleString().equals("de_DE"))
    		return GERMAN;
    	return ENGLISH;
    	
    }
    
    private String getLocaleString() {
    	return Locale.getDefault().getLanguage()+"_"+Locale.getDefault().getCountry();
    }
}
