/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.outage;

import java.util.GregorianCalendar;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsOutage;

/**
 * <p>SuppressOutages class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:djgregor@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class SuppressOutages {
    // me love you, LONG_TIME
    private static Integer LONG_TIME = Integer.valueOf(100);

	/**
	 * <p>suppress</p>
	 *
	 * @param outageid a {@link java.lang.Integer} object.
	 * @param time a {@link java.lang.String} object.
	 * @param outageService a {@link org.opennms.web.svclayer.outage.OutageService} object.
	 * @param suppressor a {@link java.lang.String} object.
	 */
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
			suppress.add(GregorianCalendar.MINUTE, WebSecurityUtils.safeParseInt(time));

		}

		if (time != "") {

			if (time == "-2") {
			    outage.setSuppressTime(null);
			} else {
			    outage.setSuppressTime(suppress.getTime());
			}
			outage.setSuppressedBy(suppressor);
			outageService.update(outage);

		}
	}

}
