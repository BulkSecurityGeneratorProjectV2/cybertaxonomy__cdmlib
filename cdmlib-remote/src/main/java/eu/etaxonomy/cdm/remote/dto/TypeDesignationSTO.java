/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.dto;


public class TypeDesignationSTO extends ReferencedEntityBaseSTO {
	private IdentifiedString status;



	/**
	 * @return
	 */
	public IdentifiedString getStatus() {
		return status;
	}

	/**
	 * @param status
	 */
	public void setStatus(IdentifiedString status) {
		this.status = status;
	}

}
