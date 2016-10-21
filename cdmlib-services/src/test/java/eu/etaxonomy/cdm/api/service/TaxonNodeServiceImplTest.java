// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNaturalComparator;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author n.hoffmann
 * @created Dec 16, 2010
 */
//@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public class TaxonNodeServiceImplTest extends CdmTransactionalIntegrationTest{


	@SpringBeanByType
	private ITaxonNodeService taxonNodeService;

	@SpringBeanByType
	private IClassificationService classificationService;

	@SpringBeanByType
	private IReferenceService referenceService;

	@SpringBeanByType
	private ITermService termService;

	@SpringBeanByType
    private INameService nameService;

	@SpringBeanByType
	private ITaxonService taxonService;

	@SpringBeanByType
	private IPolytomousKeyService polKeyService;

	@SpringBeanByType
	private IPolytomousKeyNodeService polKeyNodeService;


	private static final UUID t1Uuid = UUID.fromString("55c3e41a-c629-40e6-aa6a-ff274ac6ddb1");
	private static final UUID t2Uuid = UUID.fromString("2659a7e0-ff35-4ee4-8493-b453756ab955");
	private static final UUID classificationUuid = UUID.fromString("6c2bc8d9-ee62-4222-be89-4a8e31770878");
	private static final UUID classification2Uuid = UUID.fromString("43d67247-936f-42a3-a739-bbcde372e334");
	private static final UUID referenceUuid = UUID.fromString("de7d1205-291f-45d9-9059-ca83fc7ade14");
	private static final UUID node1Uuid= UUID.fromString("484a1a77-689c-44be-8e65-347d835f47e8");
	private static final UUID node2Uuid = UUID.fromString("2d41f0c2-b785-4f73-a436-cc2d5e93cc5b");

	private static final UUID node4Uuid = UUID.fromString("2fbf7bf5-22dd-4c1a-84e4-c8c93d1f0342");
	private static final UUID node5Uuid = UUID.fromString("c4d5170a-7967-4dac-ab76-ae2019eefde5");
	private static final UUID node6Uuid = UUID.fromString("b419ba5e-9c8b-449c-ad86-7abfca9a7340");
	private static final UUID rootNodeUuid = UUID.fromString("324a1a77-689c-44be-8e65-347d835f4111");


	private Taxon t1;
	private Taxon t2;
	private Taxon t4;
//	private Synonym s1;
	private SynonymType synonymType;
	private Reference reference;
	private String referenceDetail;
	private Classification classification;
	private TaxonNode node1;
	private TaxonNode node2;

    private TaxonNode node4;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonNodeServiceImpl#makeTaxonNodeASynonymOfAnotherTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.SynonymType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	@DataSet
	public final void testMakeTaxonNodeASynonymOfAnotherTaxonNode() {
		classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(node2Uuid);
		node4 = taxonNodeService.load(node4Uuid);
		reference = referenceService.load(referenceUuid);
//		synonymType = SynonymType.HOMOTYPIC_SYNONYM_OF();
		synonymType = CdmBase.deproxy(termService.load(SynonymType.uuidHomotypicSynonymOf), SynonymType.class) ;
		referenceDetail = "test";

		//
		//TODO

//		printDataSet(System.err, new String [] {"TaxonNode"});

		// descriptions
		t1 = node1.getTaxon();
		PolytomousKey polKey = PolytomousKey.NewInstance();
		PolytomousKeyNode keyNode = PolytomousKeyNode.NewInstance("", "", t1, null);
		keyNode.setKey(polKey);
		polKeyNodeService.save(keyNode);
		polKeyService.save(polKey);

		//nameRelations

		t1.getName().addRelationshipFromName(BotanicalName.NewInstance(Rank.SPECIES()), NameRelationshipType.ALTERNATIVE_NAME(), null );

		//taxonRelations
		t1.addTaxonRelation(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), TaxonRelationshipType.CONGRUENT_OR_EXCLUDES(), null, null);
		Synonym synonym = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
		UUID uuidSynonym = taxonService.save(synonym).getUuid();

		t1.addHomotypicSynonym(synonym);
		UUID uuidT1 = taxonService.saveOrUpdate(t1);
		t1 = null;
		t1 =(Taxon) taxonService.load(uuidT1);
		t1 = HibernateProxyHelper.deproxy(t1);
		TaxonNameBase nameT1 = t1.getName();
		UUID t1UUID = t1.getUuid();
		t2 = node2.getTaxon();
		assertEquals(2, t1.getDescriptions().size());
		Assert.assertTrue(t2.getSynonyms().isEmpty());
		Assert.assertTrue(t2.getDescriptions().size() == 0);
		assertEquals(2,t1.getSynonyms().size());
		UUID synUUID = null;
		DeleteResult result;

		t4 = node4.getTaxon();
        UUID uuidT4 = t4.getUuid();
        t4 = (Taxon) taxonService.find(uuidT4);
        TaxonNameBase name4 = nameService.find(t4.getName().getUuid());
        result = taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(node4, node2, synonymType, reference, referenceDetail);
        if (result.isError() || result.isAbort()){
            Assert.fail();
        }
        t4 = (Taxon)taxonService.find(uuidT4);
        assertNull(t4);

		//Taxon can't be deleted because of the polytomous key node
		result = taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(node1, node2, synonymType, reference, referenceDetail);
		if (result.isError() || result.isAbort()){
			Assert.fail();
		}
		commitAndStartNewTransaction(new String[]{/*"TaxonNode"*/});
		t1 = (Taxon)taxonService.find(t1Uuid);
		assertNotNull(t1);//because of the polytomous key node
		node1 = taxonNodeService.load(node1Uuid);
		assertNull(node1);

		Set<CdmBase> updatedObjects = result.getUpdatedObjects();
		Iterator<CdmBase> it = updatedObjects.iterator();
		Taxon taxon;
		while (it.hasNext()) {
			CdmBase updatedObject = it.next();
			if(updatedObject.isInstanceOf(Taxon.class)){
				taxon = HibernateProxyHelper.deproxy(updatedObject, Taxon.class);
				Set<Synonym> syns =  taxon.getSynonyms();
				assertNotNull(syns);
				if (taxon.equals(t2)){
				    assertEquals(4,syns.size());
				    Set<TaxonNameBase> typifiedNames =taxon.getHomotypicGroup().getTypifiedNames();
	                assertEquals(typifiedNames.size(),4);
	                assertTrue(taxon.getHomotypicGroup().equals( nameT1.getHomotypicalGroup()));

	                assertEquals(taxon, t2);
				}

			}

		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonNodeServiceImpl#makeTaxonNodeASynonymOfAnotherTaxonNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.SynonymType, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String)}.
	 */
	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
	public final void testMakeTaxonNodeAHeterotypicSynonymOfAnotherTaxonNode() {

	    //create data
	    classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(node2Uuid);
		reference = referenceService.load(referenceUuid);
		synonymType = CdmBase.deproxy(termService.load(SynonymType.uuidHeterotypicSynonymOf), SynonymType.class) ;
		referenceDetail = "test";

		// descriptions
		t1 = node1.getTaxon();
		PolytomousKey polKey = PolytomousKey.NewInstance();
		PolytomousKeyNode keyNode = PolytomousKeyNode.NewInstance("", "", t1, null);
		keyNode.setKey(polKey);
		polKeyNodeService.save(keyNode);
		polKeyService.save(polKey);

		//nameRelations
		t1.getName().addRelationshipFromName(BotanicalName.NewInstance(Rank.SPECIES()), NameRelationshipType.ALTERNATIVE_NAME(), null );
		TaxonNameBase<?,?> name1 = t1.getName();
		UUID name1UUID = name1.getUuid();
		//taxonRelations
		t1.addTaxonRelation(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), TaxonRelationshipType.CONGRUENT_OR_EXCLUDES(), null, null);
		Synonym t1HomotypSynonym = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);

		t1.addHomotypicSynonym(t1HomotypSynonym);
		TaxonNameBase<?,?> nameT1 = t1.getName();
		t2 = node2.getTaxon();
		assertEquals("taxon 1 must have 2 descriptions", 2, t1.getDescriptions().size());
		assertEquals("taxon 1 must have 2 synonyms", 2, t1.getSynonyms().size());
		Assert.assertTrue("taxon 2 must have no synonyms", t2.getSynonyms().isEmpty());
		Assert.assertTrue("taxon 2 must have no descriptions", t2.getDescriptions().size() == 0);

		//save
		UUID uuidSynonym = taxonService.save(t1HomotypSynonym).getUuid();

		//do it
		DeleteResult result = taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode
		        (node1, node2, synonymType, reference, referenceDetail);

		//post conditions
		if (!result.getUpdatedObjects().iterator().hasNext()){
			Assert.fail("Some updates must have taken place");
		}
		assertEquals(3,result.getUpdatedObjects().size());
		assertNotNull("Old taxon should not have been deleted as it is referenced by key node", taxonService.find(t1Uuid));
		assertNull("Old taxon node should not exist anymore", taxonNodeService.find(node1Uuid));

		t1HomotypSynonym = (Synonym)taxonService.find(uuidSynonym);
		assertNotNull(t1HomotypSynonym);

		keyNode.setTaxon(null);
		polKeyNodeService.saveOrUpdate(keyNode);
		t2 =HibernateProxyHelper.deproxy(t2);
		HibernateProxyHelper.deproxy(t2.getHomotypicGroup());
		t2.setName(HibernateProxyHelper.deproxy(t2.getName()));

		termService.saveOrUpdate(synonymType);
		assertFalse("taxon 2 must have a synonym now", t2.getSynonyms().isEmpty());
		assertEquals("taxon 2 must have 3 synonyms now, the old taxon 1 and it's 2 synonyms", 3, t2.getSynonyms().size());
		assertEquals("taxon 2 must have 2 descriptions now, taken form taxon 1", 2, t2.getDescriptions().size());

		result = taxonService.deleteTaxon(t1.getUuid(), null, null);
		if (result.isAbort() || result.isError()){
			Assert.fail();
		}
		assertNull(taxonService.find(t1Uuid));
		assertNull(taxonNodeService.find(node1Uuid));
		name1 = nameService.find(name1UUID);
		assertNotNull("taxon name 1 should still exist", name1);
		assertEquals("... but being used for the new synonym only as taxon 1 is deleted", 1, name1.getTaxonBases().size());
		t1HomotypSynonym = (Synonym)taxonService.find(uuidSynonym);
		assertNotNull(t1HomotypSynonym);

		Synonym newSynonym =(Synonym) name1.getTaxonBases().iterator().next();

//		Taxon newAcceptedTaxon = (Taxon)result.getUpdatedObjects().iterator().next();

		Taxon newAcceptedTaxon = t2;
		assertEquals("The new synonym (old accepted taxon) and it's homotypic synonym should still be homotypic", newSynonym.getHomotypicGroup(), t1HomotypSynonym.getName().getHomotypicalGroup());
		assertFalse("The new accepted taxon must not be homotypic to ", newAcceptedTaxon.getHomotypicGroup().equals(newSynonym.getName().getHomotypicalGroup()));


		assertEquals("The new accepted taxon is taxon 2", newAcceptedTaxon, t2);
		assertEquals("The new synonyms name must be the same as the old accepted taxon's name", newSynonym.getName(), nameT1);
	}


	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonNodeServiceImplTest-indexing.xml")
	public final void testIndexCreateNode() {
		Taxon taxon = Taxon.NewInstance(null, null);
		classification = classificationService.load(classificationUuid);
		node2 = taxonNodeService.load(node2Uuid);
		String oldTreeIndex = node2.treeIndex();

		TaxonNode newNode = node2.addChildTaxon(taxon, null, null);
		taxonNodeService.saveOrUpdate(newNode);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		newNode = taxonNodeService.load(newNode.getUuid());
		Assert.assertEquals("", oldTreeIndex + newNode.getId() + "#", newNode.treeIndex());
	}


	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonNodeServiceImplTest-indexing.xml")
	public final void testIndexMoveNode() {
		//in classification
		classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(node2Uuid);
		node2.addChildNode(node1, null, null);
		taxonNodeService.saveOrUpdate(node1);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		TaxonNode node6 = taxonNodeService.load(node6Uuid);
		Assert.assertEquals("Node6 treeindex is not correct", node2.treeIndex() + "2#4#6#", node6.treeIndex());

		//root of new classification
		Classification classification2 = classificationService.load(classification2Uuid);
		node1 = taxonNodeService.load(node1Uuid);
		classification2.setRootNode(HibernateProxyHelper.deproxy(classification2.getRootNode(),TaxonNode.class));
		classification2.addChildNode(node1, null, null);
		taxonNodeService.saveOrUpdate(node1);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node1 = taxonNodeService.load(node1Uuid);
		Assert.assertEquals("Node1 treeindex is not correct", "#t2#8#2#", node1.treeIndex());
		node6 = taxonNodeService.load(node6Uuid);
		Assert.assertEquals("Node6 treeindex is not correct", "#t2#8#2#4#6#", node6.treeIndex());

		//into new classification
		node2 = taxonNodeService.load(node2Uuid);
		TaxonNode node5 = taxonNodeService.load(node5Uuid);
		node2 =node5.addChildNode(node2, null, null);
		taxonNodeService.saveOrUpdate(node2);
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node2 = taxonNodeService.load(node2Uuid);
		Assert.assertEquals("Node3 treeindex is not correct", "#t2#8#2#5#3#", node2.treeIndex());

}

	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="TaxonNodeServiceImplTest-indexing.xml")
	public final void testIndexDeleteNode() {
		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node1 = taxonNodeService.load(node1Uuid);
		TaxonNode node4 = taxonNodeService.load(node4Uuid);
		String treeIndex = node1.treeIndex();
		TaxonNode node6 = taxonNodeService.load(node6Uuid);
		treeIndex= node6.treeIndex();

		HibernateProxyHelper.deproxy(node1, TaxonNode.class);
		node1.deleteChildNode(node4, false);
		TaxonNode node5 = taxonNodeService.load(node5Uuid);
		treeIndex = node5.treeIndex();

		node6 = taxonNodeService.load(node6Uuid);

		treeIndex = node6.treeIndex();
		Taxon newTaxon= Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
		UUID taxonNewUuid = taxonService.save(newTaxon).getUuid();

		node5.addChildTaxon(newTaxon, null, null);
		String node5TreeIndex =node5.treeIndex();
		taxonNodeService.saveOrUpdate(node5);

		commitAndStartNewTransaction(new String[]{"TaxonNode"});
		node5 = taxonNodeService.load(node5Uuid);
		List<TaxonNode> children =  node5.getChildNodes();
		TaxonNode node = children.get(0);
		int id = node.getId();
		Assert.assertEquals("Node6 treeindex is not correct", "#t1#1#2#6#", treeIndex);
		Assert.assertEquals("new node treeindex is not correct", node5TreeIndex + id +"#", node.treeIndex());
	}


	@Test
	@DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)
	public final void testDeleteNode(){
		classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(rootNodeUuid);
		node1 = HibernateProxyHelper.deproxy(node1);

		TaxonNode newNode = node1.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), null, null);
		UUID uuidNewNode = taxonNodeService.save(newNode).getUuid();
		newNode = taxonNodeService.load(uuidNewNode);
		UUID taxUUID = newNode.getTaxon().getUuid();
		UUID nameUUID = newNode.getTaxon().getName().getUuid();

		DeleteResult result = taxonNodeService.deleteTaxonNode(node1, null);
		if (!result.isOk()){
			Assert.fail();
		}
		newNode = taxonNodeService.load(uuidNewNode);
		node1 = taxonNodeService.load(node1Uuid);
		assertNull(newNode);
		assertNull(node1);

		t1 = (Taxon) taxonService.load(t1Uuid);
		assertNull(t1);
		Taxon newTaxon = (Taxon)taxonService.load(taxUUID);
		assertNull(newTaxon);
		BotanicalName name = (BotanicalName)nameService.load(nameUUID);
		assertNull(name);


	}

	@Test
    @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class)

    public final void testDeleteNodeWithReusedTaxon(){
        classification = classificationService.load(classificationUuid);
        node1 = taxonNodeService.load(node1Uuid);
        node2 = taxonNodeService.load(rootNodeUuid);
        node1 = HibernateProxyHelper.deproxy(node1);


        Classification classification2 = Classification.NewInstance("Classification2");
        TaxonNode nodeClassification2 =classification2.addChildTaxon(node1.getTaxon(), null, null);
        assertEquals(node1.getTaxon().getUuid(), t1Uuid);
        classificationService.save(classification2);
        List<TaxonNode> nodesOfClassification2 = taxonNodeService.listAllNodesForClassification(classification2, null, null);
        UUID nodeUUID = nodesOfClassification2.get(0).getUuid();
        assertEquals(nodeUUID, nodeClassification2.getUuid());
        Taxon newTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()),  null);
        taxonService.save(newTaxon);
        TaxonNode newNode = node1.addChildTaxon(newTaxon,null, null);
        UUID uuidNewNode = taxonNodeService.save(newNode).getUuid();
        newNode = taxonNodeService.load(uuidNewNode);
        UUID taxUUID = newNode.getTaxon().getUuid();
        UUID nameUUID = newNode.getTaxon().getName().getUuid();

        DeleteResult result = taxonNodeService.deleteTaxonNode(node1, null);
        if (!result.isOk()){
            Assert.fail();
        }
        //taxonService.getSession().flush();
        newNode = taxonNodeService.load(uuidNewNode);
        node1 = taxonNodeService.load(node1Uuid);
        assertNull(newNode);
        assertNull(node1);
        assertNotNull(taxonNodeService.load(nodeUUID));

        t1 = (Taxon) taxonService.load(t1Uuid);
        assertNotNull(t1);
        newTaxon = (Taxon)taxonService.load(taxUUID);
        assertNull(newTaxon);
        BotanicalName name = (BotanicalName)nameService.load(nameUUID);
        assertNull(name);


    }



	@Test
	@DataSet
	public final void testDeleteNodes(){
		classification = classificationService.load(classificationUuid);
		node1 = taxonNodeService.load(node1Uuid);
		node2 = taxonNodeService.load(rootNodeUuid);
		node1 = HibernateProxyHelper.deproxy(node1);
		node2 = HibernateProxyHelper.deproxy(node2);
		TaxonNode newNode = node1.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), null, null);
		UUID uuidNewNode = taxonNodeService.save(newNode).getUuid();
		List<TaxonNode> treeNodes = new ArrayList<TaxonNode>();
		treeNodes.add(node1);
		treeNodes.add(node2);

		DeleteResult result = taxonNodeService.deleteTaxonNodes(treeNodes, null);


		newNode = taxonNodeService.load(uuidNewNode);
		node1 = taxonNodeService.load(node1Uuid);
		assertNull(newNode);
		assertNull(node1);
		//taxonService.getSession().flush();
		t1 = (Taxon) taxonService.load(t1Uuid);
		assertNull(t1);
		t2 = (Taxon) taxonService.load(t2Uuid);
		assertNull(t2);


	}
	@Test
	@DataSet
	public void testMoveTaxonNode(){
	    classification = classificationService.load(classificationUuid);
	  //  Set<TaxonNode>  nodes = classification.getAllNodes();
	    List<TaxonNode>  nodes = classification.getChildNodes();
	    System.out.println(nodes.size());
	    classification.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), nodes.size(), null, null);
	   nodes =  classification.getChildNodes();
	    System.out.println(nodes.size());

	}

    @Test
    public void testCompareNaturalOrder() {
    	/*
    	 * Classification
    	 *  * Abies
    	 *  `- Abies alba
    	 *   - Abies balsamea
    	 *  * Pinus
    	 *  `- Pinus pampa
    	 */
    	Classification classification = Classification.NewInstance("Classification");
    	BotanicalName abiesName = BotanicalName.NewInstance(Rank.GENUS());
    	abiesName.setGenusOrUninomial("Abies");
    	Taxon abies = Taxon.NewInstance(abiesName, null);
    	BotanicalName abiesAlbaName = BotanicalName.NewInstance(Rank.SPECIES());
    	abiesAlbaName.setGenusOrUninomial("Abies");
    	abiesAlbaName.setSpecificEpithet("alba");
    	Taxon abiesAlba = Taxon.NewInstance(abiesAlbaName, null);
    	BotanicalName pinusName = BotanicalName.NewInstance(Rank.GENUS());
    	pinusName.setGenusOrUninomial("Pinus");
    	Taxon pinus = Taxon.NewInstance(pinusName, null);
    	BotanicalName pinusPampaName = BotanicalName.NewInstance(Rank.SPECIES());
    	pinusPampaName.setGenusOrUninomial("Pinus");
    	pinusPampaName.setSpecificEpithet("pampa");
    	Taxon pinusPampa = Taxon.NewInstance(pinusPampaName, null);

        BotanicalName abiesBalsameaName = BotanicalName.NewInstance(Rank.SPECIES());
        abiesBalsameaName.setGenusOrUninomial("Abies");
        abiesBalsameaName.setSpecificEpithet("balsamea");
        Taxon abiesBalsamea = Taxon.NewInstance(abiesBalsameaName, null);

        List<TaxonNode> nodes = new ArrayList<TaxonNode>();
    	nodes.add(classification.addChildTaxon(abies, null, null));
    	TaxonNode abiesAlbaNode = classification.addParentChild(abies, abiesAlba, null, null);
    	TaxonNode balsameaNode = classification.addParentChild(abies, abiesBalsamea, null, null);
    	nodes.add(balsameaNode);
    	nodes.add(abiesAlbaNode);
    	nodes.add(classification.addChildTaxon(pinus, null, null));
    	nodes.add(classification.addParentChild(pinus, pinusPampa, null, null));
    	classificationService.saveClassification(classification);
    	//this.taxonNodeService.save(nodes);
    	TaxonNaturalComparator comparator = new TaxonNaturalComparator();
    	List<TaxonNode> allNodes = new ArrayList<>(classification.getAllNodes());
    	Collections.sort(allNodes, comparator);

    	Assert.assertEquals(allNodes.get(0).getTaxon(), abies );
    	Assert.assertEquals(allNodes.get(2).getTaxon(), abiesBalsamea );
    	Assert.assertEquals(allNodes.get(1).getTaxon(), abiesAlba );

    	taxonNodeService.moveTaxonNode(balsameaNode, abiesAlbaNode,1);
    	classification = classificationService.load(classification.getUuid());

    	allNodes = new ArrayList<>(classification.getAllNodes());
        Collections.sort(allNodes, comparator);

        Assert.assertEquals(allNodes.get(0).getTaxon(), abies );
        Assert.assertEquals(allNodes.get(1).getTaxon(), abiesBalsamea );
        Assert.assertEquals(allNodes.get(2).getTaxon(), abiesAlba );

    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {}


}
