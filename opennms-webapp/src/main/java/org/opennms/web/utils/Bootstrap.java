/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.web.utils;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

public class Bootstrap {

    private final PageContext pageContext;

    private final List<String> headTitles = new ArrayList<>();

    private final List<Breadcrumb> breadcrumbs = new ArrayList<>();

    private String ngApp;
    private String scrollSpy;

    private Set<String> flags = new HashSet<>();

    private Bootstrap(final PageContext pageContext) {
        this.pageContext = Objects.requireNonNull(pageContext);
    }

    private static String eval(final PageContext pageContext, final String attr, final String expr) {
        try {
            return Objects.toString(ExpressionEvaluatorManager.evaluate(attr, expr, Object.class, pageContext));
        } catch (final JspException e) {
            throw new RuntimeException(e);
        }
    }

    public void build(final HttpServletRequest request) throws JspException {
        request.setAttribute("__bs_headTitles", this.headTitles);
        request.setAttribute("__bs_ngApp", this.ngApp);
        request.setAttribute("__bs_scrollSpy", this.scrollSpy);
        request.setAttribute("__bs_breadcrumbs", this.breadcrumbs);
        request.setAttribute("__bs_flags", this.flags);
    }

    public Bootstrap headTitle(final String headTitle) {
        this.headTitles.add(eval(this.pageContext, "headTitle", Objects.requireNonNull(headTitle)));
        return this;
    }

    private Bootstrap breadcrumb(final Breadcrumb entry) {
        this.breadcrumbs.add(Objects.requireNonNull(entry));
        return this;
    }

    public Bootstrap breadcrumb(final String title) {
        final var breadcrumb = new Breadcrumb(eval(this.pageContext, "breadcrumb.title", title));
        return this.breadcrumb(breadcrumb);
    }

    public Bootstrap breadcrumb(final String title, final String link) {
        final var breadcrumb = new Breadcrumb(eval(this.pageContext, "breadcrumb.title", title))
                .withLink(eval(this.pageContext, "breadcrumb.link", link));

        return this.breadcrumb(breadcrumb);
    }

    public Bootstrap scrollSpy(final String scrollSpy) {
        this.scrollSpy = scrollSpy;
        return this;
    }

    public Bootstrap ngApp(final String ngApp) {
        this.ngApp = ngApp;
        return this;
    }

    public Bootstrap flags(final String... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public static class Breadcrumb {
        public final String title;
        public final String link;

        public Breadcrumb(final String title) {
            this(title, null);
        }

        public Breadcrumb(final String title, final String link) {
            this.title = Objects.requireNonNull(title);
            this.link = link;
        }

        public Breadcrumb withLink(final String link) {
            return new Breadcrumb(this.title, link);
        }

        public String getTitle() {
            return this.title;
        }

        public String getLink() {
            return this.link;
        }
    }

    public static Bootstrap with(final PageContext pageContext) {
        return new Bootstrap(pageContext);
    }
}
