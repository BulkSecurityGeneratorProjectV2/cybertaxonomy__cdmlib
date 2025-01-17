/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.term;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * @author a.mueller
 * @since 19.05.2010
 */
public class TermDefaultCacheStrategy<T extends TermBase> extends IdentifiableEntityDefaultCacheStrategy<T> implements IIdentifiableEntityCacheStrategy<T> {
	private static final long serialVersionUID = 7687293307791110547L;

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TermDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("9cdf52c1-bac4-4b6c-a7f9-1a87401bd8f9");

	@Override
	protected UUID getUuid() {
		return uuid;
	}

	@SuppressWarnings({ })
    public static <T extends TermBase> TermDefaultCacheStrategy<T> NewInstance(@SuppressWarnings("unused") Class<T> clazz){
	    return new TermDefaultCacheStrategy<T>();
	}

	private TermDefaultCacheStrategy(){}

	@Override
	public String getTitleCache(T term) {
		String result = null;
		if (term.getRepresentations().size() > 0) {
			//use default representation (or if not exist any other)
			Representation representation = term.getRepresentation(Language.DEFAULT());
			if (representation == null){
				representation = term.getRepresentations().iterator().next();
			}
			//return label, or if not exists abbreviated label, of if not exists description
			result = representation.getLabel();
			if (isBlank(result)){
					result = representation.getAbbreviatedLabel();
			}
			if (isBlank(result)){
				result = representation.getText();
				representation.getDescription();
			}
		}
		//if still empty return toString
		if (isBlank(result)){
			result = term.getClass().getSimpleName() + "<" + term.getUuid() + ">";
		}
		return result;
	}
}