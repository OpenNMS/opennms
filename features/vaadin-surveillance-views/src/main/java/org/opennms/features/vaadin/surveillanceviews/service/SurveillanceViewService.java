package org.opennms.features.vaadin.surveillanceviews.service;

import org.opennms.netmgt.model.OnmsCategory;

import java.util.List;

public interface SurveillanceViewService {

    public List<OnmsCategory> getOnmsCategories();
}
