/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;

/**
 * Criteria for searching for business services
 *
 * @author Christian Pape <christian@opennms.org>
 */
public class BusinessServiceSearchCriteriaBuilder implements BusinessServiceSearchCriteria {

    /**
     * the compare operator for severity comparisons
     */
    public enum CompareOperator {
        Equal {
            public boolean check(int a) {
                return a == 0;
            }
        },
        LowerOrEqual {
            public boolean check(int a) {
                return a <= 0;
            }
        },
        GreaterOrEqual {
            public boolean check(int a) {
                return a >= 0;
            }
        },
        Greater {
            public boolean check(int a) {
                return a > 0;
            }
        },
        Lower {
            public boolean check(int a) {
                return a < 0;
            }
        };

        public abstract boolean check(int a);
    }

    /**
     * the order in which results are returned
     */
    public enum Order {
        Name,
        Severity,
        Level;

        public static Order of(String column) {
            for (Order order : Order.values()) {
                if (order.name().equalsIgnoreCase(column)) {
                    return order;
                }
            }
            throw new IllegalArgumentException("No column found with name '" + column + "'. Valid values are: " + Arrays.toString(Order.values()));
        }
    }

    /**
     * the sequence of the ordered results
     */
    public enum Sequence {
        Ascending,
        Descending;

        public static Sequence of(boolean asc) {
            if (asc) return Ascending;
            return Descending;
        }
    }

    /**
     * Inner pair class
     *
     * @param <A> an object
     * @param <B> another object
     */
    private static class Pair<A, B> {
        public final A a;
        public final B b;

        /**
         * Constructor for instatiating this class.
         *
         * @param a one object
         * @param b another object
         */
        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public String toString() {
            return "Pair (" + this.a + "," + this.b + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (a != null ? !a.equals(pair.a) : pair.a != null) {
                return false;
            }
            return b != null ? b.equals(pair.b) : pair.b == null;

        }

        @Override
        public int hashCode() {
            int result = a != null ? a.hashCode() : 0;
            result = 31 * result + (b != null ? b.hashCode() : 0);
            return result;
        }

        /**
         * Returns the first associated object.
         *
         * @return the object
         */
        public A getA() {
            return a;
        }

        /**
         * Returns the second associated object.
         *
         * @return the object
         */
        public B getB() {
            return b;
        }

        /**
         * Constructs an instance for two given objects.
         *
         * @param a   the object
         * @param b   another object
         * @param <A> the type of the first object
         * @param <B> the type of the second object
         * @return the instance created
         */
        public static <A, B> Pair<A, B> of(A a, B b) {
            return new Pair<A, B>(a, b);
        }
    }

    /**
     * the name filters
     */
    private List<String> m_nameFilters = new ArrayList<>();
    /**
     * the severity filters
     */
    private List<Pair<CompareOperator, Status>> m_severityFilters = new ArrayList<>();
    /**
     * the attribute filters
     */
    private List<Pair<String, String>> m_attributeFilters = new ArrayList<>();
    /**
     * the order parameters
     */
    private Order m_order = Order.Name;
    /**
     * the results limit
     */
    private int m_limit = 0;

    private int m_offset = 0;

    /**
     * ascending or descending?
     */
    private Sequence m_sequence = Sequence.Ascending;

    /**
     * Default constructor
     */
    public BusinessServiceSearchCriteriaBuilder() {
    }

    @Override
    public List<BusinessService> apply(BusinessServiceManager businessServiceManager, List<BusinessService> businessServiceDTOs) {
        Stream<BusinessService> s = businessServiceDTOs.stream();

        for (String nameRegexp : m_nameFilters) {
            s = s.filter(p -> p.getName().matches(nameRegexp));
        }

        for (Pair<String, String> pair : m_attributeFilters) {
            s = s.filter(p -> p.getAttributes().containsKey(pair.getA()) && p.getAttributes().get(pair.getA()).matches(pair.getB()));
        }

        if (!m_severityFilters.isEmpty()) {
            s = s.filter(bs -> {
                boolean accepted = false;
                for (Pair<CompareOperator, Status> pair : m_severityFilters) {
                    accepted |= pair.getA().check(businessServiceManager.getOperationalStatus(bs).compareTo(pair.getB()));
                }
                return accepted;
            });
        }

        Comparator<BusinessService> comparator = new Comparator<BusinessService>() {

            private final BusinessServiceGraph graph = businessServiceManager.getGraph();

            @Override
            public int compare(BusinessService p1, BusinessService p2) {
                switch (m_order) {
                    case Name: {
                        return p1.getName().compareTo(p2.getName());
                    }
                    case Severity: {
                        return businessServiceManager.getOperationalStatus(p1).compareTo(businessServiceManager.getOperationalStatus(p2));
                    }
                    case Level: {
                        return Integer.compare(graph.getVertexByBusinessServiceId(p1.getId()).getLevel(),
                                               graph.getVertexByBusinessServiceId(p2.getId()).getLevel());
                    }
                    default:
                        throw new IllegalArgumentException("Order not set");
                }
            }
        };

        if (m_sequence.equals(Sequence.Descending)) {
            comparator = comparator.reversed();
        }

        s = s.sorted(comparator);

        if (m_offset > 0) {
            s = s.skip(m_offset);
        }

        if (m_limit > 0) {
            s = s.limit(m_limit);
        }

        return s.collect(Collectors.toList());
    }

    public BusinessServiceSearchCriteriaBuilder attribute(String key, String valueRegexp) {
        m_attributeFilters.add(Pair.of(key, valueRegexp));
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder name(String nameRegexp) {
        m_nameFilters.add(nameRegexp);
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder order(Order order) {
        m_order = order;
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder filterSeverity(CompareOperator compareOperator, Status severity) {
        m_severityFilters.add(Pair.of(compareOperator, severity));
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder greaterOrEqualSeverity(Status severity) {
        filterSeverity(CompareOperator.GreaterOrEqual, severity);
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder equalSeverity(Status severity) {
        filterSeverity(CompareOperator.Equal, severity);
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder lowerOrEqualSeverity(Status severity) {
        filterSeverity(CompareOperator.LowerOrEqual, severity);
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder lowerSeverity(Status severity) {
        filterSeverity(CompareOperator.Lower, severity);
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder greaterSeverity(Status severity) {
        filterSeverity(CompareOperator.Greater, severity);
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder inSeverity(List<Status> severities) {
        for (Status eachStatus : severities) {
            filterSeverity(CompareOperator.Equal, eachStatus);
        }
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder limit(int limit) {
        this.m_limit = limit;
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder offset(int offset) {
        this.m_offset = offset;
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder order(Sequence sequence) {
        m_sequence = sequence;
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder order(String column, boolean asc) {
        this.order(Order.of(column));
        this.order(Sequence.of(asc));
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder asc() {
        m_sequence = Sequence.Ascending;
        return this;
    }

    public BusinessServiceSearchCriteriaBuilder desc() {
        m_sequence = Sequence.Descending;
        return this;
    }

    public void prepareForCounting() {
        m_order = Order.Name;
        m_limit = 0;
        m_offset = 0;
    }

    public Order getOrder() {
        return m_order;
    }

    public Sequence getSequence() {
        return m_sequence;
    }
}
