/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.web.controller.alarm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.svclayer.TroubleTicketProxy;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class AlarmTicketController extends MultiActionController {
    private TroubleTicketProxy m_troubleTicketProxy;
    
    public AlarmTicketController() {
        super();
        log().debug("AlarmTicketController created");
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log().debug("AlarmTicketController handleRequestInternal called");
        return super.handleRequestInternal(request, response);
    }
    
    static class CommandBean {
        Integer alarm;
        String redirect;
        public Integer getAlarm() {
            return alarm;
        }
        public void setAlarm(Integer alarm) {
            this.alarm = alarm;
        }
        public String getRedirect() {
            return redirect;
        }
        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }
        
        
    }
    
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response, CommandBean bean) throws Exception {
        m_troubleTicketProxy.createTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response, CommandBean bean) {
        m_troubleTicketProxy.updateTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }
    public ModelAndView close(HttpServletRequest request, HttpServletResponse response, CommandBean bean) {
        m_troubleTicketProxy.closeTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }

    public void setTroubleTicketProxy(TroubleTicketProxy troubleTicketProxy) {
        m_troubleTicketProxy = troubleTicketProxy;
    }

    /**
     * @return logger for this class
     */
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
}
