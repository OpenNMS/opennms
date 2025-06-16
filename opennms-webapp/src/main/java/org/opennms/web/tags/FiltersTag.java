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
package org.opennms.web.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.NormalizedAcknowledgeType;
import org.opennms.web.filter.NormalizedQueryParameters;
import org.opennms.web.filter.QueryParameters;
import org.opennms.web.tags.filters.FilterCallback;

/**
 * Renders the filter area for a given set of filters.
 */
public class FiltersTag extends TagSupport {

    private static final long serialVersionUID = -8419211806241306127L;

    private static final String TEMPLATE = "{LEADING}{FILTERS}";

    private static final String FILTER_TEMPLATE = "<a class=\"btn btn-primary\" style=\"white-space:nowrap;\" href=\"{REMOVE_LINK}\" title=\"{REMOVE_LINK_TITLE}\">{FILTER_DESCRIPTION}&nbsp;&nbsp;<i class=\"fa fa-close\"></i></a>&nbsp;&nbsp;";

    private String showRemoveLink;

    private String showAcknowledgeFilter;

    private String acknowledgeFilterPrefix;

    private String acknowledgeFilterSuffix;

    private OnmsFilterFavorite favorite;

    private QueryParameters parameters;

    private FilterCallback filterCallback;

    private String context;

    public void setContext(String context) {
        if (context != null && context.startsWith("/")) context = context.substring(1, context.length());
        this.context = context;
    }

    public void setAcknowledgeFilterPrefix(String acknowledgeFilterPrefix) {
        this.acknowledgeFilterPrefix = acknowledgeFilterPrefix;
    }

    public void setAcknowledgeFilterSuffix(String acknowledgeFilterSuffix) {
        this.acknowledgeFilterSuffix = acknowledgeFilterSuffix;
    }

    public void setCallback(FilterCallback filterCallback) {
        this.filterCallback = filterCallback;
    }

    public void setShowAcknowledgeFilter(String showAcknowledgeFilter) {
        this.showAcknowledgeFilter = showAcknowledgeFilter;
    }

    public void setShowRemoveLink(String showRemoveLink) {
        this.showRemoveLink = showRemoveLink;
    }

    public void setParameters(QueryParameters parameters) {
        this.parameters = parameters;
    }

    public void setFavorite(OnmsFilterFavorite favorite) {
        this.favorite = favorite;
    }

    private List<Filter> getFilters() {
        if (parameters == null || parameters.getFilters() == null) return new ArrayList<>();
        return parameters.getFilters();

    }

    @Override
    public int doStartTag() throws JspException {
        final String leadingString = getLeading();
        final StringBuilder filterBuffer = new StringBuilder();

        for (Filter eachFilter : getFilters()) {
            NormalizedQueryParameters params = new NormalizedQueryParameters(parameters);
            params.getFilters().remove(eachFilter);
            filterBuffer.append(FILTER_TEMPLATE
                    .replaceAll("\\{FILTER_DESCRIPTION\\}", WebSecurityUtils.sanitizeString(eachFilter.getTextDescription()))
                    .replaceAll("\\{REMOVE_LINK\\}", isShowRemoveLink() ? filterCallback.createLink(getUrlBase(), params, favorite) : "")
                    .replaceAll("\\{REMOVE_LINK_TITLE\\}", isShowRemoveLink() ? "Remove filter" : "")
            );
        }

        out(TEMPLATE
                .replaceAll("\\{LEADING\\}", leadingString)
                .replaceAll("\\{FILTERS\\}", filterBuffer.toString()));
        return EVAL_BODY_INCLUDE;
    }

    private void out(String content) throws JspException {
        try {
            pageContext.getOut().write(content);
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    private boolean isShowRemoveLink() {
        return Boolean.valueOf(showRemoveLink);
    }

    private boolean isShowAcknowledgeFilter() {
        return Boolean.valueOf(showAcknowledgeFilter);
    }

    private boolean isAcknowledgeType() {
        if (parameters != null) {
            return parameters.getAckType().equals(NormalizedAcknowledgeType.ACKNOWLEDGED);
        }
        return false;
    }

    private boolean isUnacknowledgeType() {
        if (parameters != null) {
            return parameters.getAckType().equals(NormalizedAcknowledgeType.UNACKNOWLEDGED);
        }
        return false;
    }

    private String getAcknowledgeFilterPrefix() {
        return acknowledgeFilterPrefix != null ? acknowledgeFilterPrefix : "";
    }

    private String getAcknowledgeFilterSuffix() {
        return acknowledgeFilterSuffix != null ? acknowledgeFilterSuffix : "";
    }

    private String getLeading() {
        final StringBuilder leadingString = new StringBuilder();
        if (isShowAcknowledgeFilter()) {
            NormalizedQueryParameters params = new NormalizedQueryParameters(parameters);
            if (isAcknowledgeType()) {
                params.setAckType(NormalizedAcknowledgeType.UNACKNOWLEDGED);
                leadingString.append(FILTER_TEMPLATE
                        .replaceAll("\\{FILTER_DESCRIPTION\\}", getAcknowledgeFilterPrefix() + " acknowledged")
                        .replaceAll("\\{REMOVE_LINK\\}", isShowRemoveLink() ? filterCallback.createLink(getUrlBase(), params, favorite) : "")
                        .replaceAll("\\{REMOVE_LINK_TITLE\\}", isShowRemoveLink() ? "Show outstanding " + getAcknowledgeFilterSuffix() : ""));
            } else if (isUnacknowledgeType()) {
                params.setAckType(NormalizedAcknowledgeType.ACKNOWLEDGED);
                leadingString.append(FILTER_TEMPLATE
                        .replaceAll("\\{FILTER_DESCRIPTION\\}", getAcknowledgeFilterPrefix() + " outstanding")
                        .replaceAll("\\{REMOVE_LINK\\}", isShowRemoveLink() ? filterCallback.createLink(getUrlBase(), params, favorite) : "")
                        .replaceAll("\\{REMOVE_LINK_TITLE\\}", isShowRemoveLink() ? "Show acknowledged " + getAcknowledgeFilterSuffix() : ""));
            }
        }
        return leadingString.toString();
    }

    private String getUrlBase() {
        String urlBase = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
        if (!urlBase.endsWith("/")) urlBase += "/";
        return urlBase + context;
    }
}
