/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;

import eu.etaxonomy.cdm.jaxb.FormattedTextAdapter;
import eu.etaxonomy.cdm.jaxb.LSIDAdapter;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NonViralName;

/**
 * Superclass for the primary CDM classes that can be referenced from outside via LSIDs and contain a simple generated title string as a label for human reading.
 * All subclasses inherit the ability to store additional properties that are stored as {@link Extension Extensions}, basically a string value with a type term.
 * Any number of right statements can be attached as well as multiple {@link OriginalSource} objects. 
 * Original sources carry a reference to the source, an ID within that source and the original title/label of this object as it was used in that source (originalNameString).
 * A Taxon for example that was taken from 2 sources like FaunaEuropaea and IPNI would have two originalSource objects.
 * The originalSource representing that taxon as it was found in IPNI would contain IPNI as the reference, the IPNI id of the taxon and the name of the taxon exactly as it was used in IPNI.
 *  
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:27
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentifiableEntity", propOrder = {
    "lsid",
    "titleCache",
    "protectedTitleCache",
    "rights",
    "extensions",
    "sources"
})
@MappedSuperclass
public abstract class IdentifiableEntity extends AnnotatableEntity 
implements ISourceable, IIdentifiableEntity, Comparable<IdentifiableEntity> {
	private static final long serialVersionUID = -5610995424730659058L;
	private static final Logger logger = Logger.getLogger(IdentifiableEntity.class);

	@XmlTransient
	public static final boolean PROTECTED = true;
	@XmlTransient
	public static final boolean NOT_PROTECTED = false;
	
	@XmlElement(name = "LSID", type = String.class)
	@XmlJavaTypeAdapter(LSIDAdapter.class)
	@Embedded
	private LSID lsid;
	
	@XmlElement(name = "TitleCache", required = true)
	@XmlJavaTypeAdapter(FormattedTextAdapter.class)
	@Column(length=255, name="titleCache")
	@Fields({@Field(index = org.hibernate.search.annotations.Index.TOKENIZED),
	     	 @Field(name = "titleCache_forSort", index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
	})
	@FieldBridge(impl=StripHtmlBridge.class)
	private String titleCache;
	
	//if true titleCache will not be automatically generated/updated
	@XmlElement(name = "ProtectedTitleCache")
	private boolean protectedTitleCache;
	
    @XmlElementWrapper(name = "Rights")
    @XmlElement(name = "Rights")
    @OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<Rights> rights = new HashSet<Rights>();
	
    @XmlElementWrapper(name = "Extensions")
    @XmlElement(name = "Extension")
    @OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<Extension> extensions = new HashSet<Extension>();
	
    @XmlElementWrapper(name = "Sources")
    @XmlElement(name = "OriginalSource")
    @OneToMany(fetch = FetchType.LAZY)		
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<OriginalSource> sources = new HashSet<OriginalSource>();

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getLsid()
	 */
	public LSID getLsid(){
		return this.lsid;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#setLsid(java.lang.String)
	 */
	public void setLsid(LSID lsid){
		this.lsid = lsid;
	}

	/**
	 * By default, we expect most cdm objects to be abstract things 
	 * i.e. unable to return a data representation.
	 * 
	 * Specific subclasses (e.g. Sequence) can override if necessary.
	 */
	public byte[] getData() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#generateTitle()
	 */
	public abstract String generateTitle();

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getTitleCache()
	 */
	public String getTitleCache(){
		if (protectedTitleCache){
			return this.titleCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (titleCache == null){
			this.setTitleCache(generateTitle(),protectedTitleCache) ; //for truncating
		}
		return titleCache;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#setTitleCache(java.lang.String)
	 */
	public void setTitleCache(String titleCache){
		setTitleCache(titleCache, PROTECTED);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#setTitleCache(java.lang.String, boolean)
	 */
	public void setTitleCache(String titleCache, boolean protectCache){
		//TODO truncation of title cache
		if (titleCache != null && titleCache.length() > 254){
			logger.warn("Truncation of title cache: " + this.toString() + "/" + titleCache);
			titleCache = titleCache.substring(0, 249) + "...";
		}
		this.titleCache = titleCache;
		this.setProtectedTitleCache(protectCache);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getRights()
	 */
	public Set<Rights> getRights() {
		return this.rights;		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#addRights(eu.etaxonomy.cdm.model.media.Rights)
	 */
	public void addRights(Rights right){
		this.rights.add(right);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#removeRights(eu.etaxonomy.cdm.model.media.Rights)
	 */
	public void removeRights(Rights right){
		this.rights.remove(right);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getExtensions()
	 */
	public Set<Extension> getExtensions(){
		return this.extensions;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#addExtension(eu.etaxonomy.cdm.model.common.Extension)
	 */
	public void addExtension(Extension extension){
		this.extensions.add(extension);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#removeExtension(eu.etaxonomy.cdm.model.common.Extension)
	 */
	public void removeExtension(Extension extension){
		this.extensions.remove(extension);
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#isProtectedTitleCache()
	 */
	public boolean isProtectedTitleCache() {
		return protectedTitleCache;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#setProtectedTitleCache(boolean)
	 */
	public void setProtectedTitleCache(boolean protectedTitleCache) {
		this.protectedTitleCache = protectedTitleCache;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getSources()
	 */
	public Set<OriginalSource> getSources() {
		return this.sources;		
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#addSource(eu.etaxonomy.cdm.model.common.OriginalSource)
	 */
	public void addSource(OriginalSource source) {
		if (source != null){
			IdentifiableEntity oldSourcedObj = source.getSourcedObj();
			if (oldSourcedObj != null && oldSourcedObj != this){
				oldSourcedObj.getSources().remove(source);
			}
			this.sources.add(source);
			source.setSourcedObj(this);
		}
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#removeSource(eu.etaxonomy.cdm.model.common.OriginalSource)
	 */
	public void removeSource(OriginalSource source) {
		this.sources.remove(source);		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#toString()
	 */
	 @Override
	public String toString() {
		String result;
		if (titleCache == null){
			result = super.toString();
		}else{
			result = this.titleCache;
		}
		return result;	
	}
	 
	 public int compareTo(IdentifiableEntity identifiableEntity) {

		 int result = 0;
		 
		 if (identifiableEntity == null) {
			 throw new NullPointerException("Cannot compare to null.");
		 }

		 // First, compare the name cache.
		 // TODO: Avoid using instanceof operator
		 // Use Class.getDeclaredMethod() instead to find out whether class has getNameCache() method?

		 if ((identifiableEntity instanceof NonViralName) && (this instanceof NonViralName)) {

			 String thisNameCache = ((NonViralName<?>)this).getNameCache();
			 String specifiedNameCache = ((NonViralName<?>)identifiableEntity).getNameCache();
			 result = thisNameCache.compareTo(specifiedNameCache);
		 }
		 
		 // Compare the title cache if name cache is equal
		 if (result == 0) {
			 String thisTitleCache = getTitleCache();
			 String specifiedTitleCache = identifiableEntity.getTitleCache();
			 result = thisTitleCache.compareTo(specifiedTitleCache);
			 
		 }
		 return result;
	 }
	 
//****************** CLONE ************************************************/
	 
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		IdentifiableEntity result = (IdentifiableEntity)super.clone();
		
		//Extensions
		result.extensions = new HashSet<Extension>();
		for (Extension extension : this.extensions ){
			Extension newExtension = (Extension)extension.clone();
			result.addExtension(newExtension);
		}
		
		//OriginalSources
		result.sources = new HashSet<OriginalSource>();
		for (OriginalSource originalSource : this.sources){
			OriginalSource newSource = (OriginalSource)originalSource.clone();
			result.addSource(newSource);
		}
		
		//Rights
		result.rights = new HashSet<Rights>();
        for(Rights rights : this.rights) {
        	result.addRights(rights);
        }
		
		//result.setLsid(lsid);
		//result.setTitleCache(titleCache); 
		//result.setProtectedTitleCache(protectedTitleCache);  //must be after setTitleCache
		
		//no changes to: lsid, titleCache, protectedTitleCache
		
		//empty titleCache
		if (! protectedTitleCache){
			titleCache = null;
		}
		return result;
	}
}