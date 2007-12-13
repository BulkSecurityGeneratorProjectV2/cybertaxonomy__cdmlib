/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.taxon.Synonym;


/**
 * This Simple Transfer Object (STO) is used as surrogate for instances of {@link Taxon} and {@link Synonym},  
 * thus a TaxonSTO my represent acceptet ad unaccepted taxa. Therfore the flag isAccepted has been introduced 
 * by which accepted taxa can be clearly identified.
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 13.12.2007 14:55:32
 *
 */
public class TaxonSTO {
	
	private NameSTO name;
	private UUID sensu;
	private boolean isAccepted;
	
}
