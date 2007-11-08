/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.occurrence;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:32
 */
@Entity
public class LivingIndividual extends Occurrence {
	static Logger logger = Logger.getLogger(LivingIndividual.class);
	private ArrayList parents;
	private ArrayList offspring;

	public ArrayList getParents(){
		return this.parents;
	}

	/**
	 * 
	 * @param parents    parents
	 */
	public void setParents(ArrayList parents){
		this.parents = parents;
	}

	public ArrayList getOffspring(){
		return this.offspring;
	}

	/**
	 * 
	 * @param offspring    offspring
	 */
	public void setOffspring(ArrayList offspring){
		this.offspring = offspring;
	}

}