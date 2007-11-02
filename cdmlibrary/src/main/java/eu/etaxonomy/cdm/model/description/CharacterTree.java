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
 * Character trees arrange concepts and characters. They may also be used to
 * define flat char. subsets for filtering purposes.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:40
 */
public class CharacterTree extends VersionableEntity {
	static Logger logger = Logger.getLogger(CharacterTree.class);

	private ArrayList characters;

	public ArrayList getCharacters(){
		return characters;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCharacters(ArrayList newVal){
		characters = newVal;
	}

}