/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.reference.endnote.in;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

@Component
public class EndnoteImportConfigurator extends ImportConfiguratorBase<EndnoteImportState> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(EndnoteImportConfigurator.class);
	
	public static EndnoteImportConfigurator NewInstance(String url,
			ICdmDataSource destination){
		return new EndnoteImportConfigurator(url, destination);
	}
	
	private boolean doRecords = true;
//	private boolean doSpecimen = true;

	private Method functionRecordsDetailed = null; 
	private IEndnotePlaceholderClass placeholderClass;
	
	//	rdfNamespace
	Namespace EndnoteNamespace;

	protected void makeIoClassList(){
		ioClassList = new Class[]{
			EndnoteRecordsImport.class
		};
	};

	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private EndnoteImportConfigurator() {
		super();
//		setSource(url);
//		setDestination(destination);
	}
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private EndnoteImportConfigurator(String url, ICdmDataSource destination) {
		super();
		setSource(url);
		setDestination(destination);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public EndnoteImportState getNewState() {
		return new EndnoteImportState(this);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSource()
	 */
	public String getSource() {
		return (String)super.getSource();
	}
	
	/**
	 * @param file
	 */
	public void setSource(String file) {
		super.setSource(file);
	}
	
	/**
	 * @return
	 */
	public Element getSourceRoot(){
		String source = getSource();
		try {
			URL url;
			url = new URL(source);
			Object o = url.getContent();
			InputStream is = (InputStream)o;
			Element root = XmlHelp.getRoot(is);
			makeNamespaces(root);
			return root;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean makeNamespaces(Element root){
		EndnoteNamespace = root.getNamespace();
		if (EndnoteNamespace == null){
			logger.warn("At least one Namespace is NULL");
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public ReferenceBase getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = Database.NewInstance();
			sourceReference.setTitleCache("XXX");
		}
		return sourceReference;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource();
		}
	}
	
	public Namespace getEndnoteNamespace() {
		return EndnoteNamespace;
	}

	public void setEndnoteNamespace(Namespace EndnoteNamespace) {
		this.EndnoteNamespace = EndnoteNamespace;
	}
	

	/**
	 * @return the funMetaDataDetailed
	 */
	public Method getFunctionRecordsDetailed() {
		if (functionRecordsDetailed == null){
			functionRecordsDetailed = getDefaultFunction(EndnoteRecordsImport.class, "defaultRecordsDetailedFunction");
		}
		return functionRecordsDetailed;
		
	}

	/**
	 * @param funMetaDataDetailed the funMetaDataDetailed to set
	 */
	public void setFunctionRecordsDetailed(Method functionRecordsDetailed) {
		this.functionRecordsDetailed = functionRecordsDetailed;
	}
	
	/**
	 * @return the doMetaData
	 */
	public boolean isDoRecords() {
		return doRecords;
	}

	/**
	 * @param doMetaData the doMetaData to set
	 */
	public void setDoRecords(boolean doRecords) {
		this.doRecords = doRecords;
	}

	/**
	 * @return the doSpecimen
	 */
//	public boolean isDoSpecimen() {
//		return doSpecimen;
//	}

	/**
	 * @param doSpecimen the doSpecimen to set
	 */
//	public void setDoSpecimen(boolean doSpecimen) {
//		this.doSpecimen = doSpecimen;
//	}

	/**
	 * @return the placeholderClass
	 */
	public IEndnotePlaceholderClass getPlaceholderClass() {
		if (placeholderClass == null){
			placeholderClass = new IEndnotePlaceholderClass();
		}
		return placeholderClass;
	}

	/**
	 * @param placeholderClass the placeholderClass to set
	 */
	public void setPlaceholderClass(IEndnotePlaceholderClass placeholderClass) {
		this.placeholderClass = placeholderClass;
	}
	
}
