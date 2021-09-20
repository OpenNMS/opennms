/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.inventory;

/**
 * <p>RancidReportExecCommClass class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class RancidReportExecCommClass {

    private String date;
    
    private String fieldhas;
    
    private String reporttype;
    
    private String reportfiletype;
    
    private String reportemail;
    
    /**
     * <p>Getter for the field <code>date</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDate() {
        return date;
    }
    /**
     * <p>Setter for the field <code>date</code>.</p>
     *
     * @param date a {@link java.lang.String} object.
     */
    public void setDate(String date) {
        this.date = date;
    }
    /**
     * <p>Getter for the field <code>fieldhas</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFieldhas() {
        return fieldhas;
    }
    /**
     * <p>Setter for the field <code>fieldhas</code>.</p>
     *
     * @param fieldhas a {@link java.lang.String} object.
     */
    public void setFieldhas(String fieldhas) {
        this.fieldhas = fieldhas;
    }
    /**
     * <p>Getter for the field <code>reporttype</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReporttype() {
        return reporttype;
    }
    /**
     * <p>Setter for the field <code>reporttype</code>.</p>
     *
     * @param reporttype a {@link java.lang.String} object.
     */
    public void setReporttype(String reporttype) {
        this.reporttype = reporttype;
    }
    /**
     * <p>Getter for the field <code>reportfiletype</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportfiletype() {
        return reportfiletype;
    }
    /**
     * <p>Setter for the field <code>reportfiletype</code>.</p>
     *
     * @param reportfiletype a {@link java.lang.String} object.
     */
    public void setReportfiletype(String reportfiletype) {
        this.reportfiletype = reportfiletype;
    }
    /**
     * <p>Getter for the field <code>reportemail</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getReportemail() {
        return reportemail;
    }
    /**
     * <p>Setter for the field <code>reportemail</code>.</p>
     *
     * @param reportemail a {@link java.lang.String} object.
     */
    public void setReportemail(String reportemail) {
        this.reportemail = reportemail;
    }    
}
