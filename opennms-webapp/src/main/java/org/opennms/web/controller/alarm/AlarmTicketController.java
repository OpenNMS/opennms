/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: May 1, 2007
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
package org.opennms.web.controller.alarm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.svclayer.TroubleTicketProxy;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * <p>AlarmTicketController class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.6.12
 */
public class AlarmTicketController extends MultiActionController {
    private TroubleTicketProxy m_troubleTicketProxy;
    
    /**
     * <p>Constructor for AlarmTicketController.</p>
     */
    public AlarmTicketController() {
        super();
        log().debug("AlarmTicketController created");
    }

    /** {@inheritDoc} */
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
    
    /**
     * <p>create</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response a {@link javax.servlet.http.HttpServletResponse} object.
     * @param bean a {@link org.opennms.web.controller.alarm.AlarmTicketController.CommandBean} object.
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     * @throws java.lang.Exception if any.
     */
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response, CommandBean bean) throws Exception {
        m_troubleTicketProxy.createTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }
    /**
     * <p>update</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response a {@link javax.servlet.http.HttpServletResponse} object.
     * @param bean a {@link org.opennms.web.controller.alarm.AlarmTicketController.CommandBean} object.
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     */
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response, CommandBean bean) {
        m_troubleTicketProxy.updateTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }
    /**
     * <p>close</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param response a {@link javax.servlet.http.HttpServletResponse} object.
     * @param bean a {@link org.opennms.web.controller.alarm.AlarmTicketController.CommandBean} object.
     * @return a {@link org.springframework.web.servlet.ModelAndView} object.
     */
    public ModelAndView close(HttpServletRequest request, HttpServletResponse response, CommandBean bean) {
        m_troubleTicketProxy.closeTicket(bean.getAlarm());
        return new ModelAndView("redirect:"+bean.getRedirect());
    }

    /**
     * <p>setTroubleTicketProxy</p>
     *
     * @param troubleTicketProxy a {@link org.opennms.web.svclayer.TroubleTicketProxy} object.
     */
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
