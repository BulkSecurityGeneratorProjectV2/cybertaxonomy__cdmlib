/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.hibernate.sql.InFragment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Suite.SuiteClasses;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.test.suite.CdmTestSuite;

/**
 * @author a.mueller
 *
 */
public class BotanicNameCacheStrategyTest {
	private static final Logger logger = Logger.getLogger(BotanicNameCacheStrategyTest.class);
	
	private BotanicalName familyName;
	private BotanicalName genusName;
	private BotanicalName subGenusName;
	private BotanicalName speciesName;
	private BotanicalName subSpeciesName;
	
	private final String familyNameString = "Familia";
	private final String genusNameString = "Genus";
	private final String speciesNameString = "Abies alba";
	private final String subSpeciesNameString = "Abies alba subsp. beta";
	
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
		familyName = BotanicalName.PARSED_NAME(familyNameString, Rank.FAMILY());
		genusName = BotanicalName.PARSED_NAME(genusNameString, Rank.GENUS());
		
		//TODO
		subGenusName = new BotanicalName(Rank.SUBGENUS());
		subGenusName.setUninomial("Genus");
		subGenusName.setInfraGenericEpithet("InfraGenericPart");
		
		speciesName = BotanicalName.PARSED_NAME(speciesNameString);
		subSpeciesName = BotanicalName.PARSED_NAME(subSpeciesNameString);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	

/********* TEST *******************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy#NewInstance()}.
	 */
	@Test
	public final void testNewInstance() {
		BotanicNameCacheStrategy cacheStrategy = BotanicNameCacheStrategy.NewInstance();
		assertNotNull(cacheStrategy);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetNameCache() {
		assertEquals(subSpeciesNameString, subSpeciesName.getNameCache());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy#getFullNameCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetFullNameCache() {
		assertEquals(subSpeciesNameString, subSpeciesName.getNameCache());
		logger.warn("Not yet fully implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy#getUninomialNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetUninomialNameCache() {
		assertEquals(familyNameString, familyName.getNameCache());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy#getInfraGenusNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetInfraGenusNameCache() {
		String methodName = "getInfraGenusNameCache";
		Method method = getMethod(BotanicNameCacheStrategy.class, methodName, BotanicalName.class);
		
		BotanicNameCacheStrategy cacheStrategy = BotanicNameCacheStrategy.NewInstance();
		this.getValue(method, cacheStrategy, subGenusName);
		assertEquals("Genus (InfraGenericPart)", subGenusName.getNameCache());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy#getSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetSpeciesNameCache() {
		assertEquals(speciesNameString, speciesName.getNameCache());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.BotanicNameCacheStrategy#getInfraSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetInfraSpeciesNameCache() {
		assertEquals(subSpeciesNameString, subSpeciesName.getNameCache());
	}
	
	
	protected Method getMethod(Class clazz, String methodName, Class paramClazzes){
		Method method;
		try {
			method = clazz.getDeclaredMethod("getInfraGenusNameCache", BotanicalName.class);
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
