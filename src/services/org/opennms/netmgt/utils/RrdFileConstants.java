package org.opennms.netmgt.utils;

import java.io.*;

/**
 * A convenience class containing RRD file and directory
 * related constants.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski</a>
 */ 
public class RrdFileConstants extends Object
{

	/** The longest an RRD filename can be, currently 1024 characters. */
	public static final int MAX_RRD_FILENAME_LENGTH = 1024;
	
	/** Suffix common to all RRD filenames. */
	public static final String RRD_SUFFIX = ".rrd";
	
	/** Convenience filter that matches only RRD files. */
	public static final FilenameFilter RRD_FILENAME_FILTER = new FilenameFilter() {
		public boolean accept(File file, String name) {
			return name.endsWith(RRD_SUFFIX);
        	}
	}; 
	
	/** Convenience filter that matches directories with RRD files in them. */
	public static final FileFilter INTERFACE_DIRECTORY_FILTER = new FileFilter() {
		public boolean accept(File file) {
                    return isValidRRDInterfaceDir(file);
		}
	};    
	
	
	/** 
	* Convenience filter that matches integer-named directories that either 
	* contain RRD files or directories that contain RRD files.
	*/
	public static final FileFilter NODE_DIRECTORY_FILTER = new FileFilter() {
		public boolean accept(File file) {
		    return isValidRRDNodeDir(file);
		}
	};    
	
	public static final boolean isValidRRDNodeDir(File file) {
	    if(!file.isDirectory()) {
		return false;              
	    }
	    
	    try {
		//if the directory name is an integer
		Integer.parseInt(file.getName());
	    }
	    catch (Exception e) {
		return false;
	    }
		
	    //if the node dir contains RRDs, then it is queryable
	    File[] nodeRRDs = file.listFiles(RRD_FILENAME_FILTER);
	    if(nodeRRDs != null && nodeRRDs.length > 0) {
		return true;
	    }
			      
	    //if the node dir contains queryable interface directories, then
	    //it is queryable                              
	    File[] intfDirs = file.listFiles(INTERFACE_DIRECTORY_FILTER);            
	    if(intfDirs != null && intfDirs.length > 0) {
		return true;
	    }
	    
	    return false;
	}

	public static final boolean isValidRRDInterfaceDir(File file) {
	    if(!file.isDirectory()) {
		return false;              
	    }
				
	    File[] intfRRDs = file.listFiles(RRD_FILENAME_FILTER);
		
	    if(intfRRDs != null && intfRRDs.length > 0) {
		return true;
	    }
		
	    return false;
	}
	    
	/**
	 * Checks an RRD filename to make sure it is of the proper length
	 * and does not contain any unexpected charaters.  
	 * 
	 * <p>The maximum length is specified by the 
	 * {@link #MAX_RRD_FILENAME_LENGTH MAX_RRD_FILENAME_LENGTH} constant.  
	 * The only valid characters are letters (A-Z and a-z), numbers (0-9), 
	 * dashes (-), dots (.), and underscores (_).  These precautions are 
	 * necessary since the RRD filename is used on the commandline and 
	 * specified in the graph URL.</p>
	 */
	public static boolean isValidRrdName(String rrd) {
		if( rrd == null ) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}
	
		int length = rrd.length(); 
	
		if( length > MAX_RRD_FILENAME_LENGTH ) {
			return false;
		}
	
		//cannot contain references to higher directories for security's sake
		if(rrd.indexOf("..") >= 0) {
			return false;
		}
	
		for( int i=0; i < length; i++ ) {
			char c = rrd.charAt(i);
		
			if( !(('A' <= c && c <= 'Z') || 
				  ('a' <= c && c <= 'z') || 
				  ('0' <= c && c <= '9') || 
				  (c == '_') || 
				  (c =='.') || 
				  (c =='-') ||
				  (c == '/')) 
			   ) {
				return false;
			}
		}            
	
		return true;
	}
	

	/**
	 * Checks an RRD filename to make sure it is of the proper length
	 * and does not contain any unexpected charaters.  
	 * 
	 * <p>The maximum length is specified by the 
	 * {@link #MAX_RRD_FILENAME_LENGTH MAX_RRD_FILENAME_LENGTH} constant.  
	 * The only valid characters are letters (A-Z and a-z), numbers (0-9), 
	 * dashes (-), dots (.), and underscores (_).  These precautions are 
	 * necessary since the RRD filename is used on the commandline and 
	 * specified in the graph URL.</p>
	 *
	 * @deprecated This method name does not use the correct Java naming 
	 * convention.  Please use {@link #isValidRrdName() isValidRrdName} instead.
	 */
	public static boolean isValidRRDName(String rrd) {
		if( rrd == null ) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}
	
		int length = rrd.length(); 
	
		if( length > MAX_RRD_FILENAME_LENGTH ) {
			return false;
		}
	
		//cannot contain references to higher directories for security's sake
		if(rrd.indexOf("..") >= 0) {
			return false;
		}
	
		for( int i=0; i < length; i++ ) {
			char c = rrd.charAt(i);
		
			if( !(('A' <= c && c <= 'Z') || 
				  ('a' <= c && c <= 'z') || 
				  ('0' <= c && c <= '9') || 
				  (c == '_') || 
				  (c =='.') || 
				  (c =='-') ||
				  (c == '/')) 
			   ) {
				return false;
			}
		}            
	
		return true;
	}
	
	/** 
	 * Note this method will <strong>not</strong> handle references to
	 * higher directories ("..").
	 */
	public static String convertToValidRrdName(String rrd) {
		if(rrd == null) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}
		
		StringBuffer buffer = new StringBuffer(rrd);
		
		//truncate after the max length 
		if(rrd.length() > MAX_RRD_FILENAME_LENGTH) {
			buffer.setLength(MAX_RRD_FILENAME_LENGTH-1);
		}
		
		int length = buffer.length();
		
		for( int i=0; i < length; i++ ) {
			char c = buffer.charAt(i);
		
			if( !(('A' <= c && c <= 'Z') || 
				  ('a' <= c && c <= 'z') || 
				  ('0' <= c && c <= '9') || 
				  (c == '_') || 
				  (c =='.') || 
				  (c =='-') ||
				  (c == '/')) 
			   ) {
				buffer.setCharAt(i, '_');
			}
		}            
		
		return buffer.toString();
	}
}
