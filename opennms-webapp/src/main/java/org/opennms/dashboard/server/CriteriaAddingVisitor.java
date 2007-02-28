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
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCriteria;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class CriteriaAddingVisitor implements Visitor, InitializingBean {
    
    private OnmsCriteria m_criteria;
    private CategoryDao m_categoryDao;
    private View m_view;
    private SurveillanceView m_survView;
    
    public CriteriaAddingVisitor(OnmsCriteria criteria) {
        m_criteria = criteria;
    }

    public void visitAll() {
        
        addCriteriaForCategories(m_criteria, m_survView.getColumnCategories());
        addCriteriaForCategories(m_criteria, m_survView.getRowCategories());
        
    }

    public void visitGroup(SurveillanceGroup group) {

        if (group.isColumn()) {
            addCriteriaForCategories(m_criteria, m_survView.getRowCategories());
            addCriteriaForCategories(m_criteria, m_survView.getCategoriesForColumn(group.getId()));
        } else {
            addCriteriaForCategories(m_criteria, m_survView.getColumnCategories());
            addCriteriaForCategories(m_criteria, m_survView.getCategoriesForRow(group.getId()));
        }
            
    }
    
    public void visitIntersection(SurveillanceGroup row, SurveillanceGroup column) {
        
        addCriteriaForCategories(m_criteria, m_survView.getCategoriesForRow(row.getId()));
        addCriteriaForCategories(m_criteria, m_survView.getCategoriesForColumn(column.getId()));
        
    }
    
    public void addCriteriaForCategories(OnmsCriteria criteria, Set<String> categories) {
        String[] categoryNames = categories.toArray(new String[categories.size()]);
        addCriteriaForCategories(criteria, categoryNames);
    }
    
    public void addCriteriaForCategories(OnmsCriteria criteria, String... categories) {
        String sql = "{alias}.nodeId in (select distinct cn.nodeId from category_node cn join categories c on cn.categoryId = c.categoryId where c.categoryName in (" + commaDelimitedQuestionMarks(categories.length) + "))";
        criteria.add(Restrictions.sqlRestriction(sql, categories, arrayOfType(categories.length, new StringType())));
    }
    
    public View getView() {
        return m_view;
    }
    
    public void setView(View view) {
        m_view = view;
    }

    
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

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
