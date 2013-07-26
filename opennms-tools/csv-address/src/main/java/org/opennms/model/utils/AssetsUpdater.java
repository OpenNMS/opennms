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

package org.opennms.model.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;


public class AssetsUpdater {

    private static final String PROPERTY_CSV_FILE = "csv.file";
    private static final String PROPERTY_DB_SVR = "db.server";
    private static final String PROPERTY_DB_NAME = "db.name";
    private static final String PROPERTY_DB_USER = "db.user";
    private static final String PROPERTY_DB_PW = "db.password";
    private static final String PROPERTY_FOREIGN_SOURCE_FORMATTER = "foreign.source.formatter";
    private static final String PROPERTY_DB_QUERY ="db.query";
    private static final String PROPERTY_DB_QUERY_COLUMN = "db.query.column";
    
    protected static final String PROPERTY_FIELD_PREFIX = "field";
    
    private static File m_csvFile = new File("/tmp/addresses.csv");
    private static String m_dbSvr = "127.0.0.1";
    private static String m_dbName = "opennms";
    private static String m_dbUser = "opennms";
    private static String m_dbPass = "opennms";
    private static String m_foreignSourceFormatter = "org.opennms.model.utils.NullFormatter";
    
    private static String m_dbQuery = "SELECT * FROM assets WHERE nodeid = ?";
    private static int m_dbQueryColumn = 1;
    
	private static Map<Integer, String> m_fieldMap = new HashMap<Integer, String>();
	private static ForeignSourceFormatter m_formatter;

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
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
        	Properties props = System.getProperties();
        	validateProperties(props);
        } catch (IOException e) {
        	e.printStackTrace();
        	try {
				usageReport();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        	System.exit(1);
        } catch (IllegalArgumentException e) {
			e.printStackTrace();
        	System.exit(1);
		} catch (InstantiationException e) {
			e.printStackTrace();
        	System.exit(1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
        	System.exit(1);
		}
        
        try {
        	parseCsv2(m_csvFile);
//        	List<Address> addresses = parseCsv(m_csvFile);
//			updateAssetTable(addresses);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        System.out.println("Finished.");
    }
    
    protected static void parseCsv2(final File csv) throws ClassNotFoundException, SQLException, IOException {
	
	String sql = m_dbQuery;
	
	Connection con = createConnection(false);
	PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
	
	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)));
	CSVReader csvReader = new CSVReader(br);
		String[] line;
		int lineCnt = 0;
		while((line = csvReader.readNext()) != null) {
			
			System.out.println("Processing csv line: "+ String.valueOf(++lineCnt));
	
			if (line.length != m_fieldMap.size()+1) {
				continue;
			}
	
			String foreignSource = m_formatter.formatForeignSource(StringUtils.isBlank(line[0]) ? null : line[0]);
	
			System.out.println("Running query for foreignSource: "+foreignSource+" ...");
			ps.setString(1, foreignSource);
			ResultSet rs = ps.executeQuery();
			rs.last();
			int rows = rs.getRow();
			if (rows < 1) {
				rs.close();
				System.out.println("No results found for foreignsource: "+foreignSource+"; continuing to next foreignsource...");
				continue;
			}
			System.out.println("Found "+rows+" rows.");
	
			rs.beforeFirst();
	
			while (rs.next()) {
				System.out.println("Updating node: "+rs.getInt("nodeid"));
	
				Set<Entry<Integer, String>> entrySet = m_fieldMap.entrySet();
				for (Entry<Integer, String> entry : entrySet) {
					int csvField = entry.getKey()-1;
					String columnName = entry.getValue();
					System.out.println("\t"+"updating column: "+columnName+" with csv field: "+line[csvField]);
					rs.updateString(columnName, line[csvField]);
				}
				
				rs.updateRow();
			}
			rs.close();
		}
	
		try {
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			con.rollback();
		}
	
		ps.close();
		con.close();
	}

    private static Connection createConnection(boolean autoCommit) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + m_dbSvr + ":5432/"+m_dbName, m_dbUser, m_dbPass);
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    //need to do some better exception handling here
    protected static void validateProperties(Properties props) throws IOException, FileNotFoundException, IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException  {
    	
    	createCsvFileMappingFromProperties(props);
		
    	createCsvFilePointer();
    	
    	createDbConnectionSettingsFromProperties();
    	
    	m_dbQuery = props.getProperty(PROPERTY_DB_QUERY, m_dbQuery);
    	m_dbQueryColumn = Integer.valueOf(props.getProperty(PROPERTY_DB_QUERY_COLUMN, String.valueOf(m_dbQueryColumn)));
    	
    	m_foreignSourceFormatter = System.getProperty(PROPERTY_FOREIGN_SOURCE_FORMATTER, m_foreignSourceFormatter);
		Class<?> formatterClass = Class.forName(m_foreignSourceFormatter);
    	
    	m_formatter = (ForeignSourceFormatter) formatterClass.newInstance();

    }

	private static void createCsvFilePointer() throws IOException,
			FileNotFoundException {
		String csvFileName = System.getProperty(PROPERTY_CSV_FILE, m_csvFile.getCanonicalPath());
    	System.out.println("\t"+PROPERTY_CSV_FILE+":"+m_csvFile);

    	m_csvFile = new File(csvFileName);
    	if (!m_csvFile.exists()) {
    		throw new FileNotFoundException("CSV Input File: "+csvFileName+"; Not Found!");
    	}
	}

	private static void createDbConnectionSettingsFromProperties() {
		m_dbSvr = System.getProperty(PROPERTY_DB_SVR, m_dbSvr);
    	System.out.println("\t"+PROPERTY_DB_SVR+":"+m_dbSvr);

    	m_dbName = System.getProperty(PROPERTY_DB_NAME, m_dbName);
    	System.out.println("\t"+PROPERTY_DB_NAME+":"+m_dbName);

    	m_dbUser = System.getProperty(PROPERTY_DB_USER, m_dbUser);
    	System.out.println("\t"+PROPERTY_DB_USER+":"+m_dbUser);

    	m_dbPass = System.getProperty(PROPERTY_DB_PW, m_dbPass);
    	System.out.println("\t"+PROPERTY_DB_PW+":"+m_dbPass);
	}

	protected static void createCsvFileMappingFromProperties(Properties props) {
		Set<Object> keySet = props.keySet();
		String regex = "^"+PROPERTY_FIELD_PREFIX +"([0-9]+)$";
		Pattern pattern = Pattern.compile(regex);
		
		for (Object key : keySet) {
			
			Matcher m = pattern.matcher(key.toString());

			if (!m.matches() || m.groupCount() != 1) {
				continue;
			}
			
			Integer mapKey = Integer.valueOf(m.group(1));
			String mapValue = props.getProperty(key.toString());
			m_fieldMap.put(mapKey, mapValue);
		}

    	int mapEntries = m_fieldMap.size();
		System.out.println("found "+mapEntries+" field mapping properties");
		
		if (mapEntries == 0) {
			createDefaultMapping();
		}
		
		Set<Entry<Integer, String>> entrySet = m_fieldMap.entrySet();
		for (Entry<Integer, String> entry : entrySet) {
			Integer key = entry.getKey();
			String value = entry.getValue();
			System.out.println("csv field: "+key+" maps to asset column name: "+value);
		}

	}

	private static void createDefaultMapping() {
		System.out.println("Using default field settings...");
		m_fieldMap.put(2, "address1");
		m_fieldMap.put(3, "city");
		m_fieldMap.put(4, "state");
		m_fieldMap.put(5, "zip");
		m_fieldMap.put(6, "country");
		m_fieldMap.put(7, "department");
		m_fieldMap.put(8, "division");
		m_fieldMap.put(9, "region");
	}

    private static void usageReport() throws IOException {        
        System.err.println("Usage: java CsvRequistionParser [<Property>...]\n" +
                "\n" +
                "Supported Properties:\n" +
                "\t"+PROPERTY_CSV_FILE+": default:"+m_csvFile.getCanonicalPath()+"\n" +
                "\t"+PROPERTY_DB_SVR+": default:"+m_dbSvr+"\n" +
                "\t"+PROPERTY_DB_NAME+": default:"+m_dbName+"\n" +
                "\t"+PROPERTY_DB_USER+": default:"+m_dbUser+"\n" +
                "\t"+PROPERTY_DB_PW+": default:"+m_dbPass+"\n" +
                "\t"+PROPERTY_FOREIGN_SOURCE_FORMATTER+": default: "+m_foreignSourceFormatter+" \n" +
                "\t"+PROPERTY_FIELD_PREFIX+"[2...]: (field1 is required to be the \"foreign source\") with defaults: \n" +
                		"\t\tfield2=address1 \n" +
                		"\t\tfield3=city \n" +
                		"\t\tfield4=state \n" +
                		"\t\tfield5=state \n" +
                		"\t\tfield6=zip \n" +
                		"\t\tfield7=department \n" +
                		"\t\tfield8=division \n" +
                		"\t\tfield9=region\n" +
                "\n" +
                "\n" +
                "Example:\n" +
                "\t java -D"+PROPERTY_CSV_FILE+"=/tmp/mynodes.csv \\\n" +
                "\t\t-D"+PROPERTY_DB_SVR+"=localhost \\\n" +
                "\t\t-D"+PROPERTY_DB_NAME+"=opennms \\\n" +
                "\t\t-D"+PROPERTY_DB_USER+"=opennms \\\n" +
                "\t\t-D"+PROPERTY_DB_PW+"=opennms \\\n" +
                "\t\t-D"+PROPERTY_FOREIGN_SOURCE_FORMATTER+"=org.opennms.model.utils.StoreFormatter \\\n" +
                "\t\t-D"+PROPERTY_FIELD_PREFIX+"10=address2 \\\n" +
                "\t\t-jar opennms-csv-address-1.13.0-SNAPSHOT-jar-with-dependencies.jar" +
                "\n" +
                "\n" +
                "The default format fits the following sample:" +
                "\n\n" +
                "#ForeignSource,Address,City,State,Zip,Country,Department,Division,Region\n" +
                "HQ,220 Chatham Business Drive,Pittsboro,NC,27312,USA,RTP,Piedmont,SouthEast\n"
                );
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
