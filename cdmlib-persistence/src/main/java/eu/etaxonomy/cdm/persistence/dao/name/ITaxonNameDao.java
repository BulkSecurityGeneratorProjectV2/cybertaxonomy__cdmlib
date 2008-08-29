/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.name;

import java.util.List;

import org.hibernate.Criteria;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

/**
 * @author a.mueller
 *
 */
public interface ITaxonNameDao extends IIdentifiableDao<TaxonNameBase> {
	
	public List<ReferencedEntityBase> getAllNomenclaturalStatus(Integer limit, Integer start);

	public List<ReferencedEntityBase> getAllTypeDesignations(Integer limit, Integer start);

}
