package org.opennms.web.asset;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;

import java.util.Map;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.assetLocation.*;
import org.opennms.web.element.NetworkElementFactory;
import org.w3c.dom.*;
import org.apache.xpath.*;


import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;

/**
 * @author rssntn67@yahoo.it
 *
 * 
 */

public class AssetLocationTreeView {

	private String dtdPath = "";
	private String fileXslt = "";

	private String title = " ";
	private String mappingImageFile = "";
	private String imagePath = "Icons/";
	private String rootImage = "";

	private boolean expanded = false;

	private String fileContent = "";


	private String defaultFolderImg;
	private String defaultLeafImg;

	private Map typeImageMap;

	public AssetLocationTreeView() {
		typeImageMap = new HashMap();
	}

	public void setDtdPath(String dtdPath) {
		if (!dtdPath.endsWith(File.separator)) {
			dtdPath = dtdPath + File.separator;
		}
		this.dtdPath = dtdPath;
	}

	public void setFileXslt(String fileXslt) throws IOException {
		this.fileXslt = fileXslt;
	}

	public void setImagePath(String imagePath) {
		if (!imagePath.endsWith(File.separator)) {
			imagePath = imagePath + File.separator;
		}
		this.imagePath = imagePath;
	}

	public void setMappingImageFile(String mappingImageFile) {
		this.mappingImageFile = mappingImageFile;
	}

	public void setRootImage(String string) {
		rootImage = string;
	}

	public void setRootTitle(String title) throws IOException {
		this.title = title;
	}

	public void setExpanded(boolean b) {
		expanded = b;
	}

	private void parseMappingImageFile()
		throws
			TransformerException,
			SAXException,
			IOException,
			ParserConfigurationException,
			FileNotFoundException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		BufferedReader filebuf = null;
		filebuf = new BufferedReader(new FileReader(mappingImageFile));
		String nextStr = new String(filebuf.readLine());
		String strToParse = new String(nextStr);
		while (true) {
			nextStr = filebuf.readLine();
			if (nextStr == null)
				break;
			strToParse = strToParse + nextStr;
		}
		filebuf.close();
		Document document =
			builder.parse(new InputSource(new StringReader(strToParse)));
		document.getDocumentElement().normalize();
		Node root =
			XPathAPI.selectSingleNode(
				document,
				"/associations/@defaultFolderImage");
		defaultFolderImg = root.getNodeValue();
		root =
			XPathAPI.selectSingleNode(
				document,
				"/associations/@defaultLeafImage");
		defaultLeafImg = root.getNodeValue();
		String xpath = "/associations/association";
		NodeList nodeList = XPathAPI.selectNodeList(document, xpath);
		int n = nodeList.getLength();
		Node currentNode;

		String type, image;
		for (int i = 0; i < n; i++) {
			currentNode = nodeList.item(i);
			type = getData(currentNode, "type");
			image = getData(currentNode, "image");
			typeImageMap.put(type, image);
		}
	}

	private String getData(Node node, String tag) throws TransformerException {
		Node selectedNode = XPathAPI.selectSingleNode(node, tag);
		if (selectedNode == null)
			return "";
		selectedNode = selectedNode.getFirstChild();
		if (selectedNode == null)
			return "";
		else
			return ((Text) selectedNode).getData();
	}

	private void createTreeView()
		throws
			SQLException,
			IOException,
			FileNotFoundException,
			MarshalException,
			ValidationException,
			ClassNotFoundException {

		AssetLocationFactory.init();

		int code = 1;
		openTreeView(title);
		String sFCode = "" + code;
		openFolder(title, rootImage, sFCode, expanded);
		Map buildingMap =
			new TreeMap(AssetLocationFactory.getInstance().getBuildings());
		Iterator iterator1 = buildingMap.keySet().iterator();

		while (iterator1.hasNext()) {
			String building = (String) iterator1.next();
			Building curBuild = (Building) buildingMap.get(building);
			String sFldCode = building;
			openFolder(
				building,
				(String) typeImageMap.get("Buildings"),
				sFldCode,
				false);
			Map roomMap =
				new TreeMap(
					AssetLocationFactory.getInstance().getRooms(curBuild));
			Iterator iterator2 = roomMap.keySet().iterator();
			while (iterator2.hasNext()) {
				String room = (String) iterator2.next();
				code++;
				openFolder(
					"Room " + room,
					(String) typeImageMap.get("Rooms"),
					room,
					false);
				Map nodeMap =
					new TreeMap(
						AssetLocationFactory.getInstance().getAssetNode(
							building,
							room));
				Iterator iterator3 = nodeMap.keySet().iterator();
				while (iterator3.hasNext()) {
					String node = (String) iterator3.next();
					addLeaf(
						NetworkElementFactory.getNodeLabel(
							Integer.parseInt(node)),
						(String) typeImageMap.get("Nodes"),
						node);
				}
				closeFolder();
			}
			closeFolder();
		}
		closeFolder();
		closeTreeView();
	}

	private void openTreeView(String t) {
		if (t != null)
			title = t;
		fileContent += "<treeview title=\"" + title + "\">\n";
	}

	private void closeTreeView() {
		fileContent += "</treeview>";
	}

	private void openFolder(
		String title,
		String img,
		String code,
		boolean expanded) {
		String pathImg = imagePath;
		if (img != null) {
			pathImg = imagePath + img;
			if (!(new File(pathImg)).exists())
				img = defaultFolderImg;
		} else {
			img = defaultFolderImg;
		}
		fileContent += "\t<folder title=\""
			+ title
			+ "\" img=\""
			+ img
			+ "\" code=\""
			+ code
			+ "\" expanded=\""
			+ expanded
			+ "\">\n";
	}

	private void closeFolder() {
		fileContent += "\t</folder>\n";
	}

	private void addLeaf(String title, String img, String code) {
		String pathImg = imagePath;
		if (img != null && !img.equals((String) typeImageMap.get(""))) {
			pathImg = imagePath + img;
			;
			if (!(new File(pathImg)).exists()) {
				img = defaultLeafImg;
			}
		} else {
			img = defaultLeafImg;
		}
		fileContent += "\t\t<leaf title=\""
			+ title
			+ "\" img=\""
			+ img
			+ "\" code=\""
			+ code
			+ "\"/>\n";
	}

	public String getHtmlTreeView()
		throws
			SQLException,
			TransformerException,
			SAXException,
			ValidationException,
			IOException,
			MarshalException,
			ParserConfigurationException,
			FileNotFoundException,
			ClassNotFoundException {
		fileContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<!DOCTYPE treeview SYSTEM \""
				+ dtdPath
				+ "treeview.dtd\">\n"
				+ "<?xml-stylesheet type=\"text/xsl\" href=\""
				+ fileXslt
				+ "\"?>\n";

		parseMappingImageFile();
		createTreeView();

		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer =
				tFactory.newTransformer(
					new javax.xml.transform.stream.StreamSource(fileXslt));
		} catch (TransformerConfigurationException tce) {
			throw (
				new IllegalArgumentException("Bean's parameter 'fileXslt' not valid."));
		}
		Reader r = new StringReader(fileContent);
		Writer w = new StringWriter();
		try {
			transformer.transform(
				new StreamSource(r),
				new javax.xml.transform.stream.StreamResult(w));
		} catch (TransformerException t) {
			throw (
				new IllegalArgumentException("Bean's parameters not properly configured."));
		}
		return (String) w.toString();
	}

}
