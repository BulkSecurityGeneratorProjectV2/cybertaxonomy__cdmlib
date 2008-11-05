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
import eu.etaxonomy.cdm.model.common.Language;

import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * This class represents measurement units such as "centimeter" or "degree
 * Celsius".
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
 */
@Entity
public class MeasurementUnit extends DefinedTermBase {
	static Logger logger = Logger.getLogger(MeasurementUnit.class);
	
	/** 
	 * Class constructor: creates a new empty measurement unit instance.
	 * 
	 * @see #MeasurementUnit(String, String, String)
	 */
	protected MeasurementUnit(){
		super();
	}

	/** 
	 * Creates a new measurement unit with a description
	 * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new measurement unit to be created 
	 * @param	label  		 the string identifying the new measurement unit
	 * 						 to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new measurement unit to be created
	 * @see 				 #NewInstance()
	 */
	public MeasurementUnit(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
	/** 
	 * Creates a new empty measurement unit instance.
	 * 
	 * @see #MeasurementUnit(String, String, String)
	 */
	public static MeasurementUnit NewInstance(){
		return new MeasurementUnit();
	}
	
	/** 
	 * Creates a new empty measurement unit instance.
	 * 
	 * @see #MeasurementUnit(String, String, String)
	 */
	public static MeasurementUnit NewInstance(String term, String label, String labelAbbrev){
		return new MeasurementUnit(term, label, labelAbbrev);
	}
}