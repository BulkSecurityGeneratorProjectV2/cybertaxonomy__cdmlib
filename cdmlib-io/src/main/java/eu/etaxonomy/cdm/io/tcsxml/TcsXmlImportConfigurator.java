 package eu.etaxonomy.cdm.io.tcsxml;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class TcsXmlImportConfigurator extends ImportConfiguratorBase implements IImportConfigurator {

	private static final Logger logger = Logger.getLogger(TcsXmlImportConfigurator.class);
	
	private boolean doMetaData = true;
	private boolean doSpecimen = true;

	private Method functionMetaDataDetailed = null; 
	private ITcsXmlPlaceholderClass placeholderClass;
	
	//	rdfNamespace
	Namespace tcsXmlNamespace;

	protected static Namespace nsTcsXml = Namespace.getNamespace("http://www.tdwg.org/schemas/tcs/1.01");

		
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			TcsXmlMetaDataIO.class
			, TcsXmlSpecimensIO.class
			, TcsXmlPublicationsIO.class
			, TcsXmlTaxonNameIO.class
			, TcsXmlTaxonNameRelationsIO.class
		};
	};
	
	public static TcsXmlImportConfigurator NewInstance(String url,
			ICdmDataSource destination){
		return new TcsXmlImportConfigurator(url, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private TcsXmlImportConfigurator(String url, ICdmDataSource destination) {
		super();
		setSource(url);
		setDestination(destination);
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
		//String strTnNamespace = "http://rs.tdwg.org/ontology/voc/TaxonName#";
		//Namespace taxonNameNamespace = Namespace.getNamespace("tn", strTnNamespace);

		String prefix;
		tcsXmlNamespace = root.getNamespace();
		if (tcsXmlNamespace == null 
				/**|| tcNamespace == null 
				 * || tnNamespace == null 
				 * || commonNamespace == null 
				 * ||	geoNamespace == null 
				 * || publicationNamespace == null*/){
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
	
	public Namespace getTcsXmlNamespace() {
		return tcsXmlNamespace;
	}

	public void setTcsXmlNamespace(Namespace tcsXmlNamespace) {
		this.tcsXmlNamespace = tcsXmlNamespace;
	}
	

	/**
	 * @return the funMetaDataDetailed
	 */
	public Method getFunctionMetaDataDetailed() {
		if (functionMetaDataDetailed == null){
			functionMetaDataDetailed = getDefaultFunction(TcsXmlMetaDataIO.class, "defaultMetaDataDetailedFunction");
		}
		return functionMetaDataDetailed;
		
	}

	/**
	 * @param funMetaDataDetailed the funMetaDataDetailed to set
	 */
	public void setFunctionMetaDataDetailed(Method functionMetaDataDetailed) {
		this.functionMetaDataDetailed = functionMetaDataDetailed;
	}
	
	/**
	 * @return the doMetaData
	 */
	public boolean isDoMetaData() {
		return doMetaData;
	}

	/**
	 * @param doMetaData the doMetaData to set
	 */
	public void setDoMetaData(boolean doMetaData) {
		this.doMetaData = doMetaData;
	}


	/**
	 * @return the doSpecimen
	 */
	public boolean isDoSpecimen() {
		return doSpecimen;
	}

	/**
	 * @param doSpecimen the doSpecimen to set
	 */
	public void setDoSpecimen(boolean doSpecimen) {
		this.doSpecimen = doSpecimen;
	}

	/**
	 * @return the placeholderClass
	 */
	public ITcsXmlPlaceholderClass getPlaceholderClass() {
		if (placeholderClass == null){
			placeholderClass = new DefaultTcsXmlPlaceholders();
		}
		return placeholderClass;
	}

	/**
	 * @param placeholderClass the placeholderClass to set
	 */
	public void setPlaceholderClass(ITcsXmlPlaceholderClass placeholderClass) {
		this.placeholderClass = placeholderClass;
	}

	
}
