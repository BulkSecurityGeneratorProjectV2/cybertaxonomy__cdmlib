/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.l10n;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.IEnumTerm;

/**
 * @author a.kohlbecker
 * @since May 28, 2020
 */
public class EnumTerm_L10n<T extends IEnumTerm<?>> {

    private T enumTerm;

    public EnumTerm_L10n(T enumTerm) {
        this.enumTerm = enumTerm;
    }

    public String localizedMessage() {

        List<Language> languages = LocaleContext.getLanguages();
        String message_L10n = null;
        if(languages != null){
            for(Language language : languages) {
                message_L10n = enumTerm.getMessage(language);
                if(message_L10n != null){
                    return message_L10n;
                }
            }
        }
        message_L10n = enumTerm.getMessage();

        return message_L10n;
    }

}
