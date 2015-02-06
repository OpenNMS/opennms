package org.opennms.features.vaadin.surveillanceviews.service;

import org.opennms.features.vaadin.surveillanceviews.model.Category;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.SurveillanceStatus;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface for the surveillance view service.
 */
public interface SurveillanceViewService {
    List<OnmsCategory> getOnmsCategories();

    List<String> getRtcCategories();

    AlarmDao getAlarmDao();

    AlarmRepository getAlarmRepository();

    NodeDao getNodeDao();

    SurveillanceStatus[][] calculateCellStatus(final View view);

    Set<OnmsCategory> getOnmsCategoriesFromViewCategories(final Collection<Category> viewCats);
}
