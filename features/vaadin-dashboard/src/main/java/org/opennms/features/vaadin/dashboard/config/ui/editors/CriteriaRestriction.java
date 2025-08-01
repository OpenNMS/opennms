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
package org.opennms.features.vaadin.dashboard.config.ui.editors;

import org.opennms.core.criteria.CriteriaBuilder;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This {@link Enum} represents the different types of restrictions used to construct a criteria.
 */
public enum CriteriaRestriction {
    Asc() {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.asc();
        }
    },
    Desc() {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.desc();
        }
    },
    Between(CriteriaEntry.Property, CriteriaEntry.Value, CriteriaEntry.Value) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.between(values[0].toString(), values[1], values[2]);
        }
    },
    Contains(CriteriaEntry.Property, CriteriaEntry.StringValue) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.contains(values[0].toString(), values[1]);
        }
    },
    Distinct() {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.distinct();
        }
    },
    Eq(CriteriaEntry.Property, CriteriaEntry.Value) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.eq(values[0].toString(), values[1]);
        }
    },
    Ge(CriteriaEntry.Property, CriteriaEntry.Value) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.ge(values[0].toString(), values[1]);
        }
    },
    Gt(CriteriaEntry.Property, CriteriaEntry.Value) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.gt(values[0].toString(), values[1]);
        }
    },
    Ilike(CriteriaEntry.Property, CriteriaEntry.StringValue) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.ilike(values[0].toString(), values[1]);
        }
    },
    In(CriteriaEntry.Property, CriteriaEntry.StringValue) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            Set<Object> set = new HashSet<>();

            Class<?> clazz = criteriaBuilderHelper.getTypeOfProperty(property);

            for (String string : String.valueOf(values[1]).split("\\|")) {
                set.add(criteriaBuilderHelper.parseCriteriaValue(clazz, string));
            }

            criteriaBuilder.in(values[0].toString(), set);
        }
    },
    Iplike(CriteriaEntry.Property, CriteriaEntry.StringValue) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.iplike(values[0].toString(), values[1]);
        }
    },
    IsNull(CriteriaEntry.Property) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.isNull(values[0].toString());
        }
    },
    IsNotNull(CriteriaEntry.Property) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.isNotNull(values[0].toString());
        }
    },
    Le(CriteriaEntry.Property, CriteriaEntry.Value) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.le(values[0].toString(), values[1]);
        }
    },
    Lt(CriteriaEntry.Property, CriteriaEntry.Value) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.lt(values[0].toString(), values[1]);
        }
    },
    Like(CriteriaEntry.Property, CriteriaEntry.StringValue) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.like(values[0].toString(), values[1]);
        }
    },
    Limit(CriteriaEntry.IntegerValue) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.limit(Integer.valueOf(values[0].toString()));
        }
    },
    Ne(CriteriaEntry.Property, CriteriaEntry.Value) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.ne(values[0].toString(), values[1]);
        }
    },
    Not() {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.not();
        }
    },
    OrderBy(CriteriaEntry.Property) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            criteriaBuilder.orderBy(values[0].toString());
        }
    };

    private CriteriaEntry[] m_entries;

    /**
     * Constructor
     *
     * @param entries the entries this restriction requires
     */
    CriteriaRestriction(CriteriaEntry... entries) {
        m_entries = entries;
    }

    /**
     * This method returns a {@link CriteriaRestriction} for a given string. The search is case insensitive.
     *
     * @param name the name to search for
     * @return the {@link CriteriaRestriction} found
     */
    public static CriteriaRestriction valueOfIgnoreCase(String name) {
        for (CriteriaRestriction criteriaRestriction : values()) {
            if (criteriaRestriction.name().equalsIgnoreCase(name)) {
                return criteriaRestriction;
            }
        }

        throw new IllegalArgumentException("No enum constant " + name + " found");
    }

    /**
     * Returns the defined entries for this restriction.
     *
     * @return the entries required
     */
    public CriteriaEntry[] getEntries() {
        return m_entries;
    }

    /**
     * This method adds a given criteria to a {@link CriteriaBuilder}.
     *
     * @param criteriaBuilderHelper the {@link CriteriaBuilderHelper} to be used
     * @param criteriaBuilder       the {@link CriteriaBuilder} to be used
     * @param values                the values of this criteria
     */
    public void addRestrictionToCriteriaBuilder(CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, String[] values) {
        List<Object> listOfObjects = new ArrayList<>();

        int i = 0;

        String property = null;

        for (CriteriaEntry entry : getEntries()) {
            switch (entry) {
                case Property: {
                    property = values[i];
                    listOfObjects.add(values[i]);
                    break;
                }
                case Value: {
                    Class<?> clazz = criteriaBuilderHelper.getTypeOfProperty(property);

                    Object object = criteriaBuilderHelper.parseCriteriaValue(clazz, values[i]);

                    if (object == null) {
                        LoggerFactory.getLogger(CriteriaBuilderHelper.class).warn("Cannot parse value '" + values[i] + "' for class " + clazz.getSimpleName());
                        return;
                    } else {
                        listOfObjects.add(object);
                    }
                    break;
                }
                case IntegerValue: {
                    Class<?> clazz = Integer.class;

                    Object object = criteriaBuilderHelper.parseCriteriaValue(clazz, values[i]);

                    if (object == null) {
                        LoggerFactory.getLogger(CriteriaBuilderHelper.class).warn("Cannot parse value '" + values[i] + "' for class " + clazz.getSimpleName());
                        return;
                    } else {
                        listOfObjects.add(object);
                    }
                    break;
                }
                case StringValue: {
                    Class<?> clazz = String.class;

                    Object object = criteriaBuilderHelper.parseCriteriaValue(clazz, values[i]);

                    if (object == null) {
                        LoggerFactory.getLogger(CriteriaBuilderHelper.class).warn("Cannot parse value '" + values[i] + "' for class " + clazz.getSimpleName());
                        return;
                    } else {
                        listOfObjects.add(object);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
            i++;
        }

        addRestriction(property, criteriaBuilderHelper, criteriaBuilder, listOfObjects.toArray());
    }

    /**
     * This abstract method adds a criteria to a {@link CriteriaBuilder}.
     *
     * @param property              the property
     * @param criteriaBuilderHelper the {@link CriteriaBuilderHelper} to be used
     * @param criteriaBuilder       the {@link CriteriaBuilder} to be used
     * @param values                the values for this restriction
     */
    public abstract void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values);
}