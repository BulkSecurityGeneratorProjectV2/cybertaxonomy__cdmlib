/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.model.term.init.ITermInitializer;
import eu.etaxonomy.cdm.model.term.init.ITermLoader;
import eu.etaxonomy.cdm.model.term.init.TermLoader;

/**
 * @author a.mueller
 */
public class DefaultTermInitializer implements ITermInitializer {

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(DefaultTermInitializer.class);
	protected ITermLoader termLoader = new TermLoader();

	private boolean omit = false;

	@Override
    public void initialize() {
		if (!omit){
		    termLoader.unloadAllTerms();
		    doInitialize();
		}
	}

	protected void doInitialize(){
		Map<UUID,DefinedTermBase> terms = new HashMap<>();

//		for(Class<? extends DefinedTermBase<?>> clazz : classesToInitialize) {
		for(VocabularyEnum vocabularyEnum : VocabularyEnum.values()) {
//			Class<? extends DefinedTermBase<?>> clazz = vocabularyEnum.getClazz();
			TermVocabulary<?> voc  = termLoader.loadTerms(vocabularyEnum, terms);
			setDefinedTerms(vocabularyEnum.getClazz(),voc);
		}
	}

	protected <S extends DefinedTermBase<S>> void setDefinedTerms(Class<? extends DefinedTermBase<?>> clazz, TermVocabulary<S> vocabulary) {
		DefinedTermBase<S> newInstance = getInstance(clazz);
		newInstance.setDefaultTerms(vocabulary);
	}

	private  <T extends DefinedTermBase> T getInstance(Class<? extends DefinedTermBase> termClass) {
		try {
			Constructor<T> c = ((Class<T>)termClass).getDeclaredConstructor();
			c.setAccessible(true);
			T termInstance = c.newInstance();
			return termInstance;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

    public void setOmit(boolean omit) {
        this.omit = omit;
    }

    public boolean isOmit() {
        return omit;
    }
}