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
package org.opennms.netmgt.jasper;

import org.opennms.netmgt.jasper.grafana.GrafanaExecutorFactory;
import org.opennms.netmgt.jasper.measurement.MeasurementExecutorFactory;

import net.sf.jasperreports.engine.query.QueryExecuterFactory;

/**
 * These are the supported "query languages" to be used within Jasper Report (*.jrxml) files.
 */
public enum SupportedLanguage {
    Measurement(new MeasurementExecutorFactory()), Grafana(new GrafanaExecutorFactory());

    private final QueryExecuterFactory factory;

    private SupportedLanguage(QueryExecuterFactory factory) {
        this.factory = factory;
    }

    public QueryExecuterFactory getExecutorFactory() {
        return factory;
    }

    public static String[] names() {
        final SupportedLanguage[] supportedLanguages = SupportedLanguage.values();
        final String[] supportedLanguagesNames = new String[supportedLanguages.length];
        for (int i=0; i<supportedLanguages.length; i++) {
            supportedLanguagesNames[i] = supportedLanguages[i].name().toLowerCase();
        }
        return supportedLanguagesNames;
    }

    public static SupportedLanguage createFrom(String language) {
        for (SupportedLanguage supportedLanguage : SupportedLanguage.values()) {
            if (supportedLanguage.name().equalsIgnoreCase(language)) {
                return supportedLanguage;
            }
        }
        return null;
    }
}
