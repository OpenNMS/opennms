/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.reporting.repository.local;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.opennms.features.reporting.dao.LegacyLocalReportsDao;
import org.opennms.features.reporting.dao.LocalReportsDao;
import org.opennms.features.reporting.dao.jasper.LegacyLocalJasperReportsDao;
import org.opennms.features.reporting.dao.jasper.LocalJasperReportsDao;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.model.basicreport.LegacyLocalReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegacyLocalReportRepository implements ReportRepository {

	private Logger logger = LoggerFactory.getLogger(LegacyLocalReportRepository.class);

	private LocalReportsDao m_localReportsDao = new LegacyLocalReportsDao();

	private LocalJasperReportsDao m_localJasperReportsDao = new LegacyLocalJasperReportsDao();

	private final String REPOSITORY_ID = "local";
    
    private final String REPOSITORY_NAME="Legacy local repository";
    
    private final String REPOSITORY_DESCRIPTION="Providing OpenNMS community reports from local disk.";
    
    private final String MANAGEMENT_URL ="http://localhost/manageLegacyLocalRepositoy";

	@Override
	public List<BasicReportDefinition> getReports() {
		List<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
		for (BasicReportDefinition report : m_localReportsDao.getReports()) {
			BasicReportDefinition resultReport = new LegacyLocalReportDefinition();
			try {
				BeanUtils.copyProperties(resultReport, report);
				resultReport.setId(REPOSITORY_ID + "_" + report.getId());
			} catch (IllegalAccessException e) {
				logger.error("IllegalAccessException during BeanUtils.copyProperties for BasicReportDefinion '{}'", e.getMessage());
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				logger.error("InvocationTargetException during BeanUtils.copyProperties for BasicReportDefinion '{}'", e.getMessage());
				e.printStackTrace();
			}
			resultList.add(resultReport);
		}
		return resultList;
	}

	@Override
	public List<BasicReportDefinition> getOnlineReports() {
		List<BasicReportDefinition> resultList = new ArrayList<BasicReportDefinition>();
		for (BasicReportDefinition report : m_localReportsDao.getOnlineReports()) {
			BasicReportDefinition resultReport = new LegacyLocalReportDefinition();
			try {
				BeanUtils.copyProperties(resultReport, report);
				resultReport.setId(REPOSITORY_ID + "_" + report.getId());
			} catch (IllegalAccessException e) {
				logger.error("IllegalAccessException during BeanUtils.copyProperties for BasicReportDefinion '{}'", e.getMessage());
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				logger.error("InvocationTargetException during BeanUtils.copyProperties for BasicReportDefinion '{}'", e.getMessage());
				e.printStackTrace();
			}
			resultList.add(resultReport);
		}
		return resultList;
	}

	@Override
	public String getReportService(String id) {
		id = id.substring(id.indexOf("_") + 1);
		return m_localReportsDao.getReportService(id);
	}

	@Override
	public String getDisplayName(String id) {
		id = id.substring(id.indexOf("_") + 1);
		return m_localReportsDao.getDisplayName(id);
	}

	@Override
	public String getEngine(String id) {
		id = id.substring(id.indexOf("_") + 1);
		return m_localJasperReportsDao.getEngine(id);
	}

	@Override
	public InputStream getTemplateStream(String id) {
		id = id.substring(id.indexOf("_") + 1);
		return m_localJasperReportsDao.getTemplateStream(id);
	}

	@Override
	public String getRepositoryId() {
		return REPOSITORY_ID;
	}

    @Override
    public String getRepositoryName() {
        return REPOSITORY_NAME;
    }

    @Override
    public String getRepositoryDescription() {
        return REPOSITORY_DESCRIPTION;
    }

    @Override
    public String getManagementUrl() {
        return MANAGEMENT_URL;
    }
}
