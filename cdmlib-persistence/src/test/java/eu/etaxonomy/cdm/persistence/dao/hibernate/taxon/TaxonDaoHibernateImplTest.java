package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @author ben.clark
 *
 */
public class TaxonDaoHibernateImplTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByType	
	private ITaxonDao taxonDao;
	
	@SpringBeanByType	
	private IReferenceDao referenceDao;
	
	private UUID uuid;
	private UUID sphingidae;
	private UUID acherontia;
	private UUID acherontiaLachesis;
	private AuditEvent previousAuditEvent;
	private AuditEvent mostRecentAuditEvent;

	@Before
	public void setUp() {
		uuid = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
		sphingidae = UUID.fromString("54e767ee-894e-4540-a758-f906ecb4e2d9");
		acherontia = UUID.fromString("c5cc8674-4242-49a4-aada-72d63194f5fa");
		acherontiaLachesis = UUID.fromString("b04cc9cb-2b4a-4cc4-a94a-3c93a2158b06");
		previousAuditEvent = new AuditEvent();
		previousAuditEvent.setRevisionNumber(1000);
		previousAuditEvent.setUuid(UUID.fromString("a680fab4-365e-4765-b49e-768f2ee30cda"));
		mostRecentAuditEvent = new AuditEvent();
		mostRecentAuditEvent.setRevisionNumber(1025);
		mostRecentAuditEvent.setUuid(UUID.fromString("afe8e761-8545-497b-9134-6a6791fc0b0d"));
		AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)
	}
	
	@After
	public void tearDown() {
		AuditEventContextHolder.clearContext();
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#TaxonDaoHibernateImpl()}.
	 */
	@Test
	@DataSet
	public void testInit() {
		assertNotNull("Instance of ITaxonDao expected",taxonDao);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	@DataSet
	public void testGetRootTaxa() { 
		ReferenceBase sec = referenceDao.findById(1);
		assert sec != null : "sec must exist";
		
		List<Taxon> rootTaxa = taxonDao.getRootTaxa(sec);
		assertNotNull("getRootTaxa should return a List",rootTaxa);
		assertFalse("The list should not be empty",rootTaxa.isEmpty());
		assertEquals("There should be one root taxon",1, rootTaxa.size());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	@DataSet
	public void testGetTaxaByName() {
		ReferenceBase sec = referenceDao.findById(1);
		assert sec != null : "sec must exist";
		
		List<TaxonBase> results = taxonDao.getTaxaByName("Aus", sec);
		assertNotNull("getTaxaByName should return a List",results);
		assertFalse("The list should not be empty",results.isEmpty());
	}	
	
	@Test
	@DataSet
	public void testFindByUuid() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(uuid);
		assertNotNull("findByUuid should return a taxon",taxon);
		assertTrue("findByUuid should return a taxon with it's name initialized",Hibernate.isInitialized(taxon.getName()));
	}
	
	@Test
	@DataSet
	public void testCountRelatedTaxa()	{
		Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
		assert taxon != null : "taxon must exist"; 
		
		int numberOfRelatedTaxa = taxonDao.countRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN());
		assertEquals("countRelatedTaxa should return 23", 23, numberOfRelatedTaxa);
	}
	
	@Test
	@DataSet
	public void testRelatedTaxa() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
		assert taxon != null : "taxon must exist"; 
		
		List<TaxonRelationship> relatedTaxa = taxonDao.getRelatedTaxa(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), null, null);
		assertNotNull("getRelatedTaxa should return a List",relatedTaxa);
		assertEquals("getRelatedTaxa should return all 23 related taxa",relatedTaxa.size(),23);
		assertTrue("getRelatedTaxa should return TaxonRelationship objects with the relatedFrom taxon initialized",Hibernate.isInitialized(relatedTaxa.get(0).getFromTaxon()));
	}
	
	@Test
	@DataSet
	public void testGetRelatedTaxaPaged()	{
		Taxon taxon = (Taxon)taxonDao.findByUuid(sphingidae);
		assert taxon != null : "taxon must exist";
		
		List<TaxonRelationship> firstPage = taxonDao.getRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), 10, 0);
		List<TaxonRelationship> secondPage = taxonDao.getRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),10, 1);
		List<TaxonRelationship> thirdPage = taxonDao.getRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), 10, 2);
		
		assertNotNull("getRelatedTaxa: 10, 0 should return a List",firstPage);
		assertEquals("getRelatedTaxa: 10, 0 should return a List with 10 elements",10,firstPage.size());
		assertNotNull("getRelatedTaxa: 10, 1 should return a List",secondPage);
		assertEquals("getRelatedTaxa: 10, 1 should return a List with 10 elements",secondPage.size(),10);
		assertNotNull("getRelatedTaxa: 10, 2 should return a List",thirdPage);
		assertEquals("getRelatedTaxa: 10, 2 should return a List with 3 elements",thirdPage.size(),3);
	}
	
	@Test
	@DataSet
	public void testCountSynonymRelationships() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
		int numberOfSynonymRelationships = taxonDao.countSynonyms(taxon,null);
		assertEquals("countSynonymRelationships should return 5",5,numberOfSynonymRelationships);
	}
	
	@Test
	@DataSet
	public void testSynonymRelationships()	{
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
		List<SynonymRelationship> synonyms = taxonDao.getSynonyms(taxon, null, null, null);
		
		assertNotNull("getSynonyms should return a List",synonyms);
		assertEquals("getSynonyms should return 5 SynonymRelationship entities",synonyms.size(),5);
		assertTrue("getSynonyms should return SynonymRelationship objects with the synonym initialized",Hibernate.isInitialized(synonyms.get(0).getSynonym()));
	}
	
	@Test
	@DataSet
	public void testCountSynonymRelationshipsByType()	{
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
		int numberOfTaxonomicSynonyms = taxonDao.countSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		assertEquals("countSynonyms should return 4",numberOfTaxonomicSynonyms, 4);
	}
	
	@Test
	@DataSet
	public void testSynonymRelationshipsByType() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
        List<SynonymRelationship> synonyms = taxonDao.getSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null);
		
        assertNotNull("getSynonyms should return a List",synonyms);
		assertEquals("getSynonyms should return 4 SynonymRelationship entities",synonyms.size(),4);
	}
	
	@Test
	@DataSet
	public void testPageSynonymRelationships(){
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		
		List<SynonymRelationship> firstPage = taxonDao.getSynonyms(taxon, null, 4, 0);
		List<SynonymRelationship> secondPage = taxonDao.getSynonyms(taxon, null, 4, 1);
		
		assertNotNull("getSynonyms: 4, 0 should return a List",firstPage);
		assertEquals("getSynonyms: 4, 0 should return 4 SynonymRelationships", firstPage.size(),4);
		assertNotNull("getSynonyms: 4, 1 should return a List",secondPage);
		assertEquals("getSynonyms: 4, 1 should return 1 SynonymRelationship",secondPage.size(),1);
	}
	
	@Test
	@DataSet
	public void testGetTaxonMatchingUninomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(true, "Smerinthus", null, null, null,null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return two Taxa",2,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 5",5,result.get(0).getId());
	}
	
	@Test
	@DataSet
	public void testGetTaxonMatchingSpeciesBinomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(true,"Smerinthus", null, "kindermannii", null,null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return one Taxon",1,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 8",8,result.get(0).getId());
	}
	  
	@Test
	@DataSet
	public void testGetTaxonMatchingTrinomial() {
		List<TaxonBase> result = taxonDao.findTaxaByName(true,"Cryptocoryne", null,"purpurea","borneoensis",null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertEquals("findTaxaByName should return one Taxon",1,result.size());
		assertEquals("findTaxaByName should return a Taxon with id 38",38,result.get(0).getId());
	}
	
	@Test
	@DataSet
	public void testNegativeMatch() {
		List<TaxonBase> result = taxonDao.findTaxaByName(true,"Acherontia", null,"atropos","dehli",null,null,null);
		
		assertNotNull("findTaxaByName should return a List", result);
		assertTrue("findTaxaByName should return an empty List",result.isEmpty());
	}
	
	@Test
	@DataSet
	public void testCountAllTaxa() {
		int numberOfTaxa = taxonDao.count(Taxon.class);
		assertEquals("count should return 33 taxa",33, numberOfTaxa);
	}
	
	@Test
	@DataSet
	public void testListAllTaxa() {
		List<Taxon> taxa = taxonDao.list(Taxon.class,100, 0);
		assertNotNull("list should return a List",taxa);
		assertEquals("list should return 33 taxa",33, taxa.size());
	}
	
	@Test
	@DataSet
	@ExpectedDataSet
	public void testDelete() {
		Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
		assert taxon != null : "taxon must exist";
		taxonDao.delete(taxon);
		setComplete();
		endTransaction();
	}
	
	@Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testFindDeleted() {
    	TaxonBase taxon = taxonDao.findByUuid(acherontia);
    	assertNull("findByUuid should return null in this view", taxon);
    	assertFalse("exist should return false in this view",taxonDao.exists(acherontia));
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testFindDeletedInPreviousView() {
    	AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
    	Taxon taxon = (Taxon)taxonDao.findByUuid(acherontia);
    	assertNotNull("findByUuid should return a taxon in this view",taxon);
    	assertTrue("exists should return true in this view", taxonDao.exists(acherontia));
    	    	
    	try{
    		assertEquals("There should be 3 relations to this taxon in this view",3,taxon.getRelationsToThisTaxon().size());
    	} catch(Exception e) {
    		fail("We should not experience any problems initializing proxies with envers");
    	}
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testGetAuditEvents() {
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(taxon, null,null,null);
    	assertNotNull("getAuditEvents should return a list",auditEvents);
    	assertFalse("the list should not be empty",auditEvents.isEmpty());
    	assertEquals("There should be two AuditEventRecords in the list",2, auditEvents.size());
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testGetAuditEventsFromNow() {
    	AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
    	TaxonBase taxon =  taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	List<AuditEventRecord<TaxonBase>> auditEvents = taxonDao.getAuditEvents(taxon, null,null,AuditEventSort.FORWARDS);
    	assertNotNull("getAuditEvents should return a list",auditEvents);
    	assertFalse("the list should not be empty",auditEvents.isEmpty());
    	assertEquals("There should be one audit event in the list",1,auditEvents.size());
    }

    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void testCountAuditEvents() {
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	int numberOfAuditEvents = taxonDao.countAuditEvents(taxon,null);
    	assertEquals("countAuditEvents should return 2",numberOfAuditEvents,2);
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getPreviousAuditEvent() {
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	AuditEventRecord<TaxonBase> auditEvent = taxonDao.getPreviousAuditEvent(taxon);
    	assertNotNull("getPreviousAuditEvent should not return null as there is at least one audit event prior to the current one",auditEvent);
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getPreviousAuditEventAtBeginning() {
    	AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	AuditEventRecord<TaxonBase> auditEvent = taxonDao.getPreviousAuditEvent(taxon);
    	assertNull("getPreviousAuditEvent should return null if we're at the first audit event anyway",auditEvent);
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getNextAuditEvent() {
    	AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	AuditEventRecord<TaxonBase> auditEvent = taxonDao.getNextAuditEvent(taxon);
    	assertNotNull("getNextAuditEvent should not return null as there is at least one audit event after the current one",auditEvent);
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFindDeleted.xml")
    public void getNextAuditEventAtEnd() {
    	AuditEventContextHolder.getContext().setAuditEvent(mostRecentAuditEvent);
    	TaxonBase taxon = taxonDao.findByUuid(sphingidae);
    	assert taxon != null : "taxon cannot be null";
    	
    	AuditEventRecord<TaxonBase> auditEvent = taxonDao.getNextAuditEvent(taxon);
    	assertNull("getNextAuditEvent should return null as there no more audit events after the current one",auditEvent);
    }
    
	@Test
	@DataSet
	@ExpectedDataSet
	@Ignore
	public void testAddChild() throws Exception {
		Taxon parent = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
		assert parent != null : "taxon cannot be null";
		Taxon child = Taxon.NewInstance(null, null);
		child.setTitleCache("Acherontia lachesis diehli Eitschberger, 2003", true);
		child.addTaxonRelation(parent, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(),null, null);
		taxonDao.save(child);
		setComplete();
		endTransaction();
	}
	
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testFind() {
    	Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
    	assert taxon != null : "taxon cannot be null";
    	
    	assertEquals("getTaxonomicChildrenCount should return 1 in this view",1,taxon.getTaxonomicChildrenCount());
    	assertEquals("getRelationsToThisTaxon should contain 1 TaxonRelationship in this view",1,taxon.getRelationsToThisTaxon().size());
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testFindInPreviousView() {
    	AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
    	Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
    	assert taxon != null : "taxon cannot be null";
    	
    	assertEquals("getTaxonomicChildrenCount should return 0 in this view",0,taxon.getTaxonomicChildrenCount());
    	assertTrue("getRelationsToThisTaxon should contain 0 TaxonRelationship in this view",taxon.getRelationsToThisTaxon().isEmpty());
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testGetRelations() {
    	Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
    	assert taxon != null : "taxon cannot be null";
    	List<TaxonRelationship> taxonRelations = taxonDao.getRelatedTaxa(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), null, null);
    	assertNotNull("getRelatedTaxa should return a list", taxonRelations);
    	assertEquals("there should be one TaxonRelationship in the list in the current view",1,taxonRelations.size());
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testCountRelations() {
    	Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
    	assert taxon != null : "taxon cannot be null";
    	assertEquals("countRelatedTaxa should return 1 in the current view",1, taxonDao.countRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()));
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testGetRelationsInPreviousView() {
       AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
       Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
       assert taxon != null : "taxon cannot be null";
    
    	List<TaxonRelationship> taxonRelations = taxonDao.getRelatedTaxa(taxon, TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(), null, null);
    	assertNotNull("getRelatedTaxa should return a list",taxonRelations);
    	assertTrue("there should be no TaxonRelationships in the list in the prior view",taxonRelations.isEmpty());
    }
    
    @Test
    @DataSet("TaxonDaoHibernateImplTest.testFind.xml")
    public void testCountRelationsInPreviousView() {
    	AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
    	Taxon taxon = (Taxon)taxonDao.findByUuid(acherontiaLachesis);
    	assert taxon != null : "taxon cannot be null";
    	assertEquals("countRelatedTaxa should return 0 in the current view",0, taxonDao.countRelatedTaxa(taxon,TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN()));
    }
}
