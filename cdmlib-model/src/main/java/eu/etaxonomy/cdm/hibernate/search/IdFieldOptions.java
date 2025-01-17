/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * Global default options for ID fields:
 * <ul>
 * <li>Store.YES</li>
 * <li>Index.NOT_ANALYZED</li>
 * <li>TermVector.NO</li>
 * <li>Boost = 1.0f (neutral default boost)</li>
 *
 * @author andreas
 * @since Sep 24, 2012
 */
public class IdFieldOptions implements LuceneOptions {

	private static final Logger logger = LogManager.getLogger(IdFieldOptions.class);

    @Override
    public Store getStore() {
        return Store.YES;
    }

    @Override
    public Index getIndex() {
        return Index.NOT_ANALYZED;
    }

    @Override
    public TermVector getTermVector() {
        return TermVector.NO;
    }

    @Override
    public float getBoost() {
        return 1.0f;
    }

	@Override
	public void addFieldToDocument(String arg0, String arg1, Document arg2) {
		logger.warn("not yet implemented");
		// TODO Auto-generated method stub
	}

	@Override
	public void addNumericFieldToDocument(String arg0, Object arg1, Document arg2) {
		logger.warn("not yet implemented");
		// TODO Auto-generated method stub
	}

	@Override
    public void addSortedDocValuesFieldToDocument(String fieldName, String indexedString, Document document) {
        logger.warn("not yet implemented");
        // TODO Auto-generated method stub
    }

    @Override
    public void addNumericDocValuesFieldToDocument(String fieldName, Number numericValue, Document document) {
        logger.warn("not yet implemented");
        // TODO Auto-generated method stub
    }

	@Override
	public String indexNullAs() {
		logger.warn("not yet implemented");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompressed() {
		logger.warn("not yet implemented");
		// TODO Auto-generated method stub
		return false;
	}
}