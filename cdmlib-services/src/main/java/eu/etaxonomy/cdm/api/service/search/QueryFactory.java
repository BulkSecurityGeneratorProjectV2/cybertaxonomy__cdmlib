// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.hibernate.search.PaddedIntegerBridge;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.kohlbecker
 * @date Sep 14, 2012
 *
 */
public class QueryFactory {

    public static final Logger logger = Logger.getLogger(QueryFactory.class);

    private LuceneSearch luceneSearch;

    Set<String> textFieldNames = new HashSet<String>();

    private BooleanQuery finalQuery;

    public Set<String> getTextFieldNames() {
        return textFieldNames;
    }

    public String[] getTextFieldNamesAsArray() {
        return textFieldNames.toArray(new String[textFieldNames.size()]);
    }


    public QueryFactory(LuceneSearch luceneSearch){
        this.luceneSearch = luceneSearch;
    }

    /**
     * @param fieldName
     * @param queryString
     * @param isTextField whether this field is a field containing free text in contrast to e.g. ID fields.
     * @return
     */
    public Query newTermQuery(String fieldName, String queryString, boolean isTextField){

        if(isTextField){
            textFieldNames.add(fieldName);
        }

         // in order to support the full query syntax we must use the parser here
        String luceneQueryString = fieldName + ":(" + queryString + ")";
        try {
            return luceneSearch.parse(luceneQueryString);
        } catch (ParseException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * only to be used for text fields, see {@link #newTermQuery(String, String, boolean)}
     * @param fieldName
     * @param queryString
     * @return a {@link TermQuery} or a {@link WildcardQuery}
     */
    public Query newTermQuery(String fieldName, String queryString){
        return newTermQuery(fieldName, queryString, true);
    }

    /**
     * DefinedTerm representations and MultilanguageString maps are stored in the Lucene index by the {@link DefinedTermBaseClassBridge}
     * and {@link MultilanguageTextFieldBridge } in a consistent way. One field per language and also in one additional field for all languages.
     * This method is a convenient means to retrieve a Lucene query string for such the fields.
     *
     * @param name name of the term field as in the Lucene index. Must be field created by {@link DefinedTermBaseClassBridge}
     * or {@link MultilanguageTextFieldBridge }
     * @param languages the languages to search for exclusively. Can be <code>null</code> to search in all languages
     * @return
     */
    public Query newLocalizedTermQuery(String name, String queryString, List<Language> languages) {

        BooleanQuery localizedTermQuery = new BooleanQuery();
        localizedTermQuery.add(newTermQuery(name + ".label", queryString), Occur.SHOULD);
        if(languages == null || languages.size() == 0){
            localizedTermQuery.add(newTermQuery(name + ".ALL", queryString), Occur.SHOULD);
        } else {
            for(Language lang : languages){
                localizedTermQuery.add(newTermQuery(name + "." + lang.getUuid().toString(), queryString), Occur.SHOULD);
            }
        }
        return localizedTermQuery;
    }

    /**
     * convenience method for localized searches on {@link DefinedTermBase}
     * instances, it adds the field name suffix "representations" to the
     * <code>name</code> parameter and calls
     * {@link #newLocalizedTermQuery(String, String, List)}
     *
     * @param name
     * @param queryString
     * @param languages
     * @return
     */
    public Query newDefinedTermBaseQuery(String name, String queryString, List<Language> languages) {
        return newLocalizedTermQuery(name + ".representations", queryString, languages);
    }

    /**
     * @param idFieldName
     * @param entitiy
     * @return
     */
    public Query newEntityIdQuery(String idFieldName, CdmBase entitiy){
        return newTermQuery("inDescription.taxon.taxonNodes.classification.id", PaddedIntegerBridge.paddInteger(entitiy.getId()), false);
    }

    /**
     *  TODO open range queries [0 TO *] not working in the current version of lucene (https://issues.apache.org/jira/browse/LUCENE-995)
     *  so we are using integer maximum as workaround
     * @param idFieldName
     * @param entitiy
     * @return
     */
    public Query newIdNotNullQuery(String idFieldName){
        return new RangeQuery(
                    new Term(idFieldName, PaddedIntegerBridge.paddInteger(0)),
                    new Term(idFieldName, PaddedIntegerBridge.paddInteger(Integer.MAX_VALUE)),
                    false
            );
    }

    /**
     * creates a query for searching for documents in which the field specified by <code>uuidFieldName</code> matches at least one of the uuid
     * of the <code>entities</code>, the sql equivalent of this is <code>WHERE uuidFieldName IN (uuid_1, uuid_2, ...) </code>.
     * @param uuidFieldName
     * @param entities
     * @return
     */
    public Query newEntityUuidQuery(String uuidFieldName, List<? extends IdentifiableEntity> entities){

        BooleanQuery uuidInQuery = new BooleanQuery();
        if(entities != null && entities.size() > 0 ){
            for(IdentifiableEntity entity : entities){
                uuidInQuery.add(newTermQuery(uuidFieldName, entity.getUuid().toString(), false), Occur.SHOULD);
            }
        }
        return uuidInQuery;
    }

    public void setFinalQuery(BooleanQuery finalQuery) {
        this.finalQuery = finalQuery;
    }

    public BooleanQuery getFinalQuery(){
        return finalQuery;
    }

    public LuceneSearch getLuceneSearch() {
        return luceneSearch;
    }

}
