/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.opennms.netmgt.config.ViewsDisplayFactory;
import org.opennms.netmgt.config.viewsdisplay.Section;
import org.opennms.netmgt.config.viewsdisplay.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>CategoryList class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CategoryList {
	
	private static final Logger LOG = LoggerFactory.getLogger(CategoryList.class);


    protected final CategoryModel m_model;

    /**
     * Display rules from viewsdisplay.xml. If null, then just show all known
     * categories under the header "Category". (See the getSections method.)
     */
    protected final List<Section> m_sections;

    protected final int m_disconnectTimeout;

    /**
     * <p>Constructor for CategoryList.</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public CategoryList() throws ServletException {
        try {
            m_model = CategoryModel.getInstance();
        } catch (Throwable e) {
            LOG.error("failed to instantiate the category model: {}", e, e);
            throw new ServletException("failed to instantiate the category model: " + e, e);
        }

        List<Section> sections = Collections.emptyList();
        int disconnectTimeout = 130000;
        try {
            ViewsDisplayFactory.init();
            ViewsDisplayFactory viewsDisplayFactory = ViewsDisplayFactory.getInstance();

            View view = viewsDisplayFactory.getDefaultView();

            if (view != null) {
                sections = view.getSections();
                disconnectTimeout  = viewsDisplayFactory.getDisconnectTimeout();
                LOG.debug("found display rules from viewsdisplay.xml");
            } else {
                LOG.debug("did not find display rules from viewsdisplay.xml");
            }
        } catch (Throwable e) {
            LOG.error("Couldn't open viewsdisplay factory on categories box: {}", e, e);
        }

        m_sections = sections;
        m_disconnectTimeout = disconnectTimeout;
    }


    /**
     * For the given map of category names to Category objects, organize the
     * categories into the currently active display rules.
     *
     * <p>
     * If there are no display rules, a single section named <em>Category</em>
     * will be returned. It will include all the categories in the category map,
     * in alphabetical order by category name.
     * </p>
     *
     * @param categoryMap a {@link java.util.Map} object.
     * @return a {@link java.util.List} object.
     * @throws java.io.IOException if any.
     */
    private List<Section> getSections(Map<String, Category> categoryMap) throws IOException {
        if (m_sections != null) {
            // Just return the display rules as a list.
            return m_sections;
        }

        List<Section> sectionList = null;

        Section section = new Section();
        section.setSectionName("Category");

        // Put the categories in a TreeMap to sort them alphabetically.
        TreeMap<String, Category> orderedMap = new TreeMap<String, Category>(categoryMap);

        // Iterate over the categories, adding each to the name list.
        for (Iterator<Map.Entry<String,Category>> i = orderedMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String,Category> entry = i.next();
            Category category = (Category) entry.getValue();

            section.addCategory(category.getName());
        }

        // Add our one section to the sections list.
        sectionList = new ArrayList<Section>();
        sectionList.add(section);

        return sectionList;
    }

    /**
     * <p>getCategoryData</p>
     *
     * @return a {@link java.util.Map} object.
     * @throws java.io.IOException if any.
     */
    public Map<String, List<Category>> getCategoryData() throws IOException {

        Map<String, Category> categoryMap = m_model.getCategoryMap();
        List<Section> sectionList = getSections(categoryMap);

        Map<String, List<Category>> categoryData = new LinkedHashMap<String, List<Category>>();

        for (Section section : sectionList) {
            List<Category> categories = new LinkedList<Category>();

            for (final String categoryName : section.getCategories()) {
                final Category category = (Category) categoryMap.get(categoryName);

                if (category == null) {
                    categories.add(new Category(categoryName));
                } else {
                    categories.add(category);
                }
            }

            categoryData.put(section.getSectionName(), categories);
        }

        return Collections.unmodifiableMap(categoryData);
    }

    /**
     * Returns the earliest update time for the categories in categoryData.
     *
     * @param categoryData
     *            category data to evaluate. From getCategoryData().
     * @returns the earliest update time. If one of the categories has no RTC
     *          data, -1 is returned. If no categories exist in categoryData, 0
     *          is returned.
     * @return a long.
     */
    public static long getEarliestUpdate(Map<String,List<Category>> categoryData) {
        long earliestUpdate = 0;

        for (final Entry<String, List<Category>> entry : categoryData.entrySet()) {
            final List<Category> categories = entry.getValue();

            for (final Category category : categories) {
                if (category.getLastUpdated() == null) {
                    return -1;
                } else if (earliestUpdate == 0 || earliestUpdate > category.getLastUpdated().getTime()) {
                    earliestUpdate = category.getLastUpdated().getTime();
                }
            }
        }

        return earliestUpdate;
    }

    /**
     * <p>isDisconnected</p>
     *
     * @return a boolean.
     * @throws java.io.IOException if any.
     */
    public boolean isDisconnected() throws IOException {
        return isDisconnected(getEarliestUpdate(getCategoryData()));
    }

    /**
     * <p>isDisconnected</p>
     *
     * @param earliestUpdate a long.
     * @return a boolean.
     */
    public boolean isDisconnected(long earliestUpdate) {
        if (earliestUpdate < 1 || (earliestUpdate + m_disconnectTimeout) < System.currentTimeMillis()) {
            return true;
        } else {
            return false;
        }
    }
}
