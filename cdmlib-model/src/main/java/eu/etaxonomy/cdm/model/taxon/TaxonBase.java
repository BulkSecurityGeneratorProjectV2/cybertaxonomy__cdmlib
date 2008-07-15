/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * The upmost (abstract) class for the use of a {@link name.TaxonNameBase taxon name} in a {@link reference.ReferenceBase reference}
 * or within a taxonomic view/treatment either as a {@link Taxon taxon}
 * ("accepted" respectively "correct" name) or as a (junior) {@link Synonym synonym}.
 * Within a taxonomic view/treatment or a reference a taxon name can be used
 * only in one of both described meanings. The reference using the taxon name
 * is generally cited with "sec." (secundum, sensu). For instance:
 * "Juncus longirostris Kuvaev sec. Kirschner, J. et al. 2002".
 * <P>
 * This class corresponds to: <ul>
 * <li> TaxonConcept according to the TDWG ontology
 * <li> TaxonConcept according to the TCS
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:56
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonBase", propOrder = {
    "name",
    "sec"
})
@Entity
@Table(appliesTo="TaxonBase", indexes = { @Index(name = "taxonBaseTitleCacheIndex", columnNames = { "persistentTitleCache" }) })
public abstract class TaxonBase extends IdentifiableEntity {
	
	static Logger logger = Logger.getLogger(TaxonBase.class);
	
	private static Method methodTaxonNameAddTaxonBase;
	
	private static void initMethods()  { 
		if (methodTaxonNameAddTaxonBase == null){
			try {
				methodTaxonNameAddTaxonBase = TaxonNameBase.class.getDeclaredMethod("addTaxonBase", TaxonBase.class);
				methodTaxonNameAddTaxonBase.setAccessible(true);
			} catch (Exception e) {
				e.printStackTrace();
				//TODO handle exception
			}
		}
	}
	
	//The assignment to the Taxon or to the Synonym class is not definitive
    @XmlAttribute(name = "isDoubtful")
	private boolean isDoubtful;
	
    @XmlElement(name = "Name", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private TaxonNameBase name;
	
	// The concept reference
    @XmlElement(name = "Sec")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private ReferenceBase sec;

	
// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty (abstract) taxon.
	 * 
	 * @see 	#TaxonBase(TaxonNameBase, ReferenceBase)
	 */
	protected TaxonBase(){
		super();
	}
	
	/** 
	 * Class constructor: creates a new (abstract) taxon with the
	 * {@link name.TaxonNameBase taxon name} used and the {@link reference.ReferenceBase reference}
	 * using it.
	 * 
	 * @param  taxonNameBase	the taxon name used
	 * @param  sec				the reference using the taxon name
	 * @see    #TaxonBase()
	 */
	protected TaxonBase(TaxonNameBase taxonNameBase, ReferenceBase sec){
		super();
		if (taxonNameBase != null){
			initMethods(); 
			this.invokeSetMethod(methodTaxonNameAddTaxonBase, taxonNameBase);  
		}
		this.setSec(sec);
	}

//********* METHODS **************************************/

	/**
	 * Generates and returns the string with the full scientific name (including
	 * authorship) of the {@link name.TaxonNameBase taxon name} used in this
	 * (abstract) taxon as well as the title of the {@link reference.ReferenceBase reference} using
	 * this taxon name. This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.
	 * This method overrides the generic and inherited
	 * IdentifiableEntity#generateTitle() method.
	 *
	 * @return  the string with the full scientific name of the taxon name
	 *			and with the title of the reference involved in this (abstract) taxon
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 */
	@Override
	public String generateTitle() {
		String title;
		if (name != null && name.getTitleCache() != null){
			title = name.getTitleCache() + " sec. ";
			if (sec != null){
				title += sec.getTitleCache();
			}else{
				title += "???";
			}
		}else{
			title = this.toString();
		}
		return title;
	}
	
	/** 
	 * Returns the {@link name.TaxonNameBase taxon name} used in this (abstract) taxon.
	 */
	@ManyToOne
	@JoinColumn(name="taxonName_fk")
	@Cascade(CascadeType.SAVE_UPDATE)
	public TaxonNameBase getName(){
		return this.name;
	}
	@Deprecated //for hibernate use only, use taxon.addDescription() instead
	private void setName(TaxonNameBase newName){
		this.name = newName;
	}
	
	/** 
	 * Returns the {@link name.HomotypicalGroup homotypical group} of the
	 * {@link name.TaxonNameBase taxon name} used in this (abstract) taxon.
	 */
	@Transient
	public HomotypicalGroup getHomotypicGroup(){
		if (this.getName() == null){
			return null;
		}else{
			return this.getName().getHomotypicalGroup();
		}
	}

	/**
	 * Returns the boolean value indicating whether the assignment of this
	 * (abstract) taxon to the {@link Taxon Taxon} or to the {@link Synonym Synonym} class is definitive
	 * (false) or not (true). If this flag is set the use of this (abstract)
	 * taxon as an "accepted/correct" name or as a (junior) "synonym" might
	 * still change in the course of taxonomical working process. 
	 */
	public boolean isDoubtful(){
		return this.isDoubtful;
	}
	/**
	 * @see  #isDoubtful()
	 */
	public void setDoubtful(boolean isDoubtful){
		this.isDoubtful = isDoubtful;
	}

	/** 
	 * Returns the {@link reference.ReferenceBase reference} of this (abstract) taxon.
	 * This is the reference or the treatment using the {@link name.TaxonNameBase taxon name}
	 * in this (abstract) taxon.
	 */
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	public ReferenceBase getSec() {
		return sec;
	}

	/**
	 * @see  #getSec()
	 */
	public void setSec(ReferenceBase sec) {
		this.sec = sec;
	}
	
	/**
	 * Returns the boolean value indicating whether this (abstract) taxon
	 * might be saved (true) or not (false). An (abstract) taxon is meaningful
	 * as long as both the {@link name.TaxonNameBase taxon name} and the {@link reference.ReferenceBase reference}
	 * exist (are not "null").
	 */
	@Transient
	public boolean isSaveable(){
		if (  (this.getName() == null)  ||  (this.getSec() == null)  ){
			return false;
		}else{
			this.toString();
			return true;
		}
	}
	

}