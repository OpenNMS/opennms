
package org.opennms.web.admin.notification.noticeWizard;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.sql.Connection;
import javax.servlet.http.*;
import java.net.URLEncoder;

import org.opennms.web.*;
import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.notifications.*;
import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.filter.FilterParseException;

import javax.servlet.*;
/**
 * A servlet that handles the data comming in from the notification wizard jsps.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class NotificationWizardServlet extends HttpServlet
{
        public static final String SOURCE_PAGE_NOTICES         = "eventNotices.jsp";
        public static final String SOURCE_PAGE_UEIS            = "chooseUeis.jsp";
        public static final String SOURCE_PAGE_RULE            = "buildRule.jsp";
        public static final String SOURCE_PAGE_VALIDATE        = "validateRule.jsp";
        public static final String SOURCE_PAGE_PATH            = "choosePath.jsp";
        
        public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
	{
		String sourcePage = request.getParameter( "sourcePage" );
                HttpSession user = request.getSession(true);
                
                StringBuffer redirectString = new StringBuffer();
                
                if (sourcePage.equals(SOURCE_PAGE_NOTICES))
                {
                        String userAction = request.getParameter("userAction");
                        
                        if (userAction.equals("delete"))
                        {
                                try
                                {
                                        NotificationFactory.getInstance().removeNotification(request.getParameter("notice"));
                                        redirectString.append(SOURCE_PAGE_NOTICES);
                                }
                                catch (Exception e)
                                {
                                        throw new ServletException("Couldn't save/reload notifications configuration file.", e);
                                }
                        }
                        else if (userAction.equals("edit"))
                        {
                                //get the path that was choosen in the select
                                Notification oldNotice = null;
                                
                                try {
                                        oldNotice = NotificationFactory.getInstance().getNotification(request.getParameter("notice"));
                                } catch (Exception e)
                                {
                                        throw new ServletException("couldn't get a copy of the notification to edit.", e);
                                }
                                
                                //copy the old path into the new path
                                Notification newNotice = copyNotice(oldNotice);
                                user.setAttribute("newNotice", newNotice);
                                
                                redirectString.append(SOURCE_PAGE_UEIS);
                        }
                        else if (userAction.equals("new"))
                        {
                                Notification newNotice = new Notification();
                                newNotice.setRule("IPADDR IPLIKE *.*.*.*");
                                newNotice.setNumericMessage("111-%noticeid%");
                                newNotice.setSubject("Notice #%noticeid%");
                                newNotice.setStatus("off");
                                
                                user.setAttribute("newNotice", newNotice);
                                
                                redirectString.append(SOURCE_PAGE_UEIS);
                        }
                        else if (userAction.equals("on") || userAction.equals("off"))
                        {
                                try
                                {
                                        NotificationFactory.getInstance().updateStatus(request.getParameter("notice"), userAction);
                                        redirectString.append(SOURCE_PAGE_NOTICES);
                                }
                                catch (Exception e)
                                {
                                        throw new ServletException("Couldn't save/reload notifications configuration file.", e);
                                }
                        }
                }
                else if (sourcePage.equals(SOURCE_PAGE_UEIS))
                {
                        Notification newNotice = (Notification)user.getAttribute("newNotice");
                        newNotice.setUei(request.getParameter("uei"));
                        
                        Map params = new HashMap();
                        params.put("newRule", newNotice.getRule());
                        
                        redirectString.append(SOURCE_PAGE_RULE).append(makeQueryString(params));
                }
                else if (sourcePage.equals(SOURCE_PAGE_RULE))
                {
                        StringBuffer rule = new StringBuffer("IPADDR IPLIKE ");
                        rule.append(request.getParameter("ipaddr"));
                        
                        String services[] = request.getParameterValues("services");
                        if (services!=null)
                        {
                                rule.append(" & ").append(" (");
                                
                                for (int i = 0; i < services.length; i++)
                                {
                                        rule.append("is").append(services[i]);
                                        if (i < services.length-1)
                                                rule.append(" | ");
                                }
                                
                                rule.append(" )");
                        }
                        
                        String notServices[] = request.getParameterValues("notServices");
                        if (notServices!=null)
                        {
                                rule.append(" & ").append(" (");
                                
                                for (int i = 0; i < notServices.length; i++)
                                {
                                        rule.append("!is").append(notServices[i]);
                                        if (i < notServices.length-1)
                                                rule.append(" & ");
                                }
                                
                                rule.append(" )");
                        }
                        
                        Map params = new HashMap();
                        params.put("newRule", rule.toString());
                        if (services!=null) 
                        {
                                params.put("services", services);
                        }
                        if (notServices!=null)
                        {
                                params.put("notServices", notServices);
                        }
                        params.put("ipaddr", request.getParameter("ipaddr"));
                        
                        //page to redirect to, either validate or skip validation
                        String redirectPage = request.getParameter("nextPage");
                        
                        //now lets see if the rule is syntactically valid
                        Filter filter = new Filter();
                        try
                        {
                                filter.validateRule(rule.toString());
                        }
                        catch (FilterParseException e)
                        {
                                //page to redirect to if the rule is invalid
                                params.put("mode", "failed");
                                redirectPage = SOURCE_PAGE_RULE;
                        }
                        
                        //save the rule if we are bypassing validation
                        if (redirectPage.equals(SOURCE_PAGE_PATH))
                        {
                                Notification newNotice = (Notification)user.getAttribute("newNotice");
                                newNotice.setRule( rule.toString() );
                        }
                        
                        redirectString.append(redirectPage).append(makeQueryString(params));
                }
                else if (sourcePage.equals(SOURCE_PAGE_VALIDATE))
                {
                        String userAction = request.getParameter("userAction");
                        
                        if (userAction.equals("rebuild"))
                        {
                                Map params = new HashMap();
                                params.put("newRule", request.getParameter("newRule"));
                                params.put("ipaddr", request.getParameter("ipaddr"));
                                String services[] = request.getParameterValues("services");
                                if (services != null)
                                        params.put("services", services);
                                params.put("mode", "rebuild");
                                
                                redirectString.append(SOURCE_PAGE_RULE).append(makeQueryString(params));
                        }
                        else
                        {
                                Notification newNotice = (Notification)user.getAttribute("newNotice");
                                newNotice.setRule( request.getParameter("newRule") );
                                
                                redirectString.append(SOURCE_PAGE_PATH);
                        }
                }
                else if (sourcePage.equals(SOURCE_PAGE_PATH))
                {
                        Notification newNotice = (Notification)user.getAttribute("newNotice");
                        newNotice.setDestinationPath(request.getParameter("path"));
                        
                        String description = request.getParameter("description");
                        if (description != null && !description.trim().equals(""))
                                newNotice.setDescription(description);
                        else
                                newNotice.setDescription(null);
                        
                        newNotice.setTextMessage(request.getParameter("textMsg"));
                        
                        String subject = request.getParameter("subject");
                        if (subject != null && !subject.trim().equals(""))
                                newNotice.setSubject(subject);
                        else
                                newNotice.setSubject(null);
                                
                        String numMessage = request.getParameter("numMsg");
                        if (numMessage != null && !numMessage.trim().equals(""))
                                newNotice.setNumericMessage(numMessage);
                        else
                                newNotice.setNumericMessage(null);
                        
                        String oldName = newNotice.getName();
                        newNotice.setName(request.getParameter("name"));
                        
                        try
                        {
                                if (oldName!=null && !oldName.equals(newNotice.getName()))
                                {
                                        //replacing a path with a new name
                                        NotificationFactory.getInstance().replaceNotification(oldName, newNotice);
                                }
                                else
                                {
                                        NotificationFactory.getInstance().addNotification(newNotice);
                                }
                        }
                        catch (Exception e)
                        {
                                throw new ServletException("Couldn't save/reload notification configuration file.", e);
                        }
                        
                        redirectString.append(SOURCE_PAGE_NOTICES);
                }
                
                if (redirectString.toString().equals(""))
                        throw new ServletException("no redirect specified for this wizard!");
                
                response.sendRedirect(redirectString.toString());
        }
        
        /**
         *
         */
        private Notification copyNotice(Notification oldNotice)
        {
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
                
                Parameter parameters[] = oldNotice.getParameter();
                for (int i = 0; i < parameters.length; i++)
                {
                        Parameter newParam = new Parameter();
                        newParam.setName(parameters[i].getName());
                        newParam.setValue(parameters[i].getValue());
                        
                        newNotice.addParameter(newParam);
                }
                
                return newNotice;
        }
        
        private String makeQueryString(Map map)
        {
                StringBuffer buffer = new StringBuffer();
                String separator = "?";
                
                Iterator i = map.keySet().iterator();
                while(i.hasNext())
                {
                        String key = (String)i.next();
                        Object value = map.get(key);
                        if (value instanceof String[])
                        {
                                String[] list = (String[])value;
                                for (int j = 0; j < list.length; j++)
                                {
                                        buffer.append( separator ).append( key ).append( "=" ).append( URLEncoder.encode( list[j] ) );
                                        separator = "&";
                                }
                        }
                        else
                        {
                                buffer.append( separator ).append( key ).append( "=" ).append( URLEncoder.encode( (String)value ) );
                        }
                        separator = "&";
                }
                
                return buffer.toString();
        }
}
