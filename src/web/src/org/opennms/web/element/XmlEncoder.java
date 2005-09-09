/*
 * Creato il 7-lug-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.element;

/**
 * @author micmas
 *
 * Per modificare il modello associato al commento di questo tipo generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
public final class XmlEncoder {
	public static String encode(String str)
	{
		char[] specialChar = {'&', '<', '>', '"', '\''};
		
		int n = specialChar.length;
		for (int i = 0; i < n; i++)
		{
			char a = specialChar[i];
			String aa = Integer.toHexString((int)a);
			String substitute = "&#x" + aa + ";"; 
			str = str.replaceAll(String.valueOf(a), substitute);
		}
		
		return str;
	}
}
