
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCriteria;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>CategoryDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class CategoryDaoHibernate extends AbstractCachingDaoHibernate<OnmsCategory, Integer, String> implements CategoryDao {

    /**
     * <p>Constructor for CategoryDaoHibernate.</p>
     */
    public CategoryDaoHibernate() {
        super(OnmsCategory.class, false);
    }
    
    /** {@inheritDoc} */
    @Override
    public OnmsCategory findByName(String name) {
        return findByName(name, true);
    }

    /** {@inheritDoc} */
    @Override
    public OnmsCategory findByName(String name, boolean useCached) {
        if (useCached) {
            return findByCacheKey("from OnmsCategory as category where category.name = ?", name);
        } else {
            return findUnique("from OnmsCategory as category where category.name = ?", name);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    protected String getKey(OnmsCategory category) {
        return category.getName();
    }
    
    /**
     * <p>getAllCategoryNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<String> getAllCategoryNames() {
        return findObjects(String.class, "select category.name from OnmsCategory as category");
    }

    /**
     * <p>getCriterionForCategorySetsUnion</p>
     *
     * @param categories an array of {@link java.lang.String} objects.
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<Criterion> getCriterionForCategorySetsUnion(String[]... categories) {
        Assert.notNull(categories, "categories argument must not be null");
        Assert.isTrue(categories.length >= 1, "categories must have at least one set of categories");

        // Build a list a list of category IDs to use when building the restrictions
        List<List<Integer>> categoryIdsList = new ArrayList<List<Integer>>(categories.length);
        for (String[] categoryStrings : categories) {
            List<Integer> categoryIds = new ArrayList<Integer>(categoryStrings.length);
            for (String categoryString : categoryStrings) {
                OnmsCategory category = findByName(categoryString);
                if (category == null) {
                    throw new IllegalArgumentException("Could not find category for name '" + categoryString + "'");
                }
                categoryIds.add(category.getId());
            }
            categoryIdsList.add(categoryIds);
        }

        List<Criterion> criteria = new ArrayList<Criterion>(categoryIdsList.size());

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

        return criteria;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.CategoryDao#getCategoriesWithAuthorizedGroup(java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public List<OnmsCategory> getCategoriesWithAuthorizedGroup(String groupName) {
        OnmsCriteria crit = new OnmsCriteria(OnmsCategory.class);
        crit.add(Restrictions.sqlRestriction("{alias}.categoryId in (select cg.categoryId from category_group cg where cg.groupId = ?)", groupName, Hibernate.STRING));
        return findMatching(crit);
    }
}
