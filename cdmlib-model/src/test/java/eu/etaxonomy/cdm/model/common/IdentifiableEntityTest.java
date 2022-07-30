/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.babadshanjan
 * @since 02.02.2009
 */
public class IdentifiableEntityTest extends EntityTestBase {

	private TaxonName abies;
	private TaxonName abiesMill;
	private TaxonName abiesAlba;
	private TaxonName abiesAlbaMichx;
	private TaxonName abiesAlbaMill;
	private TaxonName abiesAlbaxPinusBeta;
	private TaxonName pinusBeta;

	private Taxon abiesTaxon;
	private Taxon abiesMillTaxon;

	private TaxonName abiesAutonym;
	private Taxon abiesAutonymTaxon;

	private TaxonName abiesBalsamea;
	private Taxon abiesBalsameaTaxon;
//	private Taxon abiesAlbaxPinusBetaTaxon;

	@Before
	public void setUp() throws Exception {

		abies = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), null);
		abies.setNameCache("Abies");
		abies.setTitleCache("Abies", true);
		Reference sec = ReferenceFactory.newArticle();
		sec.setTitle("Abies alba Ref");

		abiesTaxon = Taxon.NewInstance(abies, sec);

		abiesMill = TaxonNameFactory.NewNonViralInstance(Rank.GENUS(), null);
		abiesMill.setNameCache("Abies");
		abiesMill.setTitleCache("Abies Mill.", true);
		abiesMillTaxon = Taxon.NewInstance(abiesMill, sec);

		abiesAlba = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlba.setNameCache("Abies alba");
		abiesAlba.setTitleCache("Abies alba", true);

		abiesAlbaMichx = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlbaMichx.setNameCache("Abies alba");
		abiesAlbaMichx.setTitleCache("Abies alba Michx.", true);

		abiesAlbaMill = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES(), null);
		abiesAlbaMill.setNameCache("Abies alba");
		abiesAlbaMill.setTitleCache("Abies alba Mill.", true);

		abiesAutonym  = TaxonNameFactory.NewNonViralInstance(Rank.SECTION_BOTANY());
		abiesAutonym.setGenusOrUninomial("Abies");
		abiesAutonym.setInfraGenericEpithet("Abies");

		abiesAutonym.setTitleCache("Abies Mill. sect. Abies", true);
		abiesAutonym.getNameCache();
		abiesAutonymTaxon = Taxon.NewInstance(abiesAutonym, sec);

		abiesBalsamea  = TaxonNameFactory.NewNonViralInstance(Rank.SECTION_BOTANY());
		abiesBalsamea.setGenusOrUninomial("Abies");
		abiesBalsamea.setInfraGenericEpithet("Balsamea");
		abiesBalsamea.getNameCache();
		abiesBalsamea.setTitleCache("Abies sect. Balsamea L.", true);
		abiesBalsameaTaxon = Taxon.NewInstance(abiesBalsamea, sec);

		abiesAlbaxPinusBeta = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		pinusBeta = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
		pinusBeta.setGenusOrUninomial("Pinus");
		pinusBeta.setSpecificEpithet("beta");
		abiesAlbaxPinusBeta.setHybridFormula(true);
		abiesAlbaxPinusBeta.addHybridParent(abiesAlba, HybridRelationshipType.FIRST_PARENT(), null);
		abiesAlbaxPinusBeta.addHybridParent(pinusBeta, HybridRelationshipType.SECOND_PARENT(), null);
	}

	@Test
	public void testAddCredit() {
		assertNotNull("A list should always be returned",abies.getCredits());
		assertTrue("No credits should exist",abies.getCredits().isEmpty());
		String text1 = "Credit1";
		String text2 = "Credit2";
		String text3 = "Credit0"; //for sorting order
		Person person = Person.NewTitledInstance("Me");
		TimePeriod timePeriod = TimePeriod.NewInstance(1925);
		abies.addCredit(Credit.NewInstance(person, timePeriod, text1));
		assertEquals("Number of credits should be 1",1,abies.getCredits().size());
		abies.addCredit(Credit.NewInstance(person, timePeriod, text2));
		assertEquals("Number of credits should be 2",2,abies.getCredits().size());
		abies.addCredit(Credit.NewInstance(person, timePeriod, text3));
		assertEquals("Number of credits should be 3",3,abies.getCredits().size());
		assertEquals("Credit0 should be last in list", text3, abies.getCredits(2).getText());
	}

	@Test
	public void testRemoveCredit() {
		assertNotNull("A list should always be returned",abies.getCredits());
		String text1 = "Credit1";
		String text2 = "Credit2";
		Person person = Person.NewTitledInstance("Me");
		Credit credit1 = Credit.NewInstance(person, null, text1);
		Credit credit2 = Credit.NewInstance(person, null, text2);
		abies.addCredit(credit1);
		abies.addCredit(credit2);
		assertEquals("Number of credits should be 2",2,abies.getCredits().size());
		abies.removeCredit(credit1);
		assertNotNull("A list should always be returned",abies.getCredits());
		assertFalse("The list should not be empty",abies.getCredits().isEmpty());
		assertEquals("Number of credits should be 1",1,abies.getCredits().size());
		assertEquals("Remaining credit should be credit2",credit2,abies.getCredits().get(0));
		abies.removeCredit(credit2);
		assertNotNull("A list should always be returned",abies.getCredits());
		assertTrue("No credits should exist",abies.getCredits().isEmpty());
	}

	@Test
	public void testClone(){
		IdentifiableEntity<?> clone = abies.clone();
		assertNotNull(clone);
		assertEquals(clone.annotations, abies.annotations);
		assertEquals(clone.markers, abies.markers);
		assertFalse(clone.uuid.equals(abies.uuid));
	}
}