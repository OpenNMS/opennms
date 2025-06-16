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
package org.opennms.protocols.http;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.protocols.xml.config.Parameter;

/**
 * The Class SampleData.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="sample-data")
@XmlAccessorType(XmlAccessType.FIELD)
public class SampleData {

    /** The parameters. */
    @XmlElement(name="parameter")
    private List<Parameter> parameters = new ArrayList<>();

    /**
     * Adds the parameter.
     *
     * @param name the name
     * @param value the value
     */
    public void addParameter(String name, String value) {
        parameters.add(new Parameter(name, value));
    }

    /**
     * Gets the parameter.
     *
     * @param name the name
     * @return the parameter
     */
    public String getParameter(String name) {
        for (Parameter p : parameters) {
            if (p.getName().equals(name)) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the parameters.
     *
     * @return the parameters
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters.
     *
     * @param parameters the new parameters
     */
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

}
