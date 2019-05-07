/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.owl.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.descriptive.owl.in.StructureTreeOwlImport;
import eu.etaxonomy.cdm.io.descriptive.owl.in.StructureTreeOwlImportConfigurator;
import eu.etaxonomy.cdm.io.descriptive.owl.in.StructureTreeOwlImportState;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 *
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
public class StructureTreeOwlImportTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private StructureTreeOwlImport structureTreeImport;

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private IFeatureTreeService featureTreeService;

    @SpringBeanByType
    private IVocabularyService vocabularyService;

    private StructureTreeOwlImportConfigurator configurator;

    private FeatureTree tree;

    @Before
    public void setUp() throws URISyntaxException {
        URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/owl/in/test_structures.owl");
		URI uri = url.toURI();
		assertNotNull(url);
		configurator = StructureTreeOwlImportConfigurator.NewInstance(uri);
    }

    @Test
    public void testInit() {
        assertNotNull("structureTreeImport should not be null", structureTreeImport);
    }

    @Test
    @DataSet(value="/eu/etaxonomy/cdm/database/BlankDataSet.xml")
    public void testDoInvoke() {
        StructureTreeOwlImportState state = configurator.getNewState();
        structureTreeImport.doInvoke(state);
        this.setComplete();
        this.endTransaction();

        String treeLabel = "TestStructures";
        List<FeatureTree> trees = featureTreeService.listByTitle(FeatureTree.class, treeLabel, MatchMode.EXACT, null, null, null, null, null);
        List<String> nodeProperties = new ArrayList<>();
        nodeProperties.add("term");
        tree = featureTreeService.loadWithNodes(trees.iterator().next().getUuid(), null, nodeProperties);
        assertNotNull("featureTree should not be null", tree);

        assertEquals("Tree has wrong term type", TermType.Structure, tree.getTermType());
        assertEquals("Wrong number of distinct features", 4, tree.getDistinctFeatures().size());
        List rootChildren = tree.getRootChildren();
        assertEquals("Wrong number of root children", 1, rootChildren.size());
        Object root = rootChildren.iterator().next();
        assertTrue("Root is no feature node", root instanceof FeatureNode);
        assertEquals("Root node has wrong term type", TermType.Structure, ((FeatureNode)root).getTermType());
        FeatureNode<DefinedTerm> rootNode = (FeatureNode<DefinedTerm>) root;
        List<FeatureNode<DefinedTerm>> childNodes = rootNode.getChildNodes();
        assertEquals("Wrong number of children", 2, childNodes.size());

        String inflorescenceLabel = "inflorescence";
        String inflorescenceDescription = " the part of the plant that bears the flowers, including all its bracts  branches and flowers  but excluding unmodified leaves               ";
        List<DefinedTerm> records = termService.findByRepresentationText(inflorescenceDescription, DefinedTerm.class, null, null).getRecords();
        assertEquals("wrong number of terms found for \"inflorescence\"", 1, records.size());
        DefinedTerm inflorescence = records.iterator().next();
        assertEquals(inflorescenceLabel, inflorescence.getLabel(Language.ENGLISH()));

        for (FeatureNode<DefinedTerm> featureNode : childNodes) {
            assertTrue("Child node not found. Found node with term: "+featureNode.getTerm().getLabel(),
                    featureNode.getTerm().getUuid().equals(inflorescence.getUuid())
                    || featureNode.getTerm().getLabel(Language.ENGLISH()).equals("Flower"));
            if(featureNode.getTerm().getUuid().equals(inflorescence.getUuid())){
                assertEquals("Term mismatch", inflorescence, featureNode.getTerm());
                inflorescence = featureNode.getTerm();

                String englishDescription = inflorescence.getDescription(Language.ENGLISH());
                assertTrue("Description not found", CdmUtils.isNotBlank(englishDescription));
                assertEquals("Description wrong", inflorescenceDescription, englishDescription);
                assertEquals("wrong number of representations", 2, inflorescence.getRepresentations().size());
                Representation germanRepresentation = inflorescence.getRepresentation(Language.GERMAN());
                assertNotNull("Representation is null for "+Language.GERMAN(), germanRepresentation);
                assertEquals("wrong description", "Der Teil der Pflanze, der die Bluete traegt", germanRepresentation.getDescription());
                assertEquals("wrong label", "Infloreszenz", germanRepresentation.getLabel());
            }
        }
        assertNotNull("term is null", inflorescence);
        assertEquals("Wrong term type", TermType.Structure, inflorescence.getTermType());

        List<TermVocabulary> vocs = vocabularyService.findByTitle(TermVocabulary.class, treeLabel, MatchMode.EXACT, null, null, null, null, Arrays.asList("terms")).getRecords();
        assertEquals("wrong number of vocabularies", 1, vocs.size());
        TermVocabulary termVoc = vocs.iterator().next();
        assertEquals("Wrong vocabulary label", treeLabel, termVoc.getTitleCache());
        assertEquals(4, termVoc.getTerms().size());
        assertTrue("Term not included in vocabulary", termVoc.getTerms().contains(inflorescence));

    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}


}