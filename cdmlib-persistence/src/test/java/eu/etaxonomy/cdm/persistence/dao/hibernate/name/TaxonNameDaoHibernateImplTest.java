package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class TaxonNameDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	ITaxonNameDao taxonNameDao;
	
	private UUID cryptocoryneGriffithiiUuid;
	private UUID acherontiaUuid;
	private UUID acherontiaLachesisUuid;
	private UUID atroposUuid;
	
	@Before
	public void setUp() {
		cryptocoryneGriffithiiUuid = UUID.fromString("497a9955-5c5a-4f2b-b08c-2135d336d633");
	    acherontiaUuid = UUID.fromString("c2cab2ad-3e3a-47b8-8aa8-d9e1c0857647");
	    acherontiaLachesisUuid = UUID.fromString("7969821b-a2cf-4d01-95ec-6a5ed0ca3f69");
	    atroposUuid = UUID.fromString("27004fcc-14d4-47d4-a3e1-75750fdb5b79");
	}
	
	@Test
	public void testGetHybridRelationships() {
		BotanicalName cryptocoryneGriffithii = (BotanicalName)taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid);
		assert cryptocoryneGriffithii!= null : "name must exist";
		
		List<HybridRelationship> result = taxonNameDao.getHybridNames(cryptocoryneGriffithii, null, null, null,null,null);
		
		assertNotNull("getHybridNames should return a list",result);
		assertFalse("the list should not be empty", result.isEmpty());
		assertEquals("getHybridNames should return 1 HybridRelationship instance",1,result.size());
	}
	
	@Test
	public void testCountHybridRelationships() {
		BotanicalName cryptocoryneGriffithii = (BotanicalName)taxonNameDao.findByUuid(cryptocoryneGriffithiiUuid);
		assert cryptocoryneGriffithii != null : "name must exist";
		
		int count = taxonNameDao.countHybridNames(cryptocoryneGriffithii, null);
		
		assertEquals("countHybridNames should return 1",1,count);
	}
	
	@Test
	public void testGetNameRelationships() {
		TaxonNameBase acherontia = taxonNameDao.findByUuid(acherontiaUuid);
		assert acherontia != null : "name must exist";
		
		List<NameRelationship> result = taxonNameDao.getNameRelationships(acherontia, NameRelationship.Direction.relatedFrom, null, null,null,null, null);
		
		assertNotNull("getRelatedNames should return a list",result);
		assertFalse("the list should not be empty", result.isEmpty());
		assertEquals("getRelatedNames should return 1 NameRelationship instance",1,result.size());
		
		// testing inverted direction
		TaxonNameBase atropos = taxonNameDao.findByUuid(atroposUuid);
		assert atropos != null : "name must exist";
		
		result = taxonNameDao.getNameRelationships(atropos, NameRelationship.Direction.relatedTo, null, null,null,null, null);
		
		assertNotNull("getRelatedNames should return a list",result);
		assertFalse("the list should not be empty", result.isEmpty());
		assertEquals("getRelatedNames should return 2 NameRelationship instance",2,result.size());
	}
	
	@Test
	public void testCountNameRelationships() {
		TaxonNameBase acherontia = taxonNameDao.findByUuid(acherontiaUuid);
		assert acherontia != null : "name must exist";
		
		int count = taxonNameDao.countNameRelationships(acherontia, NameRelationship.Direction.relatedFrom, null);
		
		assertEquals("countRelatedNames should return 1",1,count);
		
		// testing inverted direction
		TaxonNameBase atropos = taxonNameDao.findByUuid(atroposUuid);
		assert atropos != null : "name must exist";
		
		count = taxonNameDao.countNameRelationships(atropos, NameRelationship.Direction.relatedTo, null);
		
		assertEquals("countRelatedNames should return 2",2,count);
	}

	@Test
	public void testGetTypeDesignations() {
		TaxonNameBase acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
		assert acherontiaLachesis != null : "name must exist";
		
		List<TypeDesignationBase> result = taxonNameDao.getTypeDesignations(acherontiaLachesis, null, null, null);
		
		assertNotNull("getTypeDesignations should return a list",result);
		assertFalse("the list should not be empty", result.isEmpty());
		assertEquals("getTypeDesignations should return 1 TypeDesignationBase instance",1,result.size());
	}
	
	@Test
	public void testCountTypeDesignations() {
		TaxonNameBase acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
		assert acherontiaLachesis != null : "name must exist";
		
		int count = taxonNameDao.countTypeDesignations(acherontiaLachesis, null);
		
		assertEquals("countTypeDesignations should return 1",1,count);
	}
	
	@Test
	public void testSearchNames() {
		List<TaxonNameBase> result = taxonNameDao.searchNames("Atropos", null, null, null, Rank.GENUS(), null, null, null, null);
		
		assertNotNull("searcNames should return a list",result);
		assertFalse("the list should not be empty", result.isEmpty());
		assertEquals("searchNames should return 3 TaxonNameBase instances",3,result.size());
	}
	
	@Test
	public void testCountNames() {
		int count = taxonNameDao.countNames("Atropos", null, null, null, Rank.GENUS());
		
		assertEquals("countNames should return 3",3,count);
	}
	
	@Test
	/**
	 * This test check for a specific bug where the rank of a taxon name base
	 * has no order index (=0)
	 */
	@Ignore //FIXME resolve bug Ticket #686
	public void testMissingRankOrderIndex() {
		TaxonNameBase acherontiaLachesis = taxonNameDao.findByUuid(acherontiaLachesisUuid);
		Rank rank = null;
		try {
			rank = Rank.getRankByName(acherontiaLachesis.getRank().getLabel());
		} catch (UnknownCdmTypeException e) {
			e.printStackTrace();
		}
		assertNotNull(rank);
		assertFalse("Rank are equal, level must not be higher", rank.isHigher(acherontiaLachesis.getRank()));
		assertFalse("Rank are equal, level must not be lower", rank.isLower(acherontiaLachesis.getRank()));
	}
}
