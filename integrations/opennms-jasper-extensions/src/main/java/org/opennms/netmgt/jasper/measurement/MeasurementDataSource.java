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
package org.opennms.netmgt.jasper.measurement;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;


public class MeasurementDataSource implements JRRewindableDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementDataSource.class);

    private int numberRows;
    protected QueryResponse response;
    private int currentRow = -1;

    public MeasurementDataSource(InputStream inputStream) {
        this(inputStream != null ? JaxbUtils.unmarshal(QueryResponse.class, inputStream) : null);
    }

    public MeasurementDataSource(QueryResponse queryResponse) {
        this.response = queryResponse;
        this.numberRows = 0;
        if (queryResponse != null && queryResponse.getTimestamps() != null) {
            this.numberRows = queryResponse.getTimestamps().length;
        }
        LOG.debug("New {} created with {} rows: {}", getClass().getName(), numberRows, response);
    }

    protected Object getFieldValue(final String name, final int rowIndex) {
        if (response == null) {
            return null;
        }
        LOG.debug("Getting field value for field {}:{}", rowIndex, name);
        if ("step".equals(name)) {
            return response.getStep();
        }
        if ("start".equals(name)) {
            return response.getStart();
        }
        if ("end".equals(name)) {
            return response.getEnd();
        }
        if ("timestamp".equals(name) && response.getTimestamps() != null && response.getTimestamps().length > rowIndex) {
            return new Date(response.getTimestamps()[rowIndex]);
        }

        double[] values = response.columnsWithLabels().get(name);
        if (values != null && values.length > rowIndex) {
            return values[rowIndex];
        }
        return null;
    }

    protected int getRowCount() {
        return numberRows;
    }

    /**
     * Returns the supported fields.
     * @return The supported fields.
     */
    protected List<JRField> getFields() {
        List<JRField> fields = new ArrayList<>();

        // constant fields
        fields.add(createField("step", Long.class));
        fields.add(createField("end", Long.class));
        fields.add(createField("start", Long.class));

        // dynamic fields
        fields.add(createField("timestamp", Date.class));
        if (response != null) {
            for (String eachLabel : response.getLabels()) {
                fields.add(createField(eachLabel, Double.class));
            }
        }
        return fields;
    }

    protected static JRField createField(String name, Class<?> clazz) {
        final JRDesignField field = new JRDesignField();
        field.setValueClass(clazz);
        field.setName(name);
        return field;
    }

    @Override
    public boolean next() {
        currentRow++;
        return currentRow < getRowCount();
    }

    @Override
    public Object getFieldValue(JRField jrField) {
        return getFieldValue(jrField.getName(), currentRow);
    }

    @Override
    public void moveFirst() {
        currentRow = -1;
    }

    protected int getCurrentRow() {
        return currentRow;
    }
}
