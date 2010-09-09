// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.printpublisher;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

import eu.etaxonomy.printpublisher.XMLHelper.EntityType;

/**
 * Implementation of an IXMLEntityFactory that is connected to a CDM Community Server on 
 * a remote machine. API call will be executed by accessing the the servers REST API.
 * 
 * @author n.hoffmann
 * @created Apr 6, 2010
 * @version 1.0
 */
public class RemoteXMLEntityFactory extends AbstractXmlEntityFactory{
	private static final Logger logger = Logger
			.getLogger(RemoteXMLEntityFactory.class);
	
	private URL serviceUrl;
	
	private static final String UUID = "{uuid}";
	
	private static final String CLASSIFICATIONS = "classifications?start=0&limit=-1";
	private static final String CLASSIFICATION_CHILD_NODES = "classification/" + UUID + "/childNodes/";
	private static final String TAXONNODE_CHILD_NODES = "taxonNode/" + UUID + "/childNodes/";
	private static final String TAXONNODE =  "taxonNode/" + UUID;
	private static final String TAXONNODE_TAXON = TAXONNODE + "/taxon";
	
	private static final String FEATURETREES = "featuretrees?start=0&limit=-1";
	private static final String FEATURETREE = "featuretree/" + UUID;
	private static final String FEATURENODE = "featurenode/" + UUID;
	private static final String FEATURENODE_FEATURE = FEATURENODE + "/feature";
	
	private static final String NAME_TYPE_DESIGNATIONS = "name/" + UUID + "/typeDesignations";
	
	private static final String TAXON_ACCEPTED = "portal/taxon/" + UUID;
	private static final String TAXON_SYNONYMY = "portal/taxon/" + UUID + "/synonymy";
	private static final String TAXON_DESCRIPTIONS = "portal/taxon/" + UUID + "/descriptions";



	
	
	/**
	 * Creates new instance of this factory and connects it to the given 
	 * CDM Community Stores access point.
	 * 
	 * Typically, there is no need to instantiate this class. 
	 */
	protected RemoteXMLEntityFactory(URL webserviceUrl){
		this.serviceUrl = webserviceUrl;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getClassifications()
	 */
	public List<Element> getClassifications(){
		Element result = queryService(CLASSIFICATIONS);
		return processElementList(result);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getChildNodes(org.jdom.Element)
	 */
	public List<Element> getChildNodes(Element treeNode){
		EntityType entityType = XMLHelper.getEntityType(treeNode);
		
		Element result = null;
		
		if(EntityType.CLASSIFICATION.equals(entityType)){
			result = queryService(treeNode, CLASSIFICATION_CHILD_NODES);
		}
		else if(EntityType.TAXON_NODE.equals(entityType)){
			result = queryService(treeNode, TAXONNODE_CHILD_NODES);
		}
		
		return processElementList(result);
	}	
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getTaxonNode(java.util.UUID)
	 */
	public Element getTaxonNode(UUID taxonNodeUuid) {
		return queryService(taxonNodeUuid, TAXONNODE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureTree(java.util.UUID)
	 */
	public Element getFeatureTree(UUID featureTreeUuid) {
		return queryService(featureTreeUuid, FEATURETREE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getFeatureTrees()
	 */
	public List<Element> getFeatureTrees() {
		Element result = queryService(FEATURETREES);
		return processElementList(result);
	}
	
	public Element getFeatureNode(UUID featureNodeUuid) {
		Element result = queryService(featureNodeUuid, FEATURENODE);
		return result;
	}

	public Element getFeatureForFeatureNode(UUID featureNodeUuid) {
		Element result = queryService(featureNodeUuid, FEATURENODE_FEATURE);
		return result;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getTaxonFromTaxonNode(org.jdom.Element)
	 */
	public Element getTaxonForTaxonNode(Element taxonNodeElement) {
		return queryService(taxonNodeElement, TAXONNODE_TAXON);
	}
	

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getAcceptedTaxonElement(org.jdom.Element)
	 */
	public Element getAcceptedTaxonElement(Element taxonElement) {
		Element result = queryService(taxonElement, TAXON_ACCEPTED);
		return result;		
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getSynonymy(org.jdom.Element)
	 */
	public List<Element> getSynonymy(Element taxonElement) {
		Element result = queryService(taxonElement, TAXON_SYNONYMY);
		
		List<Element> elementList = new ArrayList<Element>();
		
		for(Object child : result.getChildren()){
			if(child instanceof Element){
				Element childElement = (Element) ((Element)child).clone();
				
				childElement.detach();
				
				elementList.add(childElement);
			}
		}
		
		return elementList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getTypeDesignations(org.jdom.Element)
	 */
	public List<Element> getTypeDesignations(Element nameElement) {
		Element result = queryService(nameElement, NAME_TYPE_DESIGNATIONS);
		return processElementList(result);
	}

	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.printpublisher.IXMLEntityFactory#getDescriptions(org.jdom.Element)
	 */
	public Element getDescriptions(Element taxonElement) {
		Element result = queryService(taxonElement, TAXON_DESCRIPTIONS);
		return result;
	}	
	
	/**
	 * Queries the service with the uuid of the given element
	 * 
	 * @param element
	 * @param restRequest
	 * @return
	 */
	private Element queryService(Element element, String restRequest){
		UUID uuid = XMLHelper.getUuid(element);
		return queryService(uuid, restRequest);
	}

	/**
	 * Queries the service with the given uuid 
	 * 
	 * @param uuid
	 * @param restRequest
	 * @return
	 */
	private Element queryService(UUID uuid, String restRequest){
		String request = restRequest.replace(UUID, uuid.toString());
		return queryService(request);
	}
	
	/**
	 * 
	 * @param restRequest
	 * @return
	 */
	private Element queryService(String restRequest){
		
		try {
			// create the request url
			URL newUrl = new URL(serviceUrl.getProtocol(),
								 serviceUrl.getHost(),
								 serviceUrl.getPort(),
								 serviceUrl.getPath() 
								 + restRequest);
			// open a connection 
			HttpURLConnection connection = (HttpURLConnection) newUrl.openConnection();
			// set the accept property to XML so we can use jdom to handle the content
			connection.setRequestProperty("Accept", "text/xml");
		
			
			logger.info("Firing request for URL: " + newUrl);
				
			int responseCode = connection.getResponseCode();
			
			// get the content at the resource
			InputStream content = (InputStream) connection.getContent();
			
			// build the jdom document
			Document document = builder.build(content);
			
			if(responseCode == HttpURLConnection.HTTP_OK){
				return document.getRootElement();
			}else{
				XMLOutputter outputter = new XMLOutputter();
				String output = outputter.outputString(document.getRootElement());
				logger.error(output);
			}
			
		} catch (IOException e) {
			logger.error("No content for request: " + restRequest);
		} catch (JDOMException e) {
			logger.error("Error building the document.", e);
		}
		
		// error
		return null;
	}
	
	/**
	 * The access point of a CDM Community Server
	 * 
	 * @return the serviceUrl
	 */
	public URL getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * The CDM Community Servers access point
	 * 
	 * @param serviceUrl
	 */
	public void setServiceUrl(URL serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
}
