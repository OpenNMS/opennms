//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
package org.opennms.web.asset;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.MissingParameterException;
import org.opennms.web.asset.*;


public class ModifyAssetServlet extends HttpServlet
{
    protected AssetModel model;


    public void init() throws ServletException {
        this.model = new AssetModel();
    }
  

    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String nodeIdString = request.getParameter( "node" );
        String isNewString = request.getParameter( "isnew" );
        
        if( nodeIdString == null ) {
          throw new MissingParameterException( "node", new String[] { "node", "isnew" } ); 
        }
        
        if( isNewString == null ) {
          throw new MissingParameterException( "isnew", new String[] { "node", "isnew" } ); 
        }
        
        int nodeId = Integer.parseInt( nodeIdString );
        boolean isNew = Boolean.valueOf( isNewString ).booleanValue();

        Asset asset = this.parms2Asset( request, nodeId );

        try {        
            if( isNew ) {
              this.model.createAsset( asset );
            }
            else {
              this.model.modifyAsset( asset );
            }
            
            response.sendRedirect( "modify.jsp?node=" + nodeId );
        }
        catch( SQLException e ) {
            throw new ServletException( "database error", e );
        }
    }


    protected Asset parms2Asset( HttpServletRequest request, int nodeId ) {
        Asset asset = new Asset();
        
        asset.setNodeId(nodeId);
        asset.setCategory(this.stripBadCharacters(request.getParameter("category")));
        asset.setManufacturer(this.stripBadCharacters(request.getParameter("manufacturer")));
        asset.setVendor(this.stripBadCharacters(request.getParameter("vendor")));
        asset.setModelNumber(this.stripBadCharacters(request.getParameter("modelnumber")));
        asset.setSerialNumber(this.stripBadCharacters(request.getParameter("serialnumber")));
        asset.setDescription(this.stripBadCharacters(request.getParameter("description")));
        asset.setCircuitId(this.stripBadCharacters(request.getParameter("circuitid")));
        asset.setAssetNumber(this.stripBadCharacters(request.getParameter("assetnumber")));
        asset.setOperatingSystem(this.stripBadCharacters(request.getParameter("operatingsystem")));
        asset.setRack(this.stripBadCharacters(request.getParameter("rack")));
        asset.setSlot(this.stripBadCharacters(request.getParameter("slot")));
        asset.setPort(this.stripBadCharacters(request.getParameter("port")));
        asset.setRegion(this.stripBadCharacters(request.getParameter("region")));
        asset.setDivision(this.stripBadCharacters(request.getParameter("division")));
        asset.setDepartment(this.stripBadCharacters(request.getParameter("department")));
        asset.setAddress1(this.stripBadCharacters(request.getParameter("address1")));
        asset.setAddress2(this.stripBadCharacters(request.getParameter("address2")));
        asset.setCity(this.stripBadCharacters(request.getParameter("city")));
        asset.setState(this.stripBadCharacters(request.getParameter("state")));
        asset.setZip(this.stripBadCharacters(request.getParameter("zip")));
        asset.setBuilding(this.stripBadCharacters(request.getParameter("building")));
        asset.setFloor(this.stripBadCharacters(request.getParameter("floor")));
        asset.setRoom(this.stripBadCharacters(request.getParameter("room")));
        asset.setVendorPhone(this.stripBadCharacters(request.getParameter("vendorphone")));
        asset.setVendorFax(this.stripBadCharacters(request.getParameter("vendorfax")));
        asset.setDateInstalled(this.stripBadCharacters(request.getParameter("dateinstalled")));
        asset.setLease(this.stripBadCharacters(request.getParameter("lease")));
        asset.setLeaseExpires(this.stripBadCharacters(request.getParameter("leaseexpires")));
        asset.setSupportPhone(this.stripBadCharacters(request.getParameter("supportphone")));
        asset.setMaintContract(this.stripBadCharacters(request.getParameter("maintcontract")));
        asset.setVendorAssetNumber(this.stripBadCharacters(request.getParameter("vendorassetnumber")));
        asset.setMaintContractExpires(this.stripBadCharacters(request.getParameter("maintcontractexpires")));
        asset.setSupportPhone(this.stripBadCharacters(request.getParameter("supportphone")));
        asset.setComments(this.stripBadCharacters(request.getParameter("comments")));
        
        asset.setUserLastModified(request.getRemoteUser());
        asset.setLastModifiedDate(new Date());
        
        return( asset );
    }


    public String stripBadCharacters( String s ) {
	if( s != null ) {
	    s = s.replace( '\n', ' ' );
            s = s.replace( '\f', ' ' );
            s = s.replace( '\r', ' ' );
	    s = s.replace( ',', ' ' );
	}

	return( s );
    }        
}            

