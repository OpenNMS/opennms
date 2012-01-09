package org.opennms.netmgt.jasper.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class FileTraversal{
    
    private final File m_file;
    private List<FilenameFilter> m_filterList = new ArrayList<FilenameFilter>();
    
    public FileTraversal(File f) {
        m_file = f;
        if(!m_file.exists()) {
            System.err.println("Directory does not exist");
        }
    }
    
    public List<String> traverseDirectory() {
        List<String> paths = new ArrayList<String>();
        
        addTopLevelIfNecessary(paths);
        
        traverseDirectory(m_file, paths);
        return paths;
    }
    
    private void addTopLevelIfNecessary(List<String> paths) {
        File[] fList = m_file.listFiles();
        for(File f : fList) {
            if(f.isFile()) {
                onDirectory(m_file, paths);
                break;
            }
        }
        
        
    }

    private void traverseDirectory(File f, List<String> dirPaths) {
        if(f.isDirectory()) {
            
            final File[] children = f.listFiles();
            
            for(File child : children) {
                if(child.isDirectory()) {
                    onDirectory(child, dirPaths);
                    traverseDirectory(child, dirPaths);
                }
                
            }
            return;
        }
        
        onFile(f);
    }

    private void onFile(File f) {
        System.err.println(f.getName());
    }

    private void onDirectory(File f, List<String> dirPaths) {
        if(validateFiles(f)) {
            dirPaths.add(f.getAbsolutePath());
        }
    }

    private boolean validateFiles(final File f) {
        for(FilenameFilter filter : m_filterList) {
            String[] files = f.list(filter);
            if(files.length == 0) {
                return false;
            }
        }
        
        return true;
        
    }

    private void addFilenameFilter(final String filterName) {
        m_filterList.add(new FilenameFilter() {
            
            public boolean accept(File dir, String name) {
                return name.contains(filterName);
            }
        });
        
    }
    
    public void addFilenameFilters(String[] filenames) {
        if(filenames != null) {
            for(String filename : filenames) {
                addFilenameFilter(filename);
            }
        }
    }
    
    public FileTraversal addAndFilenameFilter(final String nameToFilterOn) {
        addFilenameFilter(nameToFilterOn);
        return this;
    }
    
}