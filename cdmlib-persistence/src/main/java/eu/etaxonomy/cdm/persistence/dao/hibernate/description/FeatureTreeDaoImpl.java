/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.persistence.dao.description.IFeatureDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;


@Repository
public class FeatureTreeDaoImpl extends CdmEntityDaoBase<Feature> implements IFeatureDao{
	private static final Logger logger = Logger.getLogger(FeatureTreeDaoImpl.class);

	public FeatureTreeDaoImpl() {
		super(Feature.class); 
	}

}
