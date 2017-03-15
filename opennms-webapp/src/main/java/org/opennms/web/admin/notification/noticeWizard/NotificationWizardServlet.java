/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.config.notifications.Parameter;
import org.opennms.netmgt.config.notifications.Varbind;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.web.api.Util;

/**
 * A servlet that handles the data comming in from the notification wizard jsps.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class NotificationWizardServlet extends HttpServlet {
    private static final long serialVersionUID = -5623373180751511103L;

    private static final Pattern SERVICES = Pattern.compile("\\s*\\&\\s*\\(\\s*\\!?is.+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern WHITESPACE_BEGINNING = Pattern.compile("^\\s");
    private static final Pattern WHITESPACE_END = Pattern.compile("\\s$");

    //SOURCE_PAGE_EVENTS_VIEW is more of a tag than an actual page - can't be used for navigation as is
    /** Constant <code>SOURCE_PAGE_OTHER_WEBUI="eventslist"</code> */
    public static final String SOURCE_PAGE_OTHER_WEBUI = "eventslist";

    /** Constant <code>SOURCE_PAGE_NOTICES="eventNotices.htm"</code> */
    public static final String SOURCE_PAGE_NOTICES = "eventNotices.htm";

    /** Constant <code>SOURCE_PAGE_NOTIFS_FOR_UEI="notifsForUEI.jsp"</code> */
    public static final String SOURCE_PAGE_NOTIFS_FOR_UEI = "notifsForUEI.jsp";

    /** Constant <code>SOURCE_PAGE_UEIS="chooseUeis.htm"</code> */
    public static final String SOURCE_PAGE_UEIS = "chooseUeis.htm";

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
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final String sourcePage = request.getParameter("sourcePage");
        final HttpSession user = request.getSession(true);

        /*
         * FIXME: Why do we do this for every request in doPost instead of
         * once in init?
         */
        try {
            NotifdConfigFactory.init();
        } catch (final Throwable t) {
            throw new ServletException("Failed to initialize NotifdConfigFactory: " + t.getMessage(), t);
        }
        try {
            NotificationFactory.init();
        } catch (final Throwable t) {
            throw new ServletException("Failed to initialize NotificationFactory: " + t.getMessage(), t);
        }

        if (SOURCE_PAGE_NOTICES.equals(sourcePage)) {
            response.sendRedirect(processNotices(request, user));
        } else if (SOURCE_PAGE_UEIS.equals(sourcePage)) {
            response.sendRedirect(processUeis(request, user));
        } else if (SOURCE_PAGE_RULE.equals(sourcePage)) {
            response.sendRedirect(processRule(request, user));
        } else if (SOURCE_PAGE_VALIDATE.equals(sourcePage)) {
            response.sendRedirect(processValidate(request, user));
        } else if (SOURCE_PAGE_PATH.equals(sourcePage)) {
            response.sendRedirect(processPath(request, user));
        } else if (SOURCE_PAGE_PATH_OUTAGE.equals(sourcePage)) {
            response.sendRedirect(processPathOutage(request));
        } else if (SOURCE_PAGE_VALIDATE_PATH_OUTAGE.equals(sourcePage)) {
            response.sendRedirect(processValidatePathOutage(request));
        } else if (SOURCE_PAGE_OTHER_WEBUI.equals(sourcePage)) {
            response.sendRedirect(processOtherWebUi(request, user));
        } else if (SOURCE_PAGE_NOTIFS_FOR_UEI.equals(sourcePage)) {
            response.sendRedirect(processNotificationsForUei(request, user));
        } else {
            throw new ServletException("no redirect specified for this wizard!");
        }
    }

    private String processNotices(final HttpServletRequest request, final HttpSession user) throws ServletException {
        final String userAction = request.getParameter("userAction");

        if ("delete".equals(userAction)) {
            try {
                getNotificationFactory().removeNotification(request.getParameter("notice"));
            } catch (final Throwable t) {
                throw new ServletException("Couldn't save/reload notifications configuration file: " + t.getMessage(), t);
            }
            return SOURCE_PAGE_NOTICES;
        } else if ("edit".equals(userAction)) {
            return edit(request, user);
        } else if ("new".equals(userAction)) {
            user.setAttribute("newNotice", buildNewNotification("off"));
            return SOURCE_PAGE_UEIS;
        } else if ("on".equals(userAction) || "off".equals(userAction)) {
            try {
                getNotificationFactory().updateStatus(request.getParameter("notice"), userAction);
            } catch (final Throwable t) {
                throw new ServletException("Couldn't save/reload notifications configuration file: " + t.getMessage(), t);
            }

            return SOURCE_PAGE_NOTICES;
        } else {
            // FIXME: We should do something if we hit this
            return "";
        }
    }

    private String processUeis(final HttpServletRequest request, final HttpSession user) {
        final Notification newNotice = (Notification) user.getAttribute("newNotice");
        newNotice.setUei(request.getParameter("uei"));

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("newRule", toSingleQuote(newNotice.getRule()));

        return SOURCE_PAGE_RULE + makeQueryString(params);
    }

    private String processValidate(final HttpServletRequest request, final HttpSession user) {
        final String userAction = request.getParameter("userAction");

        if ("rebuild".equals(userAction)) {
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("newRule", request.getParameter("newRule"));
            final String[] services = request.getParameterValues("services");
            if (services != null) {
                params.put("services", services);
            }
            params.put("mode", "rebuild");

            return SOURCE_PAGE_RULE + makeQueryString(params);
        } else {
            final Notification newNotice = (Notification) user.getAttribute("newNotice");
            newNotice.setRule(request.getParameter("newRule"));
            return SOURCE_PAGE_PATH;
        }
    }

    private String processRule(final HttpServletRequest request, final HttpSession user) {
        String ruleString = request.getParameter("newRule");
        ruleString = toSingleQuote(ruleString);
        ruleString = stripExtraWhite(ruleString);
        ruleString = stripServices(ruleString);
        ruleString = checkParens(ruleString);

        final StringBuffer rule = new StringBuffer(ruleString);

        final String[] services = request.getParameterValues("services");
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

        final String[] notServices = request.getParameterValues("notServices");
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

        final Map<String, Object> params = new HashMap<String, Object>();
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
        } catch (final FilterParseException e) {
            // page to redirect to if the rule is invalid
            params.put("mode", "failed");
            redirectPage = SOURCE_PAGE_RULE;
        }

        // save the rule if we are bypassing validation
        if (redirectPage.equals(SOURCE_PAGE_PATH)) {
            final Notification newNotice = (Notification) user.getAttribute("newNotice");
            newNotice.setRule(rule.toString());
        }

        return redirectPage + makeQueryString(params);
    }

    private String processPath(final HttpServletRequest request, final HttpSession user) throws ServletException {
        final Notification newNotice = (Notification) user.getAttribute("newNotice");
        newNotice.setDestinationPath(request.getParameter("path"));

        final String description = request.getParameter("description");
        if (description != null && !description.trim().equals("")) {
            newNotice.setDescription(description);
        } else {
            newNotice.setDescription(null);
        }

        newNotice.setTextMessage(request.getParameter("textMsg"));

        final String subject = request.getParameter("subject");
        if (subject != null && !subject.trim().equals("")) {
            newNotice.setSubject(subject);
        } else {
            newNotice.setSubject(null);
        }

        final String numMessage = request.getParameter("numMsg");
        if (numMessage != null && !numMessage.trim().equals("")) {
            newNotice.setNumericMessage(numMessage);
        } else {
            newNotice.setNumericMessage(null);
        }

        final String oldName = newNotice.getName();
        newNotice.setName(request.getParameter("name"));

        final String varbindName = request.getParameter("varbindName");
        final String varbindValue = request.getParameter("varbindValue");

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
        } catch (final Throwable t) {
            throw new ServletException("Couldn't save/reload notification configuration file.", t);
        }

        final String suppliedReturnPage=(String)user.getAttribute("noticeWizardReturnPage");
        if (suppliedReturnPage != null && !"".equals(suppliedReturnPage)) {
            // Remove this attribute once we have consumed it, else the user may later
            // get returned to a potentially unexpected page here
            user.removeAttribute("noticeWizardReturnPage");
            return suppliedReturnPage;
        } else {
            return SOURCE_PAGE_NOTICES;
        }
    }

    private String processPathOutage(final HttpServletRequest request) {
        String newRule = request.getParameter("newRule");
        newRule = toSingleQuote(newRule);
        newRule = stripExtraWhite(newRule);
        newRule = stripServices(newRule);
        newRule = checkParens(newRule);

        String redirectPage = SOURCE_PAGE_VALIDATE_PATH_OUTAGE;

        final String criticalService = request.getParameter("criticalSvc");
        final String showNodes = request.getParameter("showNodes");
        final String criticalIp = request.getParameter("criticalIp");

        final Map<String, Object> params = new HashMap<String, Object>();
        if (newRule != null) {
            params.put("newRule", newRule);
        }
        if (criticalService != null) {
            params.put("criticalSvc", criticalService);
        }
        if (showNodes != null) {
            params.put("showNodes", showNodes);
        }
        if (criticalIp != null && !criticalIp.equals("")) {
            params.put("criticalIp", criticalIp);
            try {
                getFilterDao().validateRule("IPADDR IPLIKE " + criticalIp);
            } catch (final FilterParseException e) {
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

    private String processValidatePathOutage(final HttpServletRequest request) {
        final String userAction = request.getParameter("userAction");
        final String criticalIp = request.getParameter("criticalIp");
        final String criticalSvc = request.getParameter("criticalSvc");
        final String newRule = request.getParameter("newRule");

        String redirectPage = SOURCE_PAGE_NOTIFICATION_INDEX;

        final Map<String, Object> params = new HashMap<String, Object>();
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
            } catch (final Exception e) {
                params.put("mode", "Update failed");
                redirectPage = SOURCE_PAGE_PATH_OUTAGE;
            }
        }

        return redirectPage + makeQueryString(params);
    }

    private String processOtherWebUi(final HttpServletRequest request, final HttpSession user) throws ServletException {
        /*
         * We've come from elsewhere in the Web UI page, and will have a UEI.  
         * If there are existing notices for this UEI, then go to a page listing them allowing editing.  
         * If there are none, then create a notice, populate the UEI, and go to the buildRule page.
         */
        user.setAttribute("noticeWizardReturnPage", request.getParameter("returnPage"));
        final String uei = request.getParameter("uei");

        try {
            final boolean hasUei = getNotificationFactory().hasUei(uei);
            if (hasUei) {
                //There are existing notifications for this UEI - goto a listing page
                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("uei", uei);                   
                return SOURCE_PAGE_NOTIFS_FOR_UEI + makeQueryString(params);
            } else {
                return newNotifWithUEI(request, user);
            }
        } catch (final Exception e) {
            throw new ServletException("Exception while checking if there is an existing notification for UEI "+uei, e);
        }
    }

    private String processNotificationsForUei(final HttpServletRequest request, final HttpSession user) throws ServletException {
        final String userAction = request.getParameter("userAction");
        if ("edit".equals(userAction)) {
            return edit(request, user);
        } else if ("new".equals(userAction)) {
            return newNotifWithUEI(request, user);
        } else {
            // FIXME: What do we do here if neither of the userActions match?
            return "";
        }
    }

    private String newNotifWithUEI(final HttpServletRequest request, final HttpSession user) {
        final String uei = request.getParameter("uei");
        final Notification newNotice = buildNewNotification("on");
        newNotice.setUei(uei);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("newRule", toSingleQuote(newNotice.getRule()));
        user.setAttribute("newNotice", newNotice);

        return SOURCE_PAGE_RULE + makeQueryString(params);  
    }

    private Notification buildNewNotification(final String status) {
        final Notification notice = new Notification();
        notice.setRule("IPADDR IPLIKE *.*.*.*");
        notice.setNumericMessage("111-%noticeid%");
        notice.setSubject("Notice #%noticeid%");
        notice.setStatus(status);
        return notice;
    }

    /**
     * Common code for two source pages that can't really be considered the same
     */
    private String edit(final HttpServletRequest request, final HttpSession user) throws ServletException {
        try {
            final Notification oldNotice = getNotificationFactory().getNotification(request.getParameter("notice"));
            user.setAttribute("newNotice", copyNotice(oldNotice));
            return SOURCE_PAGE_UEIS;
        } catch (final Throwable t) {
            throw new ServletException("couldn't get a copy of the notification to edit.", t);
        }
    }

    /**
     * 
     */
    private Notification copyNotice(final Notification oldNotice) {
        final Notification newNotice = new Notification();

        newNotice.setName(oldNotice.getName());
        newNotice.setWriteable(oldNotice.getWriteable());
        newNotice.setDescription(oldNotice.getDescription().orElse(null));
        newNotice.setUei(oldNotice.getUei());
        newNotice.setRule(oldNotice.getRule());
        newNotice.setDestinationPath(oldNotice.getDestinationPath());
        newNotice.setNoticeQueue(oldNotice.getNoticeQueue().orElse(null));
        newNotice.setTextMessage(oldNotice.getTextMessage());
        newNotice.setSubject(oldNotice.getSubject().orElse(null));
        newNotice.setNumericMessage(oldNotice.getNumericMessage().orElse(null));
        newNotice.setStatus(oldNotice.getStatus());
        newNotice.setVarbind(oldNotice.getVarbind());

        for (final Parameter parameter : oldNotice.getParameter()) {
            final Parameter newParam = new Parameter();
            newParam.setName(parameter.getName());
            newParam.setValue(parameter.getValue());
            newNotice.addParameter(newParam);
        }

        return newNotice;
    }

    // FIXME: Is this a duplicate of a similar method elsewhere?
    private String makeQueryString(final Map<String, Object> map) {
        final StringBuffer buffer = new StringBuffer();
        String separator = "?";

        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof String[]) {
                final String[] list = (String[]) value;
                for (final String valueEntry : list) {
                    buffer.append(separator).append(key).append("=").append(Util.encode(valueEntry));
                    separator = "&";
                }
            } else {
                buffer.append(separator).append(key).append("=").append(Util.encode((String) value));
            }
            separator = "&";
        }

        return buffer.toString();
    }

    private static String toSingleQuote(final String rule) {
        final StringBuffer buffer = new StringBuffer(rule);

        for (int i = 0; (i < buffer.length()); i++) {
            if ((i < buffer.length() - 5) && (buffer.substring(i, i + 6).equals("&quot;"))) {
                buffer.replace(i, i + 6, "'");
            } else if (buffer.charAt(i) == '"') {
                buffer.replace(i, i + 1, "'");
            }
        }

        return buffer.toString();
    }

    private static String stripExtraWhite(final String s) {
        final Matcher matcher1 = WHITESPACE.matcher(s);
        final Matcher matcher2 = WHITESPACE_BEGINNING.matcher(matcher1.replaceAll(" "));
        final Matcher matcher3 = WHITESPACE_END.matcher(matcher2.replaceAll(""));
        return matcher3.replaceAll("");
    }

    private static String stripServices(final String s) {
        return SERVICES.matcher(s).replaceAll("");
    }

    private static String checkParens(final String rule) {
        if (rule.length() == 0) {
            return rule;
        } else if ((rule.charAt(0) != '(') || (rule.charAt(rule.length() - 1) != ')')) {
            return "(" + rule + ")";
        } else {
            return rule;
        }
    }

    private void deleteCriticalPath(final int node, final Connection conn) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_CRITICAL_PATH);
            d.watch(stmt);
            stmt.setInt(1, node);
            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }

    private void setCriticalPath(final int node, final String criticalIp, final String criticalSvc, final Connection conn) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            final PreparedStatement stmt = conn.prepareStatement(SQL_SET_CRITICAL_PATH);
            d.watch(stmt);
            stmt.setInt(1, node);
            stmt.setString(2, InetAddressUtils.normalize(criticalIp));
            stmt.setString(3, criticalSvc);
            stmt.execute();
        } finally {
            d.cleanUp();
        }
    }

    private void updatePaths(final String rule, final String criticalIp, final String criticalSvc) throws FilterParseException, SQLException {
        final Connection conn = DataSourceFactory.getInstance().getConnection();
        final DBUtils d = new DBUtils(getClass(), conn);
        try {
            final SortedMap<Integer, String> nodes = getFilterDao().getNodeMap(rule);
            for (final Map.Entry<Integer, String> entry : nodes.entrySet()) {
                final int key = entry.getKey().intValue();
                deleteCriticalPath(key, conn);
                if (criticalIp != null && !"".equals(criticalIp)) {
                    setCriticalPath(key, criticalIp, criticalSvc, conn);
                }
            }
        } finally {
            d.cleanUp();
        }
    }

    private FilterDao getFilterDao() {
        return FilterDaoFactory.getInstance();
    }

    private NotificationFactory getNotificationFactory() {
        return NotificationFactory.getInstance();
    }
}
