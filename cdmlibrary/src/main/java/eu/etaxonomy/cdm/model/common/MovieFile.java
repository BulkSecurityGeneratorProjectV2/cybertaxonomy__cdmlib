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
 * @created 08-Nov-2007 13:06:35
 */
@Entity
public class MovieFile extends ImageFile {
	static Logger logger = Logger.getLogger(MovieFile.class);
	//Length of movie in seconds
	private int duration;

	public int getDuration(){
		return this.duration;
	}

	/**
	 * 
	 * @param duration    duration
	 */
	public void setDuration(int duration){
		this.duration = duration;
	}

}