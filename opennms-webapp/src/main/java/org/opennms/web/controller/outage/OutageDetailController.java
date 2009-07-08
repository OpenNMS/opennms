/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.controller.outage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.outage.Outage;
import org.opennms.web.outage.OutageIdNotFoundException;
import org.opennms.web.outage.WebOutageRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class OutageDetailController extends AbstractController implements InitializingBean {

    private String m_successView;
    private WebOutageRepository m_webOutageRepository;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int outageId = -1;

        String outageIdString = request.getParameter("id");
        if (outageIdString == null) {
            throw new MissingParameterException("id");
        }

        try {
            outageId = WebSecurityUtils.safeParseInt(outageIdString);
        }
        catch( NumberFormatException e ) {
            throw new OutageIdNotFoundException("The outage id must be an integer.", outageIdString);
        }        

        Outage outage = m_webOutageRepository.getOutage(outageId);

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("outage", outage);
        return modelAndView;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webOutageRepository, "webOutageRepository must be set");
    }

    private String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }
    
    public void setWebOutageRepository(WebOutageRepository webOutageRepository) {
        m_webOutageRepository = webOutageRepository;
    }

}
