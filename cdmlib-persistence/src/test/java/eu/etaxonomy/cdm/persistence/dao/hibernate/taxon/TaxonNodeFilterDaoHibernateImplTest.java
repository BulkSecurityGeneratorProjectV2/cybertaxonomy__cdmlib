/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @date 2014/06/13
 *
 */
public class TaxonNodeFilterDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonNodeDao taxonNodeDao;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private TaxonNodeFilterDaoHibernateImpl filterDao;


    private Classification classification1;
    private TaxonNode node1;
    private TaxonNode node2;
    private TaxonNode node3;
    private TaxonNode node4;
    private TaxonNode node5;
    private Taxon taxon1;
    private Taxon taxon2;
    private Taxon taxon3;
    private Taxon taxon4;
    private Taxon taxon5;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        classification1 = Classification.NewInstance("TestClassification");
        Reference citation = null;
        String microCitation = null;
        taxon1 = Taxon.NewInstance(null, null);
        taxon2 = Taxon.NewInstance(null, null);
        taxon3 = Taxon.NewInstance(null, null);
        taxon4 = Taxon.NewInstance(null, null);
        taxon5 = Taxon.NewInstance(null, null);
        node1 = classification1.addChildTaxon(taxon1, citation, microCitation);
        node1= taxonNodeDao.save(node1);

        node2 = classification1.addChildTaxon(taxon2, citation, microCitation);
        node2 = taxonNodeDao.save(node2);
        node3 = node1.addChildTaxon(taxon3, citation, microCitation);
        taxonNodeDao.save(node3);
        node4 = node3.addChildTaxon(taxon4, citation, microCitation);
        taxonNodeDao.save(node4);
        node5 = node3.addChildTaxon(taxon5, citation, microCitation);
        node5 = taxonNodeDao.save(node5);
        //MergeResult result = taxonNodeDao.merge(node5, true);
        //node5 = (TaxonNode) result.getMergedEntity();

        //taxonNodeDao.save(node5);



        classificationDao.save(classification1);


    }

    @Test
    public void testListUuidsBySubtree() {
        Classification classification = classificationDao.findByUuid(classification1.getUuid());
        TaxonNodeFilter filter = new TaxonNodeFilter(node1);
        List<UUID> listUuid = filterDao.listUuids(filter);
//      List<TaxonNode> children = taxonNodeDao.listChildrenOf(node1, null, null, null, true);
        Assert.assertEquals("All 4 children should be returned", 4, listUuid.size());
        Assert.assertTrue(listUuid.contains(node4.getUuid()));
        Assert.assertFalse(listUuid.contains(node2.getUuid()));
        Assert.assertFalse(listUuid.contains(classification.getRootNode().getUuid()));


        filter = new TaxonNodeFilter(classification.getRootNode());
        listUuid = filterDao.listUuids(filter);
        //FIXME still unclear if (empty) root node should be part of the result
        Assert.assertEquals("All 6 children should be returned", 6, listUuid.size());

        filter = new TaxonNodeFilter(node3);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 3 children should be returned", 3, listUuid.size());

        filter.orSubtree(node2);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 3 children and node 2 should be returned", 4, listUuid.size());
        Assert.assertTrue(listUuid.contains(node2.getUuid()));

        filter = new TaxonNodeFilter(node1).notSubtree(node4);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("Node and 2 children but not node4 should be returned", 3, listUuid.size());
        Assert.assertFalse(listUuid.contains(node4.getUuid()));

        //uuids
        filter = TaxonNodeFilter.NewSubtreeInstance(node3.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 3 children should be returned", 3, listUuid.size());


        filter = TaxonNodeFilter.NewSubtreeInstance(taxon1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("A NON subtree uuid should not return a result", 0, listUuid.size());

    }

    @Test
    public void testListUuidsByClassification() {
        Classification classification = classificationDao.findByUuid(classification1.getUuid());

        TaxonNodeFilter filter;
        List<UUID> listUuid;

        filter = new TaxonNodeFilter(classification);
        listUuid = filterDao.listUuids(filter);
        //FIXME still unclear if (empty) root node should be part of the result
        Assert.assertEquals("All 6 children should be returned", 6, listUuid.size());

        filter = TaxonNodeFilter.NewClassificationInstance(classification.getUuid());
        listUuid = filterDao.listUuids(filter);
        //FIXME still unclear if (empty) root node should be part of the result
        Assert.assertEquals("All 6 children should be returned", 6, listUuid.size());

        filter = TaxonNodeFilter.NewClassificationInstance(taxon1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("A NON classification uuid should not return a result", 0, listUuid.size());

    }

    @Test
    public void testListUuidsByTaxon() {

        TaxonNodeFilter filter;
        List<UUID> listUuid;

        filter = TaxonNodeFilter.NewTaxonInstance(taxon1);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 1 taxon should be returned", 1, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonInstance(taxon1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 1 taxon should be returned", 1, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonInstance(taxon1.getUuid()).orTaxon(taxon2.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 2 taxa should be returned", 2, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonInstance(node1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("A NON taxon uuid should not return a result", 0, listUuid.size());
    }

    @Test
    public void testListUuidsByTaxonNode() {

        TaxonNodeFilter filter;
        List<UUID> listUuid;

        filter = TaxonNodeFilter.NewTaxonNodeInstance(node1);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 1 taxon should be returned", 1, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonNodeInstance(node1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 1 nodes should be returned", 1, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonNodeInstance(node1.getUuid())
                .orTaxonNode(node2.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 2 nodes should be returned", 2, listUuid.size());

        filter = TaxonNodeFilter.NewTaxonNodeInstance(taxon1.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("A NON taxon node uuid should not return a result", 0, listUuid.size());

    }

    @Test
    public void testListUuidsCombined() {
        Classification classification = classificationDao.findByUuid(classification1.getUuid());
        TaxonNodeFilter filter = new TaxonNodeFilter(node1);
        List<UUID> listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("All 4 children should be returned", 4, listUuid.size());

        filter.orClassification(classification.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("Still 4 children should be returned", 4, listUuid.size());

        filter.orTaxon(taxon3).orTaxon(taxon4);
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("The 2 children should be returned", 2, listUuid.size());

        filter.orTaxonNode(node3.getUuid());
        listUuid = filterDao.listUuids(filter);
        Assert.assertEquals("1 node should remain", 1, listUuid.size());

    }



    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
