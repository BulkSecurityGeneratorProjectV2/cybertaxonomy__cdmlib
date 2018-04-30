/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.common;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 22.01.2008
 *
 */
public class CdmUtilsTest {
	private static final Logger logger = Logger.getLogger(CdmUtilsTest.class);


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
	public void testGetHomeDir() {
		//Assert.assertEquals("", CdmUtils.getHomeDir());
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

	/**
	 * This is a default test for fast running any simple test. It can be overriden and ignored whenever needed.
	 */
	@Test
	public void testAny(){
		String str = "Noms vernaculaires:";
		if (! str.matches("Nom(s)? vernaculaire(s)?\\:")){
			System.out.println("NO");
		}
	}

    @Test
    public void testEqualsIgnoreWS(){
        String str1 = null;
        String str2 = null;
        Assert.assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str2 = "Any ";
        Assert.assertFalse(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "Any ";
        Assert.assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "An y eer";
        str2 = "A nye er";
        Assert.assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "An y eer";
        str2 = "A nyfffe er";
        Assert.assertFalse(CdmUtils.equalsIgnoreWS(str1, str2));

    }

    /**
     * This test can be used for functional testing of any task but should
     * never be committed when failing.
     */
    @Test
    public void testSomething(){
       String MCL = "MCL[0-9]{1,3}(\\-[0-9]{1,4}(\\-[0-9]{1,4}(\\-[0-9]{1,3}(\\-[0-9]{1,3})?)?)?)?";
//        String MCL = "a{1,3}";
        String filter = "Acc "+MCL;

       String notes = "Acc: 0x is Hieracium djimilense subsp. neotericum Zahn MCL293-3140-00-630";
       String result;
       if (notes.matches("Acc:.*")){
           if (notes.matches("Acc: .*\\$$") || (notes.matches("Acc: .*"+MCL))){
               result = null;
           }else if (notes.matches("Acc: .*(\\$|"+MCL+")\\s*\\{.*\\}")){
               notes = notes.substring(notes.indexOf("{")+1, notes.length()-1);
               result = notes;
           }else if (notes.matches("Acc: .*(\\$|"+MCL+")\\s*\\[.*\\]")){
               notes = notes.substring(notes.indexOf("[")+1, notes.length()-1);
               result = notes;
           }else{
               logger.warn("Namenote: " + notes);
               result = notes;
           }
       }else if (notes.matches("Syn:.*")){
           if (notes.matches("Syn: .*\\$$") || (notes.matches("Syn: .*"+MCL))){
               result = null;
           }else if (notes.matches("Syn: .*(\\$|"+MCL+")\\s*\\{.*\\}")){
               notes = notes.substring(notes.indexOf("{")+1, notes.length()-1);
               result = notes;
           }else if (notes.matches("Syn: .*(\\$|"+MCL+")\\s*\\[.*\\]")){
               notes = notes.substring(notes.indexOf("[")+1, notes.length()-1);
               result = notes;
           }else{
               logger.warn("Namenote: " + notes);
               result = notes;
           }
       }else{
           result = notes;
       }
       System.out.println(result);
    }

}
