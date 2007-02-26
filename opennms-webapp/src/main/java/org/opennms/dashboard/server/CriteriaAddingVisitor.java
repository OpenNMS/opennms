package org.opennms.dashboard.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;
import org.opennms.dashboard.client.SurveillanceGroup;
import org.opennms.dashboard.client.Visitor;
import org.opennms.netmgt.config.surveillanceViews.Category;
import org.opennms.netmgt.config.surveillanceViews.ColumnDef;
import org.opennms.netmgt.config.surveillanceViews.Columns;
import org.opennms.netmgt.config.surveillanceViews.RowDef;
import org.opennms.netmgt.config.surveillanceViews.Rows;
import org.opennms.netmgt.config.surveillanceViews.View;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.web.svclayer.dao.SurveillanceViewConfigDao;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class CriteriaAddingVisitor implements Visitor {
    
    private OnmsCriteria m_criteria;
    private SurveillanceViewConfigDao m_surveillanceViewConfigDao;
    private CategoryDao m_categoryDao;

    public CriteriaAddingVisitor(OnmsCriteria criteria) {
        m_criteria = criteria;
    }

    public void visitAll() {
        View view = m_surveillanceViewConfigDao.getDefaultView();

        List<Category> columnCategories = new ArrayList<Category>();
        List<Category> rowCategories = new ArrayList<Category>();
        
        List<ColumnDef> columnDefs = getColumnDefs(view.getColumns());
        for (ColumnDef columnDef : columnDefs) {
            columnCategories.addAll(getCategories(columnDef));
        }

        List<RowDef> rowDefs = getRowDefs(view.getRows());
        for (RowDef rowDef : rowDefs) {
            rowCategories.addAll(getCategories(rowDef));
        }
        
        Set<String> categoryNames = new HashSet<String>();
        for (Category category : columnCategories) {
            categoryNames.add(category.getName());
        }
        
        addCriteriaForCategories(m_criteria, categoryNames.toArray(new String[categoryNames.size()]));
    }

    public void visitGroup(SurveillanceGroup group) {
        addCriteriaForGroup(group);
    }
    
    public void visitIntersection(SurveillanceGroup row, SurveillanceGroup column) {
        addCriteriaForGroup(row);
        addCriteriaForGroup(column);
    }

    private void addCriteriaForGroup(SurveillanceGroup group) {
        View view = m_surveillanceViewConfigDao.getDefaultView();
        
        List<Category> categories = null;
        if (group.isColumn()) {
            List<ColumnDef> columnDefs = getColumnDefs(view.getColumns());
            for (ColumnDef columnDef : columnDefs) {
                if (group.getLabel().equals(columnDef.getLabel())) {
                    categories = getCategories(columnDef);
                    break;
                }
            }
        } else {
            List<RowDef> rowDefs = getRowDefs(view.getRows());
            for (RowDef rowDef : rowDefs) {
                if (group.getLabel().equals(rowDef.getLabel())) {
                    categories = getCategories(rowDef);
                    break;
                }
            }
        }
        
        if (categories == null) {
            throw new IllegalArgumentException("Couldn't find categories for group " + group);
        }
        
        List<String> categoryNames = new ArrayList<String>(categories.size());
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        
        addCriteriaForCategories(m_criteria, categoryNames.toArray(new String[categoryNames.size()]));
    }

    @SuppressWarnings("unchecked")
    private List<ColumnDef> getColumnDefs(Columns columns) {
        return columns.getColumnDefCollection();
    }
    
    @SuppressWarnings("unchecked")
    private List<Category> getCategories(ColumnDef columnDef) {
        return columnDef.getCategoryCollection();
    }

    @SuppressWarnings("unchecked")
    private List<RowDef> getRowDefs(Rows rows) {
        return rows.getRowDefCollection();
    }
    
    @SuppressWarnings("unchecked")
    private List<Category> getCategories(RowDef rowDef) {
        return rowDef.getCategoryCollection();
    }

    private void addCriteriaForCategories(OnmsCriteria criteria, String[]... categories) {
        Assert.notNull(criteria, "criteria argument must not be null");
        Assert.notNull(categories, "categories argument must not be null");
        Assert.isTrue(categories.length >= 1, "categories must have at least one set of categories");

        // Build a list a list of category IDs to use when building the restrictions
        List<List<Integer>> categoryIdsList = new ArrayList<List<Integer>>(categories.length);
        for (String[] categoryStrings : categories) {
            List<Integer> categoryIds = new ArrayList<Integer>(categoryStrings.length);
            for (String categoryString : categoryStrings) {
                OnmsCategory category = m_categoryDao.findByName(categoryString);
                if (category == null) {
                    throw new IllegalArgumentException("Could not find category for name '" + categoryString + "'");
                }
                categoryIds.add(category.getId());
            }
            categoryIdsList.add(categoryIds);
        }
        
        for (List<Integer> categoryIds : categoryIdsList) {
            Type[] types = new Type[categoryIds.size()];
            String[] questionMarks = new String[categoryIds.size()];
            Type theOneAndOnlyType = new IntegerType();
            
            for (int i = 0; i < categoryIds.size(); i++) {
                types[i] = theOneAndOnlyType;
                questionMarks[i] = "?";
            }
            String sql = "{alias}.nodeId in (select distinct cn.nodeId from category_node cn where cn.categoryId in (" + StringUtils.arrayToCommaDelimitedString(questionMarks) + "))";
            criteria.add(Restrictions.sqlRestriction(sql, categoryIds.toArray(new Integer[categoryIds.size()]), types));
        }
    }
    
    public void setSurveillanceViewConfigDao(SurveillanceViewConfigDao surveillanceViewConfigDao) {
        m_surveillanceViewConfigDao = surveillanceViewConfigDao;
    }
    
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }
}
