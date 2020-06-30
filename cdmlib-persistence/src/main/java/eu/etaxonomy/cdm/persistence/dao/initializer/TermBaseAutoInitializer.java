/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.util.Optional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.term.TermBase;

/**
 * Note: Probably not really needed as we are currently loading representations and inverse representations eager.
 *
 * @author a.kohlbecker
 * @since 30.07.2010
 */
public class TermBaseAutoInitializer extends AutoPropertyInitializer<TermBase> {

    @Override
    public void initialize(TermBase bean) {
        beanInitializer.initializeInstance(bean.getRepresentations());
        if (RelationshipTermBase.class.isAssignableFrom(bean.getClass())){
            beanInitializer.initializeInstance(CdmBase.deproxy(bean, RelationshipTermBase.class).getInverseRepresentations());
        }
    }

    @Override
    public Optional<String> hibernateFetchJoin(Class<?> clazz, String beanAlias){
    	String result = String.format(" LEFT JOIN FETCH %s.representations ", beanAlias);
    	if (RelationshipTermBase.class.isAssignableFrom(clazz)){
            result += String.format(" LEFT JOIN FETCH %s.inverseRepresentations ", beanAlias);
        }
    	return Optional.of(result);
    }

}
