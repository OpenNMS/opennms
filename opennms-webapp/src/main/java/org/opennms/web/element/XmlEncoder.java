//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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
 * @since 1.6.12
 */
public final class XmlEncoder {
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
