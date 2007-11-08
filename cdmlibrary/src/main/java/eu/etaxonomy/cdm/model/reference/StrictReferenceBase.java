/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import eu.etaxonomy.cdm.model.common.TimePeriod;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * A year() method is required to get the year of publication out of the
 * datePublished field
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:54
 */
@Entity
public abstract class StrictReferenceBase extends ReferenceBase {
	static Logger logger = Logger.getLogger(StrictReferenceBase.class);
	//Title of the reference
	private String title;
	//The date range assigned to the reference. ISO Date range like. Flexible, year can be left out, etc
	private TimePeriod datePublished;

	public String getTitle(){
		return this.title;
	}

	/**
	 * 
	 * @param title    title
	 */
	public void setTitle(String title){
		this.title = title;
	}

	public TimePeriod getDatePublished(){
		return this.datePublished;
	}

	/**
	 * 
	 * @param datePublished    datePublished
	 */
	public void setDatePublished(TimePeriod datePublished){
		this.datePublished = datePublished;
	}

	/**
	 * returns a formatted string containing the entire reference citation including
	 * authors
	 */
	@Transient
	public String getCitation(){
		return "";
	}

	@Transient
	public String getYear(){
		return null;
	}

}