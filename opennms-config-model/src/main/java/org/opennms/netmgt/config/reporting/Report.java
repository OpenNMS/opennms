/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class Report.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
public class Report implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * the name of this report
     */
    @XmlAttribute(name = "id", required = true)
    private String id;

    /**
     * type of this report (calendar/classic)
     */
    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlElement(name = "parameters")
    private Parameters parameters;

    /**
     * template to convert to display the report
     *  in PDF format
     */
    @XmlElement(name = "pdf-template")
    private String pdfTemplate;

    /**
     * template to convert to display the report
     *  in PDF format with embedded SVG
     */
    @XmlElement(name = "svg-template")
    private String svgTemplate;

    /**
     * template to convert to display the report
     *  in HTML format
     */
    @XmlElement(name = "html-template")
    private String htmlTemplate;

    /**
     * path to the logo file
     */
    @XmlElement(name = "logo", required = true)
    private String logo;

    public Report() {
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Report) {
            Report temp = (Report)obj;
            boolean equals = Objects.equals(temp.id, id)
                && Objects.equals(temp.type, type)
                && Objects.equals(temp.parameters, parameters)
                && Objects.equals(temp.pdfTemplate, pdfTemplate)
                && Objects.equals(temp.svgTemplate, svgTemplate)
                && Objects.equals(temp.htmlTemplate, htmlTemplate)
                && Objects.equals(temp.logo, logo);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'htmlTemplate'. The field 'htmlTemplate' has the
     * following description: template to convert to display the report
     *  in HTML format
     * 
     * @return the value of field 'HtmlTemplate'.
     */
    public String getHtmlTemplate() {
        return this.htmlTemplate;
    }

    /**
     * Returns the value of field 'id'. The field 'id' has the following
     * description: the name of this report
     * 
     * @return the value of field 'Id'.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the value of field 'logo'. The field 'logo' has the following
     * description: path to the logo file
     * 
     * @return the value of field 'Logo'.
     */
    public String getLogo() {
        return this.logo;
    }

    /**
     * Returns the value of field 'parameters'.
     * 
     * @return the value of field 'Parameters'.
     */
    public Parameters getParameters() {
        return this.parameters;
    }

    /**
     * Returns the value of field 'pdfTemplate'. The field 'pdfTemplate' has the
     * following description: template to convert to display the report
     *  in PDF format
     * 
     * @return the value of field 'PdfTemplate'.
     */
    public String getPdfTemplate() {
        return this.pdfTemplate;
    }

    /**
     * Returns the value of field 'svgTemplate'. The field 'svgTemplate' has the
     * following description: template to convert to display the report
     *  in PDF format with embedded SVG
     * 
     * @return the value of field 'SvgTemplate'.
     */
    public String getSvgTemplate() {
        return this.svgTemplate;
    }

    /**
     * Returns the value of field 'type'. The field 'type' has the following
     * description: type of this report (calendar/classic)
     * 
     * @return the value of field 'Type'.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            id, 
            type, 
            parameters, 
            pdfTemplate, 
            svgTemplate, 
            htmlTemplate, 
            logo);
        return hash;
    }

    /**
     * Sets the value of field 'htmlTemplate'. The field 'htmlTemplate' has the
     * following description: template to convert to display the report
     *  in HTML format
     * 
     * @param htmlTemplate the value of field 'htmlTemplate'.
     */
    public void setHtmlTemplate(final String htmlTemplate) {
        this.htmlTemplate = htmlTemplate;
    }

    /**
     * Sets the value of field 'id'. The field 'id' has the following description:
     * the name of this report
     * 
     * @param id the value of field 'id'.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the value of field 'logo'. The field 'logo' has the following
     * description: path to the logo file
     * 
     * @param logo the value of field 'logo'.
     */
    public void setLogo(final String logo) {
        this.logo = logo;
    }

    /**
     * Sets the value of field 'parameters'.
     * 
     * @param parameters the value of field 'parameters'.
     */
    public void setParameters(final Parameters parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the value of field 'pdfTemplate'. The field 'pdfTemplate' has the
     * following description: template to convert to display the report
     *  in PDF format
     * 
     * @param pdfTemplate the value of field 'pdfTemplate'.
     */
    public void setPdfTemplate(final String pdfTemplate) {
        this.pdfTemplate = pdfTemplate;
    }

    /**
     * Sets the value of field 'svgTemplate'. The field 'svgTemplate' has the
     * following description: template to convert to display the report
     *  in PDF format with embedded SVG
     * 
     * @param svgTemplate the value of field 'svgTemplate'.
     */
    public void setSvgTemplate(final String svgTemplate) {
        this.svgTemplate = svgTemplate;
    }

    /**
     * Sets the value of field 'type'. The field 'type' has the following
     * description: type of this report (calendar/classic)
     * 
     * @param type the value of field 'type'.
     */
    public void setType(final String type) {
        this.type = type;
    }

}
