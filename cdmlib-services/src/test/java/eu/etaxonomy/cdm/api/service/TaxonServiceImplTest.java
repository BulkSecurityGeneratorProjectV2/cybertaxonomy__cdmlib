/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class TaxonServiceImplTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(TaxonServiceImplTest.class);
	
	@SpringBeanByType
	private ITaxonService service;
	
	@SpringBeanByType
	private INameService nameService;
	
/****************** TESTS *****************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao)}.
	 */
	@Test
	public final void testSetDao() {
		logger.warn("Not implemented yet");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getTaxonByUuid(java.util.UUID)}.
	 */
	@Test
	public final void testGetTaxonByUuid() {
		Taxon expectedTaxon = Taxon.NewInstance(null, null);
		UUID uuid = service.saveTaxon(expectedTaxon);
		TaxonBase actualTaxon = service.getTaxonByUuid(uuid);
		assertEquals(expectedTaxon, actualTaxon);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#saveTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public final void testSaveTaxon() {
		Taxon expectedTaxon = Taxon.NewInstance(null, null);
		UUID uuid = service.saveTaxon(expectedTaxon);
		TaxonBase actualTaxon = service.getTaxonByUuid(uuid);
		assertEquals(expectedTaxon, actualTaxon);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#removeTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public final void testRemoveTaxon() {
		Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(null), null);
		UUID uuid = service.saveTaxon(taxon);
		service.removeTaxon(taxon);
		TaxonBase actualTaxon = service.getTaxonByUuid(uuid);
		assertNull(actualTaxon);
	}
	
	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#loadTreeBranchTo(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.name.Rank, java.util.List)}
	 * .
	 */
	@Test
	public final void loadTreeBranchTo() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#searchTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase)}
	 * .
	 */
	@Test
	public final void testSearchTaxaByName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)}
	 * .
	 */
	@Test
	public final void testGetRootTaxa() {
		logger.warn("Not yet implemented"); // TODO
	}
	
//	@Ignore
	@Test
	public final void testPrintDataSet() {
		
		printDataSet(System.out);
	}
	
	public final void testMakeTaxonSynonym() {
		
	}
}
