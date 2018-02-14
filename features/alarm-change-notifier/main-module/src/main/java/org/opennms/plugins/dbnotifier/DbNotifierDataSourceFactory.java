/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.dbnotifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impossibl.postgres.jdbc.PGDataSource;


/**
 * This reads the OpenNMS database configuration from the 
 * OpenNMS database configuration file opennms-datasources.xml
 * and uses it to configure the jdbc-ng connector with 
 * userName, passWord, dataBaseName, hostname, port;
 * 
 * The opennms-datasources.xml location should be set in the dataSourceFileUri parameter. 
 * If it is not set then the factory tries to load the file from the class path.
 * 
 * We use DOM parsing to read the datasource-configuration 
 * since the JAXB XML classes are not in the Karaf classpath
 * 
 <datasource-configuration>
  <jdbc-data-source name="opennms" 
        database-name="opennms" 
        class-name="org.postgresql.Driver" 
        url="jdbc:postgresql://localhost:5432/opennms"
        user-name="opennms"
        password="opennms" />
  </datasource-configuration>
 * 
 * If the databaseName attribute is set then the factory simply uses the relevant
 * field values rather than loading from opennms-datasources.xml
 */
public class DbNotifierDataSourceFactory {
	private static final Logger LOG = LoggerFactory.getLogger(DbNotifierDataSourceFactory.class);

	private static final String OPENNMS_DATASOURCE_CONFIG_FILE_NAME="opennms-datasources.xml";

	private static final String OPENNMS_DATA_SOURCE_NAME = "opennms";

	private String userName  = null;
	private String passWord  = null;
	private String dataBaseName  = null;
	private String hostname=null;
	private int port=5432;
	private String dsfileUri=null;


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getDataBaseName() {
		return dataBaseName;
	}

	public void setDataBaseName(String dataBaseName) {
		this.dataBaseName = dataBaseName;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPort() {
		return Integer.toString(port);
	}

	public void setPort(String port) {
		this.port = Integer.parseInt(port);
	}

	/**
	 * @return the dsfileUri
	 */
	public String getDataSourceFileUri() {
		return dsfileUri;
	}

	/**
	 * @param dsfileUri the dsfileUri to set
	 */
	public void setDataSourceFileUri(String fileUri) {
		this.dsfileUri = fileUri;
	}

	public PGDataSource getPGDataSource(){

		PGDataSource pgdc=new PGDataSource();
		pgdc.setHost(hostname);
		pgdc.setPort(port);
		pgdc.setDatabase(dataBaseName);
		pgdc.setUser(userName);
		pgdc.setPassword(passWord);

		return pgdc;

	}


	/**
	 *  This method initialises the class by loading the database values
	 *  either from the config .cfg file or from opennms-datasources.xml
	 */
	public void init(){

		if(dataBaseName!=null){
			LOG.debug("using values supplied in .cfg file for host: "+hostname
					+ " port: "+port
					+ " dataBaseName "+ dataBaseName+" userName: "+userName+ "  password :" + passWord);
		} else {

			try {

				// try loading from file or from classpath
				InputStream istream=null;
				if(dsfileUri!=null){
					File dsFile=new File(dsfileUri);
					LOG.debug("loading database config from "+dsFile.getAbsolutePath());
					istream= new FileInputStream(dsFile);
				}

				if(istream==null){	
					LOG.debug("loading database config from classpath file"+OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
					istream= this.getClass().getClassLoader().getResourceAsStream(OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
					if(istream==null) throw new RuntimeException("could not load database config from classpath "+OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
				}

				DataSourceConfiguration dsconfig;
				try (Reader reader = new InputStreamReader(istream)) {
				    dsconfig = JaxbUtils.unmarshal(DataSourceConfiguration.class, reader);
				}
				JdbcDataSource[] datasourceArray = dsconfig.getJdbcDataSource();

				String dataSourceName=null;
				JdbcDataSource ds=null;
				int index = 0;
				
				while( ( ! OPENNMS_DATA_SOURCE_NAME.equals(dataSourceName) ) && (index<datasourceArray.length) ){
					ds=datasourceArray[index];
					dataSourceName= ds.getName();
					index++;
				}
				
				if ( ! OPENNMS_DATA_SOURCE_NAME.equals(dataSourceName)){
					throw new RuntimeException("no datasource "+OPENNMS_DATA_SOURCE_NAME
							+ " found in "+OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
				}
				
				userName = ds.getUserName();
				passWord = ds.getPassword();
				dataBaseName = ds.getDatabaseName();
				
				String urlStr = ds.getUrl();

				// parse out the hostname and port by removing jdbc:postgresql:
				String baseUrl=	urlStr.replace("jdbc:postgresql:","http:");

				URL url= new URL(baseUrl);
				String protocol = url.getProtocol();
				hostname = url.getHost();
				port = url.getPort();

				LOG.debug("decoded urlStr:"+urlStr +""
						+ " baseUrl:"+baseUrl
						+ " protocol:"+protocol
						+ " host:"+hostname
						+ " port:"+port);

				LOG.debug("Using jdbc-data-source values supplied for "+OPENNMS_DATA_SOURCE_NAME
						+ " datasource in "
						+ OPENNMS_DATASOURCE_CONFIG_FILE_NAME
						+ " file for host: "+hostname
						+ " port: "+port
						+ " dataBaseName "+ dataBaseName+" userName: "+userName+ "  password :" + passWord);

			} catch (Exception e) {
				throw new RuntimeException("cannot load database values from .cfg or "+OPENNMS_DATASOURCE_CONFIG_FILE_NAME, e);
			}
		}
	}

}


