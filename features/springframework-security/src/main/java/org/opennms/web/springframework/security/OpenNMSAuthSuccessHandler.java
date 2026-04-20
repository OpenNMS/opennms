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
package org.opennms.web.springframework.security;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.web.api.Util;
import org.opennms.web.api.WizardGateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.util.StringUtils;

@SuppressWarnings("java:S2068")
public class OpenNMSAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    // username/password combination that triggers the password change prompt in the Vue UI
    public static final String PASSWORD_GATE_USERNAME = "admin";
    public static final String PASSWORD_GATE_PASSWORD = "admin";

    //  attribute set when admin logs in with the default password.
    // The Vue WelcomeModal wizard reads this via GET /opennms/api/v2/account/requiresPasswordChange
    // and clears it after the password is changed or dismissed.
    public static final String REQUIRES_PASSWORD_CHANGE_SESSION_ATTR = "requiresPasswordChange";

    protected final Logger logger = LoggerFactory.getLogger(OpenNMSAuthSuccessHandler.class);
    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final AtomicReference<ServiceRegistry> serviceRegistryRef = new AtomicReference<>();

    // URL of the Vue wizard app — used when any first-sign-in wizard step needs to be shown.
    // Configured via <property name="wizardUrl"> in applicationContext-spring-security.xml.
    private String wizardUrl = "/ui/index.html";

    public void setWizardUrl(String wizardUrl) {
        this.wizardUrl = wizardUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        // changing JSESSIONID to prevent Session Fixation attacks, see NMS-15310
        request.changeSessionId();

        boolean requiresWizard = false;

        // If admin logged in with the default password, flag the session so the Vue
        // WelcomeModal wizard can prompt for a password change in-app instead of
        // interrupting the flow with a full-page JSP redirect.
        if (isDefaultAdminLogin(request.getParameter("j_username"), request.getParameter("j_password"))) {
            request.getSession(true).setAttribute(REQUIRES_PASSWORD_CHANGE_SESSION_ATTR, Boolean.TRUE);
            requiresWizard = true;
            logger.debug("User logged in with default admin credentials. Setting '{}' session flag for Vue wizard.", REQUIRES_PASSWORD_CHANGE_SESSION_ATTR);
        }

        // Check whether any datachoices notice (Usage Statistics or Product Enrollment)
        // still needs acknowledgment — redirect to the Vue wizard if so.
        if (!requiresWizard) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            boolean isAdminOrRest = isAdmin || authorities.stream().anyMatch(a -> "ROLE_REST".equals(a.getAuthority()));
            WizardGateService wizardGate = getWizardGateService();
            if (wizardGate != null && wizardGate.hasUnacknowledgedNotices(isAdmin, isAdminOrRest)) {
                requiresWizard = true;
                logger.debug("Pending datachoices notices detected; redirecting to Vue wizard.");
            }
        }

        // If any wizard step is needed, bypass the saved-request logic and send the user
        // directly to the Vue wizard. After the wizard completes it will redirect to index.jsp.
        if (requiresWizard) {
            super.clearAuthenticationAttributes(request);
            this.getRedirectStrategy().sendRedirect(request, response, Util.calculateUrlBase(request, wizardUrl));
            return;
        }

        final DefaultSavedRequest savedRequest = (DefaultSavedRequest) this.requestCache.getRequest(request, response);

        if (savedRequest == null) {
            super.clearAuthenticationAttributes(request);
            this.getRedirectStrategy().sendRedirect(request, response, createTargetURL(request, response));
        } else {
            String targetUrlParameter = this.getTargetUrlParameter();

            boolean useSavedRequest = !this.isAlwaysUseDefaultTargetUrl() &&
                (targetUrlParameter == null || !StringUtils.hasText(request.getParameter(targetUrlParameter)));

            // make sure we are redirecting to an actual page, not e.g. a URL to an asset
            // TODO: Determine why assets are getting saved in the requestCache
            if (useSavedRequest) {
                final String servletPathLower = savedRequest.getServletPath().toLowerCase();

                if (LoginModuleUtils.isInvalidSavedRequestUrl(servletPathLower)) {
                    useSavedRequest = false;
                }
            }

            if (useSavedRequest) {
                this.clearAuthenticationAttributes(request);
                final String targetUrl = Util.calculateUrlBase(request, savedRequest.getServletPath() + (savedRequest.getQueryString() == null ? "" : "?" + savedRequest.getQueryString()));
                this.logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
                this.getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } else {
                this.requestCache.removeRequest(request, response);
                this.getRedirectStrategy().sendRedirect(request, response, createTargetURL(request, response));
            }
        }
    }

    private String createTargetURL(HttpServletRequest request, HttpServletResponse response) {
        return Util.calculateUrlBase(request, determineTargetUrl(request, response));
    }

    private WizardGateService getWizardGateService() {
        try {
            ServiceRegistry registry = serviceRegistryRef.updateAndGet(ref ->
                ref != null ? ref : BeanUtils.getBean("soaContext", "serviceRegistry", ServiceRegistry.class));
            Collection<WizardGateService> providers = registry.findProviders(WizardGateService.class);
            return providers.isEmpty() ? null : providers.iterator().next();
        } catch (Exception e) {
            logger.warn("Could not retrieve WizardGateService; skipping datachoices notice check.", e);
            return null;
        }
    }

    private boolean isDefaultAdminLogin(String username, String password) {
        return username != null && username.equals(PASSWORD_GATE_USERNAME) &&
            password != null && password.equals(PASSWORD_GATE_PASSWORD);
    }
}
