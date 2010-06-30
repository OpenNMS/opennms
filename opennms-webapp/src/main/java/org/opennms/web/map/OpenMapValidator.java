/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 6, 2007
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

package org.opennms.web.map;

import javax.servlet.http.HttpServletRequest;
import org.opennms.web.WebSecurityUtils;
import org.springframework.validation.Errors;

/**
 * <p>OpenMapValidator class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class OpenMapValidator extends MapApplianceValidator {
	/** {@inheritDoc} */
	public boolean supports(Class aClass) {
		return aClass.equals(HttpServletRequest.class);
	}

	/** {@inheritDoc} */
	public void validate(Object o, Errors errors) {
		
		
		if (!action.equals(MapsConstants.OPENMAP_ACTION) ) 
			errors.rejectValue("Action", MapsConstants.OPENMAP_ACTION+"Failed" , null, "action should be " + MapsConstants.OPENMAP_ACTION); 
		String mapIdentificator = request.getParameter("MapId");
		if (mapIdentificator == null) 
			errors.rejectValue("MapId", MapsConstants.OPENMAP_ACTION+"Failed" , null, "HttpServletReqiest parameter MapId is required");
		
		try {
			WebSecurityUtils.safeParseInt(mapIdentificator) ;
		} catch (NumberFormatException e) {
			errors.rejectValue("MapId", MapsConstants.OPENMAP_ACTION+"Failed" , null, "MapId is not an Integer");
		}
			
	}
}
