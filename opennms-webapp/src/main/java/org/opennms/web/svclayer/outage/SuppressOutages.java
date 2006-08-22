package org.opennms.web.svclayer.outage;

import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opennms.netmgt.model.OnmsOutage;

public class SuppressOutages {

	private static Integer LONG_TIME = new Integer(100);

	private static final Log log = LogFactory.getLog(SuppressOutages.class);

	public void suppress(Integer outageid, String time,OutageService outageService, String suppressor) {

		OnmsOutage outage = (OnmsOutage) outageService.load(outageid);
		GregorianCalendar suppress = new GregorianCalendar();

		if (time.equals("-1")) {
			// Suppress forever
			suppress.add(GregorianCalendar.YEAR, LONG_TIME);

		} else if (time == "") {
			// Just ignore this for now.

		} else {
			// We just append the time to the suppresstime column.
			suppress.add(GregorianCalendar.MINUTE, Integer.parseInt(time));

		}

		if (time != "") {

			outage.setSuppressTime(suppress.getTime());
			outage.setSuppressedBy(suppressor);
			outageService.update(outage);

		}
	}

}
