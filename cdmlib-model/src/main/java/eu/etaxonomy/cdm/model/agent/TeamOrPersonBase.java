/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.agent;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.cache.agent.INomenclaturalAuthorCacheStrategy;


/**
 * The abstract class for such {@link Agent agents} ({@link Person persons} or {@link Team teams}) who might also be used
 * for authorship of {@link reference.ReferenceBase references} or of {@link name.TaxonNameBase taxon names}.
 * 
 * @author a.mueller
 * @version 1.0
 * @created 17-APR-2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeamOrPersonBase", propOrder = {
	"nomenclaturalTitle"
})
@Entity
public abstract class TeamOrPersonBase<T extends TeamOrPersonBase> extends Agent implements INomenclaturalAuthor {
	static Logger logger = Logger.getLogger(TeamOrPersonBase.class);

	@XmlElement(name="NomenclaturalTitle")
	protected String nomenclaturalTitle;
	
	@XmlTransient
	protected INomenclaturalAuthorCacheStrategy<T> cacheStrategy;

	/**
	 * Returns the identification string (nomenclatural abbreviation) used in
	 * nomenclature for this {@link Person person} or this {@link Team team}.
	 * 
	 * @see  INomenclaturalAuthor#getNomenclaturalTitle()
	 */
	public String getNomenclaturalTitle() {
		return nomenclaturalTitle;
	}

	/** 
	 * @see     #getNomenclaturalTitle()
	 */
	public void setNomenclaturalTitle(String nomenclaturalTitle) {
		this.nomenclaturalTitle = nomenclaturalTitle;
	}

}
