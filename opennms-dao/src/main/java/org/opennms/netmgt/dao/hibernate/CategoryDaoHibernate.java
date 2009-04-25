
/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Dec 09: Format code, add getCriterionForCategorySetsUnion. - dj@opennms.org
 * 2007 Jul 03: Organize imports. - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */
package org.opennms.netmgt.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.IntegerType;
import org.hibernate.type.Type;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsRestrictions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class CategoryDaoHibernate extends AbstractCachingDaoHibernate<OnmsCategory, Integer, String> implements CategoryDao {

    public CategoryDaoHibernate() {
        super(OnmsCategory.class, false);
    }

    public OnmsCategory findByName(String name) {
        return findByCacheKey("from OnmsCategory as category where category.name = ?", name);
    }
    
    

    @Override
    protected String getKey(OnmsCategory category) {
        return category.getName();
    }

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
    public List<OnmsCategory> getCategoriesWithAuthorizedGroup(String groupName) {
        OnmsCriteria crit = new OnmsCriteria(OnmsCategory.class);
        crit.add(Restrictions.sqlRestriction("{alias}.categoryId in (select cg.categoryId from category_group cg where cg.groupId = ?", groupName, Hibernate.STRING));
        return findMatching(crit);
    }
}
