/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class PolytomousKeyTest extends EntityTestBase {

	private static Logger logger = LogManager.getLogger(PolytomousKeyTest.class);

	private PolytomousKey key1;
	private Taxon taxon1;

	@Before
	public void setUp() throws Exception {
		key1 = PolytomousKey.NewInstance();
		key1.setTitleCache("My Test Key", true);
		PolytomousKeyNode root = key1.getRoot();
		root.setQuestion(KeyStatement.NewInstance("Is this Aus bus?"));
		// child1
		taxon1 = Taxon.NewInstance(null, null);
		taxon1.setTitleCache("Aus bus L.", true);
		PolytomousKeyNode child1 = PolytomousKeyNode.NewInstance("Yes", null,
				taxon1, null);
		Feature feature1 = Feature.NewInstance(null, "Leaf", null);
		child1.setFeature(feature1);
		root.addChild(child1);

		// child2
		Taxon taxon2 = Taxon.NewInstance(null, null);
		taxon2.setTitleCache("Cus dus Mill.", true);
		PolytomousKeyNode child2 = PolytomousKeyNode.NewInstance("No");
		child2.setTaxon(taxon2);
		root.addChild(child2);
		// child3
		Taxon taxon3 = Taxon.NewInstance(null, null);
		taxon3.setTitleCache("Cus dus subs. rus L.", true);
		PolytomousKeyNode child3 = PolytomousKeyNode
				.NewInstance("Long and wide");
		child3.setTaxon(taxon3);
		child1.addChild(child3);
		// child4
		Taxon taxon4 = Taxon.NewInstance(null, null);
		taxon4.setTitleCache("Cus dus subs. zus L.", true);
		PolytomousKeyNode child4 = PolytomousKeyNode
				.NewInstance("Small and narrow");
		child4.setTaxon(taxon4);
		child1.addChild(child4);

		PolytomousKey key2 = PolytomousKey.NewTitledInstance("Second Key");
		child3.setSubkey(key2);

		child4.setOtherNode(key2.getRoot());

	}

	// ********************* Tests
	// *******************************************************/

	@Test
	public void testNewInstance() {
		PolytomousKey newKey = PolytomousKey.NewInstance();
		Assert.assertNotNull(newKey);
	}

	@Test
	public void testNewTitledInstance() {
		logger.warn("testNewTitledInstance Not yet implemented");
	}

	@Test
	public void testPolytomousKey() {
		PolytomousKey newKey = new PolytomousKey();
		Assert.assertNotNull(newKey);
	}

	@Test
	public void testPrint() {
		PrintStream stream = null;
		String strKey = key1.print(stream);
//		System.out.println(strKey);
		Assert.assertEquals(
				"",
				"My Test Key\n"
				+ "  1. Is this Aus bus?\n"
				+ "    a) Yes ... 2, Aus bus L.\n"
				+ "    b) No ... Cus dus Mill.\n"
				+ "  2. Leaf\n"
				+ "    a) Long and wide ... Cus dus subs. rus L., Second Key\n"
				+ "    b) Small and narrow ... Cus dus subs. zus L., Second Key 1\n",
				strKey);
	}


	@Test
	public void testClone() {
		PolytomousKey clone = key1.clone();
		assertNotNull(clone.getRoot());
		assertNotSame(clone.getRoot(), key1.getRoot());
		assertTrue(clone.getRoot().getChildren().size() == 0);
		assertTrue(key1.getRoot().getChildren().size()> 0);
	}
}
