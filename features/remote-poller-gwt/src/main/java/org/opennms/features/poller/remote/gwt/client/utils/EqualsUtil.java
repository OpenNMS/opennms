package org.opennms.features.poller.remote.gwt.client.utils;

/**
 * <p>EqualsUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class EqualsUtil {
	/**
	 * <p>areEqual</p>
	 *
	 * @param aThis a boolean.
	 * @param aThat a boolean.
	 * @return a boolean.
	 */
	static public boolean areEqual(boolean aThis, boolean aThat){
	    return aThis == aThat;
	  }

	  /**
	   * <p>areEqual</p>
	   *
	   * @param aThis a char.
	   * @param aThat a char.
	   * @return a boolean.
	   */
	  static public boolean areEqual(char aThis, char aThat){
	    return aThis == aThat;
	  }

	  /**
	   * <p>areEqual</p>
	   *
	   * @param aThis a long.
	   * @param aThat a long.
	   * @return a boolean.
	   */
	  static public boolean areEqual(long aThis, long aThat){
	    /*
	    * Implementation Note
	    * Note that byte, short, and int are handled by this method, through
	    * implicit conversion.
	    */
	    return aThis == aThat;
	  }

	  /**
	   * <p>areEqual</p>
	   *
	   * @param aThis a float.
	   * @param aThat a float.
	   * @return a boolean.
	   */
	  static public boolean areEqual(float aThis, float aThat){
		  return Float.valueOf(aThis) == Float.valueOf(aThat);
	  }

	  /**
	   * <p>areEqual</p>
	   *
	   * @param aThis a double.
	   * @param aThat a double.
	   * @return a boolean.
	   */
	  static public boolean areEqual(double aThis, double aThat){
		  return Double.valueOf(aThis) == Double.valueOf(aThat);
	  }

	  /**
	   * Possibly-null object field.
	   *
	   * Includes type-safe enumerations and collections, but does not include
	   * arrays. See class comment.
	   *
	   * @param aThis a {@link java.lang.Object} object.
	   * @param aThat a {@link java.lang.Object} object.
	   * @return a boolean.
	   */
	  static public boolean areEqual(Object aThis, Object aThat){
	    return aThis == null ? aThat == null : aThis.equals(aThat);
	  }

}
