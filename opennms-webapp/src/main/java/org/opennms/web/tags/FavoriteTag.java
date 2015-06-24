/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.tags;

import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.NormalizedQueryParameters;
import org.opennms.web.filter.QueryParameters;
import org.opennms.web.tags.filters.FilterCallback;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

public class FavoriteTag extends TagSupport {

    private static final long serialVersionUID = -2620230289707902880L;

    public static interface Action {

        public String getJavascript(FavoriteTag favoriteTag);

    }

    public static Action CLEAR_FILTERS = new Action() {

        private static final String DESELECT_FAVORITE_JAVASCRIPT_TEMPLATE =
                "   function clearFilters() {\n" +
                "       window.location.href = '{CLEAR_URL}'\n" +
                "   }";

        @Override
        public String getJavascript(FavoriteTag favoriteTag) {
            final NormalizedQueryParameters parameters = new NormalizedQueryParameters(favoriteTag.parameters);
            parameters.setFilters(new ArrayList<Filter>());

            return DESELECT_FAVORITE_JAVASCRIPT_TEMPLATE
                    .replaceAll(
                            "\\{CLEAR_URL\\}",
                            createLink(favoriteTag.filterCallback, favoriteTag.getContext(), parameters, null));
        }
    };

    public static Action DELETE_FAVORITE = new Action() {

        private static final String DELETE_FAVORITE_JAVASCRIPT_TEMPLATE =
                "   function deleteFavorite(favoriteId) {\n" +
                "       var reallyDelete = confirm('Do you really want to delete this favorite?');\n" +
                "       if (reallyDelete) {\n" +
                "           window.location.href = '{DELETE_FAVORITE_URL}' + favoriteId;\n" +
                "       }\n" +
                "   }";

        @Override
        public String getJavascript(FavoriteTag favoriteTag) {
            /* /opennms/event/deleteFavorite */
            final String urlBase = favoriteTag.getUrlBase() + favoriteTag.getDeleteFilterController();
            final String deleteFavoriteUrl = createLink(favoriteTag.filterCallback, urlBase, favoriteTag.parameters, null);

            return DELETE_FAVORITE_JAVASCRIPT_TEMPLATE.
                    replaceAll("\\{DELETE_FAVORITE_URL\\}",MessageFormat.format("{0}&favoriteId=", deleteFavoriteUrl));
        }
    };

    public static Action CREATE_FAVORITE = new Action() {

        private static final String CREATE_FAVORITE_JAVASCRIPT_TEMPLATE =
                "   function createFavorite() {\n" +
                "       var favoriteName = prompt(\"Please enter a name for this filter favorite:\", \"\");\n" +
                "       if (favoriteName != null && favoriteName != '') {\n" +
                "           window.location.href = '{CREATE_FAVORITE_URL}' + favoriteName;\n" +
                "       }\n" +
                "}";

        @Override
        public String getJavascript(FavoriteTag favoriteTag) {
            final String urlBase = favoriteTag.getUrlBase() + favoriteTag.getCreateFilterController(); // /opennms/event/createFavorite
            final String createUrl = createLink(favoriteTag.filterCallback, urlBase, favoriteTag.parameters, null);

            /* /opennms/event/createFavorite?...&favoriteName="" */
            final String createFavoriteURL = MessageFormat.format("{0}&favoriteName=",createUrl);

            return CREATE_FAVORITE_JAVASCRIPT_TEMPLATE
                    .replaceAll("\\{CREATE_FAVORITE_URL\\}", createFavoriteURL);
        }
    };

    private static String createLink(FilterCallback callback, String urlBase, QueryParameters params, OnmsFilterFavorite favorite) {
        return callback.createLink(urlBase, params, favorite).replaceAll("&amp;", "&");
    }

    /**
     * The callback to handle link-creation and such. Is needed to determine e.g. to create an alert or event-link.
     */
    private FilterCallback filterCallback;

    /**
     * Is needed to decide if the IMG_TEMPLATE should be rendered as an empty or a filled star.
     * Can be null. If null, then an empty-star is shown. Otherwise the filled star is shown.
     */
    private OnmsFilterFavorite favorite;

    /**
     * The QueryParamters (e.g. {@link org.opennms.web.event.EventQueryParms}, {@link org.opennms.web.alarm.AlarmQueryParms}, {@link org.opennms.web.filter.NormalizedQueryParameters}.
     * Is needed to get the filter parameters from the original request.
     */
    private QueryParameters parameters;

    /**
     * The controller to invoke on favorite creation (e.g. /event/createFavorite)
      */
    private String createFavoriteController;

    /**
     * The controller to invoke on favorite deletion (e.g. /event/deleteFavorite)
     */
    private String deleteFavoriteController;


    private String context;

    public void setCallback(FilterCallback callback) {
        this.filterCallback = callback;
    }

    public void setParameters(QueryParameters parameters) {
        this.parameters = parameters;
    }

    public void setCreateFavoriteController(String createFavoriteController) {
        if (createFavoriteController.startsWith("/")) { // remove leading "/"
            createFavoriteController = createFavoriteController.substring(1, createFavoriteController.length());
        }
        this.createFavoriteController = createFavoriteController;
    }

    public void setDeleteFavoriteController(String deleteFavoriteController) {
        if (deleteFavoriteController.startsWith("/")) { // remove leading "/"
            deleteFavoriteController = deleteFavoriteController.substring(1, deleteFavoriteController.length());
        }
        this.deleteFavoriteController = deleteFavoriteController;
    }

    public void setFavorite(OnmsFilterFavorite favorite) {
        this.favorite = favorite;
    }

    public void setContext(String context) {
        if (context != null && context.startsWith("/")) {
            context = context.substring(1, context.length());
        }
        this.context = context;
    }

    public OnmsFilterFavorite getFavorite() {
        return favorite;
    }

    @Override
    public int doStartTag() throws JspException {
        // Print out a javascript block with the select and deselect functions
        out(String.format(
            "<script type=\"text/javascript\">\n%s\n%s\n%s\n</script>\n",
            CREATE_FAVORITE.getJavascript(this),
            DELETE_FAVORITE.getJavascript(this),
            CLEAR_FILTERS.getJavascript(this)
        ));
        return EVAL_BODY_INCLUDE;
    }

    private String getUrlBase() {
        String contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();
        if (!contextPath.endsWith("/")) contextPath += "/";
        return contextPath;
    }

    private void out(String content) throws JspException {
        try {
            pageContext.getOut().write(content);
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    private String getCreateFilterController() {
        return createFavoriteController;
    }

    private String getDeleteFilterController() {
        return deleteFavoriteController;
    }

    private String getContext() {
        return getUrlBase() + context;
    }
}
