/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.tcsrdf;

import org.apache.log4j.Logger;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
public class CdmUnclearMapper extends CdmSingleAttributeRDFMapperBase {
	private static final Logger logger = Logger.getLogger(CdmUnclearMapper.class);
	
	
	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmUnclearMapper(String sourceElementString, String sourceNamespace) {
		super(sourceElementString, null);
		this.sourceNameSpace = sourceNamespace;
	}

	/**
	 * @param dbValue
	 * @param cdmValue
	 */
	public CdmUnclearMapper(String dbAttributString) {
		super(dbAttributString, null);
	}
	
	public String getSourceNamespace(){
		return sourceNameSpace;
	}
	
	public Class getTypeClass(){
		return String.class;
	}
	
	public boolean mapsSource(Resource content, Statement parentElement){
		return super.mapsSource(content, parentElement);
	}
	
	public String toString(){
		return this.getSourceElement();
	}

	


}
