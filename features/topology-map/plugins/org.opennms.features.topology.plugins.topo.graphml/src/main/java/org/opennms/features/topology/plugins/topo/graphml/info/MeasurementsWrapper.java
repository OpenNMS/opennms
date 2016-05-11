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

package org.opennms.features.topology.plugins.topo.graphml.info;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.gwt.thirdparty.guava.common.primitives.Doubles;

public class MeasurementsWrapper {
    private final static Logger LOG = LoggerFactory.getLogger(GenericInfoPanelItemProvider.class);

    private final MeasurementsService measurementsService;

    public MeasurementsWrapper(MeasurementsService measurementsService) {
        this.measurementsService=measurementsService;
    }

    public double getLastValue(final String resource, final String attribute) {
        return getLastValue(resource, attribute, "AVERAGE");
    }

    public double getLastValue(final String resource, final String attribute, final String aggregation) {
        long end = System.currentTimeMillis();
        long start = end - (15 * 60 * 1000);

        QueryResponse.WrappedPrimitive[] columns = queryInt(resource, attribute, start, end, 300000, aggregation).getColumns();

        if (columns.length > 0) {
            double[] values = columns[0].getList();
            if (values.length > 0) {
                for(int i = values.length-1; i >= 0; i--) {
                    if (!Double.isNaN(values[i])) {
                        return values[i];
                    }
                }
            }
        }
        return Double.NaN;
    }

    public List<Double> query(final String resource, final String attribute, final long start, final long end, final long step, final String aggregation) {
        QueryResponse.WrappedPrimitive[] columns = queryInt(resource, attribute, start, end, step, aggregation).getColumns();

        if (columns.length > 0) {
            return Doubles.asList(columns[0].getList());
        }

        return Collections.emptyList();
    }

    private QueryResponse queryInt(final String resource, final String attribute, final long start, final long end, final long step, final String aggregation) {
        QueryRequest request = new QueryRequest();
        request.setRelaxed(true);
        request.setStart(start);
        request.setEnd(end);
        request.setStep(step);

        Source source = new Source();
        source.setAggregation(aggregation);
        source.setTransient(false);
        source.setAttribute(attribute);
        source.setResourceId(resource);
        source.setLabel(attribute);

        request.setSources(Collections.singletonList(source));

        try {
            return measurementsService.query(request);
        } catch (Exception ex) {
            // TODO error handling
            throw Throwables.propagate(ex);
        }
    }
}
