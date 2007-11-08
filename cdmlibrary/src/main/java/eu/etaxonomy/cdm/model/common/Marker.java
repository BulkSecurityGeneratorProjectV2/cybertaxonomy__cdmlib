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
 * This class aims to make available some "flags" for identifiable entities in a
 * flexible way. Application developers (and even users) can define their own
 * "flags" as a MarkerType.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:33
 */
@Entity
public class Marker extends VersionableEntity {
	static Logger logger = Logger.getLogger(Marker.class);
	private boolean flag;
	private MarkerType type;

	public MarkerType getType(){
		return this.type;
	}

	/**
	 * 
	 * @param type    type
	 */
	public void setType(MarkerType type){
		this.type = type;
	}

	public boolean getFlag(){
		return this.flag;
	}

	/**
	 * 
	 * @param flag    flag
	 */
	public void setFlag(boolean flag){
		this.flag = flag;
	}

}