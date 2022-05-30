/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * Lucene index class bridge which sets the uuids of the accepted taxon for the
 * TaxonBase object into the index.
 * <p>
 * Adds id fields with the uuid and id of the accepted taxon of the
 * current {@link TaxonBase} entity. Id fields should not be analyzed, therefore
 * this FieldBridge ignores the settings {@link org.hibernate.annotations.Index}
 * annotation and always sets this to <code>NOT_ANALYZED</code>.
 *
 *
 * @author c.mathew
 * @author a.kohlbecker
 * @since 26 Jul 2013
 */
public class AcceptedTaxonBridge implements FieldBridge { // TODO inherit from AbstractClassBridge since this base class provides presets for id fields?

    public final static String DOC_KEY_UUID_SUFFIX = ".uuid";
    public static final String DOC_KEY_ID_SUFFIX = ".id";
    public final static String DOC_KEY_PUBLISH_SUFFIX = ".publish";
    public final static String DOC_KEY_TREEINDEX = "taxonNodes.treeIndex";
    public final static String DOC_KEY_CLASSIFICATION_ID = "taxonNodes.classification.id";
    public final static String ACC_TAXON = "accTaxon"; //there are probably still some places not using this constant, but for renaming in future we should try to use it everywhere

    @Override
    public void set(String name, Object value, Document document,
            LuceneOptions luceneOptions) {
        String accTaxonUuid = "";

        boolean isSynonym = false;
        Taxon accTaxon;
        if(value instanceof Taxon){
            accTaxon = (Taxon)value;
        }else if (value instanceof Synonym){
            accTaxon = ((Synonym)value).getAcceptedTaxon();
            isSynonym = true;
        }else{
            throw new RuntimeException("Unhandled taxon base class: " + value.getClass().getSimpleName());
        }

        // in the case of taxon this is just the uuid
        if(accTaxon != null) {
            //id
            Field canonicalNameIdField = new StringField(name + DOC_KEY_ID_SUFFIX,
                    Integer.toString(accTaxon.getId()),
                    luceneOptions.getStore()
                    );
            document.add(canonicalNameIdField);
            //uuid
            accTaxonUuid = accTaxon.getUuid().toString();
            Field canonicalNameUuidField = new StringField(name + DOC_KEY_UUID_SUFFIX,
                    accTaxonUuid,
                    luceneOptions.getStore()
                    );
            //TODO  do we really need to set the boost for an id field?
            canonicalNameUuidField.setBoost(luceneOptions.getBoost());
            document.add(canonicalNameUuidField);

            //publish
            Field accPublishField = new StringField(name + DOC_KEY_PUBLISH_SUFFIX,
                    Boolean.toString(accTaxon.isPublish()),
                    luceneOptions.getStore()
                    );
            document.add(accPublishField);

            //treeIndex + Classification
            if (isSynonym && ACC_TAXON.equals(name)){
                for (TaxonNode node : accTaxon.getTaxonNodes()){
                    //treeIndex
                    Field treeIndexField;
                    if (node.treeIndex()!= null){  //TODO find out why this happens in TaxonServiceSearchTest.testFindByDescriptionElementFullText_modify_Classification
                        treeIndexField = new StringField(DOC_KEY_TREEINDEX,
                                node.treeIndex(),
                                luceneOptions.getStore()
                                );
                        document.add(treeIndexField);
                    }

                    //classification
                    if (node.getClassification() != null){  //should never be null, but who knows
                        Field classificationIdField = new StringField(DOC_KEY_CLASSIFICATION_ID,
                                Integer.toString(node.getClassification().getId()),
                                luceneOptions.getStore()
                                );
                        document.add(classificationIdField);
                    }
                }
            }
        }
    }
}