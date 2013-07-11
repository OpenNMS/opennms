/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc. OpenNMS(R) is
 * a registered trademark of The OpenNMS Group, Inc. OpenNMS(R) is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/ Knowledge base for all the
 * drl files in alarm-notification/drools directory will be created at
 * startup.
 */
package org.opennms.netmgt.notification.filter;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.opennms.core.utils.ConfigFileConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsFileLoader implements Serializable {
	private static final Logger LOG = LoggerFactory.getLogger(DroolsFileLoader.class);

	private static final long serialVersionUID = 1L;

	private static HashMap<String, KnowledgeBase> s_knowledgeBaseHash;
	public DroolsFileLoader(String reloadDrl) {
		
	}
	
	public DroolsFileLoader() {
		s_knowledgeBaseHash = new HashMap<String, KnowledgeBase>();
		File drlFolder = new File(ConfigFileConstants.getHome()
				+ "/etc/alarm-notification/drools/");
		File[] listOfFiles = drlFolder.listFiles();
		if (listOfFiles == null) {
			LOG.debug(
					"No drl file in directory <OPENNMS_HOME>/etc/alarm-notification/drools/");
			return;
		}
		for (int i = 0; i < listOfFiles.length; i++) {
			String drlName = listOfFiles[i].getName();
			KnowledgeBase knowledgeBase = null;
			try {
				KnowledgeBuilder builder = KnowledgeBuilderFactory
						.newKnowledgeBuilder();
				File drlFile = new File(drlFolder + "/" + drlName);
				builder.add(ResourceFactory.newFileResource(drlFile),
						ResourceType.DRL);
				if (builder.hasErrors()) {
					LOG.debug("Drl file " + drlName
							+ " has the following errors : "
							+ builder.getErrors().toString());
				} else {
					LOG.debug("Drl file " + drlName
							+ " is loaded successfully");
				}
				knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
				knowledgeBase.addKnowledgePackages(builder
						.getKnowledgePackages());
			} catch (Exception e) {
				e.printStackTrace();
				LOG.debug(
						"Exception while creating builder for file " + drlName
								+ "Exception is " + e.getMessage());
			}
			s_knowledgeBaseHash.put(drlName, knowledgeBase);
		}
	}

	public boolean reloadDroolFiles(){
		boolean status = true;
		s_knowledgeBaseHash = new HashMap<String, KnowledgeBase>();
		File drlFolder = new File(ConfigFileConstants.getHome()
				+ "/etc/alarm-notification/drools/");
		File[] listOfFiles = drlFolder.listFiles();
		if (listOfFiles == null) {
			LOG.debug(
					"No drl file in directory <OPENNMS_HOME>/etc/alarm-notification/drools/");
			return status;
		}
		for (int i = 0; i < listOfFiles.length; i++) {
			String drlName = listOfFiles[i].getName();
			KnowledgeBase knowledgeBase = null;
			try {
				KnowledgeBuilder builder = KnowledgeBuilderFactory
						.newKnowledgeBuilder();
				File drlFile = new File(drlFolder + "/" + drlName);
				builder.add(ResourceFactory.newFileResource(drlFile),
						ResourceType.DRL);
				if (builder.hasErrors()) {
					LOG.debug("Drl file " + drlName
							+ " has the following errors : "
							+ builder.getErrors().toString());
				} else {
					LOG.debug("Drl file " + drlName
							+ " is loaded successfully");
				}
				knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
				knowledgeBase.addKnowledgePackages(builder
						.getKnowledgePackages());
			} catch (Exception e) {
				status = false;
				e.printStackTrace();
				LOG.debug(
						"Exception while creating builder for file " + drlName
								+ "Exception is " + e.getMessage());
			}
			s_knowledgeBaseHash.put(drlName, knowledgeBase);
		}
		
		return status;
	}
	public void setBuilderHash(HashMap<String, KnowledgeBase> knowledgeBaseHash) {
		DroolsFileLoader.s_knowledgeBaseHash = knowledgeBaseHash;
	}

	public static KnowledgeBase getKnowledgeBaseForDrl(String drlName) {
		if (DroolsFileLoader.s_knowledgeBaseHash != null)
			return DroolsFileLoader.s_knowledgeBaseHash.get(drlName);
		else
			return null;
	}
}
