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

    public static interface Action {

        String getDescription();
        String getJavascriptCallback(FavoriteTag favoriteTag);
        String getJavascript(FavoriteTag favoriteTag);

        Action CLEAR_FILTERS = new Action() {

            @Override
            public String getDescription() {
                return "clear favorite and reset all filter criteria";
            }

            @Override
            public String getJavascriptCallback(FavoriteTag favoriteTag) {
                return "clearFilters()";
            }

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

        Action DELETE_FAVORITE = new Action() {
            @Override
            public String getDescription() {
                return "delete the current selected favorite";
            }

            @Override
            public String getJavascriptCallback(FavoriteTag favoriteTag) {
                return "deleteFavorite(" + favoriteTag.getFavorite().getId() + ")";
            }

            @Override
            public String getJavascript(FavoriteTag favoriteTag) {
                /* /opennms/event/deleteFavorite */
                final String urlBase = favoriteTag.getUrlBase() + favoriteTag.getDeleteFilterController();
                final String deleteFavoriteUrl = createLink(favoriteTag.filterCallback, urlBase, favoriteTag.parameters, null);

                return DELETE_FAVORITE_JAVASCRIPT_TEMPLATE.
                        replaceAll("\\{DELETE_FAVORITE_URL\\}",MessageFormat.format("{0}&favoriteId=", deleteFavoriteUrl));
            }
        };

        Action CREATE_FAVORITE = new Action() {

            @Override
            public String getDescription() {
                return "create a favorite with current filter settings";
            }

            @Override
            public String getJavascriptCallback(FavoriteTag favoriteTag) {
                return "createFavorite()";
            }

            @Override
            public String getJavascript(FavoriteTag favoriteTag) {
                final String urlBase = favoriteTag.getUrlBase() + favoriteTag.getCreateFilterController(); // /opennms/event/createFavorite
                final String createUrl = createLink(favoriteTag.filterCallback, urlBase, favoriteTag.parameters, null);

                /* /opennms/event/createFavorite?...&favoriteName="" */
                final String createFavoriteURL = MessageFormat.format("{0}&favoriteName=",createUrl);

                return CREATE_FAVORITE_JAVASCRIPT_TEMPLATE
                        .replaceAll("\\{DEFAULT_FAVORITE\\}", favoriteTag.getDefaultFavoriteName())
                        .replaceAll("\\{CREATE_FAVORITE_URL\\}", createFavoriteURL);
            }
        };
    }

    private static Action DEFAULT_DESELECT_ACTION = Action.CLEAR_FILTERS;

    private static final String TEMPLATE = "{0}\n{1}";

    /**
     * Template for the favorite image.
     * It is either a filled or an empty star. The title onClick and such are also variable.
     */
    private static final String IMG_TEMPLATE = new String("<img style=\"cursor:pointer;\" title=\"{0}\" with=25 height=25 onClick=\"{1}\" src=\"{2}\"/>{3}");

    private static final String BUTTON_TEMPLATE = new String("<button onClick=\"{0}\" title=\"{1}\" >{2}</button>");

    private static final String JAVASCRIPT_TEMPLATE =    "<script type=\"text/javascript\">\n{SELECT_SCRIPT}\n\n{DESELECT_SCRIPT}\n</script>\n";

    private static final String CREATE_FAVORITE_JAVASCRIPT_TEMPLATE =
            "   function createFavorite() {\n" +
            "       var favoriteName = prompt(\"Please enter a favorite name\", \"{DEFAULT_FAVORITE}\");\n" +
            "       if (favoriteName != null) {\n" +
            "           window.location.href = '{CREATE_FAVORITE_URL}' + favoriteName;\n" +
            "       }\n" +
            "}";

    private static final String DELETE_FAVORITE_JAVASCRIPT_TEMPLATE =
            "   function deleteFavorite(favoriteId) {\n" +
            "       var reallyDelete = confirm('Do you really want to delete this favorite?');\n" +
            "       if (reallyDelete) {\n" +
            "           window.location.href = '{DELETE_FAVORITE_URL}' + favoriteId;\n" +
            "       }\n" +
            "   }";

    private static final String DESELECT_FAVORITE_JAVASCRIPT_TEMPLATE =
            "   function clearFilters() {\n" +
            "       window.location.href = '{CLEAR_URL}'\n" +
            "   }";

    /**
     * If no default favorite name is configured for this tag inside the JSP file this one is used as the DEFAULT value.
     */
    private static final String DEFAULT_FAVORITE_NAME = "My Favorite";

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
     * (Optional) Favorite name to use as the default value, which is shown in the "How do you want to name your favorite" prompt.
     */
    private String defaultFavoriteName;

    /**
     * The controller to invoke on favorite creation (e.g. /event/createFavorite)
      */
    private String createFavoriteController;

    /**
     * The controller to invoke on favorite deletion (e.g. /event/deleteFavorite)
     */
    private String deleteFavoriteController;


    private String context;

    private Action deselectAction;

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

    public void setDefaultFavoriteName(String favoriteName) {
        defaultFavoriteName = favoriteName;
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

    public void setOnDeselect(Action action) {
        this.deselectAction = action;
    }

    public OnmsFilterFavorite getFavorite() {
        return favorite;
    }

    @Override
    public int doStartTag() throws JspException {
        String imageString = getImageString();
        String scriptString = getScriptString();
        String output = MessageFormat.format(TEMPLATE, scriptString, imageString);
        out(output);
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Creates the image either with a filled or an empty star.
     *
     * @return The image String.
     */
    private String getImageString() {
        if (getFavorite() != null) {
            return MessageFormat.format(
                    BUTTON_TEMPLATE,
                    getDeselectAction().getJavascriptCallback(this),
                    getDeselectAction().getDescription(),
                    "remove filter");
        }
        return MessageFormat.format(
                BUTTON_TEMPLATE,
                getSelectAction().getJavascriptCallback(this),
                getSelectAction().getDescription(),
                "save filter");
    }

    /**
     * Creates the java-script String for "createFavorite" and "deleteFavorite".
     * @return
     */
    private String getScriptString() {
        final Action selectAction = getSelectAction();
        final Action deselectAction = getDeselectAction();

        return JAVASCRIPT_TEMPLATE
                .replaceAll("\\{SELECT_SCRIPT\\}", selectAction.getJavascript(this))
                .replaceAll("\\{DESELECT_SCRIPT\\}", deselectAction.getJavascript(this));
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

    private String getDefaultFavoriteName() {
        return defaultFavoriteName != null ? defaultFavoriteName : DEFAULT_FAVORITE_NAME;
    }

    private String getContext() {
        return getUrlBase() + context;
    }

    private Action getSelectAction() {
        return Action.CREATE_FAVORITE;
    }

    private Action getDeselectAction() {
        if (deselectAction != null) return deselectAction;
        return DEFAULT_DESELECT_ACTION;
    }
}
