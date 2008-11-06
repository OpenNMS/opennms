// This file is part of the OpenNMS(R) MIB Parser.
//
// Copyright (C) 2002-2003 John Rodriguez
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// See: http://www.fsf.org/copyleft/lesser.html
//

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
