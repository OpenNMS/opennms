/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.server;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.opennms.gwt.web.ui.asset.client.AssetService;
import org.opennms.gwt.web.ui.asset.shared.AssetCommand;
import org.opennms.gwt.web.ui.asset.shared.AssetSuggCommand;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 * 
 */
public class AssetServiceMockup extends RemoteServiceServlet implements AssetService {

	private static final long serialVersionUID = 386558445935186134L;

	private AssetCommand asset = new AssetCommand();
	private SimpleDateFormat onmsFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private AssetSuggCommand assetSugg = new AssetSuggCommand();
	
	public AssetServiceMockup() {
		setData();
	}

        @Override
	public AssetCommand getAssetByNodeId(int nodeId) throws Exception {
		if (nodeId == 666) {
			throw new NullPointerException("just testing error-case: nodeid 666");
		}
		return asset;
	}

        @Override
	public Boolean saveOrUpdateAssetByNodeId(int nodeId, AssetCommand asset) throws Exception {
		if (nodeId == 999) {
			throw new NullPointerException("just testing error-case: nodeid 999, you will not save!!");
		}
		this.asset = asset;
		assetSugg.addCpu(asset.getCpu());
		assetSugg.addAdditionalhardware(asset.getAdditionalhardware());
		asset.setLastModifiedDate(new Date());
		asset.setLastModifiedBy("admin");
		return true;
	}

	public AssetCommand setData() {
		asset.setNodeId("23");
		asset.setNodeLabel("Cronojon");
		asset.setLoggedInUser("admin");
		saveDataSNMP();
		saveDataConfigCategories();
		saveDataIdentification();
		saveDataLocation();
		saveDataVendor();
		saveDataAuthentication();
		saveDataHardware();
		saveDataComments();
		asset.setLastModifiedBy("admin");
		asset.setLastModifiedDate(new Date());
		return asset;
	}

	private void saveDataSNMP() {
		asset.setSnmpSysObjectId(".1.3.6.1.4.1.8072.3.2.10");
		//asset.setSnmpSysObjectId("");
		asset.setSnmpSysName("tak-ThinkPad-X200s");
		asset.setSnmpSysLocation("Sitting on the Dock of the Bay");
		asset.setSnmpSysContact("Me <me@example.org>");
		asset.setSnmpSysDescription("Linux tak-ThinkPad-X200s 2.6.35-28-generic #50-Ubuntu SMP Fri Mar 18 18:42:20 UTC 2011 x86_64");
	}

	private void saveDataConfigCategories() {
		asset.setDisplayCategory("DisplayCategory");
		asset.setNotifyCategory("NotifyCategory");
		asset.setPollerCategory("PollerCategory");
		asset.setThresholdCategory("ThresholdCategory");
	}

	private void saveDataIdentification() {
		asset.setDescription("Description");
		asset.setCategory("Category");
		asset.setManufacturer("Manufacturer");
		asset.setModelNumber("ModelNumber");
		asset.setSerialNumber("SerialNumber");
		asset.setAssetNumber("AssetNumber");
		asset.setOperatingSystem("OperatingSystem");
		Date installDate;
		try {
			installDate = onmsFormatter.parse("2000-12-27");
		} catch (ParseException e) {
			e.printStackTrace();
			installDate = new Date();
		}
		asset.setDateInstalled(onmsFormatter.format(installDate));
	}

	private void saveDataLocation() {
		asset.setRegion("Region");
		asset.setDivision("Division");
		asset.setDepartment("Department");
		asset.setAddress1("Address1");
		asset.setAddress2("Address2");
		asset.setCity("City");
		asset.setCountry("Country");
		asset.setLongitude(0f);
		asset.setLatitude(0f);
		asset.setState("State");
		asset.setZip("Zip");
		asset.setBuilding("Building");
		asset.setFloor("Floor");
		asset.setRoom("Room");
		asset.setRack("Rack");
		asset.setSlot("Slot");
		asset.setPort("Port");
		asset.setCircuitId("CircuitId");
	}

	private void saveDataVendor() {
		asset.setVendor("Vendor");
		asset.setVendorPhone("VendorPhone");
		asset.setVendorFax("VendorFax");
		asset.setLease("Lease");
		// asset.setLeaseExpires(formatter.format(new Date()));
		asset.setLeaseExpires("FooDate");
		asset.setVendorAssetNumber("VendorAssetNumber");
		asset.setMaintcontract("423423423_contract+Plus");
		
		Date maintConExp;
		try {
			maintConExp = onmsFormatter.parse("2000-12-27");
		} catch (ParseException e) {
			e.printStackTrace();
			maintConExp = new Date();
		}
		
		asset.setMaintContractExpiration(onmsFormatter.format(maintConExp));
		asset.setSupportPhone("SupportPhone");
	}

	private void saveDataAuthentication() {
		asset.setUsername("Username");
		asset.setPassword("Password");
		asset.setEnable("Enable");

		asset.setConnection("");
		ArrayList<String> connectionOptions = new ArrayList<String>();
		connectionOptions.add("");
		connectionOptions.add("telnet");
		connectionOptions.add("ssh");
		connectionOptions.add("rsh");
		asset.setConnectionOptions(connectionOptions);

		asset.setAutoenable("");
		ArrayList<String> autoenableOptions = new ArrayList<String>();
		autoenableOptions.add("");
		autoenableOptions.add("A");
		asset.setAutoenableOptions(autoenableOptions);

	}

	private void saveDataHardware() {
		asset.setCpu("Intel Centrino2");
		asset.setRam("8GB DDR3");
		asset.setStoragectrl("SATA");
		asset.setAdditionalhardware("Rocket-Tower");
		asset.setNumpowersupplies("1");
		asset.setInputpower("2400 W");

		asset.setHdd1("for Comics");
		asset.setHdd2("for Musik");
		asset.setHdd3("for Games");
		asset.setHdd4("for Programs");
		asset.setHdd5("for Chaos");
		asset.setHdd6("for failing");
	}

	private void saveDataComments() {
		asset.setComment("Es soll manchen Dichter geben, der muss dichten, um zu leben.Ist das immer so? Mitnichten,manche leben um zu dichten.");
	}

        @Override
	public AssetSuggCommand getAssetSuggestions() throws Exception {
		
		assetSugg.addDescription("001");
		assetSugg.addDescription("001");
		assetSugg.addDescription("002");
		assetSugg.addDescription("003");
		assetSugg.addDescription("004");

		assetSugg.addCategory("allo");
		assetSugg.addCategory("aallo");
		assetSugg.addCategory("ballo");
		assetSugg.addCategory("callo");

		assetSugg.addCpu("AMD");
		assetSugg.addCpu("ARM");
		assetSugg.addCpu("INTEL");
		assetSugg.addCpu("MOTOROLA");

		assetSugg.addAdditionalhardware("Laser-Canon");
		assetSugg.addAdditionalhardware("Magic-Door");
		assetSugg.addAdditionalhardware("Blackhole-Port");

		assetSugg.addAdmin("Super Mario");
		assetSugg.addAdmin("Medium Mario");
		assetSugg.addAdmin("Bad Mario");
		assetSugg.addAdmin("Pure Mario");
		
		assetSugg.addManufacturer("Atari");
		assetSugg.addManufacturer("Atari");
		assetSugg.addManufacturer("Bell-Labs");
		assetSugg.addManufacturer("Comodore");
		assetSugg.addManufacturer("Dell");

		assetSugg.addSnmpcommunity("public");
		assetSugg.addSnmpcommunity("not so public");
		assetSugg.addSnmpcommunity("private");
		
		return assetSugg;
	}
}
