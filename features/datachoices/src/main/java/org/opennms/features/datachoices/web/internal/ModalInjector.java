/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2016-2024 The OpenNMS Group, Inc.
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

package org.opennms.features.datachoices.web.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.opennms.features.datachoices.internal.StateManager;
import org.opennms.web.api.HtmlInjector;

import com.google.common.collect.Maps;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Create Data Choices markup to be injected into web pages.
 * See opennms-webapp bootstrap-footer.jsp, HtmlInjectHandler.
 *
 * We optionally inject markup for a Usage Sharing Statistics notice and a User Data Collection input dialog.
 */
public class ModalInjector implements HtmlInjector {
    public static final String CONSENT_TEXT = "I agree to receive email communications from OpenNMS";
    public static final String LEGAL_CONSENT_COMMUNICATION_TEXT = "If you consent to us contacting you, please opt in below. We will maintain your data until you request us to delete it from our systems. You may opt out of receiving communications from us at any time.";

    private StateManager m_stateManager;

    @Override
    public String inject(HttpServletRequest request) throws TemplateException, IOException {
        String usageStatisticsSharingHtml = getUsageStatisticsSharingModalHtml(request);
        String userDataCollectionHtml = getUserDataCollectionModalHtml(request);

        List<String> items = Stream.of(usageStatisticsSharingHtml, userDataCollectionHtml)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return items.isEmpty() ? null : String.join("\n", items);
    }

    protected String getUsageStatisticsSharingModalHtml(HttpServletRequest request) throws TemplateException, IOException {
        // don't display Usage Statistics Sharing notice if already acked or user previously opted-out
        boolean noticeAcked = m_stateManager.isInitialNoticeAcknowledged() != null &&
                m_stateManager.isInitialNoticeAcknowledged().booleanValue();
        boolean optedOut = m_stateManager.isEnabled() != null && !m_stateManager.isEnabled().booleanValue();
        boolean hideNotice = noticeAcked || optedOut;

        if (!hideNotice &&
                isPage("/opennms/index.jsp", request) &&
                isUserInAdminRole(request)) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("showOnLoad", true);

            return generateModalHtml("modal.ftl.html", data);
        }

        return null;
    }

    protected String getUserDataCollectionModalHtml(HttpServletRequest request) throws TemplateException, IOException {
        String show = System.getProperty("opennms.userDataCollection.show", "true");

        // don't display User Data Collection form if disabled by configuration
        if (show != null && show.toLowerCase().equals("false")) {
            return null;
        }

        // don't display User Data Collection form if already seen
        boolean noticeSeen = m_stateManager.isUserDataCollectionNoticeAcknowledged();

        if (!noticeSeen &&
                isPage("/opennms/index.jsp", request) &&
                isUserInAdminOrRestRole(request)) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("consentText", CONSENT_TEXT);
            data.put("legalConsentCommunication", LEGAL_CONSENT_COMMUNICATION_TEXT);

            return generateModalHtml("user-data-collection.ftl.html", data);
        }

        return null;
    }

    protected static String generateModalHtml(String templateName, Map<String, Object> data) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        ClassTemplateLoader ctl = new ClassTemplateLoader(ModalInjector.class, "/web");
        cfg.setTemplateLoader(ctl);

        // Load the template
        Template template = cfg.getTemplate(templateName);

        // Render to string
        Writer out = new StringWriter();
        template.process(data, out);
        out.flush();
        return out.toString();
    }

    protected static boolean isPage(String endOfUri, HttpServletRequest request) {
        final String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        return uri.endsWith(endOfUri);
    }

    protected static boolean isUserInAdminRole(HttpServletRequest request) {
        return request.isUserInRole("ROLE_ADMIN");
    }

    protected static boolean isUserInAdminOrRestRole(HttpServletRequest request) {
        return request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_REST");
    }

    public void setStateManager(StateManager stateManager) {
        m_stateManager = stateManager;
    }
}
