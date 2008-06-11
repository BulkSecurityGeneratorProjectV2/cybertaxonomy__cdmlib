/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * This class contains the measurement units such as "centimeter" or "degree
 * Celsius"
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
 */
@Entity
public class MeasurementUnit extends DefinedTermBase {
	static Logger logger = Logger.getLogger(MeasurementUnit.class);
	
	/**
	 * Factory method
	 * @return
	 */
	public static MeasurementUnit NewInstance(){
		return new MeasurementUnit();
	}
	
	/**
	 * Constructor
	 */
	protected MeasurementUnit(){
		super();
	}
	

	public MeasurementUnit(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

}