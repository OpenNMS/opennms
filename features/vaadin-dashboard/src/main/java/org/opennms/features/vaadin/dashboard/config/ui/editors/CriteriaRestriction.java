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
    In(CriteriaEntry.Property, CriteriaEntry.Value) {
        public void addRestriction(String property, CriteriaBuilderHelper criteriaBuilderHelper, CriteriaBuilder criteriaBuilder, Object[] values) {
            Set<Object> set = new HashSet<Object>();

            Class clazz = criteriaBuilderHelper.getTypeOfProperty(property);

            for (String string : String.valueOf(values[1]).split(",")) {
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
            if (criteriaRestriction.name().toLowerCase().equals(name.toLowerCase())) {
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
        List<Object> listOfObjects = new ArrayList<Object>();

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
                    Class clazz = criteriaBuilderHelper.getTypeOfProperty(property);

                    Object object = criteriaBuilderHelper.parseCriteriaValue(clazz, values[i]);

                    if (object == null) {
                        LoggerFactory.getLogger(CriteriaBuilderHelper.class).warn("Cannot parse value '" + values[i] + "' for class " + clazz.getSimpleName());
                        return;
                    } else {
                        listOfObjects.add(object);
                    }
                }
                case IntegerValue: {
                    Class clazz = Integer.class;

                    Object object = criteriaBuilderHelper.parseCriteriaValue(clazz, values[i]);

                    if (object == null) {
                        LoggerFactory.getLogger(CriteriaBuilderHelper.class).warn("Cannot parse value '" + values[i] + "' for class " + clazz.getSimpleName());
                        return;
                    } else {
                        listOfObjects.add(object);
                    }
                }
                case StringValue: {
                    Class clazz = String.class;

                    Object object = criteriaBuilderHelper.parseCriteriaValue(clazz, values[i]);

                    if (object == null) {
                        LoggerFactory.getLogger(CriteriaBuilderHelper.class).warn("Cannot parse value '" + values[i] + "' for class " + clazz.getSimpleName());
                        return;
                    } else {
                        listOfObjects.add(object);
                    }
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