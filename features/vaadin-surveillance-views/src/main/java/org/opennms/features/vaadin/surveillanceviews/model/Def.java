package org.opennms.features.vaadin.surveillanceviews.model;

import java.util.List;

public interface Def {
    String getLabel();

    String getReportCategory();

    List<Category> getCategories();

    void setLabel(String label);

    void setReportCategory(String reportCategory);

    boolean containsCategory(String name);

}
