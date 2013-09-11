package org.opennms.web.tags;

import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.filter.QueryParameters;
import org.opennms.web.tags.filters.FilterCallback;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.text.MessageFormat;

public class FavoriteTag extends TagSupport {

    private static final String TEMPLATE = "{0}\n{1}";

    private static final String IMG_TEMPLATE = new String("<img style=\"cursor:pointer;\" title=\"{0}\" with=25 height=25 onClick=\"{1}\" src=\"{2}\"/>{3}");

    private static final String JAVASCRIPT_TEMPLATE =
            "<script type=\"text/javascript\">\n" +
            "   function createFavorite() {\n" +
            "       var favoriteName = prompt(\"Please enter a favorite name\", \"{DEFAULT_FAVORITE}\");\n" +
            "       if (favoriteName != null) {\n" +
            "           window.location.href = '{CREATE_FAVORITE_URL}' + favoriteName;\n" +
            "       }\n" +
            "}\n" +
            "\n" +
            "   function deleteFavorite(favoriteId) {\n" +
            "       var reallyDelete = confirm('Do you really want to delete this favorite?');\n" +
            "       if (reallyDelete) {\n" +
            "           window.location.href = '{DELETE_FAVORITE_URL}' + favoriteId;\n" +
            "       }\n" +
            "   }\n" +
            "</script>\n";

    private static final String DEFAULT_FAVORITE_NAME = "My Favorite";

    private FilterCallback filterCallback;

    private OnmsFilterFavorite favorite;

    private QueryParameters parameters;

    private String defaultFavoriteName;

    private String createFavoriteController;

    private String deleteFavoriteController;

    public void setCallback(FilterCallback callback) {
        this.filterCallback = callback;
    }

    public void setParameters(QueryParameters parameters) {
        this.parameters = parameters;
    }

    public void setCreateFavoriteController(String createFavoriteController) {
        if (createFavoriteController.startsWith("/")) {
            createFavoriteController = createFavoriteController.substring(1, createFavoriteController.length());
        }
        this.createFavoriteController = createFavoriteController;
    }

    public void setDeleteFavoriteController(String deleteFavoriteController) {
        if (deleteFavoriteController.startsWith("/")) {
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

    private String getImageString() {
        if (getFavorite() != null) {
            return MessageFormat.format(
                    IMG_TEMPLATE,
                    "delete the current selected favorite",
                    "deleteFavorite(" + getFavorite().getId() + ")",
                    "images/star_filled.png",
                    " " + getFavorite().getName());
        }
        return MessageFormat.format(
                IMG_TEMPLATE,
                "create a favorite with current filter settings",
                "createFavorite()",
                "images/star_empty.png",
                "");
    }

    private String getScriptString() {
        /* /opennms/event/createFavorite?...&favoriteName="" */
        final String createFavoriteUrl = MessageFormat.format(
                "{0}{1}?{2}&favoriteName=",
                getUrlBase(),
                getCreateFilterController(),
                getFiltersAsStringWithoutLeadingFilter());

        /* /opennms/event/deleteFavorite?...&favoriteName="" */
        final String deleteFavoriteUrl = MessageFormat.format("{0}{1}?{2}&favoriteId=",
                getUrlBase(),
                getDeleteFilterController(),
                getFiltersAsStringWithoutLeadingFilter());

        return JAVASCRIPT_TEMPLATE
                .replaceAll("\\{DEFAULT_FAVORITE\\}", getDefaultFavoriteName())
                .replaceAll("\\{CREATE_FAVORITE_URL\\}", createFavoriteUrl)
                .replaceAll("\\{DELETE_FAVORITE_URL\\}", deleteFavoriteUrl);
    }

    private String getFiltersAsStringWithoutLeadingFilter() {
        String filterString = filterCallback.getFiltersString(parameters.getFilters());
        filterString = filterString.replaceFirst("&amp;", "").replaceAll("&amp;", "&");
        return filterString;
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
}
