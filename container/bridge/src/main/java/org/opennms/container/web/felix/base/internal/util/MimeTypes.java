/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.container.web.felix.base.internal.util;

import java.util.Map;
import java.util.HashMap;

public final class MimeTypes
{
    private final static MimeTypes INSTANCE =
            new MimeTypes();

    private final Map<String, String> extMap;

    private MimeTypes()
    {
        this.extMap = new HashMap<String, String>();
        this.extMap.put("abs", "audio/x-mpeg");
        this.extMap.put("ai", "application/postscript");
        this.extMap.put("aif", "audio/x-aiff");
        this.extMap.put("aifc", "audio/x-aiff");
        this.extMap.put("aiff", "audio/x-aiff");
        this.extMap.put("aim", "application/x-aim");
        this.extMap.put("art", "image/x-jg");
        this.extMap.put("asf", "video/x-ms-asf");
        this.extMap.put("asx", "video/x-ms-asf");
        this.extMap.put("au", "audio/basic");
        this.extMap.put("avi", "video/x-msvideo");
        this.extMap.put("avx", "video/x-rad-screenplay");
        this.extMap.put("bcpio", "application/x-bcpio");
        this.extMap.put("bin", "application/octet-stream");
        this.extMap.put("bmp", "image/bmp");
        this.extMap.put("body", "text/html");
        this.extMap.put("cdf", "application/x-cdf");
        this.extMap.put("cer", "application/x-x509-ca-cert");
        this.extMap.put("class", "application/java");
        this.extMap.put("cpio", "application/x-cpio");
        this.extMap.put("csh", "application/x-csh");
        this.extMap.put("css", "text/css");
        this.extMap.put("dib", "image/bmp");
        this.extMap.put("doc", "application/msword");
        this.extMap.put("dtd", "application/xml-dtd");
        this.extMap.put("dv", "video/x-dv");
        this.extMap.put("dvi", "application/x-dvi");
        this.extMap.put("eps", "application/postscript");
        this.extMap.put("etx", "text/x-setext");
        this.extMap.put("exe", "application/octet-stream");
        this.extMap.put("gif", "image/gif");
        this.extMap.put("gk", "application/octet-stream");
        this.extMap.put("gtar", "application/x-gtar");
        this.extMap.put("gz", "application/x-gzip");
        this.extMap.put("hdf", "application/x-hdf");
        this.extMap.put("hqx", "application/mac-binhex40");
        this.extMap.put("htc", "text/x-component");
        this.extMap.put("htm", "text/html");
        this.extMap.put("html", "text/html");
        this.extMap.put("hqx", "application/mac-binhex40");
        this.extMap.put("ief", "image/ief");
        this.extMap.put("jad", "text/vnd.sun.j2me.app-descriptor");
        this.extMap.put("jar", "application/java-archive");
        this.extMap.put("java", "text/plain");
        this.extMap.put("jnlp", "application/x-java-jnlp-file");
        this.extMap.put("jpe", "image/jpeg");
        this.extMap.put("jpeg", "image/jpeg");
        this.extMap.put("jpg", "image/jpeg");
        this.extMap.put("js", "text/javascript");
        this.extMap.put("kar", "audio/x-midi");
        this.extMap.put("latex", "application/x-latex");
        this.extMap.put("m3u", "audio/x-mpegurl");
        this.extMap.put("mac", "image/x-macpaint");
        this.extMap.put("man", "application/x-troff-man");
        this.extMap.put("mathml", "application/mathml+xml");
        this.extMap.put("me", "application/x-troff-me");
        this.extMap.put("mid", "audio/x-midi");
        this.extMap.put("midi", "audio/x-midi");
        this.extMap.put("mif", "application/x-mif");
        this.extMap.put("mov", "video/quicktime");
        this.extMap.put("movie", "video/x-sgi-movie");
        this.extMap.put("mp1", "audio/x-mpeg");
        this.extMap.put("mp2", "audio/x-mpeg");
        this.extMap.put("mp3", "audio/x-mpeg");
        this.extMap.put("mpa", "audio/x-mpeg");
        this.extMap.put("mpe", "video/mpeg");
        this.extMap.put("mpeg", "video/mpeg");
        this.extMap.put("mpega", "audio/x-mpeg");
        this.extMap.put("mpg", "video/mpeg");
        this.extMap.put("mpv2", "video/mpeg2");
        this.extMap.put("ms", "application/x-wais-source");
        this.extMap.put("nc", "application/x-netcdf");
        this.extMap.put("oda", "application/oda");
        this.extMap.put("ogg", "application/ogg");
        this.extMap.put("pbm", "image/x-portable-bitmap");
        this.extMap.put("pct", "image/pict");
        this.extMap.put("pdf", "application/pdf");
        this.extMap.put("pgm", "image/x-portable-graymap");
        this.extMap.put("pic", "image/pict");
        this.extMap.put("pict", "image/pict");
        this.extMap.put("pls", "audio/x-scpls");
        this.extMap.put("png", "image/png");
        this.extMap.put("pnm", "image/x-portable-anymap");
        this.extMap.put("pnt", "image/x-macpaint");
        this.extMap.put("ppm", "image/x-portable-pixmap");
        this.extMap.put("ppt", "application/powerpoint");
        this.extMap.put("ps", "application/postscript");
        this.extMap.put("psd", "image/x-photoshop");
        this.extMap.put("qt", "video/quicktime");
        this.extMap.put("qti", "image/x-quicktime");
        this.extMap.put("qtif", "image/x-quicktime");
        this.extMap.put("ras", "image/x-cmu-raster");
        this.extMap.put("rdf", "application/rdf+xml");
        this.extMap.put("rgb", "image/x-rgb");
        this.extMap.put("rm", "application/vnd.rn-realmedia");
        this.extMap.put("roff", "application/x-troff");
        this.extMap.put("rtf", "application/rtf");
        this.extMap.put("rtx", "text/richtext");
        this.extMap.put("sh", "application/x-sh");
        this.extMap.put("shar", "application/x-shar");
        this.extMap.put("shtml", "text/x-server-parsed-html");
        this.extMap.put("sit", "application/x-stuffit");
        this.extMap.put("smf", "audio/x-midi");
        this.extMap.put("snd", "audio/basic");
        this.extMap.put("src", "application/x-wais-source");
        this.extMap.put("sv4cpio", "application/x-sv4cpio");
        this.extMap.put("sv4crc", "application/x-sv4crc");
        this.extMap.put("svg", "image/svg+xml");
        this.extMap.put("svgz", "image/svg+xml");
        this.extMap.put("swf", "application/x-shockwave-flash");
        this.extMap.put("t", "application/x-troff");
        this.extMap.put("tar", "application/x-tar");
        this.extMap.put("tcl", "application/x-tcl");
        this.extMap.put("tex", "application/x-tex");
        this.extMap.put("texi", "application/x-texinfo");
        this.extMap.put("texinfo", "application/x-texinfo");
        this.extMap.put("tif", "image/tiff");
        this.extMap.put("tiff", "image/tiff");
        this.extMap.put("tr", "application/x-troff");
        this.extMap.put("tsv", "text/tab-separated-values");
        this.extMap.put("txt", "text/plain");
        this.extMap.put("ulw", "audio/basic");
        this.extMap.put("ustar", "application/x-ustar");
        this.extMap.put("xbm", "image/x-xbitmap");
        this.extMap.put("xml", "text/xml");
        this.extMap.put("xpm", "image/x-xpixmap");
        this.extMap.put("xsl", "application/xml");
        this.extMap.put("xslt", "application/xslt+xml");
        this.extMap.put("xwd", "image/x-xwindowdump");
        this.extMap.put("vsd", "application/x-visio");
        this.extMap.put("vxml", "application/voicexml+xml");
        this.extMap.put("wav", "audio/x-wav");
        this.extMap.put("wbmp", "image/vnd.wap.wbmp");
        this.extMap.put("wml", "text/vnd.wap.wml");
        this.extMap.put("wmlc", "application/vnd.wap.wmlc");
        this.extMap.put("wmls", "text/vnd.wap.wmls");
        this.extMap.put("wmlscriptc", "application/vnd.wap.wmlscriptc");
        this.extMap.put("wrl", "x-world/x-vrml");
        this.extMap.put("xht", "application/xhtml+xml");
        this.extMap.put("xhtml", "application/xhtml+xml");
        this.extMap.put("xls", "application/vnd.ms-excel");
        this.extMap.put("xul", "application/vnd.mozilla.xul+xml");
        this.extMap.put("Z", "application/x-compress");
        this.extMap.put("z", "application/x-compress");
        this.extMap.put("zip", "application/zip");
    }

    public String getByFile(String file)
    {
        if (file == null) {
            return null;
        }

        int dot = file.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }

        String ext = file.substring(dot + 1);
        if (ext.length() < 1) {
            return null;
        }

        return getByExtension(ext);
    }

    public String getByExtension(String ext)
    {
        if (ext == null) {
            return null;
        }

        return this.extMap.get(ext);
    }

    public static MimeTypes get()
    {
        return INSTANCE;
    }
}
