/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
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
        crit.add(Restrictions.sqlRestriction("{alias}.categoryId in (select cg.categoryId from category_group cg where cg.groupId = ?)", groupName, StringType.INSTANCE));
        return findMatching(crit);
    }
}
