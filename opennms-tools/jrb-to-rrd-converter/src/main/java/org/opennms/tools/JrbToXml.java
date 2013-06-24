/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.tools;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDbPool;
import org.jrobin.core.RrdException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JrbToXml extends Thread {

    /**
     * Initialize using RRD DB pool
     */
    private static boolean rrdDbPoolUsed = false;

    /**
     * JRobin to RRD converter
     */
    private JrbToRrdConverter m_jrbToRrdConverter;

    /**
     * Queue for files to convert
     */
    private Queue<String> m_queue = new ConcurrentLinkedQueue<String>();

    /**
     * Queue closed status
     */
    private boolean queueClosed = false;

    /**
     * Path to rrdtool binary
     */
    private String m_rrdTool;

    /**
     * Default constructor to convert from JRB to XML
     *
     * @param jrbToRrdConverter
     */
    public JrbToXml(JrbToRrdConverter jrbToRrdConverter) {
        m_jrbToRrdConverter = jrbToRrdConverter;
        m_rrdTool = jrbToRrdConverter.getRrdTool();
    }

    /**
     * <p>Close queue</p>
     * Close queue and set queue closed status
     */
    public void close() {
        queueClosed = true;
    }

    /**
     * <p>Add path</p>
     *
     * Add absolute path to queue for thread to convert
     *
     * @param path absolute path to JRobin file to convert as {@link java.lang.String}
     */
    public void add(String path) {
        m_queue.add(path);
    }

    /**
     * <p>Get RRD DB reference</p>
     *
     * TODO: Why do we need this here?
     * @param path
     * @return
     * @throws IOException
     * @throws RrdException
     */
    private RrdDb getRrdDbReference(String path) throws IOException, RrdException {
        if (rrdDbPoolUsed) {
            return RrdDbPool.getInstance().requestRrdDb(path);
        } else {
            return new RrdDb(path);
        }
    }

    /**
     * <p>Get RRD DB reference</p>
     *
     * TODO: This method is unused. Why do we need this here?
     * @param path
     * @param xmlPath
     * @return
     * @throws IOException
     * @throws RrdException
     * @throws RrdException
     */
    private RrdDb getRrdDbReference(String path, String xmlPath) throws IOException, RrdException, RrdException {
        if (rrdDbPoolUsed) {
            return RrdDbPool.getInstance().requestRrdDb(path, xmlPath);
        } else {
            return new RrdDb(path, xmlPath);
        }
    }

    /**
     * <p>Size of the queue</p>
     * Get the size of the queue for files to convert.
     *
     * @return size of queue for files to convert
     */
    public int size() {
        return m_queue.size();
    }

    /**
     * <p>Convert to RRD</p>
     *
     * Convert the absolute path from an XML file to RRDtool.
     *
     * @param path absolute path to JRobin dumped XML file
     * @throws IOException
     * @throws RrdException
     */
    public void convertToRrd(String path) throws IOException, RrdException {
        String xmlPath = path + JrbToRrdConverter.FILE_TYPE.XML.ext();
        String rrdPath = path + JrbToRrdConverter.FILE_TYPE.RRD.ext();

        // Fork process for rrdtool and restore JRobin XML dump into RRD file
        Process p = Runtime.getRuntime().exec(m_rrdTool + " restore " + xmlPath + " " + rrdPath);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Convert JRobin to XML</p>
     * Export JRobin file to XML file using memory mapped file access.
     *
     * @param path absolute path to JRobin file without file extension as {@link java.lang.String}
     * @throws RrdException
     * @throws IOException
     */
    public void convertToXml(String path) throws RrdException, IOException {
        RrdDb rrdDb = getRrdDbReference(path + JrbToRrdConverter.FILE_TYPE.JRB.ext());

        try {
            byte[] buf = rrdDb.getXml().getBytes();
            FileChannel writeChannel = new RandomAccessFile(path + JrbToRrdConverter.FILE_TYPE.XML.ext(), "rw").getChannel();
            ByteBuffer wrBuf = writeChannel.map(FileChannel.MapMode.READ_WRITE, 0, buf.length);
            wrBuf.put(buf);
            writeChannel.close();
        } finally {
            releaseRrdDbReference(rrdDb);
        }
    }

    /**
     * <p>Release RRD database reference</p>
     *
     * @param rrdDb
     * @throws IOException
     * @throws RrdException
     */
    private void releaseRrdDbReference(RrdDb rrdDb) throws IOException, RrdException {
        if (rrdDbPoolUsed) {
            RrdDbPool.getInstance().release(rrdDb);
        } else {
            rrdDb.close();
        }
    }

    /**
     * <p>Run conversion</p>
     *
     * Run for each file in queue
     */
    public void run() {
        while (!queueClosed || !m_queue.isEmpty()) {
            while (!m_queue.isEmpty()) {
                try {
                    String path = m_queue.poll();

                    convertToXml(path);
                    convertToRrd(path);

                    m_jrbToRrdConverter.increaseConvertedFiles();
                } catch (RrdException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}