/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.controller.inventory;

public class RancidReportExecCommClass {

    private String date;
    
    private String fieldhas;
    
    private String reporttype;
    
    private String reportfiletype;
    
    private String reportemail;
    
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getFieldhas() {
        return fieldhas;
    }
    public void setFieldhas(String fieldhas) {
        this.fieldhas = fieldhas;
    }
    public String getReporttype() {
        return reporttype;
    }
    public void setReporttype(String reporttype) {
        this.reporttype = reporttype;
    }
    public String getReportfiletype() {
        return reportfiletype;
    }
    public void setReportfiletype(String reportfiletype) {
        this.reportfiletype = reportfiletype;
    }
    public String getReportemail() {
        return reportemail;
    }
    public void setReportemail(String reportemail) {
        this.reportemail = reportemail;
    }    
}
