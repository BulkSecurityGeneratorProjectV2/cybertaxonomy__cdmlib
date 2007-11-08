/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import eu.etaxonomy.cdm.model.common.MultilanguageString;
import eu.etaxonomy.cdm.model.description.Description;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * part of a specimen or observation that is being described or determined.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:41
 */
@Entity
public class ObservationalUnit extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(ObservationalUnit.class);
	//Description defining the Observational unit in the context of the original Occurrence
	private MultilanguageString definition;
	private ArrayList descriptions;
	private ArrayList<Determination> determinations;
	private Occurrence occurence;

	public ArrayList<Determination> getDeterminations(){
		return this.determinations;
	}

	/**
	 * 
	 * @param determinations    determinations
	 */
	public void setDeterminations(ArrayList determinations){
		this.determinations = determinations;
	}

	public Occurrence getOccurence(){
		return this.occurence;
	}

	/**
	 * 
	 * @param occurence    occurence
	 */
	public void setOccurence(Occurrence occurence){
		this.occurence = occurence;
	}

	public ArrayList getDescriptions(){
		return this.descriptions;
	}

	/**
	 * 
	 * @param descriptions    descriptions
	 */
	public void setDescriptions(ArrayList descriptions){
		this.descriptions = descriptions;
	}

	public MultilanguageString getDefinition(){
		return this.definition;
	}

	/**
	 * 
	 * @param definition    definition
	 */
	public void setDefinition(MultilanguageString definition){
		this.definition = definition;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}