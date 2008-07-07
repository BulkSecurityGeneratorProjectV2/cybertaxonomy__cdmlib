/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;

@Entity
public class TaxonNameDescription extends DescriptionBase {
	static Logger logger = Logger.getLogger(TaxonNameDescription.class);
	
	private TaxonNameBase taxonName;


	/**
	 * Factory method
	 * @return
	 */
	public static TaxonNameDescription NewInstance(){
		return new TaxonNameDescription();
	}
	
	/**
	 * Constructor
	 */
	public TaxonNameDescription() {
		super();
	}
	
	
	@ManyToOne
	@JoinColumn(name="taxonName_fk")
	@Cascade(CascadeType.SAVE_UPDATE)
	public TaxonNameBase getTaxonName() {
		return taxonName;
	}
	@Deprecated //for hibernate use only, use taxonName.addDescription() instead
	protected void setTaxonName(TaxonNameBase taxonName) {
		this.taxonName = taxonName;
	}


}
