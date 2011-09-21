/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 *
 */
//@Ignore
public class NameServiceImplTest extends CdmTransactionalIntegrationTest {
	private static final Logger logger = Logger.getLogger(NameServiceImplTest.class);

	@SpringBeanByType
	private INameService nameService;
	
	@SpringBeanByType
	private IOccurrenceService occurrenceService;
	
	@SpringBeanByType
	private ITaxonService taxonService;

	@SpringBeanByType
	private ITermService termService;

	
/* ******************** TESTS ********************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao)}.
	 */
	@Test
	public void testSetDao() {
//		Assert.assertNotNull(((NameServiceImpl)nameService).dao);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setVocabularyDao(eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao)}.
	 */
	@Test
	public void testSetVocabularyDao() {
//		Assert.assertNotNull(( (NameServiceImpl)nameService).vocabularyDao);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getNamesByName(java.lang.String)}.
	 */
	@Test
	public void testGetNamesByName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getTaxonNameByUuid(java.util.UUID)}.
	 */
	@Test
	public void testGetTaxonNameByUuid() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#saveTaxonName(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testSaveTaxonName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#saveTaxonNameAll(java.util.Collection)}.
	 */
	@Test
	public void testSaveTaxonNameAll() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#removeTaxon(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testRemoveTaxon() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getAllNames(int, int)}.
	 */
	@Test
	public void testGetAllNames() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getRankVocabulary()}.
	 */
	@Test
	@Ignore //FIXME assertSame does not work yet 
	public void testGetRankVocabulary() {
		//TODO move test to vocabulary service
		OrderedTermVocabulary<Rank> rankVocabulary = nameService.getRankVocabulary();
		assertNotNull(rankVocabulary);
		assertEquals(66, rankVocabulary.size());
		Rank highestRank = rankVocabulary.getHighestTerm();
		assertEquals(Rank.EMPIRE(), highestRank);
		assertEquals(Rank.DOMAIN(), rankVocabulary.getNextLowerTerm(highestRank));
		assertSame(Rank.EMPIRE(), highestRank);
		assertSame(Rank.DOMAIN(), rankVocabulary.getNextLowerTerm(highestRank));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getTypeDesignationVocabulary()}.
	 */
	@Test
	@Ignore  //not yet correctly implemented
	public void testGetTypeDesignationVocabulary() {
		//TODO move test to vocabulary service
		OrderedTermVocabulary<SpecimenTypeDesignationStatus> typeDesignationVocabulary = 
			nameService.getSpecimenTypeDesignationVocabulary();
		assertNotNull(typeDesignationVocabulary);
		assertEquals(20, typeDesignationVocabulary.size());
		SpecimenTypeDesignationStatus highestType = typeDesignationVocabulary.getHighestTerm();
		assertEquals(SpecimenTypeDesignationStatus.EPITYPE(), highestType);
		assertEquals(SpecimenTypeDesignationStatus.HOLOTYPE(), typeDesignationVocabulary.getNextLowerTerm(highestType));
		assertSame(SpecimenTypeDesignationStatus.EPITYPE(), highestType);
		assertSame(SpecimenTypeDesignationStatus.HOLOTYPE(), typeDesignationVocabulary.getNextLowerTerm(highestType));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testGenerateTitleCache() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testDeleteTaxonNameBaseWithNameRelations() {
		final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship","DescriptionBase","NomenclaturalStatus","TaxonBase","SpecimenOrObservationBase","OriginalSourceBase","DescriptionElementBase"};

		NonViralName name1 = BotanicalName.NewInstance(getSpeciesRank());
		name1.setTitleCache("Name1", true);
		TaxonNameBase<?,?> basionym = BotanicalName.NewInstance(getSpeciesRank());
		basionym.setTitleCache("basionym", true);
		
		NameRelationshipType nameRelType = (NameRelationshipType)termService.find(NameRelationshipType.BASIONYM().getUuid());
		basionym.addRelationshipToName(name1,nameRelType , null, null, null);
//		name1.addBasionym(basionym);
		nameService.save(name1);
		commitAndStartNewTransaction(tableNames);
		
		try {
			nameService.delete(name1);
			Assert.fail("Delete should throw an error as long as name relationships exist.");
		} catch (Exception e) {
			if (e.getMessage().startsWith("Name can't be deleted as it is used in name relationship")){
				//ok
				endTransaction();  //exception rolls back transaction!
				printDataSet(System.out, tableNames);
				startNewTransaction();
			}else{
				Assert.fail("Unexpected error occurred when trying to delete taxon name: " + e.getMessage());
			}
		}
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNotNull("Name should still be in database",name1);
		name1.removeBasionyms();
		nameService.delete(name1); //should throw now exception
		commitAndStartNewTransaction(tableNames);
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNull("Name should not be in database anymore",name1);

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testDeleteTaxonNameBaseWithHybridRelations() {
		final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship"};

		NonViralName name1 = BotanicalName.NewInstance(getSpeciesRank());
		name1.setTitleCache("Name1", true);
		NonViralName parent = BotanicalName.NewInstance(getSpeciesRank());
		parent.setTitleCache("parent", true);
		NonViralName child = BotanicalName.NewInstance(getSpeciesRank());
		child.setTitleCache("child", true);
		
		HybridRelationshipType relType = (HybridRelationshipType)termService.find(HybridRelationshipType.FIRST_PARENT().getUuid());
		name1.addHybridParent(parent, HybridRelationshipType.FIRST_PARENT(), null);
		nameService.save(name1);
		commitAndStartNewTransaction(tableNames); //otherwise first save is rolled back with following failing delete

		//parent
		try {
			nameService.delete(name1);
			Assert.fail("Delete should throw an error as long as hybrid parent exist.");
		} catch (Exception e) {
			if (e.getMessage().startsWith("Name can't be deleted as it is a child in")){
				//ok
				endTransaction();  //exception rolls back transaction!
				printDataSet(System.out, tableNames);
				startNewTransaction();
			}else{
				Assert.fail("Unexpected error occurred when trying to delete taxon name: " + e.getMessage());
			}
		}
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNotNull("Name should still be in database",name1);
		name1.removeHybridParent(parent);
		nameService.delete(name1); //should throw now exception
		commitAndStartNewTransaction(tableNames);
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNull("Name should not be in database anymore",name1);
		
		//child
		name1 = BotanicalName.NewInstance(getSpeciesRank());
		name1.addHybridChild(child, HybridRelationshipType.FIRST_PARENT(), null);
		nameService.save(name1);
		commitAndStartNewTransaction(tableNames);
		
		try {
			nameService.delete(name1);
			Assert.fail("Delete should throw an error as long as hybrid child exist.");
		} catch (Exception e) {
			if (e.getMessage().startsWith("Name can't be deleted as it is a parent in")){
				//ok
				endTransaction();  //exception rolls back transaction!
				startNewTransaction();
			}else{
				Assert.fail("Unexpected error occurred when trying to delete taxon name: " + e.getMessage());
			}
		}
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNotNull("Name should still be in database",name1);
		name1.removeHybridChild(child);
		nameService.delete(name1); //should throw now exception
		commitAndStartNewTransaction(tableNames);
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNull("Name should not be in database anymore",name1);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testDeleteTaxonNameBaseInConcept() {
		final String[] tableNames = new String[]{"TaxonNameBase","TaxonBase"};

		NonViralName name1 = BotanicalName.NewInstance(getSpeciesRank());
		name1.setTitleCache("Name1", true);
		TaxonNameBase<?,?> basionym = BotanicalName.NewInstance(getSpeciesRank());
		basionym.setTitleCache("basionym", true);

		Taxon taxon = Taxon.NewInstance(name1, null);
		nameService.save(name1);
		taxonService.save(taxon);
		commitAndStartNewTransaction(tableNames);

		try {
			nameService.delete(name1);
			Assert.fail("Delete should throw an error as long as name is used in a concept.");
		} catch (Exception e) {
			if (e.getMessage().startsWith("Name can't be deleted as it is used in concept")){
				//ok
				endTransaction();  //exception rolls back transaction!
				startNewTransaction();
			}else{
				Assert.fail("Unexpected error occurred when trying to delete taxon name: " + e.getMessage());
			}
		}
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNotNull("Name should still be in database",name1);
		taxon = (Taxon)taxonService.find(taxon.getUuid());
		Assert.assertNotNull("Taxon should still be in database",taxon);
		taxon.setName(basionym);
		taxonService.save(taxon);
		nameService.delete(name1); //should throw now exception
		commitAndStartNewTransaction(tableNames);
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNull("Name should not be in database anymore",name1);
		taxon = (Taxon)taxonService.find(taxon.getUuid());
		Assert.assertNotNull("Taxon should still be in database",taxon);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testDeleteTaxonNameBaseAsStoredUnder() {
		final String[] tableNames = new String[]{"TaxonNameBase","SpecimenOrObservationBase"};

		NonViralName name1 = BotanicalName.NewInstance(getSpeciesRank());
		name1.setTitleCache("Name1", true);
		Specimen specimen = Specimen.NewInstance();
		specimen.setStoredUnder(name1);

		occurrenceService.save(specimen);
		nameService.save(name1);
		try {
			commitAndStartNewTransaction(tableNames);
			nameService.delete(name1);
			Assert.fail("Delete should throw an error as long as name is used for specimen#storedUnder.");
		} catch (Exception e) {
			if (e.getMessage().startsWith("Name can't be deleted as it is used as derivedUnit#storedUnder")){
				//ok
				endTransaction();  //exception rolls back transaction!
				startNewTransaction();
			}else{
				Assert.fail("Unexpected error occurred when trying to delete taxon name: " + e.getMessage());
			}
		}
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNotNull("Name should still be in database",name1);
		specimen = (Specimen)occurrenceService.find(specimen.getUuid());
		Assert.assertNotNull("Specimen should still be in database",name1);
		specimen.setStoredUnder(null);
		occurrenceService.saveOrUpdate(specimen);
		nameService.delete(name1); //should throw now exception
		commitAndStartNewTransaction(tableNames);
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNull("Name should not be in database anymore",name1);
		specimen = (Specimen)occurrenceService.find(specimen.getUuid());
		Assert.assertNotNull("Specimen should still be in database",specimen);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testDeleteTaxonNameBaseInSource() {
		final String[] tableNames = new String[]{"TaxonNameBase","DescriptionBase","TaxonBase","OriginalSourceBase","DescriptionElementBase"};

		NonViralName name1 = BotanicalName.NewInstance(getSpeciesRank());
		name1.setTitleCache("Name1", true);
		TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(getSpeciesRank());
		taxonName.setTitleCache("taxonName", true);
		Taxon taxon = Taxon.NewInstance(taxonName, null);
		
		TaxonDescription taxonDescription = TaxonDescription.NewInstance(taxon);
		Feature feature = (Feature)termService.find(Feature.DESCRIPTION().getUuid());
		TextData textData = TextData.NewInstance("Any text", Language.DEFAULT(), null);
		textData.setFeature(feature);
		taxonDescription.addElement(textData);
		DescriptionElementSource source = DescriptionElementSource.NewInstance(null, null, name1, "");
		textData.addSource(source);
		taxonService.saveOrUpdate(taxon);
		nameService.save(name1);
		try {
			commitAndStartNewTransaction(tableNames);
			nameService.delete(name1);
			Assert.fail("Delete should throw an error as long as name is used in a source.");
		} catch (Exception e) {
			if (e.getMessage().startsWith("Name can't be deleted as it is used as descriptionElementSource#nameUsedInSource")){
				//ok
				endTransaction();  //exception rolls back transaction!
				startNewTransaction();
			}else{
				Assert.fail("Unexpected error occurred when trying to delete taxon name: " + e.getMessage());
			}
		}
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNotNull("Name should still be in database",name1);
		taxon = (Taxon)taxonService.find(taxon.getUuid());
		Assert.assertNotNull("Taxon should still be in database",name1);
		source = taxon.getDescriptions().iterator().next().getElements().iterator().next().getSources().iterator().next();
		source.setNameUsedInSource(null);
		taxonService.saveOrUpdate(taxon);
		nameService.delete(name1);  //should throw now exception
		commitAndStartNewTransaction(tableNames);
		name1 = (NonViralName)nameService.find(name1.getUuid());
		Assert.assertNull("Name should not be in database anymore",name1);
		taxon = (Taxon)taxonService.find(taxon.getUuid());
		Assert.assertNotNull("Taxon should still be in database",taxon);
		source = taxon.getDescriptions().iterator().next().getElements().iterator().next().getSources().iterator().next();
		Assert.assertNull("Source should not have a nameUsedInSource anymore",source.getNameUsedInSource());
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testDeleteTaxonNameBase() {
		final String[] tableNames = new String[]{"TaxonNameBase","NameRelationship","HybridRelationship","DescriptionBase","NomenclaturalStatus","TaxonBase","SpecimenOrObservationBase","OriginalSourceBase","DescriptionElementBase"};

		NonViralName name1 = BotanicalName.NewInstance(getSpeciesRank());
		name1.setTitleCache("Name1", true);
		
		//TaxonNameDescription
		name1 = BotanicalName.NewInstance(getSpeciesRank());
		TaxonNameDescription nameDesc = TaxonNameDescription.NewInstance(name1);
		nameService.save(name1);
		commitAndStartNewTransaction(tableNames);
		
		name1 = (NonViralName)nameService.find(name1.getUuid());
		nameService.delete(name1);  //should throw now exception
		setComplete(); 
		endTransaction();
		printDataSet(System.out, tableNames);

		//NomenclaturalStatus
		name1 = BotanicalName.NewInstance(getSpeciesRank());
		NomenclaturalStatusType nomStatusType = (NomenclaturalStatusType)termService.find(NomenclaturalStatusType.ILLEGITIMATE().getUuid());
		NomenclaturalStatus status = NomenclaturalStatus.NewInstance(nomStatusType);
		name1.addStatus(status);
		nameService.save(name1);
		commitAndStartNewTransaction(tableNames);
		
		name1 = (NonViralName)nameService.find(name1.getUuid());
		nameService.delete(name1);  //should throw now exception
		setComplete(); 
		endTransaction();
		printDataSet(System.out, tableNames);

		
	}
	
	/**
	 * @param tableNames
	 */
	private void commitAndStartNewTransaction(final String[] tableNames) {
		setComplete(); 
		endTransaction();
//		printDataSet(System.out, tableNames);
		startNewTransaction();
	}
	

	/**
	 * @return
	 */
	private Rank getSpeciesRank() {
		return (Rank)termService.find(Rank.uuidSpecies);
	}


}
