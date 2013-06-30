/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.alarm;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.EventConstants;
import org.opennms.web.svclayer.TroubleTicketProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * <p>AlarmTicketController class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class AlarmTicketController extends MultiActionController {
	
	private static final Logger LOG = LoggerFactory.getLogger(AlarmTicketController.class);

    private TroubleTicketProxy m_troubleTicketProxy;
    
    /**
     * <p>Constructor for AlarmTicketController.</p>
     */
    public AlarmTicketController() {
        super();
        LOG.debug("AlarmTicketController created");
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug("AlarmTicketController handleRequestInternal called");
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
    	Map<String,String> parameters = new HashMap<String, String>();
    	parameters.put(EventConstants.PARM_USER, request.getRemoteUser());
    	@SuppressWarnings("unchecked")
		Enumeration<String> paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()) {        
        	String paramName = paramNames.nextElement();
        	if (!paramName.equals("alarm") || !paramName.equals("redirect"))
        		parameters.put(paramName, request.getParameter(paramName));
        }
    	m_troubleTicketProxy.createTicket(bean.getAlarm(), parameters);
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
}
