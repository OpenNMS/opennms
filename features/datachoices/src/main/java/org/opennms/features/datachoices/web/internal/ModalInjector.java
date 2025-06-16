/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
 * We optionally inject markup for a Usage Sharing Statistics notice and a Product Update Enrollment input dialog.
 */
public class ModalInjector implements HtmlInjector {
    public static final String CONSENT_TEXT = "I agree to receive email communications from OpenNMS";
    public static final String LEGAL_CONSENT_COMMUNICATION_TEXT = "If you consent to us contacting you, please opt in below. We will maintain your data until you request us to delete it from our systems. You may opt out of receiving communications from us at any time.";

    private StateManager m_stateManager;

    @Override
    public String inject(HttpServletRequest request) throws TemplateException, IOException {
        String usageStatisticsSharingHtml = getUsageStatisticsSharingModalHtml(request);
        String productUpdateEnrollmentHtml = getProductUpdateEnrollmentModalHtml(request);

        List<String> items = Stream.of(usageStatisticsSharingHtml, productUpdateEnrollmentHtml)
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

    protected String getProductUpdateEnrollmentModalHtml(HttpServletRequest request) throws TemplateException, IOException {
        // don't display Product Update Enrollment form if disabled by configuration
        // also respect backward compatibility for earlier naming convention
        if (Stream.of("opennms.productUpdateEnrollment.show", "opennms.userDataCollection.show")
            .map(key -> System.getProperty(key, "true"))
            .anyMatch(key -> key != null && key.equalsIgnoreCase("false"))) {
            return null;
        }

        // don't display Product Update Enrollment form if already seen
        boolean noticeSeen = m_stateManager.isProductUpdateEnrollmentNoticeAcknowledged();

        if (!noticeSeen &&
                isPage("/opennms/index.jsp", request) &&
                isUserInAdminOrRestRole(request)) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("consentText", CONSENT_TEXT);
            data.put("legalConsentCommunication", LEGAL_CONSENT_COMMUNICATION_TEXT);

            return generateModalHtml("product-update-enrollment.ftl.html", data);
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
