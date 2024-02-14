/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.reporting.availability.render;

import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.core.io.Resource;

/**
 * ReportRenderer is the interface for rendering xml reports to pdf, pdf with
 * embedded svg and html
 *
 * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
 */
public interface ReportRenderer {

    /**
     * <p>render</p>
     *
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    void render() throws ReportRenderException;
    
    /**
     * <p>render</p>
     *
     * @param inputFileName a {@link java.lang.String} object.
     * @param outputFileName a {@link java.lang.String} object.
     * @param xlstResource a {@link org.springframework.core.io.Resource} object.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    void render(String inputFileName, String outputFileName, Resource xlstResource) throws ReportRenderException;
    
    /**
     * <p>render</p>
     *
     * @param inputFileName a {@link java.lang.String} object.
     * @param outputStream a {@link java.io.OutputStream} object.
     * @param xsltResource a {@link org.springframework.core.io.Resource} object.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    void render(String inputFileName, OutputStream outputStream, Resource xsltResource) throws ReportRenderException;
    
    /**
     * <p>render</p>
     *
     * @param inputStream a {@link java.io.InputStream} object.
     * @param outputStream a {@link java.io.OutputStream} object.
     * @param xsltResource a {@link org.springframework.core.io.Resource} object.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    void render(InputStream inputStream, OutputStream outputStream, Resource xsltResource) throws ReportRenderException;
    
    /**
     * <p>render</p>
     *
     * @param inputFileName a {@link java.lang.String} object.
     * @param xsltResource a {@link org.springframework.core.io.Resource} object.
     * @return an array of byte.
     * @throws org.opennms.reporting.availability.render.ReportRenderException if any.
     */
    byte[] render(String inputFileName, Resource xsltResource) throws ReportRenderException;

    /**
     * <p>setOutputFileName</p>
     *
     * @param outputFileName a {@link java.lang.String} object.
     */
    void setOutputFileName(String outputFileName);
    
    /**
     * <p>getOutputFileName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getOutputFileName();
    
    /**
     * <p>setInputFileName</p>
     *
     * @param inputFileName a {@link java.lang.String} object.
     */
    void setInputFileName(String inputFileName);

    /**
     * <p>setXsltResource</p>
     *
     * @param xsltResource a {@link org.springframework.core.io.Resource} object.
     */
    void setXsltResource(Resource xsltResource);
    
    /**
     * <p>setBaseDir</p>
     *
     * @param baseDir a {@link java.lang.String} object.
     */
    void setBaseDir(String baseDir);
    
    /**
     * <p>getBaseDir</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getBaseDir();

}
