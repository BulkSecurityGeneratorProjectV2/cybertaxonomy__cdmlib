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

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.ZooNameDefaultCacheStrategy;

/**
 * @author a.mueller
 *
 */
public class ZoologicalNameCacheStrategyTest {
	private static final Logger logger = Logger.getLogger(ZoologicalNameCacheStrategyTest.class);
	
	private ZooNameDefaultCacheStrategy strategy;
	private ZoologicalName familyName;
	private ZoologicalName genusName;
	private ZoologicalName subGenusName;
	private ZoologicalName speciesName;
	private ZoologicalName subSpeciesName;
	private INomenclaturalAuthor author;
	private INomenclaturalAuthor exAuthor;
	private INomenclaturalAuthor basAuthor;
	private INomenclaturalAuthor exBasAuthor;
	
	private final String familyNameString = "Familia";
	private final String genusNameString = "Genus";
	private final String speciesNameString = "Abies alba";
	private final String subSpeciesNameString = "Abies alba subsp. beta";

	private final String authorString = "L.";
	private final String exAuthorString = "Exaut.";
	private final String basAuthorString = "Basio, A.";
	private final String exBasAuthorString = "ExBas. N.";
	
	private final Integer publicationYear = 1928;
	private final Integer originalPublicationYear = 1860;
	
	

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
		strategy = ZooNameDefaultCacheStrategy.NewInstance();
		familyName = ZoologicalName.PARSED_NAME(familyNameString, Rank.FAMILY());
		genusName = ZoologicalName.PARSED_NAME(genusNameString, Rank.GENUS());
		
		subGenusName = ZoologicalName.NewInstance(Rank.SUBGENUS());
		subGenusName.setGenusOrUninomial("Genus");
		subGenusName.setInfraGenericEpithet("InfraGenericPart");
		
		speciesName = ZoologicalName.PARSED_NAME(speciesNameString);
		subSpeciesName = ZoologicalName.PARSED_NAME(subSpeciesNameString);

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
	

/********* TEST *******************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#NewInstance()}.
	 */
	@Test
	public final void testNewInstance() {
		assertNotNull(strategy);
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
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getFullNameCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetTitleCache() {
		assertNull(subSpeciesNameString, strategy.getTitleCache(null));
		subSpeciesName.setCombinationAuthorTeam(author);
		subSpeciesName.setExCombinationAuthorTeam(exAuthor);
		subSpeciesName.setBasionymAuthorTeam(basAuthor);
		subSpeciesName.setExBasionymAuthorTeam(exBasAuthor);
		subSpeciesName.setPublicationYear(publicationYear);
		subSpeciesName.setOriginalPublicationYear(originalPublicationYear);
		
		assertEquals(subSpeciesNameString, strategy.getNameCache(subSpeciesName));
		assertEquals(subSpeciesNameString + " (" + exBasAuthorString + " ex " + basAuthorString  + ", " + originalPublicationYear +")" +  " " + exAuthorString + " ex " + authorString + ", " + publicationYear, strategy.getTitleCache(subSpeciesName));
		
		//Autonym TODO are there autonyms in zoology?
		subSpeciesName.setInfraSpecificEpithet("alba");
		subSpeciesName.setCombinationAuthorTeam(author);
		subSpeciesName.setBasionymAuthorTeam(null);
		subSpeciesName.setExCombinationAuthorTeam(null);
		subSpeciesName.setExBasionymAuthorTeam(null);
		assertEquals("Abies alba alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba L. alba", strategy.getTitleCache(subSpeciesName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getAuthorCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetAuthorshipCache() {
		subSpeciesName.setCombinationAuthorTeam(author);
		assertEquals(authorString, strategy.getAuthorshipCache(subSpeciesName));
		subSpeciesName.setPublicationYear(publicationYear);
		assertEquals(authorString + ", " + publicationYear, strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setExCombinationAuthorTeam(exAuthor);
		assertEquals( exAuthorString + " ex " + authorString + ", " + publicationYear , strategy.getAuthorshipCache(subSpeciesName));
		
		subSpeciesName.setBasionymAuthorTeam(basAuthor);
		assertEquals("(" + basAuthorString + ")" +  " " + exAuthorString + " ex " + authorString  + ", " + publicationYear  , strategy.getAuthorshipCache(subSpeciesName));
		subSpeciesName.setOriginalPublicationYear(originalPublicationYear);
		assertEquals("(" + basAuthorString  + ", " + originalPublicationYear  + ")" +  " " + exAuthorString + " ex " + authorString  + ", " + publicationYear  , strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setExBasionymAuthorTeam(exBasAuthor);
		assertEquals("(" + exBasAuthorString + " ex " +  basAuthorString + ", " + originalPublicationYear  + ")" +  " " + exAuthorString + " ex " + authorString  + ", " + publicationYear   , strategy.getAuthorshipCache(subSpeciesName));
		
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
		assertEquals(speciesNameString, strategy.getNameCache(speciesName));
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
