package org.opennms.netmgt.jasper.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

class FileTraversal{
    
    private final File m_file;
    private List<FilenameFilter> m_filterList = new ArrayList<FilenameFilter>();
    
    public FileTraversal(File f) {
        m_file = f;
    }
    
    public List<String> traverseDirectory() {
        List<String> paths = new ArrayList<String>();
        traverseDirectory(m_file, paths);
        return paths;
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

    public void addNameFilter(final String filterName) {
        m_filterList.add(new FilenameFilter() {
            
            public boolean accept(File dir, String name) {
                return name.contains(filterName);
            }
        });
        
    }

    public FileTraversal addAndNameFilter(final String nameToFilterOn) {
        addNameFilter(nameToFilterOn);
        return this;
    }
    
}