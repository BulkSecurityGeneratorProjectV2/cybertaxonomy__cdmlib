/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.name;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Book;

/**
 * @author a.mueller
 * @created 26.11.2008
 * @version 1.0
 */
public class NonViralNameDefaultCacheStrategyTest {
	private static final Logger logger = Logger.getLogger(NonViralNameDefaultCacheStrategyTest.class);

	private NonViralNameDefaultCacheStrategy strategy;
	
	private static final String familyNameString = "Familia";
	private static final String genusNameString = "Genus";
	private static final String speciesNameString = "Abies alba";
	private static final String subSpeciesNameString = "Abies alba subsp. beta";
	private static final String appendedPhraseString = "app phrase";

	private static final String authorString = "L.";
	private static final String exAuthorString = "Exaut.";
	private static final String basAuthorString = "Basio, A.";
	private static final String exBasAuthorString = "ExBas. N.";

	private BotanicalName familyName;
	private BotanicalName genusName;
	private BotanicalName subGenusName;
	private BotanicalName speciesName;
	private BotanicalName subSpeciesName;
	private INomenclaturalAuthor author;
	private INomenclaturalAuthor exAuthor;
	private INomenclaturalAuthor basAuthor;
	private INomenclaturalAuthor exBasAuthor;
	private Book citationRef;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		strategy = NonViralNameDefaultCacheStrategy.NewInstance();
		familyName = BotanicalName.PARSED_NAME(familyNameString, Rank.FAMILY());
		genusName = BotanicalName.PARSED_NAME(genusNameString, Rank.GENUS());
		
		subGenusName = BotanicalName.NewInstance(Rank.SUBGENUS());
		subGenusName.setGenusOrUninomial("Genus");
		subGenusName.setInfraGenericEpithet("InfraGenericPart");
		
		speciesName = BotanicalName.PARSED_NAME(speciesNameString);
		subSpeciesName = BotanicalName.PARSED_NAME(subSpeciesNameString);

		author = Person.NewInstance();
		author.setNomenclaturalTitle(authorString);
		exAuthor = Person.NewInstance();
		exAuthor.setNomenclaturalTitle(exAuthorString);
		basAuthor = Person.NewInstance();
		basAuthor.setNomenclaturalTitle(basAuthorString);
		exBasAuthor = Person.NewInstance();
		exBasAuthor.setNomenclaturalTitle(exBasAuthorString);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//**************************** TESTS **************************************************
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetTitleCache() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getFullTitleCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetFullTitleCache() {
		logger.warn("Not yet implemented");
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetNameCache() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getAuthorshipCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetAuthorshipCache() {
		this.speciesName.setCombinationAuthorTeam(author);
		assertEquals(author.getNomenclaturalTitle(), speciesName.getAuthorshipCache());
		this.speciesName.setBasionymAuthorTeam(basAuthor);
		String expected = strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitle()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+author.getNomenclaturalTitle();
		assertEquals(expected, speciesName.getAuthorshipCache());
		String authorshipcache = "authorshipcache";
//		speciesName.setAuthorshipCache(authorshipcache);
//		assertEquals(authorshipcache, speciesName.getAuthorshipCache());
//		speciesName.setCombinationAuthorTeam(exAuthor);
//		assertEquals(authorshipcache, speciesName.getAuthorshipCache()); //cache is protected
		
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getTaggedName(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetTaggedName() {
		logger.warn("testGetTaggedName yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setNameAuthorSeperator(java.lang.String)}.
	 */
	@Test
	public void testGetSetNameAuthorSeperator() {
		String authorSeparator = "authorSeparator";
		strategy.setNameAuthorSeperator(authorSeparator);
		assertEquals(authorSeparator, strategy.getNameAuthorSeperator());
		strategy.setNameAuthorSeperator(null);
		assertNull(strategy.getNameAuthorSeperator());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setBasionymStart(java.lang.String)}.
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getBasionymStart()}.
	 */
	@Test
	public void testGetSetBasionymStart() {
		String basStart = "start";
		strategy.setBasionymStart(basStart);
		assertEquals(basStart, strategy.getBasionymStart());
		strategy.setBasionymStart(null);
		assertNull(strategy.getBasionymStart());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setBasionymEnd(java.lang.String)}.
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getBasionymEnd()}.
	*/
	@Test
	public void testGetSetBasionymEnd() {
		String basEnd = "end";
		strategy.setBasionymEnd(basEnd);
		assertEquals(basEnd, strategy.getBasionymEnd());
		strategy.setBasionymEnd(null);
		assertNull(strategy.getBasionymEnd());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setExAuthorSeperator(java.lang.String)}.
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getExAuthorSeperator()}.
	 */
	@Test
	public void testGetSetExAuthorSeperator() {
		String exAuthorSeparator = "exAuthorSeparator";
		strategy.setExAuthorSeperator(exAuthorSeparator);
		assertEquals(exAuthorSeparator, strategy.getExAuthorSeperator());
		strategy.setExAuthorSeperator(null);
		assertNull(strategy.getExAuthorSeperator());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setBasionymAuthorCombinationAuthorSeperator(java.lang.CharSequence)}.
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getBasionymAuthorCombinationAuthorSeperator()}.
	 */
	@Test
	public void testSetBasionymAuthorCombinationAuthorSeperator() {
		String basComSeparator = "basComSeparator";
		strategy.setBasionymAuthorCombinationAuthorSeperator(basComSeparator);
		assertEquals(basComSeparator, strategy.getBasionymAuthorCombinationAuthorSeperator());
		strategy.setBasionymAuthorCombinationAuthorSeperator(null);
		assertNull(strategy.getBasionymAuthorCombinationAuthorSeperator());
	}

}
