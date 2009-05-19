// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.occurrence.Specimen;

/**
 * @author a.babadshanjan
 * @created 19.05.2009
 * @version 1.0
 */
public class SpecimenTypeDesignationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(SpecimenTypeDesignationTest.class);

	private SpecimenTypeDesignation term1 = null;
	private SpecimenTypeDesignation term2 = null;

	@BeforeClass
	public static void setUpBeforeClass() {
		DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
		vocabularyStore.initialize();
	}

	@Before
	public void setUp() {
		term1 = new SpecimenTypeDesignation();
		term2 = 
			new SpecimenTypeDesignation(Specimen.NewInstance(), SpecimenTypeDesignationStatus.ISOTYPE(), 
					null, null, null, false);
	}
	
	@Test
	public void testSpecimenTypeDesignation() {
		assertNotNull(term1);
		assertNotNull(term2);
	}
	
	@Test
	public void testGetTypeStatus() {
		term1.setTypeStatus(SpecimenTypeDesignationStatus.EPITYPE());
		assertEquals(term1.getTypeStatus(), SpecimenTypeDesignationStatus.EPITYPE());
		assertTrue(term1.getTypeStatus().isInstanceOf(SpecimenTypeDesignationStatus.class));
	}
}
