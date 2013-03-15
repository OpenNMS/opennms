package org.opennms.web.navigate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.web.api.OnmsHeaderProvider;

public class DefaultOnmsHeaderProvider implements OnmsHeaderProvider {

    private List<NavBarEntry> m_navBarItems;

    @Override
    public String getHeaderHtml(HttpServletRequest request) {
        return createHeaderHtml(request);
    }
    
    private String createHeaderHtml(HttpServletRequest request) {
        return "<div id='header'>" +
              "<h1 id='headerlogo'><a href='index.jsp'><img src=\"../images/logo.png\" alt='OpenNMS Web Console Home'></a></h1>" +
          "<div id='headerinfo'>" +
          "<h2>Topology Map</h2>" +
          "<p align=\"right\" >" + 
          "User: <a href=\"/opennms/account/selfService/index.jsp\" title=\"Account self-service\"><strong>" + request.getRemoteUser() + "</strong></a>" +
          "&nbsp;(Notices " + getNoticeStatus() + " )" + 
          " - <a href=\"opennms/j_spring_security_logout\">Log out</a><br></p>"+
          "</div>" +
          "<div id='headernavbarright'>" +
          "<div class='navbar'>" +
          createNavBarHtml(request) +
          "</div>" +
          "</div>" +
          "<div class='spacer'><!-- --></div>" +
          "</div>";
    }

    private String getNoticeStatus() {
        String noticeStatus;
        try {
            noticeStatus = NotifdConfigFactory.getPrettyStatus();
            if ("Off".equals(noticeStatus)) {
              noticeStatus="<b id=\"notificationOff\">Off</b>";
            } else {
              noticeStatus="<b id=\"notificationOn\">On</b>";
            }
        } catch (Throwable t) {
            noticeStatus = "<b id=\"notificationOff\">Unknown</b>";
        }
        return noticeStatus;
    }

    private String createNavBarHtml(HttpServletRequest request) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<ul>");
        
        for (NavBarEntry entry : getNavBarItems()) {
            if(entry.evaluate(request) == DisplayStatus.DISPLAY_LINK) {
                strBuilder.append("<li><a href=\"" + entry.getUrl() +  "\" >" + entry.getName() + "</a></li>");
            }
        }
        strBuilder.append("</ul>");
        return strBuilder.toString();
    }
    
    /**
     * <p>getNavBarItems</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<NavBarEntry> getNavBarItems() {
        return m_navBarItems;
    }

    /**
     * <p>setNavBarItems</p>
     *
     * @param navBarItems a {@link java.util.List} object.
     */
    public void setNavBarItems(List<NavBarEntry> navBarItems) {
        m_navBarItems = navBarItems;
    }

}
