/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 */
public class TaxonBaseTest extends EntityTestBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(TaxonBaseTest.class);

	private Reference sec;
	private IZoologicalName name1;
	private IBotanicalName name2;
	private Taxon rootT;
	private Taxon taxon1;
	private Synonym synonym1;
	private Taxon freeT;

	@Before
	public void setUp() throws Exception {
		sec= ReferenceFactory.newBook();
		sec.setTitleCache("Schoenes saftiges Allg�u", true);
		name1 = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES(),"Panthera",null,"onca",null,null,null,"p.1467", null);
		HomotypicalGroup homotypicalGroup = HomotypicalGroup.NewInstance();
		name2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(),"Abies",null,"alba",null,null,null,"p.317", homotypicalGroup);
		// taxa
		taxon1 = Taxon.NewInstance(name1,sec);
		synonym1 = Synonym.NewInstance(name2,sec);
		freeT = Taxon.NewInstance(null, null);
	}

/**************** TESTS **************************************/

	@Test
	public final void testGetName() {
		assertEquals(name1.getTitleCache(), taxon1.getName().getTitleCache());
		assertNull(freeT.getName());
	}
//
//	/**
//	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonBase#setName(eu.etaxonomy.cdm.model.name.TaxonName)}.
//	 */
//	@Test
//	public final void testSetName() {
//		assertNull(freeT.getName());
//		freeT.setName(name2);
//		assertNotNull(freeT.getName());
//		assertSame(freeT.getName(), name2);
//		assertTrue(name1.getTaxa().contains(taxon1));
//		assertTrue(name2.getSynonyms().contains(synonym1));
//	}

	@Test
	public final void testIsSetDoubtful() {
		boolean oldValue;
		oldValue = taxon1.isDoubtful();
		taxon1.setDoubtful(!oldValue);
		assertEquals(! oldValue, taxon1.isDoubtful());
		taxon1.setDoubtful(oldValue);
		assertEquals( oldValue, taxon1.isDoubtful());
	}

	@Test
	public final void testGetSec() {
		assertEquals(sec.getTitleCache(), taxon1.getSec().getTitleCache());
		assertNull(freeT.getSec());
	}

	@Test
	public final void testSetSec() {
		assertNull(freeT.getSec());
		freeT.setSec(sec);
		assertNotNull(freeT.getSec());
		assertSame(freeT.getSec(), sec);
	}

	@Test
	public final void testClone(){

		TaxonName test = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		String genus = "test";
		String infraGenericEpithet = "test";
		test.setGenusOrUninomial(genus);
		test.setInfraGenericEpithet(infraGenericEpithet);
		Reference secRef = ReferenceFactory.newArticle();
		secRef.setTitle("Test ...");
		freeT.setSec(secRef);
		freeT.setName(test);
		Taxon clone = freeT.clone();
		assertSame(freeT.getSec(), clone.getSec());  //this was assertNull first, but we realized that it is not intuitive to remove the sec when cloning.
		assertSame(freeT.getName(), clone.getName());
	}

    /*
     * Moved from IdentifiableEntityTest to here due to #922
     */
    @Test
    public void testCompareTo() {

        TaxonName abies = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), null);
        abies.setNameCache("Abies");
        abies.setTitleCache("Abies", true);
        Reference sec = ReferenceFactory.newArticle();
        sec.setTitle("Abies alba Ref");

        Taxon abiesTaxon = Taxon.NewInstance(abies, sec);

        TaxonName abiesMill = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), null);
        abiesMill.setNameCache("Abies");
        abiesMill.setTitleCache("Abies Mill.", true);
        Taxon abiesMillTaxon = Taxon.NewInstance(abiesMill, sec);

        int result = 0;

        // "Abies" < "Abies Mill."
        result = abies.compareToName(abiesMill);
        assertTrue(result < 0);

        abiesTaxon = abies.getTaxa().iterator().next();

        assertTrue(abiesTaxon.compareToTaxon(abiesTaxon) == 0);
        assertTrue(abiesMillTaxon.compareToTaxon(abiesTaxon) > 0);
        assertTrue(abiesTaxon.compareToTaxon(abiesMillTaxon) < 0);
    }
}