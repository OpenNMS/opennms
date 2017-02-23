package org.opennms.web.controller.admin.notifications;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.utils.BundleLists;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.web.admin.notification.noticeWizard.NotificationWizardServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.collect.Maps;

public class ChooseUeisController extends AbstractController {
    @Autowired
    private EventConfDao m_eventConfDao;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // Pull the notice from the session
        HttpSession session = request.getSession(true);
        Notification newNotice = (Notification)session.getAttribute("newNotice");

        // If the notice is not present in the session, redirect to the first page of the wizard
        if (newNotice == null) {
            return new ModelAndView(new RedirectView(NotificationWizardServlet.SOURCE_PAGE_NOTICES));
        }

        return new ModelAndView("/admin/notification/noticeWizard/chooseUeis",
                "model", createModel(newNotice));
    }

    private Map<String, Object> createModel(Notification newNotice) throws FileNotFoundException,
            IOException {
        Map<String, Object> model = Maps.newHashMap();
        model.put("title", newNotice.getName()!=null ? "Editing notice: " + newNotice.getName() : "");
        model.put("noticeUei", newNotice.getUei()!=null ? newNotice.getUei() : "");
        model.put("eventSelect", buildEventSelect(newNotice));
        return model;
    }

    private String buildEventSelect(Notification notice) throws IOException,
            FileNotFoundException {
        List<Event> events = m_eventConfDao.getEventsByLabel();
        StringBuffer buffer = new StringBuffer();

        List<String> excludeList = getExcludeList();
        TreeMap<String, String> sortedMap = new TreeMap<String, String>();
        List<Event> disappearingList = new ArrayList<Event>();

        if (notice.getUei() != null && notice.getUei().startsWith("~")) {
            buffer.append("<option selected value=\"" + notice.getUei()
                    + "\">REGEX_FIELD</option>\n");
        } else {
            buffer.append("<option value=\"~^$\">REGEX_FIELD</option>\n");
        }

        for (Event e : events) {
            String uei = e.getUei();
            // System.out.println(uei);

            String label = e.getEventLabel();
            // System.out.println(label);

            String trimmedUei = stripUei(uei);
            // System.out.println(trimmedUei);

            if (!excludeList.contains(trimmedUei) && !isDisappearingEvent(e)) {
                sortedMap.put(label, uei);
            }
            if (isDisappearingEvent(e)) {
                disappearingList.add(e);
            }
        }

        for (String label : sortedMap.keySet()) {
            String uei = (String) sortedMap.get(label);
            if (uei.equals(notice.getUei())) {
                buffer.append("<option selected VALUE=" + uei + ">" + label
                        + "</option>");
            } else {
                buffer.append("<option value=" + uei + ">" + label
                        + "</option>");
            }
        }

        if (!disappearingList.isEmpty()) {
            buffer.append("<optgroup label=\"Events not eligible for notifications\" disabled=\"true\">");
            for (Event e : disappearingList) {
                String selected = " ";
                if (e.getUei().equals(notice.getUei())) {
                    selected = " selected ";
                }
                buffer.append("<option" + selected + "value=\"" + e.getUei()
                        + "\">" + e.getEventLabel() + "</option>");
            }
            buffer.append("</optgroup>");
        }

        return buffer.toString();
    }

    public String stripUei(String uei) {
        String leftover = uei;

        for (int i = 0; i < 3; i++) {
            leftover = leftover.substring(leftover.indexOf('/') + 1);
        }

        return leftover;
    }

    public List<String> getExcludeList() throws IOException,
            FileNotFoundException {
        List<String> excludes = new ArrayList<String>();

        Properties excludeProperties = new Properties();
        excludeProperties.load(new FileInputStream(ConfigFileConstants
                .getFile(ConfigFileConstants.EXCLUDE_UEI_FILE_NAME)));
        String[] ueis = BundleLists.parseBundleList(excludeProperties
                .getProperty("excludes"));

        for (int i = 0; i < ueis.length; i++) {
            excludes.add(ueis[i]);
        }

        return excludes;
    }

    public boolean isDisappearingEvent(Event e) {
        if (e.getLogmsg() != null && "donotpersist".equalsIgnoreCase(e.getLogmsg().getDest())) {
            return true;
        }
        if (e.getAlarmData() != null && e.getAlarmData().getAutoClean() == true) {
            return true;
        }
        return false;
    }

    public void setEventConfDao(EventConfDao eventConfDao) {
        m_eventConfDao = eventConfDao;
    }
}
