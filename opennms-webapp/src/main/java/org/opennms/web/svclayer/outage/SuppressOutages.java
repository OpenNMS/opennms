/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2007 Jul 24: Organize imports, format code, remove unused code. - dj@opennms.org
 * 
 *
 * Created: August 20, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.outage;

import java.util.GregorianCalendar;

import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.WebSecurityUtils;

/**
 * <p>SuppressOutages class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:djgregor@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:djgregor@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:djgregor@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class SuppressOutages {

	private static Integer LONG_TIME = new Integer(100);

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
