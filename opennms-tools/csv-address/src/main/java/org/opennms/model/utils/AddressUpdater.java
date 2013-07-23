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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;


public class AddressUpdater {

    private static final String PROPERTY_CSV_FILE = "csv.file";
    private static final String PROPERTY_DB_SVR = "db.server";
    private static final String PROPERTY_DB_NAME = "db.name";
    private static final String PROPERTY_DB_USER = "db.user";
    private static final String PROPERTY_DB_PW = "db.password";
    private static final String PROPERTY_FOREIGN_SOURCE = "foreign.source";
    
    protected static final String PROPERTY_FIELD_PREFIX = "field";
    
    private static File m_csvFile = new File("/tmp/addresses.csv");
    private static String m_dbSvr = "127.0.0.1";
    private static String m_dbName = "opennms";
    private static String m_dbUser = "opennms";
    private static String m_dbPass = "opennms";
    private static String m_foreignSource = null;
    
	private static int _csvFields = 9;
	private static Map<Integer, String> m_fieldMap = new HashMap<Integer, String>();

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
        }
        
        try {
        	List<Address> addresses = parseCsv(m_csvFile);
			updateAssetTable(addresses);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        System.out.println("Finished.");
    }
    
    private static void updateAssetTable(List<Address> addresses) throws ClassNotFoundException, SQLException {
    	
    	String sql = "" +
    			"SELECT a.id AS \"id\", " +
    			"       a.nodeid AS \"nodeid\", " +
    			"       a.address1 AS \"address1\", " +
    			"       a.city AS \"city\", " +
    			"       a.state AS \"state\", " +
    			"       a.zip AS \"zip\", " +
    			"       a.country AS \"country\", " +
    			"       a.department AS \"department\", " +
    			"       a.division AS \"division\", " +
    			"       a.region AS \"region\" " +
    			"  FROM assets a " +
    			"  JOIN node n on n.nodeid = a.nodeid " +
    			" WHERE n.foreignsource = ?";
    	
    	Connection con = createConnection();
    	con.setAutoCommit(false);
    	PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	
    	for (Address address : addresses) {
			String foreignSource = "Store" + StringUtils.leftPad(address.getForeignSource(), 4, '0');
			ps.setString(1, foreignSource);
			
			ResultSet rs = ps.executeQuery();
			
			rs.last();
			int rows = rs.getRow();
			if (rows < 1) {
				rs.close();
				System.out.println("No results found for foreignsource: "+foreignSource+"; continuing to next foreignsource...");
				continue;
			}
			
			rs.beforeFirst();
			
			while (rs.next()) {
				System.out.println("Updating node: "+rs.getInt("nodeid"));
				rs.updateString("address1", address.getAddress());
				rs.updateString("city", address.getCity());
				rs.updateString("state", address.getState());
				rs.updateString("zip", address.getZip());
				rs.updateString("country", address.getCountry());
				rs.updateString("department", address.getDepartment());
				rs.updateString("division", address.getDivision());
				rs.updateString("region", address.getRegion());
				rs.updateRow();
			}
			rs.close();
		}
    	
    	ps.close();
    	try {
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			con.rollback();
		}
    	con.close();
	}

    protected static List<Address> parseCsv2(final File csv) throws FileNotFoundException, ClassNotFoundException, SQLException {
    	
    	String sql = "" +
    			"SELECT a.* " +
    			"  FROM assets a " +
    			"  JOIN node n on n.nodeid = a.nodeid " +
    			" WHERE n.foreignsource = ?";

    	Connection con = createConnection();
    	PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	
	    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)));
    	CSVReader csvReader = new CSVReader(br);
    	try {
			String[] line;
			while((line = csvReader.readNext()) != null) {
				
				if (line.length != m_fieldMap.size()+1) {
					continue;
				}
				
				String foreignSource;
				if (m_foreignSource != null) {
					foreignSource = m_foreignSource;
				} else {
					foreignSource = line[0];
				}
				
				if (foreignSource == null) {
					continue;
				}
				
				ps.setString(1, foreignSource);
				ResultSet rs = ps.executeQuery();
				rs.last();
				int rows = rs.getRow();
				if (rows < 1) {
					rs.close();
					System.out.println("No results found for foreignsource: "+foreignSource+"; continuing to next foreignsource...");
					continue;
				}

				rs.beforeFirst();

				while (rs.next()) {
					System.out.println("Updating node: "+rs.getInt("nodeid"));

					Set<Entry<Integer, String>> entrySet = m_fieldMap.entrySet();
					for (Entry<Integer, String> entry : entrySet) {
						rs.updateString(entry.getValue(), line[entry.getKey()]);
					}
					rs.updateRow();
				}
				rs.close();
		    	
		    	try {
					con.commit();
				} catch (SQLException e) {
					e.printStackTrace();
					con.rollback();
				}
		    	
		    	ps.close();
		    	con.close();
					
				}
				
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return null;
    }
    
	/**
	 * Parses the addresses defined in a csv file to an <code>Address</code>
	 * @param csv
	 * @param m_repo
	 * @return The number of lines successfully parsed into an <code>Address</code> from the csv input file.
	 * @throws IOException
	 */
	protected static List<Address> parseCsv(final File csv) throws IOException {
	    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csv)));
	    List<Address> addresses = new LinkedList<Address>();
	    
	    String line = null;
	    int lineNum = 0;
	    while ((line = br.readLine()) != null) {
	        lineNum++;
	        if (line != null && line.startsWith("#")) {
	            continue;
	        }
	        
	        String[] fields = line.split(",", _csvFields);
	        int fieldCount = fields.length;
	        if (fieldCount != _csvFields) {
	            System.err.println("Error on line: "+Integer.toString(lineNum)+". Found "+Integer.toString(fieldCount)+" fields and expected: "+_csvFields+".");
	            continue;
	        }
	                    
	        Address address = new Address(fields);
	        System.out.println("Line "+Integer.toString(lineNum)+":"+address.toString());
	        
	        addresses.add(address);
	        
	    }
	    br.close();
	    return addresses;
	}


    private static Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://" + m_dbSvr + ":5432/"+m_dbName, m_dbUser, m_dbPass);
        return connection;
    }

    //need to do some better exception handling here
    protected static void validateProperties(Properties props) throws IOException, FileNotFoundException, IllegalArgumentException  {
    	
    	createCsvFileMappingFromProperties(props);
		
    	createCsvFilePointer();
    	
    	createDbConnectionSettingsFromProperties();
    	
    	m_foreignSource = System.getProperty(PROPERTY_FOREIGN_SOURCE);

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

	private static void createCsvFileMappingFromProperties(Properties props) {
		Set<Object> keySet = props.keySet();
		String regex = "^"+PROPERTY_FIELD_PREFIX +"([0-9]+)$";
		Pattern pattern = Pattern.compile(regex);
		
		for (Object key : keySet) {
			
			Matcher m = pattern.matcher(key.toString());

			if (!m.matches() || m.groupCount() != 1) {
				continue;
			}
			
			m_fieldMap.put(Integer.valueOf(m.group(1)), props.getProperty(key.toString()));
		}

    	int mapEntries = m_fieldMap.size();
		System.out.println("found "+mapEntries+" field mapping properties");
		
		if (mapEntries == 0) {
			createDefaultMapping();
		}
		
		for (int i=0; i < mapEntries; i++) {
			System.out.println("Mapping property: field"+i+"="+m_fieldMap.get(i)+" maps to csv field "+i+" to column name "+m_fieldMap.get(i));
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
                "\t"+PROPERTY_FOREIGN_SOURCE+": default: null\n" +
                "\t"+PROPERTY_FIELD_PREFIX+"[1...]: default: \n" +
                		"\t\tfield2=address1" +
                		"\t\tfield3=city" +
                		"\t\tfield4=state" +
                		"\t\tfield5=state" +
                		"\t\tfield6=zip" +
                		"\t\tfield7=department" +
                		"\t\tfield8=division" +
                		"\t\tfield9=region\n" +
                "\n" +
                "\n" +
                "Example:\n" +
                "\t java -D"+PROPERTY_CSV_FILE+"=/tmp/mynodes.csv \\\n" +
                "\t\t-D"+PROPERTY_DB_SVR+"=localhost \\\n" +
                "\t\t-D"+PROPERTY_DB_NAME+"=opennms \\\n" +
                "\t\t-D"+PROPERTY_DB_USER+"=opennms \\\n" +
                "\t\t-D"+PROPERTY_DB_PW+"=opennms \\\n" +
                "\t\t-D"+PROPERTY_FOREIGN_SOURCE+"=Store0001 \\\n" +
                "\t\t-D"+PROPERTY_FIELD_PREFIX+"10=address2 \\\n" +
                "\t\t-jar opennms-csv-address-1.13.0-SNAPSHOT-jar-with-dependencies.jar" +
                "\n" +
                "\n" +
                "FYI: This application expects the csv file to have 9 fields: node.foreignsource,address1,city,state.zip,country,department,division,region.  Example:" +
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
