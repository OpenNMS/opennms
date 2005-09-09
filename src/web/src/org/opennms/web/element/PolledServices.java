package org.opennms.web.element;

import java.util.ArrayList;
import java.io.IOException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.capsd.*;

import org.opennms.netmgt.config.DatabaseConnectionFactory;

public class PolledServices {
	private ArrayList managedServices = new ArrayList();
	private ArrayList polledServices = new ArrayList();

	CapsdConfiguration capsdConfig = null;
	CapsdConfigFactory capsdFactory = null;

	private static final String STMT =
		"SELECT servicename FROM (ifservices inner join service on ifservices.serviceid = service.serviceid) WHERE nodeid=? AND ipaddr LIKE ? AND status='A';";

	private PolledServices() {}

	public PolledServices(int nodeid)
		throws SQLException, IOException, MarshalException, ValidationException {
		init(nodeid, "%");
	}

	public PolledServices(int nodeid, String intf)
		throws SQLException, IOException, MarshalException, ValidationException {
		init(nodeid, intf);
	}

	private void init(int nodeid, String intf)
		throws SQLException, IOException, MarshalException, ValidationException {

		CapsdConfigFactory.init();
		capsdFactory = CapsdConfigFactory.getInstance();
		capsdConfig = capsdFactory.getConfiguration();

		ProtocolPlugin[] plugins = capsdConfig.getProtocolPlugin();
		int n = plugins.length;
		for (int i = 0; i < n; i++) {
			polledServices.add(plugins[i].getProtocol());
		}
		try {
			DatabaseConnectionFactory.init();
		}
		catch (Exception e) {}
		Connection connection = null;
		try {
			connection = DatabaseConnectionFactory.getInstance().getConnection();
		}
		catch (SQLException e) {
			throw e;
		}
		try {
			PreparedStatement servicesSelect = connection.prepareStatement(STMT);
			servicesSelect.setInt(1, nodeid);
			servicesSelect.setString(2, intf);

			ResultSet servicesSet = servicesSelect.executeQuery();
			String str;
			while (servicesSet.next()) {
				str = servicesSet.getString(1);
				managedServices.add(str);
			}
		}
		catch (SQLException e) {
			throw e;
		}
		finally {
			try {
				connection.close();
			}
			catch (SQLException e) {}
		}
	}

	public boolean isManaged(String key) {
		return managedServices.contains(key);
	}

	public boolean isPolled(String key) {
		return polledServices.contains(key);
	}

	// only for debug
	public String toString() {
		String rv = "";
		int n = managedServices.size();
		rv = "size: " + n + "\r\n";
		for (int i = 0; i < n; i++) {
			rv += managedServices.get(i) + "\r\n";
		}

		return rv;
	}
}
