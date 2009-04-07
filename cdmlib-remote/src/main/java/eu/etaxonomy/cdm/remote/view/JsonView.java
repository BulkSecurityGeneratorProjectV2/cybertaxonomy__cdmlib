// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.view;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;


public class JsonView extends BaseView implements View{
	Log log = LogFactory.getLog(JsonView.class);

	private JsonConfig jsonConfig;
	
	public enum Type{
		JSON, XML
	}

	private Type type = Type.JSON;

	private String xsl;
	
	public void setXsl(String xsl) {
		this.xsl = xsl;
	}

	public Type getType() {
		return type;
	}

	/**
	 * Default is Type.JSON
	 * @param type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	public void setJsonConfig(JsonConfig jsonConfig) {
		this.jsonConfig = jsonConfig;
	}
	
	public String getContentType() {
		return "application/json";
	}

	public void render(Map model, HttpServletRequest req, HttpServletResponse resp) throws Exception {
		
		// Retrieve data from model
		Object entity = getResponseData(model);
		
		// prepare writer
		// TODO determine preferred charset from HTTP Accept-Charset header
		//Writer out = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(),  "UTF-8"));
		PrintWriter out =  resp.getWriter();
		// create JSON Object
		boolean isCollectionType = false;
		JSON jsonObj;
		if (entity != null && Collection.class.isAssignableFrom(entity.getClass())){
			isCollectionType = true;
			jsonObj = JSONArray.fromObject(entity, jsonConfig);
//		}else if(dto instanceof Class){
//			StringBuffer jsonStr = new StringBuffer().append("{\"name\":\"").append(((Class)dto).getName()).append("\", \"simpleName\": \"").append(((Class)dto).getSimpleName()).append("\"}");
//			jsonObj = JSONObject.fromObject(jsonStr);
		}else{
			jsonObj = JSONObject.fromObject(entity, jsonConfig);
		}
		
		if(type.equals(Type.XML)){
			XMLSerializer xmlSerializer = new XMLSerializer();
			if(isCollectionType){
				xmlSerializer.setArrayName(entity.getClass().getSimpleName());
				Class elementType = Object.class;
				Collection c = (Collection)entity;
				if(c.size() > 0){
					elementType = c.iterator().next().getClass();
				}
				xmlSerializer.setObjectName(elementType.getSimpleName());
			} else if(entity != null){
				xmlSerializer.setObjectName(entity.getClass().getSimpleName());
			}
			String xml = xmlSerializer.write( jsonObj );
			if(xsl != null){
				String xslInclude = "\r\n<?xml-stylesheet type=\"text/xsl\" href=\"human.xsl\"?>\r\n";
				xml = xml.replaceFirst("\r\n", xslInclude);
			}
			out.append(xml);
		} else {
			// assuming json
			out.append(jsonObj.toString());
		}
		out.flush();
	}
}
