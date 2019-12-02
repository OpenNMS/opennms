/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.web.api.Authentication;
import org.opennms.web.navigate.MenuContext;
import org.opennms.web.api.MenuProvider;
import org.opennms.web.api.OnmsHeaderProvider;
import org.opennms.web.navigate.DefaultMenuEntry;
import org.opennms.web.navigate.DisplayStatus;
import org.opennms.web.navigate.MenuEntry;
import org.opennms.web.navigate.NavBarEntry;
import org.opennms.web.navigate.NavBarModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.AbstractView;

import freemarker.ext.beans.StringModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * This controller uses Freemarker to render the view.
 * This allows for the same code to be used to process calls via the OnmsHeaderProvider interface.
 *
 * @author jwhite
 */
public class NavBarController extends AbstractController implements InitializingBean, OnmsHeaderProvider, MenuProvider {
    private List<NavBarEntry> m_navBarItems;
    private CentralizedDateTimeFormat dateTimeFormat;
    private Configuration cfg;

    /**
     * <p>afterPropertiesSet</p>
     * @throws IOException
     */
    @Override
    public void afterPropertiesSet() throws IOException {
        Assert.state(m_navBarItems != null, "navBarItems property has not been set");

        dateTimeFormat = new CentralizedDateTimeFormat();

        // Initialize the Freemarker engine
        cfg = new Configuration(Configuration.VERSION_2_3_21);
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setServletContextForTemplateLoading(getServletContext(), "WEB-INF/templates");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    }

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return new ModelAndView(createView(), createModel(request));
    }

    @Override
    public List<MenuEntry> getMenu(final MenuContext context) {
        final List<MenuEntry> menu = new ArrayList<>();
        for (final NavBarEntry entry : getNavBarItems()) {
            final DefaultMenuEntry defaultMenuEntry = new DefaultMenuEntry(entry.getName(), entry.getUrl(), entry.evaluate(context));
            if (entry.getEntries() != null) {
                final List<MenuEntry> entries = entry.getEntries().stream()
                        .map(e -> new DefaultMenuEntry(e.getName(), e.getUrl(), e.evaluate(context)))
                        .collect(Collectors.toList());
                defaultMenuEntry.addEntries(entries);
            }
            menu.add(defaultMenuEntry);
        }
        return menu;
    }

    private Map<String, Object> createModel(final HttpServletRequest request) {
        Map<String, Object> model = new HashMap<String, Object>();

        // Create the NavBarModel
        final Map<NavBarEntry, DisplayStatus> navBar = new LinkedHashMap<NavBarEntry, DisplayStatus>();
        for (final NavBarEntry entry : getNavBarItems()) {
            navBar.put(entry, entry.evaluate(request));
        }
        model.put("model", new NavBarModel(request, navBar));

        // Add additional facts required to render the menu
        model.put("request", request);
        model.put("baseHref",
                org.opennms.web.api.Util.calculateUrlBase(request));
        model.put("isProvision", request.isUserInRole(Authentication.ROLE_PROVISION));
        model.put("isFlow", request.isUserInRole(Authentication.ROLE_FLOW_MANAGER));
        model.put("isAdmin", request.isUserInRole(Authentication.ROLE_ADMIN));
        model.put("formattedTime", this.dateTimeFormat.format(Instant.now(), extractUserTimeZone(request)));

        String noticeStatus = "Unknown";
        try {
            noticeStatus = NotifdConfigFactory.getPrettyStatus();
        } catch (final Throwable t) {
        }
        model.put("noticeStatus", noticeStatus);

        // Helper functions
        model.put("shouldDisplay", new ShouldDisplayEntryMethod(request));

        return model;
    }

    private ZoneId extractUserTimeZone(HttpServletRequest request){
        ZoneId timeZoneId = (ZoneId) request.getSession().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID);
        if(timeZoneId == null){
            timeZoneId = ZoneId.systemDefault();
        }
        return timeZoneId;
    }

    private FreemarkerView createView() throws IOException { // Fetches the template using the Freemarker Engine. It is not instatiated by default, as otherwise it is never reloaded if changed
        final Template template = cfg.getTemplate("navbar.ftl");
        final FreemarkerView view = new FreemarkerView(template);
        return view;
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

    @Override
    public String getHeaderHtml(HttpServletRequest request) throws Exception {
        return createView().renderMergedOutputModel(createModel(request));
    }

    /**
     * Spring MVC view generated by a Freemarker template with convenience
     * methods for rendering the view directly to a string.
     *
     * @author jwhite
     */
    private static class FreemarkerView extends AbstractView {
        private final Template template;

        public FreemarkerView(final Template template) {
            this.template = template;
        }

        public String renderMergedOutputModel(Map<String, Object> model) throws Exception {
            StringWriter writer = new StringWriter();
            renderMergedOutputModel(model,  writer);
            return writer.toString();
        }

        public void renderMergedOutputModel(Map<String, Object> model, Writer writer) throws Exception {
            template.process(model, writer);
        }

        @Override
        protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            renderMergedOutputModel(model, response.getWriter());
        }
    }

    /**
     * Used to determine whether or not a particular NavBarEntry should be
     * displayed.
     *
     * @author jwhite
     */
    public static class ShouldDisplayEntryMethod implements TemplateMethodModelEx {
        private final HttpServletRequest request;

        public ShouldDisplayEntryMethod(HttpServletRequest request) {
            this.request = request;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Boolean exec(List arguments) throws TemplateModelException {
            DisplayStatus entryDisplayStatus;
            if (arguments.size() == 1) {
                /*
                 * Evaluate the NavBarEntry's display status based on the
                 * current request.
                 */
                NavBarEntry entry = (NavBarEntry) ((StringModel) arguments
                        .get(0)).getWrappedObject();
                entryDisplayStatus = entry.evaluate(request);
            } else if (arguments.size() == 2) {
                /* Use the given display status */
                entryDisplayStatus = (DisplayStatus) ((StringModel) arguments
                        .get(1)).getWrappedObject();
            } else {
                throw new TemplateModelException("Wrong arguments");
            }
            return entryDisplayStatus != DisplayStatus.NO_DISPLAY;
        }
    }
}
