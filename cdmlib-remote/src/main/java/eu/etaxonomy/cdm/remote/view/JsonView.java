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

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.xml.XMLSerializer;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.View;


public class JsonView extends BaseView implements View{

    public static final Logger logger = Logger.getLogger(JsonView.class);

    private JsonConfig jsonConfig;

    public enum Type{
        JSON("application/json"),
        XML("application/xml");

        private String contentType;

        Type(String contentType){
            this.contentType = contentType;
        }

        public String getContentType(){
            return contentType;
        }
    }

    private Type type = Type.JSON;

    private String xsl = null;

    public void setXsl(String xsl) {
        this.xsl = xsl;
    }

    public String getXsl() {
        return xsl;
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

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.View#getContentType()
     */
    public String getContentType() {
        return type.getContentType();
    }

    public void render(Object entity, PrintWriter writer, String documentContextPath, String jsonpCallback) throws Exception {

        if(jsonConfig == null){
            logger.error("The jsonConfig must not be null. It must be set in the applicationContext.");
        }

        // create JSON Object
        boolean isCollectionType = false;
        JSON jsonObj;
        if (entity == null){
          jsonObj = JSONObject.fromObject("{}");
        } else if(Collection.class.isAssignableFrom(entity.getClass())){
            isCollectionType = true;
            jsonObj = JSONArray.fromObject(entity, jsonConfig);
        }else if(entity instanceof String){
            jsonObj = JSONObject.fromObject("{\"String\":\""+entity.toString().replace("\"", "\\\"")+"\"}");
        } else if(entity instanceof Integer){
            jsonObj = JSONObject.fromObject("{\"Integer\":"+((Integer)entity).intValue()+"}");
        } else if(entity instanceof Boolean){
            jsonObj = JSONObject.fromObject("{\"Boolean\":"+((Boolean)entity).toString()+"}");
        } else {
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
            if(type.equals(Type.XML) && xsl != null){
                if(documentContextPath == null){
                    documentContextPath = "";
                }
                String replace = "\r\n<?xml-stylesheet type=\"text/xsl\" href=\"" + documentContextPath + "/" + xsl + "\"?>\r\n";
                xml = xml.replaceFirst("\r\n", replace);
            }
            writer.append(xml);
        } else {
            // assuming json
            if(jsonpCallback != null){
                writer.append(jsonpCallback).append("(").append(jsonObj.toString()).append(")");
            } else {
                writer.append(jsonObj.toString());
            }
        }
        //TODO resp.setContentType(type);
        writer.flush();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.web.servlet.View#render(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // Retrieve data from model
        Object entity = getResponseData(model);

        // set content type
        response.setContentType(type.getContentType());

        PrintWriter writer = response.getWriter();

        // read jsonp parameter from query string
        String[] tokens = request.getQueryString().split("&", 0);
        String jsonpParamName = "callback";
        String jsonpCallback= null;
        for (int i = 0; i < tokens.length; i++) {
             if(tokens[i].startsWith(jsonpParamName)){
                 jsonpCallback = tokens[i].substring(jsonpParamName.length() + 1);
                 break;
             }
        }

        // render
        render(entity, writer, request.getContextPath(), jsonpCallback);
    }
}
