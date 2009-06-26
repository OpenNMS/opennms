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
 * 2007 Dec 12: Convert strings to appropriate objects for detached
 *              criteria restrictions. - dj@opennms.org
 * 2007 Dec 09: Add abililty to filter on one or two sets of
 *              categories, like in the node list. - dj@opennms.org
 * 2007 Feb 01: Indent and add buildCriteria method. - dj@opennms.org
 * 
 * Created: August 23, 2006
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

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.web.WebSecurityUtils;

/**
 * 
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class OutagesFilteringView {

    private CategoryDao m_categoryDao;

    // String whoooha = "select 1154363839::int4::abstime;";

    // Possible values returned to me

    public String filterQuery(HttpServletRequest request) {

        String queryResult = "";
        Locale locale = Locale.getDefault();
        SimpleDateFormat d_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                                         locale);

        if (request.getQueryString() != null ) {

            StringTokenizer st = new StringTokenizer(request.getQueryString(), "&");


            while (st.hasMoreTokens()) {
                String temp = st.nextToken();
                String parameterName = temp.substring(0, temp.indexOf('='));
                String parameterValue = temp.substring(temp.indexOf('=') + 1, temp.length());

                // node
                if (parameterName.startsWith("nodeid")) {

                    queryResult = queryResult + " AND outages.nodeid = '"
                    + parameterValue + "'";
                }

                if (parameterName.startsWith("not_nodeid")) {
                    queryResult = queryResult + " AND outages.nodeid <> '"
                    + parameterValue + "\'";
                }

                if (parameterName.startsWith("ipaddr")) {
                    queryResult = queryResult + " AND outages.ipaddr ='"
                    + parameterValue + "'";
                }

                if (parameterName.startsWith("not_ipaddr")) {
                    queryResult = queryResult + " AND outages.ipaddr <> '"
                    + parameterValue + "'";
                }

                if (parameterName.startsWith("smaller_iflostservice")) {
                    Date date = new Date(WebSecurityUtils.safeParseLong(parameterValue));
                    queryResult = queryResult + " AND outages.iflostservice < "
                    + "'" + d_format.format(date) + "'";

                }

                if (parameterName.startsWith("bigger_iflostservice")) {
                    Date date = new Date(WebSecurityUtils.safeParseLong(parameterValue));
                    queryResult = queryResult + " AND outages.iflostservice > "
                    + "'" + d_format.format(date) + "'";

                }

                if (parameterName.startsWith("smaller_ifregainedservice")) {
                    Date date = new Date(WebSecurityUtils.safeParseLong(parameterValue));
                    queryResult = queryResult + " AND outages.iflostservice < "
                    + "'" + d_format.format(date) + "'";
                }

                if (parameterName.startsWith("bigger_ifregainedservice")) {
                    Date date = new Date(WebSecurityUtils.safeParseLong(parameterValue));
                    queryResult = queryResult + " AND outages.iflostservice > "
                    + "'" + d_format.format(date) + "'";
                }

            }
        }

        return queryResult;
    }

    public OnmsCriteria buildCriteria(HttpServletRequest request) {
        OnmsCriteria criteria = new OnmsCriteria(OnmsOutage.class);

        if (request.getParameter("nodeid") != null && request.getParameter("nodeid").length() > 0) {
            criteria.add(Restrictions.eq("node.id", WebSecurityUtils.safeParseInt(request.getParameter("nodeid"))));
        }

        if (request.getParameter("not_nodeid") != null && request.getParameter("not_nodeid").length() > 0) {
            criteria.add(Restrictions.ne("node.id", WebSecurityUtils.safeParseInt(request.getParameter("not_nodeid"))));
        }

        if (request.getParameter("ipinterfaceid") != null  && request.getParameter("ipinterfaceid").length() > 0) {
            criteria.add(Restrictions.eq("ipInterface.id", WebSecurityUtils.safeParseInt(request.getParameter("ipinterfaceid"))));
        }

        if (request.getParameter("not_ipinterfaceid") != null && request.getParameter("not_ipinterfaceid").length() > 0) {
            criteria.add(Restrictions.ne("ipInterface.id", WebSecurityUtils.safeParseInt(request.getParameter("not_ipinterfaceid"))));
        }

        if (request.getParameter("serviceid") != null && request.getParameter("serviceid").length() > 0) {
            criteria.add(Restrictions.eq("monitoredService.serviceType.id", WebSecurityUtils.safeParseInt(request.getParameter("serviceid"))));
        }

        if (request.getParameter("not_serviceid") != null && request.getParameter("not_serviceid").length() > 0) {
            criteria.add(Restrictions.ne("monitoredService.serviceType.id", WebSecurityUtils.safeParseInt(request.getParameter("not_serviceid"))));
        }
        
        if (request.getParameter("ifserviceid") != null && request.getParameter("ifserviceid").length() > 0) {
            criteria.add(Restrictions.eq("monitoredService.id", WebSecurityUtils.safeParseInt(request.getParameter("ifserviceid"))));
        }

        if (request.getParameter("not_ifserviceid") != null && request.getParameter("not_ifserviceid").length() > 0) {
            criteria.add(Restrictions.ne("monitoredService.id", WebSecurityUtils.safeParseInt(request.getParameter("not_ifserviceid"))));
        }

        if (request.getParameter("smaller_iflostservice") != null && request.getParameter("smaller_iflostservice").length() > 0) {
            Date date = new Date(WebSecurityUtils.safeParseLong(request.getParameter("smaller_iflostservice")));
            criteria.add(Restrictions.lt("ifLostService", date));
        }

        if (request.getParameter("bigger_iflostservice") != null && request.getParameter("bigger_iflostservice").length() > 0) {
            Date date = new Date(WebSecurityUtils.safeParseLong(request.getParameter("bigger_iflostservice")));
            criteria.add(Restrictions.gt("ifLostService", date));
        }

        if (request.getParameter("smaller_ifregainedservice") != null && request.getParameter("smaller_ifregainedservice").length() > 0) {
            Date date = new Date(WebSecurityUtils.safeParseLong(request.getParameter("smaller_ifregainedservice")));
            criteria.add(Restrictions.lt("ifRegainedService", date));
        }

        if (request.getParameter("bigger_ifregainedservice") != null && request.getParameter("bigger_ifregainedservice").length() > 0) {
            Date date = new Date(WebSecurityUtils.safeParseLong(request.getParameter("bigger_ifregainedservice")));
            criteria.add(Restrictions.gt("ifRegainedService", date));
        }

        if (request.getParameter("building") != null && request.getParameter("building").length() > 0) {
            criteria.createAlias("node.assetRecord", "assetRecord");
            criteria.add(Restrictions.eq("assetRecord.building", request.getParameter("building")));
        }
        
        if (request.getParameter("category1") != null && request.getParameter("category1").length() > 0 && request.getParameter("category2") != null && request.getParameter("category2").length() > 0) {
            for (Criterion criterion : m_categoryDao.getCriterionForCategorySetsUnion(request.getParameterValues("category1"), request.getParameterValues("category2"))) {
                criteria.add(criterion);
            }
        } else if (request.getParameter("category1") != null && request.getParameter("category1").length() > 0) {
            for (Criterion criterion : m_categoryDao.getCriterionForCategorySetsUnion(request.getParameterValues("category1"))) {
                criteria.add(criterion);
            }
        }

        if ("true".equals(request.getParameter("currentOutages"))) {
            criteria.add(Restrictions.isNull("ifRegainedService"));
        }

        if ("true".equals(request.getParameter("resolvedOutages"))) {
            criteria.add(Restrictions.isNotNull("ifRegainedService"));
        }

        return criteria;
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

}