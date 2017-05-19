/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.CategoryGroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * This is the singleton class used to load the configuration from the
 * categories.xml. This provides convenience methods to get the configured
 * categories and their information, add/delete categories from category groups.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class CategoryFactory implements CatFactory {
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    
    /**
     * The singleton instance of this factory
     */
    private static CatFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private Catinfo m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * 
     */
    private CategoryFactory(final String configFile) throws IOException {
        this(new FileSystemResource(configFile));
    }
    
    /**
     * <p>Constructor for CategoryFactory.</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @throws java.io.IOException if any.
     */
    public CategoryFactory(final Resource resource) throws IOException {
        m_config = JaxbUtils.unmarshal(Catinfo.class, resource);
    }
    
    @Override
    public Lock getReadLock() {
        return m_readLock;
    }
    
    @Override
    public Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CATEGORIES_CONF_FILE_NAME);
        setInstance(new CategoryFactory(cfgFile.getPath()));
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws IOException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized CatFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

	/**
	 * <p>setInstance</p>
	 *
	 * @param singleton a {@link org.opennms.netmgt.config.api.CatFactory} object.
	 */
	public static void setInstance(final CatFactory singleton) {
		m_singleton = singleton;
		m_loaded = true;
	}
	
    /**
     * Return the categories configuration.
     *
     * @return the categories configuration
     */
    @Override
    public Catinfo getConfig() {
        try {
            getReadLock().lock();
            return m_config;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * Add a categorygroup.
     *
     * @param group
     *            category group to be added
     */
    public void addCategoryGroup(final CategoryGroup group) {
        try {
            getWriteLock().lock();
            m_config.addCategoryGroup(group);
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * Replace categorygroup.
     *
     * @param group
     *            category group to be replaced
     * @return true if categorygroup is successfully replaced
     */
    public boolean replaceCategoryGroup(final CategoryGroup group) {
        try {
            getWriteLock().lock();

            final String groupname = group.getName();
    
            for (int i = 0; i < m_config.getCategoryGroups().size(); i++) {
                final int index1 = i;
                final CategoryGroup oldCg = m_config.getCategoryGroups().get(index1);
                if (oldCg.getName().equals(groupname)) {
                    final int index = i;
                    m_config.getCategoryGroups().set(index, group);
                    return true;
                }
            }
        } finally {
            getWriteLock().unlock();
        }
        return false;
    }

    /**
     * Delete a categorygroup.
     *
     * @param group
     *            category group to be removed
     * @return true if categorygroup is successfully deleted
     */
    public boolean deleteCategoryGroup(final CategoryGroup group) {
        try {
            getWriteLock().lock();
            return m_config.removeCategoryGroup(group);
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * Delete a categorygroup.
     *
     * @param groupname
     *            category group to be removed
     * @return true if categorygroup is successfully deleted
     */
    public boolean deleteCategoryGroup(final String groupname) {
        try {
            getWriteLock().lock();
            return m_config.removeCategoryGroup(groupname);
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * Add category to a categorygroup.
     *
     * @param groupname
     *            category group to which category is to be added
     * @param cat
     *            category to be added
     * @return true if category is successfully added to the specified category
     *         group
     */
    public boolean addCategory(final String groupname, final Category cat) {
        try {
            getWriteLock().lock();
            for (final CategoryGroup cg : m_config.getCategoryGroups()) {
                if (cg.getName().equals(groupname)) {
                    cg.addCategory(cat);
                    return true;
                }
            }
        } finally {
            getWriteLock().unlock();
        }
        return false;
    }

    /**
     * Replace category in a categorygroup.
     *
     * @param groupname
     *            category group to which category is to be added
     * @param cat
     *            category to be replaced
     * @return true if category is successfully replaced in the specified
     *         category group
     */
    public boolean replaceCategory(final String groupname, final Category cat) {
        try {
            getWriteLock().lock();
            m_config.replaceCategoryInGroup(groupname, cat);
        } finally {
            getWriteLock().unlock();
        }
        return false;
    }

    /**
     * Delete category from a categorygroup.
     *
     * @param groupname
     *            category group from which category is to be removed
     * @param cat
     *            category to be deleted
     * @return true if category is successfully deleted from the specified
     *         category group
     */
    public boolean deleteCategory(final String groupname, final Category cat) {
        try {
            getWriteLock().lock();
            for (final CategoryGroup cg : m_config.getCategoryGroups()) {
                if (cg.getName().equals(groupname)) {
                    cg.removeCategory(cat);
                    return true;
                }
            }
        } finally {
            getWriteLock().unlock();
        }
        return false;
    }

    /**
     * Delete category from a categorygroup.
     *
     * @param groupname
     *            category group from which category is to be removed
     * @param catlabel
     *            label of the category to be deleted
     * @return true if category is successfully deleted from the specified
     *         category group
     */
    public boolean deleteCategory(final String groupname, final String catlabel) {
        try {
            getWriteLock().lock();
            for (final CategoryGroup cg : m_config.getCategoryGroups()) {
                if (cg.getName().equals(groupname)) {
                    cg.removeCategory(catlabel);
                }
            }
        } finally {
            getWriteLock().unlock();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Return the category specified by name.
     */
    @Override
    public Category getCategory(final String name) {
        try {
            getReadLock().lock();
            for (final CategoryGroup cg: m_config.getCategoryGroups()) {
                for (final Category cat : cg.getCategories()) {
                    if (cat.getLabel().equals(name)) {
                        return cat;
                    }
                }
            }
        } finally {
            getReadLock().unlock();
        }
        
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Return the normal value for the specified category.
     */
    @Override
    public double getNormal(final String catlabel) {
        final Category cat = getCategory(catlabel);
        return (cat == null? -1.0 : cat.getNormalThreshold());
    }

    /**
     * {@inheritDoc}
     *
     * Return the warning value for the specified category.
     */
    @Override
    public double getWarning(final String catlabel) {
        final Category cat = getCategory(catlabel);
        return (cat == null? -1.0 : cat.getWarningThreshold());
    }

    /**
     * Return the services list for the specified category.
     *
     * @param catlabel
     *            the label for the category whose services list is needed
     * @return the services list for the specified category, null if category is
     *         not found
     */
    public String[] getServices(final String catlabel) {
        final Category cat = getCategory(catlabel);
        return (cat == null? null : cat.getServices().toArray(new String[0]));
    }

    /**
     * Return the rule for the specified category.
     *
     * @param catlabel
     *            the label for the category whose services list is needed
     * @return the rule for the specified category, null if the category is not
     *         found
     */
    public String getRule(final String catlabel) {
        final Category cat = getCategory(catlabel);
        return (cat == null? null : cat.getRule());
    }

    /**
     * {@inheritDoc}
     *
     * Return the effective rule for the specified category. The category rule
     * ANDed with the rule of the category group that the category belongs to.
     */
    @Override
    public String getEffectiveRule(final String catlabel) {
        try {
            getReadLock().lock();
            for (final CategoryGroup cg : m_config.getCategoryGroups()) {
                for (final Category cat : cg.getCategories()) {
                    if (cat.getLabel().equals(catlabel)) {
                        return "(" + cg.getCommon().getRule() + ") & (" + cat.getRule() + ")";
                    }
                }
            }
        } finally {
            getReadLock().unlock();
        }
        return null;
    }

}
