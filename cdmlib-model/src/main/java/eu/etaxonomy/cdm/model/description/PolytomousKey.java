// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.tool.hbm2x.StringUtils;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * 
 * The class allowing the representation of single-access fixed dichotomous or
 * polytomous authored decision keys used to identify
 * {@link SpecimenOrObservationBase specimens or observations} (this means to
 * assign {@link Taxon taxa} to).
 * The different paths are expressed by a {@link PolytomousKey decision tree}.
 * 
 * @author h.fradin
 * @created 13.08.2009
 * @version 1.0
 * 
 * @author a.mueller
 * @created 08.11.2010
 * @version 2.0

 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolytomousKey", propOrder = {
    "coveredTaxa",
    "taxonomicScope",
    "geographicalScope",
    "scopeRestrictions",
    "root"
})
@XmlRootElement(name = "PolytomousKey")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.media.FeatureTree")
@Audited
public class PolytomousKey extends IdentifiableEntity implements IIdentificationKey{
	private static final long serialVersionUID = -3368243754557343942L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PolytomousKey.class);
	
	@XmlElementWrapper(name = "CoveredTaxa")
	@XmlElement(name = "CoveredTaxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@NotNull
	private Set<Taxon> coveredTaxa = new HashSet<Taxon>();
	
	@XmlElementWrapper(name = "TaxonomicScope")
	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
	        name="PolytomousKey_Taxon",
	        joinColumns=@JoinColumn(name="polytomousKey_fk"),
	        inverseJoinColumns=@JoinColumn(name="taxon_fk")
	)
	@NotNull
	private Set<Taxon> taxonomicScope = new HashSet<Taxon>();
	
	@XmlElementWrapper( name = "GeographicalScope")
	@XmlElement( name = "Area")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="PolytomousKey_NamedArea")
	@NotNull
	private Set<NamedArea> geographicalScope = new HashSet<NamedArea>();
	
	@XmlElementWrapper( name = "ScopeRestrictions")
	@XmlElement( name = "Restriction")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="PolytomousKey_Scope")
	@NotNull
	private Set<Scope> scopeRestrictions = new HashSet<Scope>();
	
	@XmlElement(name = "Root")
	@OneToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private PolytomousKeyNode root;
	
//******************************** STATIC METHODS ********************************/	
	
	/** 
	 * Creates a new empty identification multi-access key instance.
	 */
	public static PolytomousKey NewInstance(){
		return new PolytomousKey();
	}
	
	/** 
	 * Creates a new empty identification polytomous key instance.
	 */
	public static PolytomousKey NewTitledInstance(String title){
		PolytomousKey result = new PolytomousKey();
		result.setTitleCache(title, true);
		return result;
	}
	
	
// ************************** CONSTRUCTOR *******************************************/

	/** 
	 * Class constructor: creates a new empty multi-access key instance.
	 */
	protected PolytomousKey() {
		super();
		root = PolytomousKeyNode.NewInstance();
	}
	
	
//************************ GETTER/ SETTER 
	
	
	/** 
	 * Returns the topmost {@link FeatureNode feature node} (root node) of <i>this</i>
	 * feature tree. The root node does not have any parent. Since feature nodes
	 * recursively point to their child nodes the complete feature tree is
	 * defined by its root node.
	 */
	public PolytomousKeyNode getRoot() {
		return root;
	}
	/**
	 * @see	#getRoot() 
	 */
	public void setRoot(PolytomousKeyNode root) {
		this.root = root;
	}
	
	/** 
	 * Returns the set of possible {@link Taxon taxa} corresponding to
	 * <i>this</i> identification key.
	 */
	public Set<Taxon> getCoveredTaxa() {
		if(coveredTaxa == null) {
			this.coveredTaxa = new HashSet<Taxon>();
		}
		return coveredTaxa;
	}
	/**
	 * @see	#getCoveredTaxa() 
	 */
	protected void setCoveredTaxa(Set<Taxon> coveredTaxa) {
		this.coveredTaxa = coveredTaxa;
	}
	
	/**
	 * Adds a {@link Taxon taxa} to the set of {@link #getCoveredTaxa() covered taxa}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	taxon	the taxon to be added to <i>this</i> identification key
	 * @see    	   		#getCoveredTaxa()
	 */
	public void addCoveredTaxon(Taxon taxon) {
		this.coveredTaxa.add(taxon);
	}
	
	/** 
	 * Removes one element from the set of {@link #getCoveredTaxa() covered taxa}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	taxon	the taxon which should be removed
	 * @see     		#getCoveredTaxa()
	 * @see     		#addCoveredTaxon(Taxon)
	 */
	public void removeCoveredTaxon(Taxon taxon) {
		this.coveredTaxa.remove(taxon);
	}

	/** 
	 * Returns the set of {@link NamedArea named areas} indicating the geospatial
	 * data where <i>this</i> identification key is valid.
	 */
	public Set<NamedArea> getGeographicalScope() {
		if(geographicalScope == null) {
			this.geographicalScope = new HashSet<NamedArea>();
		}
		return geographicalScope;
	}
	
	/**
	 * Adds a {@link NamedArea geoScope} to the set of {@link #getGeoScopes() geogspatial scopes}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	geoScope	the named area to be added to <i>this</i> identification key
	 * @see    	   		 	#getGeoScopes()
	 */
	public void addGeographicalScope(NamedArea geoScope) {
		this.geographicalScope.add(geoScope);
	}
	/** 
	 * Removes one element from the set of {@link #getGeoScopes() geogspatial scopes}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	geoScope	the named area which should be removed
	 * @see     			#getGeoScopes()
	 * @see     			#addGeoScope(NamedArea)
	 */
	public void removeGeographicalScope(NamedArea geoScope) {
		this.geographicalScope.remove(geoScope);
	}

	/** 
	 * Returns the set of {@link Taxon taxa} that define the taxonomic
	 * scope of <i>this</i> identification key 
	 */
	public Set<Taxon> getTaxonomicScope() {
		if(taxonomicScope == null) {
			this.taxonomicScope = new HashSet<Taxon>();
		}
		return taxonomicScope;
	}
	
	/**
	 * Adds a {@link Taxon taxa} to the set of {@link #getTaxonomicScope() taxonomic scopes}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	taxon	the taxon to be added to <i>this</i> identification key
	 * @see    	   		#getTaxonomicScope()
	 */
	public void addTaxonomicScope(Taxon taxon) {
		this.taxonomicScope.add(taxon);
	}
	
	/** 
	 * Removes one element from the set of {@link #getTaxonomicScope() taxonomic scopes}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	taxon	the taxon which should be removed
	 * @see     		#getTaxonomicScope()
	 * @see     		#addTaxonomicScope(Taxon)
	 */
	public void removeTaxonomicScope(Taxon taxon) {
		this.taxonomicScope.remove(taxon);
	}
	
	/** 
	 * Returns the set of {@link Scope scope restrictions} corresponding to
	 * <i>this</i> identification key 
	 */
	public Set<Scope> getScopeRestrictions() {
		if(scopeRestrictions == null) {
			this.scopeRestrictions = new HashSet<Scope>();
		}
		return scopeRestrictions;
	}
	
	/**
	 * Adds a {@link Scope scope restriction} to the set of {@link #getScopeRestrictions() scope restrictions}
	 * corresponding to <i>this</i> identification key.
	 * 
	 * @param	scopeRestriction	the scope restriction to be added to <i>this</i> identification key
	 * @see    	   		#getScopeRestrictions()
	 */
	public void addScopeRestriction(Scope scopeRestriction) {
		this.scopeRestrictions.add(scopeRestriction);
	}
	
	/** 
	 * Removes one element from the set of {@link #getScopeRestrictions() scope restrictions}
	 * corresponding to <i>this</i> identification key.
	 *
	 * @param	scopeRestriction	the scope restriction which should be removed
	 * @see     		#getScopeRestrictions()
	 * @see     		#addScopeRestriction(Scope)
	 */
	public void removeScopeRestriction(Scope scopeRestriction) {
		this.scopeRestrictions.remove(scopeRestriction);
	}
	
//******************** toString *****************************************/
	
	private class IntegerObject{
		int number = 0;
		int inc(){return number++;};
		@Override 
		public String toString(){ 
			return String.valueOf(number);
		}
	}
	
	public String print(PrintStream stream){
		String title = this.getTitleCache() + "\n";
		String strPrint = title;
		
		if (stream != null){
			stream.print(title);
		}
		
		PolytomousKeyNode root = this.getRoot();
		IntegerObject no = new IntegerObject();
		no.inc();
		strPrint += printNode(root, "  ", no, "Root", stream);
		return strPrint;
	}

	
	/**
	 * TODO this is a preliminary implementation
	 * @param node
	 * @param identation
	 * @param no
	 * @param myNumber
	 * @param stream
	 * @return
	 */
	private String printNode(PolytomousKeyNode node, String identation, IntegerObject no, String myNumber, PrintStream stream) {
		int myInt = no.number;
		String result = identation + myNumber + ". ";
		if (node != null){
			if (! node.isLeaf()){
				if (node.getStatement() != null){
					String question = node.getQuestion().getLabelText(Language.DEFAULT());
					result +=  ( question == null ? "" : (question))  ;
				}
			}else{
				PolytomousKeyNode leaf = node;
				if (leaf.getTaxon() != null){
					String taxonSeparator = ", ";
					result += ": ";
					String strTaxon = "";
					if (leaf.getTaxon().getName() != null){
						strTaxon = leaf.getTaxon().getName().getTitleCache() ;
					}else{
						strTaxon = leaf.getTaxon().getTitleCache() ;
					}
					
					result +=  strTaxon + taxonSeparator ;
					result = StringUtils.chompLast(result, taxonSeparator);
				}
				
				
			}
			result += "\n";
			if (stream != null){
				stream.print(result);
			}
			char nextCounter = 'a';
			if (! node.getChildren().isEmpty()){
				no.inc();
			}
			for (PolytomousKeyNode child : node.getChildren()){
				String nextNumber = myInt + String.valueOf(nextCounter++);
				result += printNode(child, identation + "  ", no, nextNumber, stream);
			}
		}
		return result;
	}
//
//	public List<PolytomousKeyNode> getChildren() {
//		return getRoot().getChildren();
//	}

}
