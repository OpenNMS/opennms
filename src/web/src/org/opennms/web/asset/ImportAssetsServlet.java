
package org.opennms.web.asset;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.opennms.core.resource.Vault;
import org.opennms.web.asset.*;
import org.opennms.web.MissingParameterException;


/**
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ImportAssetsServlet extends HttpServlet
{

    /** The URL to redirect the client to in case of success. */
    protected String redirectSuccess;

    protected AssetModel model;


    /** 
     * Looks up the <code>dispath.success</code> parameter in the servlet's
     * config.  If not present, this servlet will throw an exception so it
     * will be marked unavailable. 
     */
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();

        this.redirectSuccess = config.getInitParameter( "redirect.success" );	

        if( this.redirectSuccess == null ) {
            throw new UnavailableException( "Require a redirect.success init parameter." );
        }

	this.model = new AssetModel();
    }


    /**
     * Acknowledge the events specified in the POST and then redirect the 
     * client to an appropriate URL for display.
     */
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String assetsText = request.getParameter( "assetsText" );

        if( assetsText == null ) {
            throw new MissingParameterException( "assetsText" );
        }

        try {
	    List assets = this.decodeAssetsText( assetsText );
	    List nodesWithAssets = this.getCurrentAssetNodesList();
	
	    int assetCount = assets.size();

	    for( int i=0; i < assetCount; i++ ) {
		Asset asset = (Asset)assets.get(i);

		//update with the current information
		asset.setUserLastModified( request.getRemoteUser() );
		asset.setLastModifiedDate( new Date() );

		if( nodesWithAssets.contains( new Integer(asset.getNodeId()) )) {
		    this.model.modifyAsset( asset );
		}
		else {
		    this.model.createAsset( asset );
		}
	    } 	    	    

            response.sendRedirect( this.redirectSuccess );
        }
        catch( SQLException e ) {
            throw new ServletException( "Database exception", e );
        }
    }
    


    public List decodeAssetsText( String text ) {
	List list = new ArrayList();

	ArrayList lines = this.splitfields( text, "\n\r", -1 );
	int lineCount = lines.size();
	
	for( int i=0; i < lineCount; i++ ) {
	    String line = (String)lines.get(i);
	    ArrayList tokens = this.splitfields( line, ",", -1 );
	
	    try {
		if( tokens.size() != 35 ) {
		    throw new NoSuchElementException();
		}
 
		Asset asset = new Asset();

		//ignore tokens.get(0) = node label (for display only)
		asset.setNodeId( Integer.parseInt( (String)tokens.get(1) ));
		asset.setCategory( URLDecoder.decode( (String)tokens.get(2) ));
		asset.setManufacturer( URLDecoder.decode( (String)tokens.get(3) ));
		asset.setVendor( URLDecoder.decode( (String)tokens.get(4) ));
		asset.setModelNumber( URLDecoder.decode( (String)tokens.get(5) ));
		asset.setSerialNumber( URLDecoder.decode( (String)tokens.get(6) ));
		asset.setDescription( URLDecoder.decode( (String)tokens.get(7) ));
		asset.setCircuitId( URLDecoder.decode( (String)tokens.get(8) ));
		asset.setAssetNumber( URLDecoder.decode( (String)tokens.get(9) ));
		asset.setOperatingSystem( URLDecoder.decode( (String)tokens.get(10) ));
		asset.setRack( URLDecoder.decode( (String)tokens.get(11) ));
		asset.setSlot( URLDecoder.decode( (String)tokens.get(12) ));
		asset.setPort( URLDecoder.decode( (String)tokens.get(13) ));
		asset.setRegion( URLDecoder.decode( (String)tokens.get(14) ));
		asset.setDivision( URLDecoder.decode( (String)tokens.get(15) ));
		asset.setDepartment( URLDecoder.decode( (String)tokens.get(16) ));
		asset.setAddress1( URLDecoder.decode( (String)tokens.get(17) ));
		asset.setAddress2( URLDecoder.decode( (String)tokens.get(18) ));
		asset.setCity( URLDecoder.decode( (String)tokens.get(19) ));
		asset.setState( URLDecoder.decode( (String)tokens.get(20) ));
		asset.setZip( URLDecoder.decode( (String)tokens.get(21) ));
		asset.setBuilding( URLDecoder.decode( (String)tokens.get(22) ));
		asset.setFloor( URLDecoder.decode( (String)tokens.get(23) ));
		asset.setRoom( URLDecoder.decode( (String)tokens.get(24) ));
		asset.setVendorPhone( URLDecoder.decode( (String)tokens.get(25) ));
		asset.setVendorFax( URLDecoder.decode( (String)tokens.get(26) ));
		asset.setDateInstalled( URLDecoder.decode( (String)tokens.get(27) ));
		asset.setLease( URLDecoder.decode( (String)tokens.get(28) ));
		asset.setLeaseExpires( URLDecoder.decode( (String)tokens.get(29) ));
		asset.setSupportPhone( URLDecoder.decode( (String)tokens.get(30) ));
		asset.setMaintContract( URLDecoder.decode( (String)tokens.get(31) ));
		asset.setVendorAssetNumber( URLDecoder.decode( (String)tokens.get(32) ));
		asset.setMaintContractExpires( URLDecoder.decode( (String)tokens.get(33) ));
		asset.setComments( URLDecoder.decode( (String)tokens.get(34) ));

		list.add( asset );
	    }
	    catch( NoSuchElementException e ) {
		this.log( "Ignoring malformed import on line " + i + ", not enough values" );
	    }
	    catch( NumberFormatException e ) {
		this.log( "Ignoring malformed import on line " + i + ", node id not a number" );
	    }
	}

	return( list );
    }


    public List getCurrentAssetNodesList() throws SQLException {
	Connection conn = Vault.getDbConnection();
	ArrayList list = new ArrayList();

	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery( "SELECT NODEID FROM ASSETS" );

	    while( rs.next() ) {		
		list.add( new Integer( rs.getInt( "NODEID" )));
	    }

	    rs.close();
	    stmt.close();
	}
	finally {
	    Vault.releaseDbConnection( conn );
	}

	return( list );
    }



    private ArrayList splitfields(String string, String sep, int maxsplit) {
        ArrayList list = new ArrayList();

        int length = string.length();
        if (maxsplit < 0)
            maxsplit = length;

        int lastbreak = 0;
        int splits = 0;
        int sepLength = sep.length();
        while (splits < maxsplit) {
            int index = string.indexOf(sep, lastbreak);
            if (index == -1)
                break;
            splits += 1;
            list.add(string.substring(lastbreak, index));
            lastbreak = index + sepLength;
        }
        if (lastbreak <= length) {
            list.add(string.substring(lastbreak, length));
        }
        return list;
    }

}

