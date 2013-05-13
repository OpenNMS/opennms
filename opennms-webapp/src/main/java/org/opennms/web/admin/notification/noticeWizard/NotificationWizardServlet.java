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

package org.opennms.web.admin.notification.noticeWizard;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Parameter;
import org.opennms.netmgt.config.notifications.Varbind;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.web.api.Util;

/**
 * A servlet that handles the data comming in from the notification wizard jsps.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 * @since 1.8.1
 */
public class NotificationWizardServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -8394875468854510137L;

    //SOURCE_PAGE_EVENTS_VIEW is more of a tag than an actual page - can't be used for navigation as is
    /** Constant <code>SOURCE_PAGE_OTHER_WEBUI="eventslist"</code> */
    public static final String SOURCE_PAGE_OTHER_WEBUI = "eventslist";
    
    /** Constant <code>SOURCE_PAGE_NOTICES="eventNotices.jsp"</code> */
    public static final String SOURCE_PAGE_NOTICES = "eventNotices.jsp";
    
    /** Constant <code>SOURCE_PAGE_NOTIFS_FOR_UEI="notifsForUEI.jsp"</code> */
    public static final String SOURCE_PAGE_NOTIFS_FOR_UEI = "notifsForUEI.jsp";

    /** Constant <code>SOURCE_PAGE_UEIS="chooseUeis.jsp"</code> */
    public static final String SOURCE_PAGE_UEIS = "chooseUeis.jsp";

    /** Constant <code>SOURCE_PAGE_RULE="buildRule.jsp"</code> */
    public static final String SOURCE_PAGE_RULE = "buildRule.jsp";

    /** Constant <code>SOURCE_PAGE_VALIDATE="validateRule.jsp"</code> */
    public static final String SOURCE_PAGE_VALIDATE = "validateRule.jsp";

    /** Constant <code>SOURCE_PAGE_PATH_OUTAGE="buildPathOutage.jsp"</code> */
    public static final String SOURCE_PAGE_PATH_OUTAGE = "buildPathOutage.jsp";

    /** Constant <code>SOURCE_PAGE_VALIDATE_PATH_OUTAGE="validatePathOutage.jsp"</code> */
    public static final String SOURCE_PAGE_VALIDATE_PATH_OUTAGE = "validatePathOutage.jsp";

    /** Constant <code>SOURCE_PAGE_PATH="choosePath.jsp"</code> */
    public static final String SOURCE_PAGE_PATH = "choosePath.jsp";

    /** Constant <code>SOURCE_PAGE_NOTIFICATION_INDEX="../index.jsp"</code> */
    public static final String SOURCE_PAGE_NOTIFICATION_INDEX = "../index.jsp";

    private static final String SQL_DELETE_CRITICAL_PATH = "DELETE FROM pathoutage WHERE nodeid=?";

    private static final String SQL_SET_CRITICAL_PATH = "INSERT INTO pathoutage (nodeid, criticalpathip, criticalpathservicename) VALUES (?, ?, ?)";

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sourcePage = request.getParameter("sourcePage");
        HttpSession user = request.getSession(true);

        /*
         * FIXME: Why do we do this for every request in doPost instead of
         * once in init?
         */
        try {
            NotifdConfigFactory.init();
        } catch (Throwable e) {
            throw new ServletException("Failed to initialize NotifdConfigFactory: " + e, e);
        }
        try {
            NotificationFactory.init();
        } catch (Throwable e) {
            throw new ServletException("Failed to initialize NotificationFactory: " + e, e);
        }
        
        String redirect;

        if (sourcePage.equals(SOURCE_PAGE_NOTICES)) {
            redirect = processNotices(request, user);
        } else if (sourcePage.equals(SOURCE_PAGE_UEIS)) {
            redirect = processUeis(request, user);
        } else if (sourcePage.equals(SOURCE_PAGE_RULE)) {
            redirect = processRule(request, user);
        } else if (sourcePage.equals(SOURCE_PAGE_VALIDATE)) {
            redirect = processValidate(request, user);
        } else if (sourcePage.equals(SOURCE_PAGE_PATH)) {
            redirect = processPath(request, user);
        } else if (sourcePage.equals(SOURCE_PAGE_PATH_OUTAGE)) {
            redirect = processPathOutage(request);
        } else if (sourcePage.equals(SOURCE_PAGE_VALIDATE_PATH_OUTAGE)) {
            redirect = processValidatePathOutage(request);
        } else if (sourcePage.equals(SOURCE_PAGE_OTHER_WEBUI)) {
            redirect = processOtherWebUi(request, user); 
        } else if (sourcePage.equals(SOURCE_PAGE_NOTIFS_FOR_UEI)) {
            redirect = processNotificationsForUei(request, user);
        } else {
            // FIXME: What do we do if there is no sourcePage match?
            redirect = "";
        }

        if (redirect.equals("")) {
            throw new ServletException("no redirect specified for this wizard!");
        }

        response.sendRedirect(redirect);
    }

    private String processNotices(HttpServletRequest request, HttpSession user) throws ServletException {
        String userAction = request.getParameter("userAction");

        if (userAction.equals("delete")) {
            try {
                getNotificationFactory().removeNotification(request.getParameter("notice"));
            } catch (Throwable e) {
                throw new ServletException("Couldn't save/reload notifications configuration file: " + e, e);
            }

            return SOURCE_PAGE_NOTICES;
        } else if (userAction.equals("edit")) {
            return edit(request, user);
        } else if (userAction.equals("new")) {
            user.setAttribute("newNotice", buildNewNotification("off"));

            return SOURCE_PAGE_UEIS;
        } else if (userAction.equals("on") || userAction.equals("off")) {
            try {
                getNotificationFactory().updateStatus(request.getParameter("notice"), userAction);
            } catch (Throwable e) {
                throw new ServletException("Couldn't save/reload notifications configuration file: " + e, e);
            }
            
            return SOURCE_PAGE_NOTICES;
        } else {
            // FIXME: We should do something if we hit this
            return "";
        }
    }

    private String processUeis(HttpServletRequest request, HttpSession user) {
        Notification newNotice = (Notification) user.getAttribute("newNotice");
        newNotice.setUei(request.getParameter("uei"));

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newRule", toSingleQuote(newNotice.getRule()));

        return SOURCE_PAGE_RULE + makeQueryString(params);
    }

    private String processValidate(HttpServletRequest request, HttpSession user) {
        String userAction = request.getParameter("userAction");

        if (userAction.equals("rebuild")) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("newRule", request.getParameter("newRule"));
            String services[] = request.getParameterValues("services");
            if (services != null) {
                params.put("services", services);
            }
            params.put("mode", "rebuild");

            return SOURCE_PAGE_RULE + makeQueryString(params);
        } else {
            Notification newNotice = (Notification) user.getAttribute("newNotice");
            newNotice.setRule(request.getParameter("newRule"));

            return SOURCE_PAGE_PATH;
        }
    }

    private String processRule(HttpServletRequest request, HttpSession user) {
        String ruleString = request.getParameter("newRule");
        ruleString = toSingleQuote(ruleString);
        ruleString = stripExtraWhite(ruleString);
        ruleString = stripServices(ruleString);
        ruleString = checkParens(ruleString);
        
        StringBuffer rule = new StringBuffer(ruleString);

        String services[] = request.getParameterValues("services");
        if (services != null) {
            rule.append(" & ").append(" (");

            for (int i = 0; i < services.length; i++) {
                rule.append("is").append(services[i]);
                if (i < services.length - 1) {
                    rule.append(" | ");
                }
            }

            rule.append(" )");
        }

        String notServices[] = request.getParameterValues("notServices");
        if (notServices != null) {
            rule.append(" & ").append(" (");

            for (int i = 0; i < notServices.length; i++) {
                rule.append("!is").append(notServices[i]);
                if (i < notServices.length - 1) {
                    rule.append(" & ");
                }
            }

            rule.append(" )");
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newRule", rule.toString());
        if (services != null) {
            params.put("services", services);
        }
        if (notServices != null) {
            params.put("notServices", notServices);
        }

        // page to redirect to, either validate or skip validation
        String redirectPage = request.getParameter("nextPage");

        // now lets see if the rule is syntactically valid
        try {
            getFilterDao().validateRule(rule.toString());
        } catch (FilterParseException e) {
            // page to redirect to if the rule is invalid
            params.put("mode", "failed");
            redirectPage = SOURCE_PAGE_RULE;
        }

        // save the rule if we are bypassing validation
        if (redirectPage.equals(SOURCE_PAGE_PATH)) {
            Notification newNotice = (Notification) user.getAttribute("newNotice");
            newNotice.setRule(rule.toString());
        }

        return redirectPage + makeQueryString(params);
    }

    private String processPath(HttpServletRequest request, HttpSession user) throws ServletException {
        Notification newNotice = (Notification) user.getAttribute("newNotice");
        newNotice.setDestinationPath(request.getParameter("path"));

        String description = request.getParameter("description");
        if (description != null && !description.trim().equals("")) {
            newNotice.setDescription(description);
        } else {
            newNotice.setDescription(null);
        }

        newNotice.setTextMessage(request.getParameter("textMsg"));

        String subject = request.getParameter("subject");
        if (subject != null && !subject.trim().equals("")) {
            newNotice.setSubject(subject);
        } else {
            newNotice.setSubject(null);
        }

        String numMessage = request.getParameter("numMsg");
        if (numMessage != null && !numMessage.trim().equals("")) {
            newNotice.setNumericMessage(numMessage);
        } else {
            newNotice.setNumericMessage(null);
        }

        String oldName = newNotice.getName();
        newNotice.setName(request.getParameter("name"));

        String varbindName = request.getParameter("varbindName");
        String varbindValue = request.getParameter("varbindValue");

        Varbind varbind=newNotice.getVarbind();           
        if (varbindName != null && !varbindName.trim().equals("") && varbindValue != null && !varbindValue.trim().equals("")) {
            if (varbind == null) {
                varbind = new Varbind();
                newNotice.setVarbind(varbind);
            }
            varbind.setVbname(varbindName);
            varbind.setVbvalue(varbindValue);
        } else {
            // Must do this to allow clearing out varbind definitions
            newNotice.setVarbind(null);
        }

        try {
            // replacing a path with a new name.
            getNotificationFactory().replaceNotification(oldName, newNotice);
        } catch (Throwable e) {
            throw new ServletException("Couldn't save/reload notification configuration file.", e);
        }

        String suppliedReturnPage=(String)user.getAttribute("noticeWizardReturnPage");
        if (suppliedReturnPage != null && !suppliedReturnPage.equals("")) {
            // Remove this attribute once we have consumed it, else the user may later
        	// get returned to a potentially unexpected page here
        	user.removeAttribute("noticeWizardReturnPage");
        	return suppliedReturnPage;
        } else {
            return SOURCE_PAGE_NOTICES;
        }
    }

    private String processPathOutage(HttpServletRequest request) {
        String newRule = request.getParameter("newRule");
        newRule = toSingleQuote(newRule);
        newRule = stripExtraWhite(newRule);
        newRule = stripServices(newRule);
        newRule = checkParens(newRule);

        String redirectPage = SOURCE_PAGE_VALIDATE_PATH_OUTAGE;
        String criticalIp = request.getParameter("criticalIp");
        
        Map<String, Object> params = new HashMap<String, Object>();
        if (newRule != null) {
            params.put("newRule", newRule);
        }
        if (request.getParameter("criticalSvc") != null) {
            params.put("criticalSvc", request.getParameter("criticalSvc"));
        }
        if (request.getParameter("showNodes") != null) {
            params.put("showNodes", request.getParameter("showNodes"));
        }
        if (criticalIp != null && !criticalIp.equals("")) {
            params.put("criticalIp", criticalIp);
            try {
                getFilterDao().validateRule("IPADDR IPLIKE " + criticalIp);
            } catch (FilterParseException e) {
                // page to redirect to if the critical IP is invalid
                params.put("mode", "Critical path IP failed");
                redirectPage = SOURCE_PAGE_PATH_OUTAGE;
            }
        }
        
        try {
            getFilterDao().validateRule(newRule);
        } catch (FilterParseException e) {
            // page to redirect to if the rule is invalid
            params.put("mode", "Current rule failed");
            redirectPage = SOURCE_PAGE_PATH_OUTAGE;
        }
        
        return redirectPage + makeQueryString(params);
    }

    private String processValidatePathOutage(HttpServletRequest request) {
        String redirectPage = SOURCE_PAGE_NOTIFICATION_INDEX;
        String userAction = request.getParameter("userAction");
        String criticalIp = request.getParameter("criticalIp");
        String criticalSvc = request.getParameter("criticalSvc");
        String newRule = request.getParameter("newRule");

        Map<String, Object> params = new HashMap<String, Object>();
        if (userAction != null && userAction.equals("rebuild")) {
            params.put("newRule", newRule);
            params.put("criticalIp", criticalIp);
            params.put("criticalSvc", criticalSvc);
            if (request.getParameter("showNodes") != null) {
                params.put("showNodes", request.getParameter("showNodes"));
            }
            redirectPage = SOURCE_PAGE_PATH_OUTAGE;
        } else {
            try {
                updatePaths(newRule, criticalIp, criticalSvc);
            } catch (FilterParseException e) {
                params.put("mode", "Update failed");
                redirectPage = SOURCE_PAGE_PATH_OUTAGE;
            } catch (SQLException e) {
                params.put("mode", "Update failed");
                redirectPage = SOURCE_PAGE_PATH_OUTAGE;
            }
        }
        
        return redirectPage + makeQueryString(params);
    }

    private String processOtherWebUi(HttpServletRequest request, HttpSession user) throws ServletException {
        /*
         * We've come from elsewhere in the Web UI page, and will have a UEI.  
         * If there are existing notices for this UEI, then go to a page listing them allowing editing.  
         * If there are none, then create a notice, populate the UEI, and go to the buildRule page.
         */
        user.setAttribute("noticeWizardReturnPage", request.getParameter("returnPage"));
        String uei = request.getParameter("uei");
        
        boolean hasUei;
        try {
            hasUei = getNotificationFactory().hasUei(uei);
        } catch (IOException e) {
            throw new ServletException("IOException while checking if there is an existing notification for UEI "+uei, e);
        } catch (MarshalException e) {
            throw new ServletException("Marshalling Exception while checking if there is an existing notification for UEI "+uei, e);
        } catch (ValidationException e) {
            throw new ServletException("Validation Exception while checking if there is an existing notification for UEI "+uei, e);
        }
        
        if (hasUei) {
            //There are existing notifications for this UEI - goto a listing page
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("uei", uei);                   
            return SOURCE_PAGE_NOTIFS_FOR_UEI + makeQueryString(params);
        } else {
            return newNotifWithUEI(request, user);
        }
    }

    private String processNotificationsForUei(HttpServletRequest request, HttpSession user) throws ServletException {
        String userAction = request.getParameter("userAction");
        if ("edit".equals(userAction)) {
            return edit(request, user);
        } else if ("new".equals(userAction)) {
            return newNotifWithUEI(request, user);
        } else {
            // FIXME: What do we do here if neither of the userActions match?
            return "";
        }
    }

    private String newNotifWithUEI(HttpServletRequest request, HttpSession user) {
        String uei = request.getParameter("uei");
        Notification newNotice = buildNewNotification("on");
        newNotice.setUei(uei);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newRule", toSingleQuote(newNotice.getRule()));

        user.setAttribute("newNotice", newNotice);
        
        return SOURCE_PAGE_RULE + makeQueryString(params);  
    }

    private Notification buildNewNotification(String status) {
        Notification notice = new Notification();
        notice.setRule("IPADDR IPLIKE *.*.*.*");
        notice.setNumericMessage("111-%noticeid%");
        notice.setSubject("Notice #%noticeid%");
        notice.setStatus(status);
        return notice;
    }
    
    /**
     * Common code for two source pages that can't really be considered the same
     */
    private String edit(HttpServletRequest request, HttpSession user) throws ServletException {
        Notification oldNotice;

        try {
            oldNotice = getNotificationFactory().getNotification(request.getParameter("notice"));
        } catch (Throwable e) {
            throw new ServletException("couldn't get a copy of the notification to edit.", e);
        }

        // copy the old path into the new path
        Notification newNotice = copyNotice(oldNotice);
        user.setAttribute("newNotice", newNotice);

        return SOURCE_PAGE_UEIS;
    }
    
    /**
     * 
     */
    private Notification copyNotice(Notification oldNotice) {
        Notification newNotice = new Notification();

        newNotice.setName(oldNotice.getName());
        newNotice.setWriteable(oldNotice.getWriteable());
        newNotice.setDescription(oldNotice.getDescription());
        newNotice.setUei(oldNotice.getUei());
        newNotice.setRule(oldNotice.getRule());
        newNotice.setDestinationPath(oldNotice.getDestinationPath());
        newNotice.setNoticeQueue(oldNotice.getNoticeQueue());
        newNotice.setTextMessage(oldNotice.getTextMessage());
        newNotice.setSubject(oldNotice.getSubject());
        newNotice.setNumericMessage(oldNotice.getNumericMessage());
        newNotice.setStatus(oldNotice.getStatus());
        newNotice.setVarbind(oldNotice.getVarbind());

        Parameter parameters[] = oldNotice.getParameter();
        for (Parameter parameter : parameters) {
            Parameter newParam = new Parameter();
            newParam.setName(parameter.getName());
            newParam.setValue(parameter.getValue());

            newNotice.addParameter(newParam);
        }

        return newNotice;
    }

    // FIXME: Is this a duplicate of a similar method elsewhere?
    private String makeQueryString(Map<String, Object> map) {
        StringBuffer buffer = new StringBuffer();
        String separator = "?";

        Iterator<String> i = map.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            Object value = map.get(key);
            if (value instanceof String[]) {
                String[] list = (String[]) value;
                for (int j = 0; j < list.length; j++) {
                    buffer.append(separator).append(key).append("=").append(Util.encode(list[j]));
                    separator = "&";
                }
            } else {
                buffer.append(separator).append(key).append("=").append(Util.encode((String) value));
            }
            separator = "&";
        }

        return buffer.toString();
    }
    
    private static String toSingleQuote(String rule) {
        StringBuffer buffer = new StringBuffer(rule);
        
        for (int i = 0; (i < buffer.length()); i++) {
            if ((i < buffer.length() - 5) && (buffer.substring(i, i + 6).equals("&quot;"))) {
                buffer.replace(i, i + 6, "'");
            } else if (buffer.charAt(i) == '"') {
                buffer.replace(i, i + 1, "'");
            }
        }
        
        return buffer.toString();
    }

    private static String stripExtraWhite(String s) {
        Pattern pattern1 = Pattern.compile("\\s+");
        Matcher matcher1 = pattern1.matcher(s);
        String mys1 = matcher1.replaceAll(" ");
        
        Pattern pattern2 = Pattern.compile("^\\s");
        Matcher matcher2 = pattern2.matcher(mys1);
        String mys2 = matcher2.replaceAll("");
        
        Pattern pattern3 = Pattern.compile("\\s$");
        Matcher matcher3 = pattern3.matcher(mys2);
        return matcher3.replaceAll("");
    }

    private static String stripServices(String s) {
        String myregex = "\\s*\\&\\s*\\(\\s*\\!?is.+";
        Pattern pattern = Pattern.compile(myregex);
        Matcher matcher = pattern.matcher(s);
        
        return matcher.replaceAll("");
    }

    private static String checkParens(String rule) {
        if (rule.length() == 0) {
            return rule;
        } else if ((rule.charAt(0) != '(') || (rule.charAt(rule.length() - 1) != ')')) {
            return "(" + rule + ")";
        } else {
            return rule;
        }
    }

    private void deleteCriticalPath(int node, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = conn.prepareStatement(SQL_DELETE_CRITICAL_PATH);
            d.watch(stmt);
            stmt.setInt(1, node);
            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }

    private void setCriticalPath(int node, String criticalIp, String criticalSvc, Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        final DBUtils d = new DBUtils(getClass());
        try {
            stmt = conn.prepareStatement(SQL_SET_CRITICAL_PATH);
            d.watch(stmt);
            stmt.setInt(1, node);
            stmt.setString(2, criticalIp);
            stmt.setString(3, criticalSvc);
            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }

    private void updatePaths(String rule, String criticalIp, String criticalSvc)
                                 throws FilterParseException, SQLException {
        Connection conn = Vault.getDbConnection();
        SortedMap<Integer, String> nodes = getFilterDao().getNodeMap(rule);
        try {
            Iterator<Integer> i = nodes.keySet().iterator();
            while (i.hasNext()) {
                Integer key = i.next();
                deleteCriticalPath(key.intValue(), conn);
                if (criticalIp != null && !criticalIp.equals("")) {
                    setCriticalPath(key.intValue(), criticalIp, criticalSvc, conn);
                }
            }
        } finally {
            Vault.releaseDbConnection(conn);
        }
    }

    private FilterDao getFilterDao() {
        return FilterDaoFactory.getInstance();
    }

    private NotificationFactory getNotificationFactory() {
        return NotificationFactory.getInstance();
    }
}
