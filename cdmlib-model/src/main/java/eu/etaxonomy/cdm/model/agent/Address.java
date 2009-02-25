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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;

/**
 * This class represents atomized postal addresses.
 * <P>
 * This class corresponds to: <ul>
 * <li> Address according to the TDWG ontology
 * <li> Address according to the TCS
 * <li> Address according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:09
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Address", propOrder = {
    "pobox",
    "street",
    "postcode",
    "locality",
    "region",
    "country",
    "location"
})
@XmlRootElement(name = "Address")
@Entity
@Audited
public class Address extends VersionableEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 682106303069088972L;

	static Logger logger = Logger.getLogger(Address.class);
	
    @XmlElement(name = "POBox")
	private String pobox;
    
    @XmlElement(name = "Street")
	private String street;
    
    @XmlElement(name = "Postcode")
	private String postcode;
    
    @XmlElement(name = "Locality", required = true)
	private String locality;
    
    @XmlElement(name = "Region")
	private String region;
    
    @XmlElement(name = "Country")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private WaterbodyOrCountry country;
    
    @XmlElement(name = "Location")
	private Point location;
	
	/**
	 * Returns the {@link WaterbodyOrCountry country} involved in <i>this</i> postal address.
	 * 
	 * @return	the country 
	 */
	public WaterbodyOrCountry getCountry(){
		return this.country;
	}

	/**
	 * @see			   #getCountry()
	 */
	public void setCountry(WaterbodyOrCountry country){
		this.country = country;
	}

	/**
	 * Returns the geophysical {@link Point location} (coordinates) of <i>this</i> postal address.
	 * The location can be useful for instance to visualize the address on a map.
	 * 
	 * @return  the point corresponding to <i>this</i> address
	 * @see		eu.etaxonomy.cdm.model.location.Point
	 */
	@XmlTransient
	public Point getLocation(){
		return this.location;
	}

	/**
	 * @see			#getLocation()
	 */
	public void setLocation(Point location){
		this.location = location;
	}

	/**
	 * Returns a string corresponding to the post office box
	 * involved in <i>this</i> postal address.
	 * 
	 * @return	the post office box string 
	 */
	public String getPobox(){
		return this.pobox;
	}

	/**
	 * @see			#getPobox()
	 */
	public void setPobox(String pobox){
		this.pobox = pobox;
	}

	/**
	 * Returns the street name and number involved in <i>this</i> postal address.
	 * Street numbers are part of the street string.
	 * 
	 * @return	the string composed of street name and number  
	 */
	public String getStreet(){
		return this.street;
	}

	/**
	 * @see			#getStreet()
	 */
	public void setStreet(String street){
		this.street = street;
	}

	/**
	 * Returns the post code number involved in <i>this</i> postal address.
	 * 
	 * @return	the post code number string
	 */
	public String getPostcode(){
		return this.postcode;
	}

	/**
	 * @see			#getPostcode()
	 */
	public void setPostcode(String postcode){
		this.postcode = postcode;
	}

	/**
	 * Returns the town (possibly with locality or suburb) involved in <i>this</i> postal address.
	 * 
	 * @return  the string representing a town
	 */
	public String getLocality(){
		return this.locality;
	}

	/**
	 * @see			#getLocality()
	 */
	public void setLocality(String locality){
		this.locality = locality;
	}

	/**
	 * Returns the region or state involved in <i>this</i> postal address.
	 * 
	 * @return  the string representing a region or a state
	 */
	public String getRegion(){
		return this.region;
	}

	/**
	 * @see			#getRegion()
	 */
	public void setRegion(String region){
		this.region = region;
	}

}