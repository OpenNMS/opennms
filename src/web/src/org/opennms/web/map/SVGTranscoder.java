//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2003 Networked Knowledge Systems, Inc.
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
//      Derek Glidden   <dglidden@opennms.org>
//      http://www.nksi.com/
//
//

package org.opennms.web.map;

import java.awt.HeadlessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.w3c.dom.Document;

/**
 * This class should be called from inside of an <img> tag.  We
 * generate an SVG document and pass it to the Batik Transcoder and
 * output a PNG file of the tree map of nodes.
 *
 * <b>It is very important to note</b> that the Batik Transcoder
 * can use <i>tremendous</i> amounts of memory when it is doing its
 * Transcoding.  That's Java for ya, I guess...
 *
 * @author <A HREF="mailto:dglidden@opennms.org">Derek Glidden</A>
 * @author <A HREF="http://www.nksi.com/">NKSi</A>
 */

public class SVGTranscoder extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        // the docbase to which all our elements will be relative
        // String base = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";

        response.setContentType("image/png");

        // create a transcoder
        PNGTranscoder t = new PNGTranscoder();

        // for some reason, the JPEGTranscoder doesn't work...
        // JPEGTranscoder t = new JPEGTranscoder();
        // t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));

        try {
            // grab SVG directly from the jsp page.
            // gosh darn it this was such a supremely elegant trick
            // until I discovered it didn't work with HTTP
            // Authentication.  *sigh*
            // TranscoderInput input = new TranscoderInput(base + "/inline.jsp");


            // create the object that will make our SVG for us
            // DocumentGenerator docgen = new DocumentGenerator();
            // now we get this from our HttpSession via jsp
            DocumentGenerator docgen = (DocumentGenerator) request.getSession().getAttribute("docgen");


            // pass the servlet context so the DocumentGenerator can find its icons
            // ServletContext ctx = getServletContext();
            // docgen.setNodes(nodes);
            // docgen.setServletContext(ctx);
            // docgen.setUrlBase(base);

            // generate and retrieve the SVG DOM we've got to get it
            // out of the Document object format into something the
            // transcoder recognizes.  For some reason, even though
            // there is a TranscoderInput(Document) constructor, it
            // doesn't seem to be able to do anything with the
            // Document object.  So I turn the Document into a String
            // and feed that into the TranscoderInput.

            Document doc = docgen.getHostDocument(true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            Writer docwriter = new OutputStreamWriter(baos, "UTF-8");
            DOMUtilities.writeDocument(doc, docwriter);
            docwriter.flush();
            docwriter.close();
            String svg = baos.toString();
            TranscoderInput input = new TranscoderInput(new StringReader(svg));

            // uncomment this if you want to see the SVG we're transcoding.
            // this is a pleasant side-effect of having to turn it into a String
            // log(svg);

            // create the transcoder output
            OutputStream ostream = response.getOutputStream();
            TranscoderOutput output = new TranscoderOutput(ostream);


            // this isn't really necessary, and in fact slows the JVM
            // down a little bit when we gc.  the Transcoder uses so
            // much memory though, that I like to keep this in here to
            // see what it's doing.

            Runtime rt = java.lang.Runtime.getRuntime();

            log("Maximum memory available: " + rt.maxMemory());
            log("Allocated memory: " + rt.totalMemory());
            log("Free memory: " + rt.freeMemory());
            log("Garbage collecting");
            java.lang.System.gc();
            log("GC finished.");
            log("Allocated memory: " + rt.totalMemory());
            log("Free memory: " + rt.freeMemory());

            log("starting transcoding");

            try {
                t.transcode(input, output);
            } catch(HeadlessException e) {
                // doesn't seem to do this anymore.  Something changed...
                log("HeadlessException in SVGTranscoder during transcoding");
                log(e.toString());
            } catch(TranscoderException e) {
                log("TranscoderException in SVGTranscoder during transcoding");
                log(e.toString());
            } catch(Exception e) {
                log("Exception in SVGTranscoder during transcoding");
                log(e.toString());
            }

            log("finished transcoding");
            log("Allocated memory: " + rt.totalMemory());
            log("Free memory: " + rt.freeMemory());
        } catch(IOException e) {
            log("IOException in SVGTranscoder");
            log(e.toString());
        } catch(Exception e) {
            log("Exception in SVGTranscoder");
            log(e.toString());
        }

    }

}
