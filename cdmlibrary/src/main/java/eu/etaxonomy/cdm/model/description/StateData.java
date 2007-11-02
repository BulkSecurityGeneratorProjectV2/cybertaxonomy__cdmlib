/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.description;


import etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:18
 */
public class StateData extends VersionableEntity {
	static Logger logger = Logger.getLogger(StateData.class);

	@Description("")
	private String modifyingText;
	private State state;
	private ArrayList modifiers;

	public State getState(){
		return state;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setState(State newVal){
		state = newVal;
	}

	public ArrayList getModifiers(){
		return modifiers;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setModifiers(ArrayList newVal){
		modifiers = newVal;
	}

	public String getModifyingText(){
		return modifyingText;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setModifyingText(String newVal){
		modifyingText = newVal;
	}

}