/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:22
 */
@Entity
public class Paragraph extends VersionableEntity {
	static Logger logger = Logger.getLogger(Paragraph.class);

	@Description("")
	private String content;
	private TextFormat format;

	public TextFormat getFormat(){
		return format;
	}

	/**
	 * 
	 * @param format
	 */
	public void setFormat(TextFormat format){
		;
	}

	public String getContent(){
		return content;
	}

	/**
	 * 
	 * @param content
	 */
	public void setContent(String content){
		;
	}

}