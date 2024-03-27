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
package org.opennms.netmgt.jasper.helper;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import net.sf.jasperreports.engine.JRException;

/**
 * Provides helper methods for the {@link org.opennms.netmgt.jasper.measurement.MeasurementDataSource}.
 */
public abstract class MeasurementsHelper {

    private static Logger LOG = LoggerFactory.getLogger(MeasurementsHelper.class);

    private MeasurementsHelper() {

    }

    /**
     * Returns the description of the interface (e.g. eth0-000000).
     *
     * @param snmpifname
     * @param snmpifdescr
     * @param snmpphysaddr
     * @return the description of the interface (e.g. eth0-000000).
     */
    public static String getInterfaceDescriptor(String snmpifname, String snmpifdescr, String snmpphysaddr) {
        return RrdLabelUtils.computeLabelForRRD(snmpifname, snmpifdescr, snmpphysaddr);
    }

    /**
     * Returns the descriptor of the node or node source, depending on the input parameters  (e.g. node[&lt;nodeId&gt;] or nodeSource[&lt;foreignSource&gt;:&lt;foreignId&gt;].
     *
     * @param nodeId
     * @param foreignSource
     * @param foreignId
     * @return the descriptor of the node or node source, depending on the input parameters (e.g. node[&lt;nodeId&gt;] or nodeSource[&lt;foreignSource&gt;:&lt;foreignId&gt;].
     */
    public static String getNodeOrNodeSourceDescriptor(String nodeId, String foreignSource, String foreignId) {
        if (!Strings.isNullOrEmpty(foreignSource) && !Strings.isNullOrEmpty(foreignId)) {
            return String.format("nodeSource[%s:%s]", foreignSource, foreignId);
        }
        return String.format("node[%s]", nodeId);
    }

    public static boolean isRunInOpennmsJvm() {
        return getSpringHelper().getSpringContext() != null;
    }

    public static SpringHelper getSpringHelper() {
        try {
            return BeanUtils.getBean("measurementDataSourceContext", "springHelper", SpringHelper.class);
        } catch (Exception ex) {
            LOG.warn("Error creating bean 'springHelper'. Creating empty SpringHelper");
            return new SpringHelper();
        }
    }

    public static QueryRequest unmarshal(String query) throws JRException {
        return JaxbUtils.unmarshal(QueryRequest.class, query);
    }

    public static String marshal(QueryRequest queryRequest) throws JRException {
        return JaxbUtils.marshal(queryRequest);
    }
}
