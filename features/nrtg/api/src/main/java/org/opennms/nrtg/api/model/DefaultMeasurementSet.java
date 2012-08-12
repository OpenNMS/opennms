/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.api.model;

import flexjson.JSONSerializer;
import flexjson.transformer.DateTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * {@inheritDoc}
 * User: chris
 * Date: 19.06.12
 * Time: 14:48
 * To change this template use File | Settings | File Templates.
 */
public class DefaultMeasurementSet implements MeasurementSet {

    private static final long serialVersionUID = 7536809905120941525L;
    private List<Measurement> m_measurements = new ArrayList<Measurement>();

    public void addMeasurement(Measurement measurement) {
        m_measurements.add(measurement);
    }

    @Override
    public String getJson() {
        return new JSONSerializer()
                .exclude("class")
                        //.transform(new DateTransformer("yyyy-MM-dd"), "timestamp")
                .serialize(getMeasurements());
    }

    @Override
    public List<Measurement> getMeasurements() {
        return m_measurements;
    }

    /**
     * This toString method is for displaying reasons in the webapp NrtGrapher only.
     * It's for prototyping only.
     *
     * @return a {@link String} that contains the metrics and there values in a easy parsable way.
     */
    @Override
    public String toString() {
//        StringBuilder sb = new StringBuilder("");
//        for (Measurement measurement : m_measurements) {
//            sb.append(measurement.getMetricId());
//            sb.append(";");
//            sb.append(measurement.getValue());
//            sb.append(";");
//            sb.append(measurement.getTimestamp());
//            sb.append("\n");
//        }
        return this.getJson();
    }
}
