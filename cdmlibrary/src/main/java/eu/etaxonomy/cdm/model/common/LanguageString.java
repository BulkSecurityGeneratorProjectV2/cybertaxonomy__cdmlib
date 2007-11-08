/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


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
public class LanguageString {
	static Logger logger = Logger.getLogger(LanguageString.class);
	private char text;
	private Language language;

	public Language getLanguage(){
		return this.language;
	}

	/**
	 * 
	 * @param language    language
	 */
	public void setLanguage(Language language){
		this.language = language;
	}

	public char getText(){
		return this.text;
	}

	/**
	 * 
	 * @param text    text
	 */
	public void setText(char text){
		this.text = text;
	}

}