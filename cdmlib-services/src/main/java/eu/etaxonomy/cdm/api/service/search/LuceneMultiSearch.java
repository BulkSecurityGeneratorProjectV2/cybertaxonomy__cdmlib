// $Id$
/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SortField;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.indexes.IndexReaderAccessor;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * A LuceneSearch which allow to run a union like search on multiple indexes at once.
 * Internally a {@link MultiReader} is being used.
 *
 * @author Andreas Kohlbecker
 * @date Dec 21, 2011
 *
 */
public class LuceneMultiSearch extends LuceneSearch {

    public static final Logger logger = Logger.getLogger(LuceneMultiSearch.class);

    private final Set<Class<? extends CdmBase>> directorySelectClasses = new HashSet<Class<? extends CdmBase>>();


    /**
     * @param luceneSearch the searches to execute together as a union like search
     * @throws Exception
     */
    public LuceneMultiSearch(LuceneSearch... luceneSearch) throws LuceneMultiSearchException {

        session = luceneSearch[0].session;
        groupByField = null; //reset
        BooleanQuery query = new BooleanQuery();

        Set<String> highlightFields = new HashSet<String>();
        List<SortField> multiSearcherSortFields = new ArrayList<SortField>();

        for(LuceneSearch search : luceneSearch){

            this.directorySelectClasses.add(search.getDirectorySelectClass());
            query.add(search.getQuery(), Occur.SHOULD);

            // add the highlightFields from each of the sub searches
            highlightFields.addAll(Arrays.asList(search.getHighlightFields()));

            // set the class for each of the sub searches
            if(search.clazz != null){
                if(clazz != null && !clazz.equals(search.clazz)){
                    throw new LuceneMultiSearchException(
                            "LuceneMultiSearch can only handle once class restriction, but multiple given: " +
                            getClazz() + ", " + search.getClazz());
                }
                setClazz(search.getClazz());
            }

            // set the groupByField for each of the sub searches
            if(search.groupByField != null){
                if(groupByField != null && !groupByField.equals(search.groupByField)){
                    throw new LuceneMultiSearchException(
                            "LuceneMultiSearch can only handle once groupByField, but multiple given: " +
                            groupByField + ", " + search.groupByField);
                }
                groupByField = search.groupByField;
            }


            // add the sort field from each of the sub searches
            for(SortField addField : search.getSortFields()){
                if(! multiSearcherSortFields.contains(addField)) {
                    multiSearcherSortFields.add(addField);
                }
            }
        }

        this.sortFields = multiSearcherSortFields.toArray(new SortField[multiSearcherSortFields.size()]);
        this.highlightFields = highlightFields.toArray(new String[highlightFields.size()]);
        this.query = query;
    }

    /**
     * @return
     */
    @Override
    public IndexSearcher getSearcher() {

        if(searcher == null){

            SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();
            List<IndexReader> readers = new ArrayList<IndexReader>();
            for(Class<? extends CdmBase> type : directorySelectClasses){
                   //OLD
//                DirectoryProvider[] directoryProviders = searchFactory.getDirectoryProviders(type);
//                logger.info(directoryProviders[0].getDirectory().toString());

//                ReaderProvider readerProvider = searchFactory.getReaderProvider();
                IndexReaderAccessor ira = searchFactory.getIndexReaderAccessor();
                IndexReader reader = ira.open(type);
//            	readers.add(readerProvider.openReader(directoryProviders[0]));
                readers.add(reader);
            }
            if(readers.size() > 1){
                MultiReader multireader = new MultiReader(readers.toArray(new IndexReader[readers.size()]), true);
                searcher = new IndexSearcher(multireader);
            } else {
                searcher = new IndexSearcher(readers.get(0));
            }
        }

        return searcher;
    }

    /**
     * does exactly the same as {@link LuceneSearch#getAnalyzer()} but perform
     * an additional check to assure that all indexes are using the same
     * analyzer
     *
     * @return
     */
    @Override
    public Analyzer getAnalyzer() {
        SearchFactory searchFactory = Search.getFullTextSession(session).getSearchFactory();
        Analyzer analyzer = null;
        for(Class<? extends CdmBase> type : directorySelectClasses){
            Analyzer a = searchFactory.getAnalyzer(type);
            if(isEqual(analyzer, a)){
                throw new RuntimeException("The LuceneMultiSearch must only be used on indexes which are using the same Analyzer.");
            }
            analyzer = a;
        }
        return analyzer;
    }

    /**
     * @param analyzer
     * @param a
     * @return
     */
    private boolean isEqual(Analyzer analyzer, Analyzer a) {
        // FIXME PatternAnalyzers must be compared by Pattern also other analyzers must be compared by their properties
        return analyzer != null && !analyzer.getClass().equals(a.getClass());
    }

}
