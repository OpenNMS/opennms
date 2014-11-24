/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

public class OidValues {
  String textOid = "DEFAULT";
  String numericOid = "DEFAULT";
  String typeId = "DEFAULT";
  boolean isInTable = false;
  boolean isIndexOfTable = false;
  String access = "not-accessible";
  public static String NOT_ACCESSIBLE = "not-accessible";
  public static String READ_ONLY = "read-only";

  public OidValues()
  {
  }

  public void setTextOid(String oid)
  {
    textOid = oid;
  }

  public String getTextOid()
  {
    return textOid;
  }

  public void setNumericOid(String oid)
  {
    numericOid = oid;
  }

  public String getNumericOid()
  {
    return numericOid;
  }

  public void setTypeId(String oid)
  {
    typeId = oid;
  }

  public String getTypeId()
  {
    return typeId;
  }

  public void setAccess(String a)
  {
    access = a;
  }

  public String getAccess()
  {
    return access;
  }

  public String toString()
  {
	return textOid + "," + numericOid + "," + typeId + "," + access;
  }
}
