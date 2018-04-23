/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import eu.etaxonomy.cdm.model.common.TermBase;

/**
 * @author a.kohlbecker
 * @since 30.07.2010
 *
 */
public class TermBaseAutoInitializer extends AutoPropertyInitializer<TermBase> {

    @Override
    public void initialize(TermBase bean) {
       beanInitializer.initializeInstance(bean.getRepresentations());
    }
    
    @Override
    public String hibernateFetchJoin(Class<?> clazz, String beanAlias) throws Exception{
    	return String.format(" LEFT JOIN FETCH %s.representations ", beanAlias); 
    }

}
