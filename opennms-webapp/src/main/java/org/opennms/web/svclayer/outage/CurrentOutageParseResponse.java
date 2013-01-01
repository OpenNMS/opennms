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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.WebSecurityUtils;

/**
 * <p>CurrentOutageParseResponse class.</p>
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class CurrentOutageParseResponse {

    static SuppressOutages m_suppress = new SuppressOutages();


    /**
     * <p>ParseResponse</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     */
    public void ParseResponse(HttpServletRequest request) {
        return;
    }

    /**
     * <p>findSelectedOutagesIDs</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param outageService a {@link org.opennms.web.svclayer.outage.OutageService} object.
     * @return a java$util$Map object.
     */
    public static  Map<String,String> findSelectedOutagesIDs(HttpServletRequest request, OutageService outageService) {
        Map<String, String> myOutages = new HashMap<String, String>();
        @SuppressWarnings("unchecked")
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            if (parameterName.startsWith("chkbx_")) {
                String outageId = StringUtils.substringAfter(parameterName, "chkbx_");
                String parameterValue = request.getParameter(parameterName);
                if (parameterValue.equals(SuppressOutageCheckBoxConstants.SELECTED)) {
                    m_suppress.suppress(WebSecurityUtils.safeParseInt(outageId), request.getParameter("suppresstime_" + outageId),
                            outageService, request.getRemoteUser().toString());

                    myOutages.remove(outageId);
                } else {
                    myOutages.remove(outageId);
                }
            }
        }

        return myOutages;
    }

}
