/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultCrud;
import eu.etaxonomy.cdm.validation.Level3;

/**
 * A {@link Runnable} performing Level-3 validation of a JPA entity
 *
 * @author ayco_holleman
 *
 */
public class Level3ValidationTask extends EntityValidationTaskBase {

    public Level3ValidationTask(CdmBase entity, IEntityValidationResultCrud dao) {
        super(entity, dao, Level3.class);
    }

    public Level3ValidationTask(CdmBase entity, CRUDEventType crudEventType, IEntityValidationResultCrud dao) {
        super(entity, crudEventType, dao, Level3.class);
    }

}
