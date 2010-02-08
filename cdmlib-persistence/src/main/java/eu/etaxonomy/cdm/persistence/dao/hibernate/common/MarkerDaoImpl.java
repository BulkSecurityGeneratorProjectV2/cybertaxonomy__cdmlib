/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.persistence.dao.common.IMarkerDao;

@Repository
public class MarkerDaoImpl extends CdmEntityDaoBase<Marker> implements IMarkerDao {

	public MarkerDaoImpl() {
		super(Marker.class);
	}

}
