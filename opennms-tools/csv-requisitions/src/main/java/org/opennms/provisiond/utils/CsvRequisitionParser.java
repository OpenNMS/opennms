/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.provisiond.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

public class CsvRequisitionParser {

	private static final String PROPERTY_FS_REPO_PATH = "fs.repo.path";
	private static final String PROPERTY_CSV_FILE = "csv.file";
	private static final String PROPERTY_FOREIGN_SOURCE = "foreign.source";
	
	private static FilesystemForeignSourceRepository m_fsr = null;
	private static File m_csvFile = new File("/tmp/nodes.csv");
	private static File m_repoPath = new File("/opt/opennms/imports");
	private static String m_foreignSource = "default";

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(createShutdownHook());
		
		if (args.length > 0) {
			try {
				usageReport();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
		
		try {
			if (!validProps()) {
				usageReport();
				System.exit(-1);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
		try {
			parseCsv(m_csvFile, m_repoPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Finished.");
	}

	
	//need to do some better exception handling here
	private static boolean validProps() throws IOException, FileNotFoundException, IllegalArgumentException  {
		
		String foreignSource = System.getProperty(PROPERTY_FOREIGN_SOURCE);
		if (foreignSource == null) {
			m_foreignSource = "default";
		} else {
			m_foreignSource = foreignSource;
		}
		
		String csvFileName = System.getProperty(PROPERTY_CSV_FILE);
		if (csvFileName == null) {
			return false;
		} else {
			m_csvFile = new File(csvFileName);
			if (!m_csvFile.exists()) {
				throw new FileNotFoundException("CSV Input File: "+csvFileName+"; Not Found!");
			}
		}
		
		String fsRepo = System.getProperty(PROPERTY_FS_REPO_PATH);
		if (fsRepo == null) {
			fsRepo = ".";
		}
		
		m_repoPath = new File(fsRepo);
		
		if (!m_repoPath.exists() || !m_repoPath.isDirectory() || !m_repoPath.canWrite()) {
			throw new IllegalArgumentException("The specified fs.repo either doesn't exist, isn't writable, or isn't a directory.");
		} else {
			m_fsr = new FilesystemForeignSourceRepository();
			m_fsr.setRequisitionPath(m_repoPath.getCanonicalPath());
		}
		
		return true;
	}

	private static void usageReport() throws IOException {		
		System.err.println("Usage: java CsvRequistionParser [<Property>...]\n" +
				"\n" +
				"Supported Properties:\n" +
				"\t"+PROPERTY_CSV_FILE+": default:"+m_csvFile.getCanonicalPath()+"\n" +
				"\t"+PROPERTY_FS_REPO_PATH+": default:"+m_repoPath.getCanonicalPath()+"\n" +
				"\t"+PROPERTY_FOREIGN_SOURCE+": default:"+m_foreignSource+"\n" +
				"\n" +
				"\n" +
				"Example:\n" +
				"\t java -D"+PROPERTY_CSV_FILE+"=/tmp/mynodes.csv "
				+PROPERTY_FS_REPO_PATH+"= /etc/opennms/imports "
				+PROPERTY_FOREIGN_SOURCE+"=MyNodes -jar opennms-csv-requisition-1.13.0-SNAPSHOT-jar-with-dependencies.jar"
				+"\n" +
				"\n" +
				"FYI: This application expects the csv file to have 2 elements, nodelabel and IP address.  Example:" +
				"\n" +
				"www.opennms.com,64.146.64.212\n" +
				"github.com,204.232.175.90\n" +
				"www.juniper.net,2600:1406:1f:195::720\n\n"
				);
	}

	protected static void parseCsv(File csv, File m_repo) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)));
		
		String line = null;
		int lineNum = 0;
		while ((line = br.readLine()) != null) {
			lineNum++;
			if (line != null && line.startsWith("#")) {
				continue;
			}
			
			String[] fields = line.split(",", 2);
			int fieldCount = fields.length;
			if (fieldCount != 2) {
				System.err.println("Error on line: "+Integer.toString(lineNum)+". Found "+Integer.toString(fieldCount)+" fields and expected 2.");
				continue;
			}
						
			RequisitionData rd = new RequisitionData(fields[0], fields[1], m_foreignSource);
			System.out.println("Line "+Integer.toString(lineNum)+":"+rd.toString());

			Requisition r;
			String foreignSource = rd.getForeignSource();
			
			r = m_fsr.getRequisition(foreignSource);
			
			if (r== null) {
				r = new Requisition(foreignSource);
			}
			
			System.err.println("Creating/Updating requistion: "+foreignSource);
			
			r.updateDateStamp();
			
			RequisitionMonitoredServiceCollection services = new RequisitionMonitoredServiceCollection();
			services.add(new RequisitionMonitoredService("ICMP"));
			services.add(new RequisitionMonitoredService("SNMP"));
			
			RequisitionInterface iface = new RequisitionInterface();
			iface.setDescr("mgmt-if");
			iface.setIpAddr(rd.getPrimaryIp());
			iface.setManaged(true);
			iface.setSnmpPrimary(PrimaryType.PRIMARY);
			iface.setStatus(Integer.valueOf(1));
			iface.setMonitoredServices(services);
			
			RequisitionInterfaceCollection ric = new RequisitionInterfaceCollection();
			ric.add(iface);
			
			//RequisitionAssetCollection rac = new RequisitionAssetCollection();
			//rac.add(new RequisitionAsset("Comment", "Customer: "+rd.getCustomerName()));
			//rac.add(new RequisitionAsset("CustomerID", rd.getCustomerId()));
			
			//add categories
			
			RequisitionNode rn = new RequisitionNode();
			//rn.setAssets(rac);
			rn.setBuilding(foreignSource);
			rn.setCategories(null);
			rn.setForeignId(rd.getForeignId());
			rn.setInterfaces(ric);
			rn.setNodeLabel(rd.getNodeLabel());
			
			r.insertNode(rn);
			m_fsr.save(r);
			
		}
		br.close();
	}
	
	public static Thread createShutdownHook() {
		Thread t = new Thread() {
			@Override
			public void run() {
				System.out.println("\nHave a nice day! :)");
				Runtime.getRuntime().halt(0);
			}
		};
		return t;
	}

}
