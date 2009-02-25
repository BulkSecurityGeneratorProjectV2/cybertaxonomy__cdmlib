package eu.etaxonomy.cdm.model.common;

import java.util.List;

public interface IMultiLanguageText {

	/**
	 * @param language
	 * @return
	 */
	public abstract String getText(Language language);

	/**
	 * @param languageString
	 * @return String the previous text in the MultilanguageSet that was associated with the language
	 * defined in languageString, or null if there was no such text before. (A null return can also indicate that the text was previously null.)
	 */
	public abstract LanguageString add(LanguageString languageString);

	/**
	 * Iterates on the languages. As soon as there exists a language string for this language in this multilanguage text
	 * it is returned.
	 * @param languages
	 * @return 
	 */
	public LanguageString getPreferredLanguageString(List<Language> languages);

}