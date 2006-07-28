/*
 * Created on 2-lug-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.web.element;

/**
 * @author antonio
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Converter {
 
 public static double highToLow(double value, int multiplicator){
	return value*multiplicator;
 }
 
 public static  double lowToHigh(double value, int divisor){
	
	return value/divisor;
  }

}
