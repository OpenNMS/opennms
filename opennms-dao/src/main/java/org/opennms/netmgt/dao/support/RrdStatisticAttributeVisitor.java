/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.api.MeasurementFetchStrategy;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.AttributeStatisticVisitor;
import org.opennms.netmgt.model.AttributeVisitor;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * <p>RrdStatisticAttributeVisitor class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class RrdStatisticAttributeVisitor implements AttributeVisitor, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(RrdStatisticAttributeVisitor.class);

    private MeasurementFetchStrategy m_fetchStrategy;

    private String m_consolidationFunction;
    private Long m_startTime;
    private Long m_endTime;
    private AttributeStatisticVisitor m_statisticVisitor;

    private interface Aggregator {
        double getValue();
        void aggregate(final double v);
    }

    private enum Aggregators implements Supplier<Aggregator> {
        AVERAGE(() -> new Aggregator() {
            private int count = 0;
            private double sum = 0;

            @Override
            public double getValue() {
                return this.count != 0
                       ? this.sum / this.count
                       : Double.NaN;
            }

            @Override
            public void aggregate(final double v) {
                this.count++;
                this.sum += v;
            }
        }),

        MIN(() -> new Aggregator(){
            private double min = Double.POSITIVE_INFINITY;

            @Override
            public double getValue() {
                return this.min;
            }

            @Override
            public void aggregate(final double v) {
                        this.min = Math.min(this.min, v);
                    }
        }),

        MAX(() -> new Aggregator(){
            private double max = Double.NEGATIVE_INFINITY;

            @Override
            public double getValue() {
                return this.max;
            }

            @Override
            public void aggregate(final double v) {
                this.max = Math.max(this.max, v);
                    } 
        }),

        LAST(() -> new Aggregator () {
            private double v = Double.NaN;

            @Override
            public double getValue() {
                return this.v;
            }

            @Override
            public void aggregate(final double v) {
                        this.v = v;
                    }
        });

        private final Supplier<Aggregator> delegate;

        Aggregators(final Supplier<Aggregator> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Aggregator get() {
            return this.delegate.get();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void visit(OnmsAttribute attribute) {
        if (!RrdGraphAttribute.class.isAssignableFrom(attribute.getClass())) {
            // Nothing to do if we can't cast to an RrdGraphAttribute
            return;
        }

        final Source source = new Source();
        source.setLabel("result");
        source.setResourceId(attribute.getResource().getId().toString());
        source.setAttribute(attribute.getName());
        source.setAggregation(m_consolidationFunction.toUpperCase());

        final FetchResults results;
        try {
            results = m_fetchStrategy.fetch(m_startTime,
                                            m_endTime,
                                            1,
                                            0,
                                            null,
                                            null,
                                            Collections.singletonList(source),
                                            false);
        } catch (final Exception e) {
            LOG.warn("Failed to fetch statistic: {}", source, e);
            return;
        }

        if (results == null) {
            LOG.warn("No statistic found: {}", source);
            return;
        }

        final double[] statistics = results.getColumns().get(source.getLabel());
        if (statistics == null || statistics.length == 0) {
            LOG.warn("Statistic is empty: {}", source);
            return;
        }

        final Aggregator aggregator = Aggregators.valueOf(m_consolidationFunction.toUpperCase()).get();

        Arrays.stream(statistics)
                .filter(v -> (! Double.isNaN(v)))
                .forEach(v -> {
                    LOG.debug("Aggregating: {}", v);
                    aggregator.aggregate(v);
                });

        double statistic = aggregator.getValue();
        
        LOG.debug("The value of {} is {}", attribute, statistic);
        
        /*
         * We don't want to do anything with NaN data, since
         * it means there is no data. We especially want to
         * stay away from it, because it will be sorted as
         * being higher than any numeric value, which will
         * leave our TopN report with most, if not all NaN
         * values at the top.
         */
        if (Double.isNaN(statistic)) {
            return;
        }
        
        m_statisticVisitor.visit(attribute, statistic);
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_fetchStrategy != null, "property fetchStrategy must be set to a non-null value");
        Assert.state(m_consolidationFunction != null, "property consolidationFunction must be set to a non-null value");
        Assert.state(m_startTime != null, "property startTime must be set to a non-null value");
        Assert.state(m_endTime != null, "property endTime must be set to a non-null value");
        Assert.state(m_statisticVisitor != null, "property statisticVisitor must be set to a non-null value");
    }

    public MeasurementFetchStrategy getFetchStrategy() {
        return m_fetchStrategy;
    }

    public void setFetchStrategy(MeasurementFetchStrategy fetchStrategy) {
        m_fetchStrategy = fetchStrategy;
    }

    /**
     * <p>getStatisticVisitor</p>
     *
     * @return a {@link org.opennms.netmgt.model.AttributeStatisticVisitor} object.
     */
    public AttributeStatisticVisitor getStatisticVisitor() {
        return m_statisticVisitor;
    }

    /**
     * <p>setStatisticVisitor</p>
     *
     * @param statisticVisitor a {@link org.opennms.netmgt.model.AttributeStatisticVisitor} object.
     */
    public void setStatisticVisitor(AttributeStatisticVisitor statisticVisitor) {
        m_statisticVisitor = statisticVisitor;
    }

    /**
     * <p>getConsolidationFunction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getConsolidationFunction() {
        return m_consolidationFunction;
    }

    /**
     * <p>setConsolidationFunction</p>
     *
     * @param consolidationFunction a {@link java.lang.String} object.
     */
    public void setConsolidationFunction(String consolidationFunction) {
        m_consolidationFunction = consolidationFunction;
    }

    /**
     * <p>getEndTime</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getEndTime() {
        return m_endTime;
    }

    /**
     * <p>setEndTime</p>
     *
     * @param endTime a {@link java.lang.Long} object.
     */
    public void setEndTime(Long endTime) {
        m_endTime = endTime;
    }

    /**
     * <p>getStartTime</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getStartTime() {
        return m_startTime;
    }

    /**
     * <p>setStartTime</p>
     *
     * @param startTime a {@link java.lang.Long} object.
     */
    public void setStartTime(Long startTime) {
        m_startTime = startTime;
    }


}
