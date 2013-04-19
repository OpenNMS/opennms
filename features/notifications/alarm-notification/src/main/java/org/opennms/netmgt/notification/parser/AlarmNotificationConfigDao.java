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
 * http://www.opennms.org/ http://www.opennms.com/ alarmNotificationConf.xml
 * is parsed by this class.
 */
package org.opennms.netmgt.notification.parser;

import java.io.BufferedReader;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.opennms.core.utils.LogUtils;

public class AlarmNotificationConfigDao extends
		AbstractJaxbConfigDao<AlarmNorthbounderConfig, AlarmNorthbounderConfig> {

	public AlarmNotificationConfigDao() {
		super(AlarmNorthbounderConfig.class, "Config for Alarm Northbounder");
	}

	@Override
	protected AlarmNorthbounderConfig translateConfig(
			AlarmNorthbounderConfig config) {
		return config;
	}

	public AlarmNorthbounderConfig getConfig() {
		return getContainer().getObject();
	}

	/**
	 * This class updates the alarmNotificationConf.xml
	 * 
	 * @param AlarmNorthbounderConfig
	 * @return boolean
	 */
	public boolean addNotification(AlarmNorthbounderConfig config,
			String fileName) {
		try {
			File file = new File(fileName);
			JAXBContext jaxbContext = JAXBContext
					.newInstance(AlarmNorthbounderConfig.class);
			Marshaller jaxbMarshaller = JaxbUtils.getMarshallerFor(config,
					jaxbContext);

			FileInputStream fstream = new FileInputStream(file);
			// System.err.println(xml);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String xml = "";
			String str;
			int count = 0;
			// Read File Line By Line
			while ((str = br.readLine()) != null) {
				xml = xml + str;
				count++;
			}
			// Close the input stream
			in.close();
			AlarmNorthbounderConfig availableconfig = null;
			// The count check is used to see if notification tag is present
			// in the xml file.If the notification tag is not present, create
			// a new xml file
			if (xml.length() != 0 && count > 2) {
				Resource resource = new ByteArrayResource(xml.getBytes());
				AlarmNotificationConfigDao dao = new AlarmNotificationConfigDao();
				dao.setConfigResource(resource);
				dao.afterPropertiesSet();

				availableconfig = dao.getConfig();

				// if a notification name is already available do not add
				String notificationName = config.getNotification().get(0)
						.getName();
				if (availableconfig.getNotification().contains(
						config.getNotification().get(0))) {
					LogUtils.debugf(this, "Notification with name "
							+ notificationName + " already exists.");
					return false;
				}
				availableconfig.getNotification().add(
						config.getNotification().get(0));
			} else
				availableconfig = config;
			jaxbMarshaller.setProperty("jaxb.noNamespaceSchemaLocation",
					"alarmNotificationConf.xsd");
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(availableconfig, file);
		} catch (JAXBException e) {
			LogUtils.errorf(this,
					"Unable to add notification because of JAXBException ", e);
			return false;
		} catch (FileNotFoundException e) {
			LogUtils.errorf(
					this,
					"Unable to add notification because of FileNotFoundException ",
					e);
			return false;
		} catch (IOException e) {
			LogUtils.errorf(this,
					"Unable to add notification because of IOException ", e);
			return false;
		} catch (Exception e) {
			LogUtils.errorf(this,
					"Unable to add notification because of Exception ", e);
			return false;
		}
		LogUtils.debugf(this, "Notification added successfully");
		return true;
	}

	/**
	 * This class deletes a notification
	 * 
	 * @param AlarmNorthbounderConfig
	 * @return boolean
	 */
	public boolean deleteNotification(AlarmNorthbounderConfig config,
			String fileName) {
		try {
			File file = new File(fileName);
			JAXBContext jaxbContext = JAXBContext
					.newInstance(AlarmNorthbounderConfig.class);
			Marshaller jaxbMarshaller = JaxbUtils.getMarshallerFor(config,
					jaxbContext);

			FileInputStream fstream = new FileInputStream(file);
			// System.err.println(xml);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String xml = "";
			String str;
			// Read File Line By Line
			while ((str = br.readLine()) != null) {
				xml = xml + str;
			}
			// Close the input stream
			in.close();
			Resource resource = new ByteArrayResource(xml.getBytes());
			AlarmNotificationConfigDao dao = new AlarmNotificationConfigDao();
			dao.setConfigResource(resource);
			dao.afterPropertiesSet();

			AlarmNorthbounderConfig availableconfig = dao.getConfig();
			String notificationName = config.getNotification().get(0).getName();
			boolean isRemoved = availableconfig.getNotification().remove(
					config.getNotification().get(0));
			if (isRemoved == false) {
				LogUtils.debugf(this, "There is no Notification with name "
						+ notificationName);
				return false;
			}

			jaxbMarshaller.setProperty("jaxb.noNamespaceSchemaLocation",
					"alarmNotificationConf.xsd");
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(availableconfig, file);
		} catch (JAXBException e) {
			LogUtils.errorf(this,
					"Unable to delete notification because of JAXBException ",
					e);
			return false;
		} catch (FileNotFoundException e) {
			LogUtils.errorf(
					this,
					"Unable to delete notification because of FileNotFoundException ",
					e);
			return false;
		} catch (IOException e) {
			LogUtils.errorf(this,
					"Unable to delete notification because of IOException ", e);
			return false;
		}
		LogUtils.debugf(this, "Notification deleted successfully");
		return true;
	}
}