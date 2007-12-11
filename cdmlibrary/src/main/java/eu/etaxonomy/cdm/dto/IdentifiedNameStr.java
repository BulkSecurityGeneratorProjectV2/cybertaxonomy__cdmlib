/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 11.12.2007 14:40:26
 */
public class IdentifiedNameStr {
	
	private String uuid;
	
	private String fullname;
	
	private List<TaggedText> taggedName = new ArrayList();
	
	
}
