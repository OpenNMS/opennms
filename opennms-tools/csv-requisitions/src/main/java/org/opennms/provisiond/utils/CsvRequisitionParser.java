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

import org.opennms.netmgt.provision.persist.FilesystemForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAssetCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

public class CsvRequisitionParser {

	private static FilesystemForeignSourceRepository m_fsr = null;

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(createShutdownHook());
		if (args.length < 2) {
			System.err.println("Requires 2 parameters.");
			System.exit(1);
		}
		
		String fn = args[0];
		String outDir = args[1];
		
		try {
			parseCsv(fn, outDir);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("Finished.");
	}
	
	protected static void parseCsv(String fname, String outDir) throws IOException {
		File csv = new File(fname);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)));
		
		String line = null;
		int lineNum = 0;
		while ((line = br.readLine()) != null) {
			lineNum++;
			if (line != null && line.startsWith("#")) {
				continue;
			}
			String[] fields = line.split(",", 6);
			int commas = fields.length;
			if (commas != 6) {
				System.out.println("Error on line: "+Integer.toString(lineNum)+". Found "+Integer.toString(commas)+" fields and expected 6");
				continue;
			}
			
			m_fsr = new FilesystemForeignSourceRepository();
			m_fsr.setRequisitionPath(outDir);
			
			RequisitionData rd;
			try {
				rd = new RequisitionData(fields);
			} catch (Throwable e) {
				System.err.println(e);
				continue;
			}
			
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
			iface.setSnmpPrimary("P");
			iface.setStatus(Integer.valueOf(1));
			iface.setMonitoredServices(services);
			
			RequisitionInterfaceCollection ric = new RequisitionInterfaceCollection();
			ric.add(iface);
			
			RequisitionAssetCollection rac = new RequisitionAssetCollection();
			rac.add(new RequisitionAsset("Comment", "Customer: "+rd.getCustomerName()));
			//rac.add(new RequisitionAsset("CustomerID", rd.getCustomerId()));
			
			//add categories
			
			RequisitionNode rn = new RequisitionNode();
			rn.setAssets(rac);
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
