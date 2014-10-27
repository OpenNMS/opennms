/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/*
 * Creato il 7-lug-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.web.element;

/**
 * <p>XmlEncoder class.</p>
 *
 * @author micmas
 *
 * Per modificare il modello associato al commento di questo tipo generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class XmlEncoder {
	/**
	 * <p>encode</p>
	 *
	 * @param str a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
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
