/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Transient;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;


 /**
 * Interface providing methods for nomenclatural references. 
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:29
 */
public interface INomenclaturalReference<T extends ReferenceBase> extends IIdentifiableEntitiy<T>{

	public final String MICRO_REFERENCE_TOKEN = "@@MicroReference";
	
	/**
	 * Returns a formatted string containing the reference citation excluding
	 * authors but including the details as used in a {@link name.TaxonNameBase taxon name}.
	 * 
	 * @see	name.TaxonNameBase
	 */
	@Transient
	public String getNomenclaturalCitation(String  microReference);

	/**
	 * Returns a string representation for the year of publication / creation
	 * of a reference.
	 */
	@Transient
	public String getYear();

	/**
	 * Returns the boolean value indicating whether the used parser 
	 * method was able to parse the string designating the reference
	 * successfully (false) or not (true).
	 */
	public boolean getHasProblem();
	
}