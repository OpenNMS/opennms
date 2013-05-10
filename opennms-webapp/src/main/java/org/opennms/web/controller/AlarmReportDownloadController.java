package org.opennms.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class AlarmReportDownloadController implements Controller {
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String filename = (String) request.getParameter("filename");
		String filepath = (String) request.getParameter("filepath");
		File file = new File(filepath);

		response.setContentType(new MimetypesFileTypeMap().getContentType(file));
		response.setContentLength((int) file.length());
		response.setHeader("content-disposition", "attachment; filename="
				+ URLEncoder.encode(filename, "UTF-8"));

		InputStream is = new FileInputStream(file);
		FileCopyUtils.copy(is, response.getOutputStream());

		return null;

	}
}