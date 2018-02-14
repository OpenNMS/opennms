/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Report Configuration for local reports
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "database-reports")
@XmlAccessorType(XmlAccessType.NONE)
public class LocalReports {

    @XmlElement(name = "report")
    private List<Report> m_reportList = new ArrayList<>();

    public List<Report> getReportList() {
        return m_reportList;
    }

    public void setReportList(List<Report> reportList) {
        this.m_reportList = reportList;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof LocalReports)) {
            return false;
        }
        LocalReports castOther = (LocalReports) other;
        return Objects.equals(m_reportList, castOther.m_reportList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_reportList);
    }
}
