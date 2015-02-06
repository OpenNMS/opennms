package org.opennms.features.vaadin.surveillanceviews.model;

import java.util.List;
import java.util.Set;

/**
 * Helper inteface to handle similar column-def/row-def stuff
 */
public interface Def {
    String getLabel();

    String getReportCategory();

    List<Category> getCategories();

    Set<String> getCategoryNames();

    void setLabel(String label);

    void setReportCategory(String reportCategory);

    boolean containsCategory(String name);
}
