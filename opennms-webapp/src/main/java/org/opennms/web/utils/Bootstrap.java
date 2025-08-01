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

    /**
     * Some pages require that 'headerLogoutForm', which posts to 'j_spring_security_logout', exist on the page.
     * Generally for logout we 'click' the Self Service menu on the Vue Menubar.
     * But for some pages, we use Angular etc. to perform a logout and need this form.
     */
    private boolean includeLogoutForm;

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
        request.setAttribute("__bs_includeLogoutForm", this.includeLogoutForm ? "true" : "false");
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

    public Bootstrap includeLogoutForm() {
        this.includeLogoutForm = true;
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
