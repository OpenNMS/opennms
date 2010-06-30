//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Aug 22: Add public constructor using a Reader for input, add a
//              setInstance method, and organize imports. - dj@opennms.org
//
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.categories.Categories;
import org.opennms.netmgt.config.categories.Category;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.dao.castor.CastorUtils;
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
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * @version $Id: $
 */
public final class CategoryFactory implements CatFactory{
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
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * 
     */
    private CategoryFactory(String configFile) throws IOException, MarshalException, ValidationException {
        this(new FileSystemResource(configFile));
    }
    
    /**
     * <p>Constructor for CategoryFactory.</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public CategoryFactory(Resource resource) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(Catinfo.class, resource);
    }
    
    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.CATEGORIES_CONF_FILE_NAME);
        setInstance(new CategoryFactory(cfgFile.getPath()));
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
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
	 * @param singleton a {@link org.opennms.netmgt.config.CatFactory} object.
	 */
	public static void setInstance(CatFactory singleton) {
		m_singleton=singleton;
		m_loaded=true;
	}
	
    /**
     * Return the categories configuration.
     *
     * @return the categories configuration
     */
    public synchronized Catinfo getConfig() {
        return m_config;
    }

    /**
     * Add a categorygroup.
     *
     * @param group
     *            category group to be added
     */
    public synchronized void addCategoryGroup(Categorygroup group) {
        m_config.addCategorygroup(group);
    }

    /**
     * Replace categorygroup.
     *
     * @param group
     *            category group to be replaced
     * @return true if categorygroup is successfully replaced
     */
    public synchronized boolean replaceCategoryGroup(Categorygroup group) {
        boolean replaced = false;

        String groupname = group.getName();

        int numCgs = m_config.getCategorygroupCount();
        for (int i = 0; i < numCgs; i++) {
            Categorygroup oldCg = m_config.getCategorygroup(i);
            if (oldCg.getName().equals(groupname)) {
                m_config.setCategorygroup(i, group);
                replaced = true;
                break;
            }
        }

        return replaced;
    }

    /**
     * Delete a categorygroup.
     *
     * @param group
     *            category group to be removed
     * @return true if categorygroup is successfully deleted
     */
    public synchronized boolean deleteCategoryGroup(Categorygroup group) {
        return m_config.removeCategorygroup(group);
    }

    /**
     * Delete a categorygroup.
     *
     * @param groupname
     *            category group to be removed
     * @return true if categorygroup is successfully deleted
     */
    public synchronized boolean deleteCategoryGroup(String groupname) {
        boolean deleted = false;

        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();
            if (cg.getName().equals(groupname)) {
                deleted = m_config.removeCategorygroup(cg);

                // make sure you break from the enumeration
                break;
            }
        }

        return deleted;
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
    public synchronized boolean addCategory(String groupname, Category cat) {
        boolean added = false;

        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();
            if (cg.getName().equals(groupname)) {
                // get categories and add
                Categories cats = cg.getCategories();
                cats.addCategory(cat);

                added = true;
                break;
            }
        }

        return added;
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
    public synchronized boolean replaceCategory(String groupname, Category cat) {
        boolean replaced = false;

        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();
            if (cg.getName().equals(groupname)) {
                String catlabel = cat.getLabel();

                // get categories and replace
                Categories cats = cg.getCategories();

                int numCats = cats.getCategoryCount();
                for (int i = 0; i < numCats; i++) {
                    Category oldCat = cats.getCategory(i);
                    if (oldCat.getLabel().equals(catlabel)) {
                        cats.setCategory(i, cat);
                        replaced = true;
                        break;
                    }
                }

            }
        }

        return replaced;
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
    public synchronized boolean deleteCategory(String groupname, Category cat) {
        boolean deleted = false;

        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();
            if (cg.getName().equals(groupname)) {
                // get categories and delete
                Categories cats = cg.getCategories();
                cats.removeCategory(cat);

                deleted = true;
                break;
            }
        }

        return deleted;
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
    public synchronized boolean deleteCategory(String groupname, String catlabel) {
        boolean deleted = false;

        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();
            if (cg.getName().equals(groupname)) {
                // get categories and delete
                Categories cats = cg.getCategories();

                Enumeration<Category> enumCat = cats.enumerateCategory();
                while (enumCat.hasMoreElements()) {
                    Category cat = enumCat.nextElement();
                    if (cat.getLabel().equals(catlabel)) {
                        cats.removeCategory(cat);

                        // make sure you break from the enumeration
                        deleted = true;
                        break;
                    }
                }

            }
        }

        return deleted;
    }

    /**
     * {@inheritDoc}
     *
     * Return the category specified by name.
     */
    public synchronized Category getCategory(String name) {
        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();

            // go through the categories
            Categories cats = cg.getCategories();

            Enumeration<Category> enumCat = cats.enumerateCategory();
            while (enumCat.hasMoreElements()) {
                Category cat = enumCat.nextElement();
                if (cat.getLabel().equals(name)) {
                    return cat;
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Return the normal value for the specified category.
     */
    public synchronized double getNormal(String catlabel) {
        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();

            // go through the categories
            Categories cats = cg.getCategories();

            Enumeration<Category> enumCat = cats.enumerateCategory();
            while (enumCat.hasMoreElements()) {
                Category cat = enumCat.nextElement();
                if (cat.getLabel().equals(catlabel)) {
                    return cat.getNormal();
                }
            }
        }

        return -1.0;
    }

    /**
     * {@inheritDoc}
     *
     * Return the warning value for the specified category.
     */
    public synchronized double getWarning(String catlabel) {
        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();

            // go through the categories
            Categories cats = cg.getCategories();

            Enumeration<Category> enumCat = cats.enumerateCategory();
            while (enumCat.hasMoreElements()) {
                Category cat = enumCat.nextElement();
                if (cat.getLabel().equals(catlabel)) {
                    return cat.getWarning();
                }
            }
        }

        return -1.0;
    }

    /**
     * Return the services list for the specified category.
     *
     * @param catlabel
     *            the label for the category whose services list is needed
     * @return the services list for the specified category, null if category is
     *         not found
     */
    public synchronized String[] getServices(String catlabel) {
        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();

            // go through the categories
            Categories cats = cg.getCategories();

            Enumeration<Category> enumCat = cats.enumerateCategory();
            while (enumCat.hasMoreElements()) {
                Category cat = enumCat.nextElement();
                if (cat.getLabel().equals(catlabel)) {
                    return cat.getService();
                }
            }
        }

        return null;
    }

    /**
     * Return the rule for the specified category.
     *
     * @param catlabel
     *            the label for the category whose services list is needed
     * @return the rule for the specified category, null if the category is not
     *         found
     */
    public synchronized String getRule(String catlabel) {
        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();

            // go through the categories
            Categories cats = cg.getCategories();

            Enumeration<Category> enumCat = cats.enumerateCategory();
            while (enumCat.hasMoreElements()) {
                Category cat = enumCat.nextElement();
                if (cat.getLabel().equals(catlabel)) {
                    return cat.getRule();
                }
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Return the effective rule for the specified category. The category rule
     * ANDed with the rule of the category group that the category belongs to.
     */
    public synchronized String getEffectiveRule(String catlabel) {
        Enumeration<Categorygroup> enumCG = m_config.enumerateCategorygroup();
        while (enumCG.hasMoreElements()) {
            Categorygroup cg = enumCG.nextElement();

            // go through the categories
            Categories cats = cg.getCategories();

            Enumeration<Category> enumCat = cats.enumerateCategory();
            while (enumCat.hasMoreElements()) {
                Category cat = enumCat.nextElement();
                if (cat.getLabel().equals(catlabel)) {
                    String catRule = "(" + cg.getCommon().getRule() + ") & (" + cat.getRule() + ")";
                    return catRule;
                }
            }
        }

        return null;
    }

}
