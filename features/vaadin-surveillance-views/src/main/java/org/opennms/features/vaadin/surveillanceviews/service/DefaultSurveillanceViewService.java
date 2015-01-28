package org.opennms.features.vaadin.surveillanceviews.service;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.categories.CatFactory;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SurveillanceViewConfigDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

public class DefaultSurveillanceViewService implements SurveillanceViewService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSurveillanceViewService.class);

    private NodeDao m_nodeDao;
    private ResourceDao m_resourceDao;
    private GraphDao m_graphDao;
    private NotificationDao m_notificationDao;
    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;
    private CategoryDao m_categoryDao;
    private AlarmDao m_alarmDao;
    private GroupDao m_groupDao;
    private OutageDao m_outageDao;

    public void setNodeDao(NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        this.m_resourceDao = resourceDao;
    }

    public void setGraphDao(GraphDao graphDao) {
        this.m_graphDao = graphDao;
    }

    public void setNotificationDao(NotificationDao notificationDao) {
        this.m_notificationDao = notificationDao;
    }

    public void setSurveillanceViewConfigDao(SurveillanceViewConfigDao surveillanceViewConfigDao) {
        this.m_surveillanceViewConfigDao = surveillanceViewConfigDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        this.m_categoryDao = categoryDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        this.m_alarmDao = alarmDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.m_groupDao = groupDao;
    }

    public void setOutageDao(OutageDao outageDao) {
        this.m_outageDao = outageDao;
    }

    @Override
    public List<OnmsCategory> getOnmsCategories() {
        getRtcCategories();
        return m_categoryDao.findAll();
    }

    public List<String> getRtcCategories() {

        CatFactory cFactory = null;

        try {
            CategoryFactory.init();
            cFactory = CategoryFactory.getInstance();

        } catch (IOException ex) {
            LOG.error("Failed to load categories information", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (MarshalException ex) {
            LOG.error("Failed to load categories information", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            LOG.error("Failed to load categories information", ex);
            throw new UndeclaredThrowableException(ex);
        }

        List<String> categories = new ArrayList<String>();

        cFactory.getReadLock().lock();

        try {
            for (Categorygroup cg : cFactory.getConfig().getCategorygroupCollection()) {
                for (final org.opennms.netmgt.config.categories.Category category : cg.getCategories().getCategoryCollection()) {
                    categories.add(category.getLabel());
                }
            }
        } finally {
            cFactory.getReadLock().unlock();
        }

        return categories;
    }
}

