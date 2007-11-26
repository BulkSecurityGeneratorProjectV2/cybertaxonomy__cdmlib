/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:55
 */
@Entity
public class SynonymRelationship extends ReferencedEntityBase {
	static Logger logger = Logger.getLogger(SynonymRelationship.class);
	private Synonym synoynm;
	private Taxon acceptedTaxon;
	private SynonymRelationshipType type;

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Taxon getAcceptedTaxon(){
		return this.acceptedTaxon;
	}

	public void setAcceptedTaxon(Taxon acceptedTaxon){
		this.acceptedTaxon = acceptedTaxon;
	}

	
	@ManyToOne
	public SynonymRelationshipType getType(){
		return this.type;
	}
	public void setType(SynonymRelationshipType type){
		this.type = type;
	}

	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Synonym getSynoynm(){
		return this.synoynm;
	}

	public void setSynoynm(Synonym synoynm){
		this.synoynm = synoynm;
	}

}