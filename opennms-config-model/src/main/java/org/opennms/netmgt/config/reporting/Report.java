/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.reporting;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("reporting.xsd")
public class Report implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * the name of this report
     */
    @XmlAttribute(name = "id", required = true)
    private String m_id;

    /**
     * type of this report (calendar/classic)
     */
    @XmlAttribute(name = "type", required = true)
    private String m_type;

    @XmlElement(name = "parameters")
    private Parameters m_parameters;

    /**
     * template to convert to display the report
     *  in PDF format
     */
    @XmlElement(name = "pdf-template")
    private String m_pdfTemplate;

    /**
     * template to convert to display the report
     *  in PDF format with embedded SVG
     */
    @XmlElement(name = "svg-template")
    private String m_svgTemplate;

    /**
     * template to convert to display the report
     *  in HTML format
     */
    @XmlElement(name = "html-template")
    private String m_htmlTemplate;

    /**
     * path to the logo file
     */
    @XmlElement(name = "logo", required = true)
    private String m_logo;

    public Report() {
    }

    public String getId() {
        return m_id;
    }

    public void setId(final String id) {
        m_id = ConfigUtils.assertNotEmpty(id, "id");
    }

    public String getType() {
        return m_type;
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertNotEmpty(type, "type");
    }

    public Optional<Parameters> getParameters() {
        return Optional.ofNullable(m_parameters);
    }

    public void setParameters(final Parameters parameters) {
        m_parameters = parameters;
    }

    public Optional<String> getPdfTemplate() {
        return Optional.ofNullable(m_pdfTemplate);
    }

    public void setPdfTemplate(final String pdfTemplate) {
        m_pdfTemplate = ConfigUtils.normalizeString(pdfTemplate);
    }

    public Optional<String> getSvgTemplate() {
        return Optional.ofNullable(m_svgTemplate);
    }

    public void setSvgTemplate(final String svgTemplate) {
        m_svgTemplate = ConfigUtils.normalizeString(svgTemplate);
    }

    public Optional<String> getHtmlTemplate() {
        return Optional.ofNullable(m_htmlTemplate);
    }

    public void setHtmlTemplate(final String htmlTemplate) {
        m_htmlTemplate = ConfigUtils.normalizeString(htmlTemplate);
    }

    public String getLogo() {
        return m_logo;
    }

    public void setLogo(final String logo) {
        m_logo = ConfigUtils.assertNotEmpty(logo, "logo");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id, 
                            m_type, 
                            m_parameters, 
                            m_pdfTemplate, 
                            m_svgTemplate, 
                            m_htmlTemplate, 
                            m_logo);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Report) {
            final Report that = (Report)obj;
            return Objects.equals(this.m_id, that.m_id)
                    && Objects.equals(this.m_type, that.m_type)
                    && Objects.equals(this.m_parameters, that.m_parameters)
                    && Objects.equals(this.m_pdfTemplate, that.m_pdfTemplate)
                    && Objects.equals(this.m_svgTemplate, that.m_svgTemplate)
                    && Objects.equals(this.m_htmlTemplate, that.m_htmlTemplate)
                    && Objects.equals(this.m_logo, that.m_logo);
        }
        return false;
    }

}
