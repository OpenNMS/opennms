/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.collection.persistence.evaluate;

import java.util.concurrent.TimeUnit;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;

import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

/**
 * A factory for creating EvaluatePersister objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluatePersisterFactory implements PersisterFactory {

    /** The Constant LOGGING_PREFFIX. */
    private static final String LOGGING_PREFFIX = "EvaluationMetrics";

    /** The evaluation statistics. */
    private EvaluateStats stats;

    /**
     * Instantiates a new evaluate persister factory.
     *
     * @param registry the metric registry
     * @param dumpFreq the dump frequency
     */
    public EvaluatePersisterFactory(MetricRegistry registry, Integer dumpFreq) {
        Assert.notNull(registry, "MetricRegistry is required");
        Assert.notNull(dumpFreq, "Dump frequency is required");
        Assert.isTrue(dumpFreq > 0, "Dump frequency must be positive");

        stats = new EvaluateStats(registry);

        Logging.withPrefix(LOGGING_PREFFIX, () -> {
            final Slf4jReporter reporter = Slf4jReporter.forRegistry(registry)
                    .outputTo(LoggerFactory.getLogger(LOGGING_PREFFIX))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            reporter.start(dumpFreq, TimeUnit.MINUTES);
        });
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.PersisterFactory#createPersister(org.opennms.netmgt.collection.api.ServiceParameters, org.opennms.netmgt.rrd.RrdRepository)
     */
    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return new EvaluatePersister(stats);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.PersisterFactory#createPersister(org.opennms.netmgt.collection.api.ServiceParameters, org.opennms.netmgt.rrd.RrdRepository, boolean, boolean, boolean)
     */
    @Override
    public Persister createPersister(ServiceParameters params,
            RrdRepository repository, boolean dontPersistCounters,
            boolean forceStoreByGroup, boolean dontReorderAttributes) {
        return new EvaluatePersister(stats);
    }

}
