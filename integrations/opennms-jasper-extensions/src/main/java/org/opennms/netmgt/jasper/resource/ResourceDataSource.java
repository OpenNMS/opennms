/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.opennms.netmgt.jasper.helper.ResourcePathFileTraversal;

public class ResourceDataSource implements JRDataSource {
    
    private class ResourceFilterFields{
        private String[] m_fields;
        private String[] m_strProps;
        
        public ResourceFilterFields(String[] fields, String[] strProps) {
            m_fields = fields;
            m_strProps = strProps;
        }
        
        public String getValueForField(String fieldName, String curPath) {
            if(contains(fieldName, m_fields)) {
                return getFilenameForField(fieldName, curPath);
            }if(contains(fieldName, m_strProps)){
                return getStringsPropertyValue(fieldName, curPath);
            }else {
                return null;
            }
            
        }
        
        private String getStringsPropertyValue(String fieldName, String curPath) {
            File curDir = new File(curPath);
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File f, String name) {
                    return name.matches("strings.properties");
                }
                
            };
            
            File[] strFiles = curDir.listFiles(filter);
            if(curDir.exists() && strFiles.length == 1) {
                File strPropFile = strFiles[0];
                Properties props = new Properties();
                try {
                    FileInputStream fis = new FileInputStream(strPropFile);
                    props.load(fis);
                    return props.getProperty(fieldName);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            
            return null;
        }

        private String getFilenameForField(String dsName, String curPath) {
            File curDir = new File(curPath);
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File file, String name) {
                    return name.matches("ds.properties");
                }
            };
            if(curDir.exists() && curDir.list(filter).length > 0) {
                return getFilenameFromDSfile(new File(curDir.getAbsoluteFile() + "" + File.separator + "ds.properties"), dsName);
            }else {
                return curDir.getAbsolutePath() + File.separator + dsName + getFileExtension();
            }
            
        }

        private String getFilenameFromDSfile(File file, String dsName) {
            Properties props = new Properties();
            String filename = "";
            try {
                FileInputStream fis = new FileInputStream(file);
                props.load(fis);
                filename = props.getProperty(dsName);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return file.getParent() + File.separator + filename + "" + getFileExtension();
        }

        private String getFileExtension() {
            String jniStrategy = System.getProperty("org.opennms.rrd.strategyClass");
            String rrdFileExtension = System.getProperty("org.opennms.rrd.fileExtension");
            
            if(jniStrategy != null && jniStrategy.contains("JniStrategy")) {
                if(rrdFileExtension != null) {
                    return rrdFileExtension;
                }else {
                    return ".rrd";
                }
                
            }else {
                return ".jrb";
            }
            
        }
        
        public boolean containsField(String fieldName) {
            return (contains(fieldName, m_fields) || contains(fieldName, m_strProps));
        }
        
        private boolean contains(String fieldName, String[] array) {
            if(array != null) {
                for(String fName : array) {
                    if(fName.equals(fieldName)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    private int m_currentRow = -1;
    private List<String> m_paths;
    private ResourceFilterFields m_filterFields;
    
    public ResourceDataSource(ResourceQuery query) {
      extractPaths(query);
      m_filterFields = new ResourceFilterFields(query.getFilters(), query.getStringProperties());
    }

    private void extractPaths(ResourceQuery query) {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(query.constructBasePath()));
        traverser.addDatasourceFilters(query.getFilters());
        m_paths = traverser.traverseDirectory();
        Collections.sort(m_paths);
        System.err.println("paths: " + m_paths);
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        return computeValueForField(field);
    }

    private Object computeValueForField(JRField field) {
        if(field.getName().toLowerCase().equals("path")) {
            String pathField = m_paths.get(m_currentRow);
            System.err.println("path field:[" + pathField + "]");
            return m_paths.get(m_currentRow);
        }else {
            if(m_filterFields.containsField(field.getName())) {
                return calculateFieldValue(field, m_paths.get(m_currentRow));
            }else {
                return null;
            }
            
        }
    }

    private String calculateFieldValue(JRField field, String absolutePath) {
        //TODO: check if there are dsName filters
        return m_filterFields.getValueForField(field.getName(), absolutePath);
    }

    @Override
    public boolean next() throws JRException {
        m_currentRow++;
        return m_currentRow < m_paths.size();
    }

}
