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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.PrintSeries;
import eu.etaxonomy.cdm.model.reference.Proceedings;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy;

/**
 * @author a.mueller
 *
 */
public class BotanicNameCacheStrategyTest {
	private static final Logger logger = Logger.getLogger(BotanicNameCacheStrategyTest.class);
	
	private static final String familyNameString = "Familia";
	private static final String genusNameString = "Genus";
	private static final String speciesNameString = "Abies alba";
	private static final String subSpeciesNameString = "Abies alba subsp. beta";
	private static final String appendedPhraseString = "app phrase";

	private static final String authorString = "L.";
	private static final String exAuthorString = "Exaut.";
	private static final String basAuthorString = "Basio, A.";
	private static final String exBasAuthorString = "ExBas. N.";

	private BotanicNameDefaultCacheStrategy strategy;
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
		strategy = BotanicNameDefaultCacheStrategy.NewInstance();
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
		
		citationRef = Book.NewInstance();
		// Gard. Dict. ed. 8, no. 1. 1768.
		citationRef.setTitle("Gard. Dict.");
		//citationRef.setPlacePublished("");
		citationRef.setVolume("1");
		citationRef.setEdition("ed. 8");
		GregorianCalendar testDate = new GregorianCalendar();
		testDate.set(1768, 1, 1);
		
		TimePeriod period = TimePeriod.NewInstance(testDate);
		
		citationRef.setDatePublished(period);
		
		//speciesName.setNomenclaturalMicroReference("89");
		NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE());
		speciesName.addStatus(nomStatus);
		
		speciesName.setNomenclaturalReference(citationRef);
		speciesName.setAppendedPhrase("app phrase");
		
//		subSpeciesName.setNomenclaturalReference(citationRef);
//		subSpeciesName.setAppendedPhrase("app phrase");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	

/********* TEST *******************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#NewInstance()}.
	 */
	@Test
	public final void testNewInstance() {
		BotanicNameDefaultCacheStrategy cacheStrategy = BotanicNameDefaultCacheStrategy.NewInstance();
		assertNotNull(cacheStrategy);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetNameCache() {
		assertEquals(subSpeciesNameString, subSpeciesName.getNameCache());
		assertNull(subSpeciesNameString, strategy.getNameCache(null));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetTitleCache() {
		assertNull(subSpeciesNameString, strategy.getTitleCache(null));
		subSpeciesName.setCombinationAuthorTeam(author);
		subSpeciesName.setExCombinationAuthorTeam(exAuthor);
		subSpeciesName.setBasionymAuthorTeam(basAuthor);
		subSpeciesName.setExBasionymAuthorTeam(exBasAuthor);
		assertEquals(subSpeciesNameString, strategy.getNameCache(subSpeciesName));
		assertEquals(subSpeciesNameString + " (" + exBasAuthorString + " ex " + basAuthorString + ")" +  " " + exAuthorString + " ex " + authorString  , strategy.getTitleCache(subSpeciesName));
		
		//Autonym
		subSpeciesName.setInfraSpecificEpithet("alba");
		subSpeciesName.setCombinationAuthorTeam(author);
		subSpeciesName.setBasionymAuthorTeam(null);
		subSpeciesName.setExCombinationAuthorTeam(null);
		subSpeciesName.setExBasionymAuthorTeam(null);
		assertEquals("Abies alba alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba L. alba", strategy.getTitleCache(subSpeciesName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getFullTitleCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetFullTitleCache() {
		assertNull(speciesNameString, strategy.getFullTitleCache(null));
		assertEquals("Abies alba app phrase, Gard. Dict. ed. 8, 1. 1768, nom. illeg.", strategy.getFullTitleCache(speciesName));
		
//		assertNull(subSpeciesNameString, strategy.getFullTitleCache(null));
//		assertEquals("Abies alba app phrase L. Gard. Dict. ed. 8, 1. 1768, nom. illeg.", strategy.getFullTitleCache(speciesName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getAuthorCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetAuthorshipCache() {
		subSpeciesName.setCombinationAuthorTeam(author);
		assertEquals(authorString, strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setExCombinationAuthorTeam(exAuthor);
		assertEquals(exAuthorString + " ex " + authorString  , strategy.getAuthorshipCache(subSpeciesName));
		
		subSpeciesName.setBasionymAuthorTeam(basAuthor);
		assertEquals("(" + basAuthorString + ")" +  " " + exAuthorString + " ex " + authorString  , strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setExBasionymAuthorTeam(exBasAuthor);
		assertEquals("(" + exBasAuthorString + " ex " + basAuthorString + ")" +  " " + exAuthorString + " ex " + authorString  , strategy.getAuthorshipCache(subSpeciesName));
		
		assertNull(subSpeciesNameString, strategy.getAuthorshipCache(null));
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getUninomialNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetGenusOrUninomialNameCache() {
		assertEquals(familyNameString, strategy.getNameCache(familyName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getInfraGenusNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetInfraGenusNameCache() {
		String methodName = "getInfraGenusNameCache";
		Method method = getMethod(NonViralNameDefaultCacheStrategy.class, methodName, NonViralName.class);
		this.getValue(method, strategy, subGenusName);
		assertEquals("Genus (InfraGenericPart)", strategy.getNameCache(subGenusName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetSpeciesNameCache() {
		String nameString = speciesNameString + " " + appendedPhraseString;
		assertEquals(nameString, strategy.getNameCache(speciesName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getInfraSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetInfraSpeciesNameCache() {
		assertEquals(subSpeciesNameString, strategy.getNameCache(subSpeciesName));
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getInfraSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testAutonyms() {
		subSpeciesName.setInfraSpecificEpithet("alba");
		subSpeciesName.setCombinationAuthorTeam(author);
		assertEquals("Abies alba alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba L. alba", strategy.getTitleCache(subSpeciesName));
	}
	
	protected Method getMethod(Class clazz, String methodName, Class paramClazzes){
		Method method;
		try {
			method = clazz.getDeclaredMethod("getInfraGenusNameCache", paramClazzes);
		} catch (SecurityException e) {
			logger.error("SecurityException " + e.getMessage());
			return null;
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException " + e.getMessage());
			return null;
		}
		return method;
	}
	
	protected String getValue(Method method, Object object,Object parameter){
		try {
			return (String)method.invoke(object, parameter);
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException " + e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException " + e.getMessage());
			return null;
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException " + e.getMessage());
			return null;
		}
	}


}
