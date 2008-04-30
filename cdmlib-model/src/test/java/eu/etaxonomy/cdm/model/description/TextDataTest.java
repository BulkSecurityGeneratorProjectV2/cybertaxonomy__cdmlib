/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;

/**
 * @author a.mueller
 * @created 23.04.2008
 * @version 1.0
 */
public class TextDataTest {
	private static Logger logger = Logger.getLogger(TextDataTest.class);

	
	private static TextData textDataLeer;
	private static TextData textData1; 
	private static TextFormat format1;
	private static LanguageString languageString1;
	
	
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
		textDataLeer = TextData.NewInstance();
		format1 = TextFormat.NewInstance();
		textData1 = TextData.NewInstance("testText", Language.DEFAULT(), format1); 
		languageString1 = LanguageString.NewInstance("langText", Language.GERMAN());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

/* ************************** TESTS **********************************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#NewInstance()}.
	 */
	@Test
	public void testNewInstance() {
		assertNotNull(textDataLeer);
		assertNotNull(textDataLeer.getMultilanguageText());
		assertEquals(0, textDataLeer.getMultilanguageText().size());
		assertEquals(0, textDataLeer.countLanguages());
		assertNull(textDataLeer.getFormat());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#NewInstance()}.
	 */
	@Test
	public void testNewInstanceStringLanguageTextFormat() {
		assertNotNull(textData1);
		assertNotNull(textData1.getMultilanguageText());
		assertEquals(1, textData1.getMultilanguageText().size());
		assertEquals(1, textData1.countLanguages());
		LanguageString languageString = LanguageString.NewInstance("testText", Language.DEFAULT());
//		assertEquals("testText", textData1.getMultilanguageText().getText(Language.DEFAULT()));
		assertNotNull(textData1.getFormat());
		assertSame(format1, textData1.getFormat());
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#TextData()}.
	 */
	@Test
	public void testTextData() {
		textDataLeer = new TextData();
		assertNotNull(textDataLeer);
		assertNotNull(textDataLeer.getMultilanguageText());
		assertEquals(0, textDataLeer.getMultilanguageText().size());
		assertEquals(0, textDataLeer.countLanguages());
		assertNull(textDataLeer.getFormat());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#getTexts()}.
	 */
	@Test
	public void testGetText() {
		assertNotNull(textData1.getText(Language.DEFAULT()));
		assertNull(textDataLeer.getText(Language.DEFAULT()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#getMultilanguageText()}.
	 */
	@Test
	public void testGetMultilanguageText() {
		assertNotNull(textData1.getMultilanguageText());
//		assertEquals("testText", textData1.getMultilanguageText().getText(Language.DEFAULT()));
		assertNotNull(textDataLeer.getMultilanguageText());
//		assertNull(textDataLeer.getMultilanguageText().getText(Language.DEFAULT()));
		assertEquals(0, textDataLeer.getMultilanguageText().size());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#setMultilanguageText()}.
	 */
	@Test
	public void testSetMultilanguageText() {
		MultilanguageSet multilanguageSet = MultilanguageSet.NewInstance();
		assertFalse(multilanguageSet.equals(textData1.getMultilanguageText()));
		textData1.setMultilanguageText(multilanguageSet);
		assertSame(multilanguageSet, textData1.getMultilanguageText());
		textData1.setMultilanguageText(null);
		assertNotNull(textData1.getMultilanguageText());
		assertEquals(0, textData1.getMultilanguageText().size());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#addText(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testPutTextStringLanguage() {
		textDataLeer.putText("xx", Language.GERMAN());
		assertNull(textDataLeer.putText("francais", Language.FRENCH()));
		textDataLeer.putText("nothing", null);
		textDataLeer.putText(null, Language.CHINESE());
		assertNotNull(textDataLeer.getMultilanguageText());
		assertEquals(4 , textDataLeer.getMultilanguageText().size());
		assertEquals("xx", textDataLeer.putText("deutsch", Language.GERMAN()));
		assertEquals(4 , textDataLeer.getMultilanguageText().size());
		assertEquals("deutsch", textDataLeer.getText(Language.GERMAN()));
		assertEquals("francais", textDataLeer.getText(Language.FRENCH()));
		assertEquals("nothing", textDataLeer.getText(null));
		assertEquals(null, textDataLeer.getText(Language.CHINESE()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#addText(eu.etaxonomy.cdm.model.common.LanguageString)}.
	 */
	@Test
	public void testPutTextLanguageString() {
		LanguageString deutsch = LanguageString.NewInstance("xx", Language.GERMAN());
		textDataLeer.putText(deutsch);
		assertNull(textDataLeer.putText(LanguageString.NewInstance("francais", Language.FRENCH())));
		textDataLeer.putText(LanguageString.NewInstance("nothing", null));
		textDataLeer.putText(LanguageString.NewInstance(null, Language.CHINESE()));
		assertNotNull(textDataLeer.getMultilanguageText());
		assertEquals(4 , textDataLeer.getMultilanguageText().size());
		assertEquals("xx", textDataLeer.putText(LanguageString.NewInstance("deutsch", Language.GERMAN())));
		assertEquals(4 , textDataLeer.getMultilanguageText().size());

		assertEquals("deutsch", textDataLeer.getText(Language.GERMAN()));
		assertEquals("francais", textDataLeer.getText(Language.FRENCH()));
		assertEquals("nothing", textDataLeer.getText(null));
		assertEquals(null, textDataLeer.getText(Language.CHINESE()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#removeText(eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testRemoveText() {
		assertEquals(1, textData1.countLanguages());
		assertNull(textData1.removeText(Language.CHINESE()));
		assertEquals(1, textData1.countLanguages());
		LanguageString deutsch = LanguageString.NewInstance("xx", Language.GERMAN());
		textData1.putText(deutsch);
		textData1.putText(LanguageString.NewInstance("nothing", null));
		assertEquals(3, textData1.countLanguages());
		assertEquals("xx", textData1.removeText(Language.GERMAN()));
		assertEquals(2, textData1.countLanguages());
		textData1.removeText(null);
		assertEquals(1, textData1.countLanguages());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#getFormat()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#setFormat(eu.etaxonomy.cdm.model.description.TextFormat)}.
	 */
	@Test
	public void testGetSetFormat() {
		textDataLeer.setFormat(format1);
		assertSame(format1, textDataLeer.getFormat());
		textDataLeer.setFormat(null);
		assertNull(textDataLeer.getFormat());	
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.description.TextData#countLanguages()}.
	 */
	@Test
	public void testCountLanguages() {
		assertEquals(1, textData1.countLanguages());
		textData1.putText(LanguageString.NewInstance("nothing", null));
		assertEquals(2, textData1.countLanguages());
		textData1.removeText(null);
		assertEquals(1, textData1.countLanguages());
		textData1.removeText(Language.FRENCH());
		assertEquals(1, textData1.countLanguages());
		textData1.removeText(Language.DEFAULT());
		assertEquals(0, textData1.countLanguages());
		
	}	


}
