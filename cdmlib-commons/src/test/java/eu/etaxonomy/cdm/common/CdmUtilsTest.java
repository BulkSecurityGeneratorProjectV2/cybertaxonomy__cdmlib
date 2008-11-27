package eu.etaxonomy.cdm.common;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CdmUtilsTest {
	private static final Logger logger = Logger.getLogger(CdmUtilsTest.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

/************************** TESTS ****************************************/
	
	@Test
	public void testGetReadableResourceStream() {
		String resourceFileName = CdmUtils.MUST_EXIST_FILE;
		try {
			InputStream inputStream = CdmUtils.getReadableResourceStream(resourceFileName);
			assertNotNull(inputStream);
		} catch (IOException e) {
			Assert.fail("IOException");
		}
	}

	@Test
	public void testGetFolderSeperator() {
		Assert.assertEquals(File.separator, CdmUtils.getFolderSeperator());
	}

	@Test
	public void testFindLibrary() {
		if (logger.isEnabledFor(Level.DEBUG)) {logger.debug(CdmUtils.findLibrary(CdmUtils.class));}
		
		String library = CdmUtils.findLibrary(CdmUtils.class);
		String endOfLibrary = "target/classes/eu/etaxonomy/cdm/common/CdmUtils.class";
		String libraryContains = "/cdmlib-commons/";
		
		Assert.assertTrue(library.endsWith(endOfLibrary)); 
		Assert.assertTrue(library.contains(libraryContains)); 
	}

}
