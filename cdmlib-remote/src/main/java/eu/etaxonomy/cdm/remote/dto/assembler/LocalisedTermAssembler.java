/**
 *
 */
package eu.etaxonomy.cdm.remote.dto.assembler;
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.remote.dto.BaseSTO;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.LocalisedTermSTO;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 23.05.2008 22:08:28
 *
 */

@Component
public class LocalisedTermAssembler extends AssemblerBase <LocalisedTermSTO, BaseTO, TermBase>  {

	@Autowired
	private IDefinedTermDao languageDao;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	LocalisedTermSTO getSTO(TermBase term, Enumeration<Locale> locales) {
		LocalisedTermSTO lt = new LocalisedTermSTO();
		Representation r = null;
		// look for terms in preferred languages
		while(locales != null && r == null && locales.hasMoreElements()) {
			Locale locale = locales.nextElement();
			Language language  = languageDao.getLangaugeByIso(locale.getLanguage());
			r = term.getRepresentation(language);
		}
		// nothing found? fall back using the first entry
		if(r == null){
			r = term.getRepresentations().iterator().next();
		}
		lt.setTerm(r.getText());
		lt.setLanguage(r.getLanguage().toString());
		return lt;
	}

	/**
	 * Method not implemented since class <code>LocalisedTermTO</code> does not exist.
	 */
	@Deprecated
	BaseTO getTO(TermBase cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("not implemented, class LocalisedTermTO does not exist.");
	}

}
