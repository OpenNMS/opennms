package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import org.opennms.netmgt.model.OnmsCategory;

import java.util.Set;

public interface SurveillanceViewDetail {
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories);
}
