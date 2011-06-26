package org.opennms.netmgt.provision.detector.modbus.client;

import java.io.IOException;
import java.net.InetAddress;

import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;
import org.opennms.netmgt.provision.support.Client;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.msg.ModbusRequest;
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest;
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse;

public class ModBusClient implements
		Client<LineOrientedRequest, LineOrientedResponse> {

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void connect(InetAddress address, int port, int timeout)
			throws IOException, Exception {
		boolean modbusSupport;
		ModbusFactory m = new ModbusFactory();
		IpParameters ip = new IpParameters();
		ip.setHost(address.getHostAddress());
		ip.setPort(port);
		ModbusMaster mp = m.createTcpMaster(ip, false);
		// the detector will attempt to read any value from register 0
		int register = 0;
		// here I'm trying to read from the modbus port - should this go into LineOrientedResponse?
		try {
			mp.init();
			ModbusRequest req = new ReadInputRegistersRequest(1, register, 1);
			ReadInputRegistersResponse rep = (ReadInputRegistersResponse) mp
					.send(req);
			// answer from the modbus is a byte
			byte[] answer = rep.getData();
			// the byte array contains two values; one value (functionCode, array[0]) needs to be set to any value
			//TODO: add test if array[0] has any value (is there a generic variable test in Java?)
			if (true) {
				modbusSupport = true;
			}
		} catch (Exception ex) {
			// how will we handle exceptions? it can be assumed that upon any exception, the device does not support modbus
			// how about timeouts..?
			modbusSupport = false;
		}
		// if a value can be read, the device support modbus

	}

	@Override
	public LineOrientedResponse receiveBanner() throws IOException, Exception {
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public LineOrientedResponse sendRequest(LineOrientedRequest request)
			throws IOException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
