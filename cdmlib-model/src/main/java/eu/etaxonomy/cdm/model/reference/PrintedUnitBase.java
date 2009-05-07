/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;

/**
 * This (abstract) class represents printed {@link PublicationBase published references} which
 * are recurrent products of publishing companies or of research organisations.
 * In this case it is generally possible to distinguish authors, editors and
 * publishers. 
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:45
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PrintedUnitBase", propOrder = {
		"editor",	
		"volume",
		"pages",
		"inSeries",
		"seriesPart"
})
@XmlRootElement(name = "PrintedUnitBase")
@Entity
@Audited
public abstract class PrintedUnitBase<S extends IReferenceBaseCacheStrategy> extends PublicationBase<S> implements IVolumeReference{
	
	static Logger logger = Logger.getLogger(PrintedUnitBase.class);
	
    @XmlElement(name = "Editor")
	private String editor;
	
    @XmlElement(name = "Volume")
	private String volume;
	
    @XmlElement(name = "Pages")
	private String pages;
	
    @XmlElement(name = "InSeries")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
	private PrintSeries inSeries;
	
    @XmlElement(name = "SeriesPart")
	private String seriesPart;

	/**
	 * Returns the printed series <i>this</i> printed unit belongs to.
	 * 
	 * @return  printed series
	 * @see 	PrintSeries
	 */
	public PrintSeries getInSeries(){
		return this.inSeries;
	}

	/**
	 * @see #getInSeries()
	 */
	public void setInSeries(PrintSeries inSeries){
		this.inSeries = inSeries;
	}

	/**
	 * Returns the string representing the name of the editor of <i>this</i>
	 * printed unit. An editor is mostly a person (team) who assumed the
	 * responsibility for the content of the publication as a whole without
	 * being the author of this content.<BR>
	 * 
	 * @return  the string identifying the editor of <i>this</i>
	 * 			printed unit
	 * @see 	PublicationBase#getPublisher()
	 */
	public String getEditor(){
		return this.editor;
	}

	/**
	 * @see #getEditor()
	 */
	public void setEditor(String editor){
		this.editor = editor;
	}

	/**
	 * Returns the string representing the volume of <i>this</i> printed unit.<BR>
	 * 
	 * @return  the string identifying the volume of <i>this</i>
	 * 			printed unit
	 */
	public String getVolume(){
		return this.volume;
	}

	/**
	 * @see #getVolume()
	 */
	public void setVolume(String volume){
		this.volume = volume;
	}

	/**
	 * Returns the string representing the pages extent of <i>this</i> printed unit.
	 * 
	 * @return  the pages string
	 */
	public String getPages(){
		return this.pages;
	}

	/**
	 * @see #getPages()
	 */
	public void setPages(String pages){
		this.pages = pages;
	}
	

	/**
	 * Returns the string representing the series part identifying <i>this</i>
	 * printed unit within the the {@link PrintSeries printed series} it belongs to.
	 * 
	 * @return  the string identifying the series part for <i>this</i>
	 * 			printed unit
	 */
	public String getSeriesPart() {
		return seriesPart;
	}

	/**
	 * @see #getSeriesPart()
	 */
	public void setSeriesPart(String seriesPart) {
		this.seriesPart = seriesPart;
	}

//*********** CLONE **********************************/	


	/** 
	 * Clones <i>this</i> printed unit. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> printed unit
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link PublicationBase PublicationBase}.
	 * 
	 * @see PublicationBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone(){
		PrintedUnitBase result = (PrintedUnitBase)super.clone();
		//no changes to: editor, inSeries, pages, volume, seriesPart
		return result;
	}
}