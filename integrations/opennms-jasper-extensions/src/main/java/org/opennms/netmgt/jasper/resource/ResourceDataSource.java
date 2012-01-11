package org.opennms.netmgt.jasper.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.opennms.netmgt.jasper.helper.ResourcePathFileTraversal;

public class ResourceDataSource implements JRDataSource {
    
    private class ResourceFilterFields{
        private String[] m_fields;
        
        public ResourceFilterFields(String[] fields) {
            m_fields = fields;
        }
        
        public String getValueForField(String fieldName, String curPath) {
            if(contains(fieldName)) {
                return getFilenameForField(fieldName, curPath);
            }else {
                return null;
            }
            
        }
        
        private String getFilenameForField(String dsName, String curPath) {
            File curDir = new File(curPath);
            FilenameFilter filter = new FilenameFilter() {

                public boolean accept(File file, String name) {
                    return name.matches("ds.properties");
                }
            };
            if(curDir.exists() && curDir.list(filter).length > 0) {
                return getFilenameFromDSfile(new File(curDir.getAbsoluteFile() + "" + File.separator + "ds.properties"), dsName);
            }else {
                return dsName + "" + getFileExtension();
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
            return filename + "" + getFileExtension();
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

        private boolean contains(String fieldName) {
            if(m_fields != null) {
                for(String fName : m_fields) {
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
      m_filterFields = new ResourceFilterFields(query.getFilters());
    }

    private void extractPaths(ResourceQuery query) {
        ResourcePathFileTraversal traverser = new ResourcePathFileTraversal(new File(query.constructBasePath()));
        traverser.addDatasourceFilters(query.getFilters());
        m_paths = traverser.traverseDirectory();
        System.err.println("paths: " + m_paths);
    }

    public Object getFieldValue(JRField field) throws JRException {
        return computeValueForField(field);
    }

    private Object computeValueForField(JRField field) {
        if(field.getName().equals("path")) {
            String pathField = m_paths.get(m_currentRow);
            System.err.println("path field:[" + pathField + "]");
            return m_paths.get(m_currentRow);
        }else {
            if(m_filterFields.contains(field.getName())) {
                return calculateFileName(field, m_paths.get(m_currentRow));
            }else {
                return null;
            }
            
        }
    }

    private String calculateFileName(JRField field, String absolutePath) {
        //TODO: check if there are dsName filters
        return absolutePath +  File.separator + m_filterFields.getValueForField(field.getName(), absolutePath);
    }

    public boolean next() throws JRException {
        m_currentRow++;
        return m_currentRow < m_paths.size();
    }

}
