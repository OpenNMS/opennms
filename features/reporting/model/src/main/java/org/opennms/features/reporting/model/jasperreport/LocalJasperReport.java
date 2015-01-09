/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.model.jasperreport;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "report")
public class LocalJasperReport implements JasperReportDefinition {

    private String m_id;

    private String m_template;

    private String m_engine;

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#getId()
     */
    @Override
    @XmlAttribute(name = "id")
    public String getId() {
        return m_id;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#getTemplate()
     */
    @Override
    @XmlAttribute(name = "template")
    public String getTemplate() {
        return m_template;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#getEngine()
     */
    @Override
    @XmlAttribute(name = "engine")
    public String getEngine() {
        return m_engine;
    }

    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {
        m_id = id;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#setTemplate(java.lang.String)
     */
    @Override
    public void setTemplate(String template) {
        m_template = template;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.features.reporting.model.jasperreport.JasperReportDefinition#setEngine(java.lang.String)
     */
    @Override
    public void setEngine(String engine) {
        m_engine = engine;
    }
}
