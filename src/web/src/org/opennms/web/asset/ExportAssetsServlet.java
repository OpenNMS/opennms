package org.opennms.web.asset;

import java.io.*;
import java.sql.SQLException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.web.asset.*;
import org.opennms.web.element.NetworkElementFactory;


/**
 * Exports the assets database to a comma-seperated values text file.
 */
public class ExportAssetsServlet extends HttpServlet
{

    protected AssetModel model;


    public void init() throws ServletException {
        this.model = new AssetModel();
    }


    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        Asset[] assets = null;

        try {
            assets = this.model.getAllAssets();
        }
        catch( SQLException e ) {
            throw new ServletException( "Database exception", e );
        }
        
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        StringBuffer buffer = new StringBuffer();            
            
        //add the headers
        buffer.append( "Node Label," );
        buffer.append( "Node ID," );
        buffer.append( "Category," );
        buffer.append( "Manufacturer," );
        buffer.append( "Vendor," );
        buffer.append( "Model Number," );
        buffer.append( "Serial Number," );
        buffer.append( "Description," );
        buffer.append( "Circuit ID," );
        buffer.append( "Asset Number," );
        buffer.append( "Operating System," );
        buffer.append( "Rack," );
        buffer.append( "Slot," );
        buffer.append( "Port," );
        buffer.append( "Region," );
        buffer.append( "Division," );
        buffer.append( "Department," );
        buffer.append( "Address 1," );
        buffer.append( "Address 2," );
        buffer.append( "City," );
        buffer.append( "State," );
        buffer.append( "Zip," );
        buffer.append( "Building," );
        buffer.append( "Floor," );
        buffer.append( "Room," );
        buffer.append( "Vendor Phone," );
        buffer.append( "Vendor Fax," );
        buffer.append( "Date Installed," );
        buffer.append( "Lease," );
        buffer.append( "Lease Expires," );
        buffer.append( "Support Phone," );
        buffer.append( "Maint Contract," );
        buffer.append( "Vendor Asset Number," );
        buffer.append( "Maint Contract Expires," );
        buffer.append( "Comments" );
        
        out.println( buffer.toString() );
        buffer.setLength(0);
    
        //print a single line for each asset
        for( int i=0; i < assets.length; i++ ) {
            Asset asset = assets[i];
 
            try {
                buffer.append( NetworkElementFactory.getNodeLabel(asset.getNodeId()) );
            }
            catch( SQLException e ) {
                //just log the error, the node label is only a human aid, anyway,
                //so don't hold up the export
                this.log( "Database error while looking up node label for node " + asset.getNodeId(), e );
            }

            buffer.append( "," );
            buffer.append( asset.getNodeId() );
            buffer.append( "," );
            buffer.append( asset.getCategory() );
            buffer.append( "," );
            buffer.append( asset.getManufacturer() );
            buffer.append( "," );
            buffer.append( asset.getVendor() );
            buffer.append( "," );
            buffer.append( asset.getModelNumber() );
            buffer.append( "," );
            buffer.append( asset.getSerialNumber() );
            buffer.append( "," );
            buffer.append( asset.getDescription() );
            buffer.append( "," );
            buffer.append( asset.getCircuitId() );
            buffer.append( "," );
            buffer.append( asset.getAssetNumber() );
            buffer.append( "," );
            buffer.append( asset.getOperatingSystem() );
            buffer.append( "," );
            buffer.append( asset.getRack() );
            buffer.append( "," );
            buffer.append( asset.getSlot() );
            buffer.append( "," );
            buffer.append( asset.getPort() );
            buffer.append( "," );
            buffer.append( asset.getRegion() );
            buffer.append( "," );
            buffer.append( asset.getDivision() );
            buffer.append( "," );
            buffer.append( asset.getDepartment() );
            buffer.append( "," );
            buffer.append( asset.getAddress1() );
            buffer.append( "," );
            buffer.append( asset.getAddress2() );
            buffer.append( "," );
            buffer.append( asset.getCity() );
            buffer.append( "," );
            buffer.append( asset.getState() );
            buffer.append( "," );
            buffer.append( asset.getZip() );
            buffer.append( "," );
            buffer.append( asset.getBuilding() );
            buffer.append( "," );
            buffer.append( asset.getFloor() );
            buffer.append( "," );
            buffer.append( asset.getRoom() );
            buffer.append( "," );
            buffer.append( asset.getVendorPhone() );
            buffer.append( "," );
            buffer.append( asset.getVendorFax() );
            buffer.append( "," );
            buffer.append( asset.getDateInstalled() );
            buffer.append( "," );
            buffer.append( asset.getLease() );
            buffer.append( "," );
            buffer.append( asset.getLeaseExpires() );
            buffer.append( "," );
            buffer.append( asset.getSupportPhone() );
            buffer.append( "," );
            buffer.append( asset.getMaintContract() );
            buffer.append( "," );
            buffer.append( asset.getVendorAssetNumber() );
            buffer.append( "," );
            buffer.append( asset.getMaintContractExpires() );
            buffer.append( "," );
            buffer.append( asset.getComments() );
            out.println( buffer.toString() );
            buffer.setLength(0);
        }
            
        out.close();
    }
}    
