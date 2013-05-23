/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.controller;

 import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * <p>
 * OnlineReportListController class.
 * </p>
 * 
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AlarmReportListController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger("OpenNMS.WEB."
			+ AlarmReportListController.class.getName());
	private int load = 0;
	private String filename = null;

	/** {@inheritDoc} */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logger.debug("start: alarm report download");

		Map<String, String> alarmList = new LinkedHashMap<String, String>();

		Properties configProp = new Properties();
		try {
			if (load == 0) {

				configProp.load(new FileInputStream(ConfigFileConstants
						.getHome() + "/etc/opennms.properties"));
				filename = configProp.getProperty("opennms.alarm.report.dir");
				load = 1;
			}

			File f = new File(filename);
			File[] files = f.listFiles();
			
			Arrays.sort( files, new Comparator<File>() {
				public int compare(File o1, File o2) {
					if ((o1.lastModified()) > (o2.lastModified())) {
						return -1;
					}
					else if ((o1.lastModified()) < (o2.lastModified())) {
						return +1;
					}
					else {
						return 0;
					}
				}
			});
			
			if (files != null && files.length > 0) {
				int page = 1;
				int recordsPerPage = 20;
				int offset = 0;
				int numberoffiles = 0;
				int mod = 0;
				numberoffiles = files.length;

				if (request.getParameter("page") != null)
					page = Integer.parseInt(request.getParameter("page"));
				if (page == 1)
					offset = 0;
				if (page != 0)
					offset = (page - 1) * recordsPerPage;

				int noOfPages = (int) Math.ceil(numberoffiles * 1.0
						/ recordsPerPage);

				if (page < noOfPages) {
					if (numberoffiles > recordsPerPage) {
						for (int i = offset; i < offset + recordsPerPage; i++) {
							if (files[i].isFile()) {
								File fileDetails = files[i];
								alarmList.put(fileDetails.getName(),
										fileDetails.getPath());
							}
						}
					}
					if (numberoffiles < recordsPerPage) {
						for (int i = offset; i < numberoffiles; i++) {
							if (files[i].isFile()) {
								File fileDetails = files[i];
								alarmList.put(fileDetails.getName(),
										fileDetails.getPath());
							}
						}
					}
				}

				if (page == noOfPages) {
					mod = numberoffiles % 20;
					if (mod == 0)
						mod = recordsPerPage;

					for (int i = numberoffiles - mod; i < numberoffiles; i++) {

						if (files[i].isFile()) {
							File fileDetails = files[i];
							alarmList.put(fileDetails.getName(),
									fileDetails.getPath());

						}
					}
				}

				request.setAttribute("noOfPages", noOfPages);
				request.setAttribute("currentPage", page);
			}
		} catch (Exception ex) {
			
			logger.debug("Could not open Config file");
			String errorMessage = ex.getMessage();
			return new ModelAndView("report/historical/alarmReportList",
					"errorMessage", errorMessage);
		}
		return new ModelAndView("report/historical/alarmReportList",
				"alarmList", alarmList);
	}

}
