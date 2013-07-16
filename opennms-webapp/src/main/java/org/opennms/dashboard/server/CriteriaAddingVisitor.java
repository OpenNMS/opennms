/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.dashboard.server;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.opennms.dashboard.client.SurveillanceGroup;
import org.opennms.dashboard.client.Visitor;
import org.opennms.netmgt.config.surveillanceViews.Category;
import org.opennms.netmgt.config.surveillanceViews.ColumnDef;
import org.opennms.netmgt.config.surveillanceViews.RowDef;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>CriteriaAddingVisitor class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class CriteriaAddingVisitor implements Visitor, InitializingBean {
    
    private OnmsCriteria m_criteria;
    private CategoryDao m_categoryDao;
    private View m_view;
    private SurveillanceView m_survView;
    
    /**
     * <p>Constructor for CriteriaAddingVisitor.</p>
     *
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     */
    public CriteriaAddingVisitor(OnmsCriteria criteria) {
        m_criteria = criteria;
    }

    /**
     * <p>visitAll</p>
     */
    @Override
    public void visitAll() {
        
        addCriteriaForCategories(m_criteria, m_survView.getColumnCategories());
        addCriteriaForCategories(m_criteria, m_survView.getRowCategories());
        
    }

    /** {@inheritDoc} */
    @Override
    public void visitGroup(SurveillanceGroup group) {

        if (group.isColumn()) {
            addCriteriaForCategories(m_criteria, m_survView.getRowCategories());
            addCriteriaForCategories(m_criteria, m_survView.getCategoriesForColumn(group.getId()));
        } else {
            addCriteriaForCategories(m_criteria, m_survView.getColumnCategories());
            addCriteriaForCategories(m_criteria, m_survView.getCategoriesForRow(group.getId()));
        }
            
    }
    
    /** {@inheritDoc} */
    @Override
    public void visitIntersection(SurveillanceGroup row, SurveillanceGroup column) {
        
        addCriteriaForCategories(m_criteria, m_survView.getCategoriesForRow(row.getId()));
        addCriteriaForCategories(m_criteria, m_survView.getCategoriesForColumn(column.getId()));
        
    }
    
    /**
     * <p>addCriteriaForCategories</p>
     *
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @param categories a {@link java.util.Set} object.
     */
    public void addCriteriaForCategories(OnmsCriteria criteria, Set<String> categories) {
        String[] categoryNames = categories.toArray(new String[categories.size()]);
        addCriteriaForCategories(criteria, categoryNames);
    }
    
    /**
     * <p>addCriteriaForCategories</p>
     *
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @param categories a {@link java.lang.String} object.
     */
    public void addCriteriaForCategories(OnmsCriteria criteria, String... categories) {
        String sql = "{alias}.nodeId in (select distinct cn.nodeId from category_node cn join categories c on cn.categoryId = c.categoryId where c.categoryName in (" + commaDelimitedQuestionMarks(categories.length) + "))";
        criteria.add(Restrictions.sqlRestriction(sql, categories, arrayOfType(categories.length, new StringType())));
    }
    
    /**
     * <p>getView</p>
     *
     * @return a {@link org.opennms.netmgt.config.surveillanceViews.View} object.
     */
    public View getView() {
        return m_view;
    }
    
    /**
     * <p>setView</p>
     *
     * @param view a {@link org.opennms.netmgt.config.surveillanceViews.View} object.
     */
    public void setView(View view) {
        m_view = view;
    }

    
    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_view != null, "view property must be set");
        Assert.state(m_categoryDao != null, "categoryDao property must be set");
        
        
        // construct a surveillance view object that makes it easier to build queries
        m_survView = new SurveillanceView();
        
        m_survView.setName(m_view.getName());
        
        for (ColumnDef colDef : m_view.getColumns().getColumnDef()) {
            for (Category category : colDef.getCategory()) {
                m_survView.addColumnCategory(colDef.getLabel(), category.getName());
            }
        }
        
        for (RowDef rowDef : m_view.getRows().getRowDef()) {
            for (Category category : rowDef.getCategory()) {
                m_survView.addRowCategory(rowDef.getLabel(), category.getName());
            }
        }
        
    }
    
    
    private Type[] arrayOfType(int length, Type initialVal) {
        Type[] array = new Type[length];
        for(int i = 0; i < length; i++) {
            array[i] = initialVal;
        }
        return array;
    }
    
    /**
     * <p>commaDelimitedQuestionMarks</p>
     *
     * @param count a int.
     * @return a {@link java.lang.String} object.
     */
    public String commaDelimitedQuestionMarks(int count) {
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < count; i++)  {
            if (i != 0) {
                buf.append(',');
            }
            buf.append('?');
        }
        return buf.toString();
    }


    private static class SurveillanceView {
        private String m_name;
        private MapToSetOf<String, String> m_columns = new MapToSetOf<String, String>();
        private MapToSetOf<String, String> m_rows = new MapToSetOf<String, String>();

        public void setName(String name) {
            m_name = name;
        }
        
        public String getName() {
            return m_name;
        }

        public void addColumnCategory(String column, String category) {
            m_columns.addValue(column, category);
        }

        public void addRowCategory(String row, String category) {
            m_rows.addValue(row, category);
        }
        
        public Set<String> getCategoriesForRow(String row) {
            return m_rows.getValueForKey(row);
        }
        
        public Set<String> getCategoriesForColumn(String column) {
            return m_columns.getValueForKey(column);
        }
        
        public Set<String> getRowCategories() {
            return m_rows.getAllValues();
        }
        
        public Set<String> getColumnCategories() {
            return m_columns.getAllValues();
        }
        
        public Set<String> getCategoriesForColumns(Collection<? extends String> columns) {
            return m_columns.getValuesForKeys(columns);
        }
        
        public Set<String> getCategoriesForRows(Collection<? extends String> rows) {
            return m_rows.getValuesForKeys(rows);
        }
    }
    
    
    private static class MapToSetOf<K, V> {
        
        private Map<K, Set<V>> m_map = new LinkedHashMap<K, Set<V>>();
        
        public void addValue(K key, V value) {
            if (!m_map.containsKey(key)) {
                m_map.put(key, new LinkedHashSet<V>());
            }
            m_map.get(key).add(value);
        }
        
        public Set<V> getValuesForKeys(Collection<? extends K> keys) {
            LinkedHashSet<V> values = new LinkedHashSet<V>();
            for (K key : keys) {
                values.addAll(m_map.get(key));
            }
            
            return values;
        }
        
        public Set<V> getAllValues() {
            return getValuesForKeys(m_map.keySet());
        }
        
        public Set<V> getValueForKey(K key) {
            return getValuesForKeys(Collections.singleton(key));
        }
        
    }


}
