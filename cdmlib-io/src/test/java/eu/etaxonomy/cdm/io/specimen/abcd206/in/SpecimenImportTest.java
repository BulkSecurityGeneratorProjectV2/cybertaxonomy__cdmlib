/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @created 29.01.2009
 */
public class SpecimenImportTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByName
	private CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	private INameService nameService;

	@SpringBeanByType
	private IOccurrenceService occurrenceService;

	@SpringBeanByType
	private ITermService termService;

	@SpringBeanByType
	private ICommonService commonService;

	@SpringBeanByType
	private ITaxonNodeService taxonNodeService;

	@SpringBeanByType
	private IReferenceService referenceService;

	private IImportConfigurator configurator;

	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/specimen/abcd206/in/Campanula_ABCD_import_3_taxa_11_units.xml";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		try {
			configurator = Abcd206ImportConfigurator.NewInstance(url.toURI(), null,false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			Assert.fail();
		}
		assertNotNull("Configurator could not be created", configurator);
	}

	@Test
	public void testInit() {
		assertNotNull("import instance should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
		assertNotNull("occurence service should not be null", occurrenceService);
		assertNotNull("term service should not be null", termService);
		assertNotNull("common service should not be null", commonService);
	}

	@Test
	@DataSet( value="../../../BlankDataSet.xml", loadStrategy=CleanSweepInsertLoadStrategy.class)
	@Ignore //TODO does currently not run in suite as we get ID problems when saving terms (NamedArea)
	public void testDoInvoke() {
		boolean result = defaultImport.invoke(configurator);
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of TaxonNames is incorrect", 4, nameService.count(TaxonNameBase.class));
		/*
		 * 5 taxon nodes:
		 *
         * Classification
         * - Campanula
         *   - Campanula patula
         *   - Campanula tridentata
         *   - Campanula lactiflora
         */
        assertEquals("Number of TaxonNodes is incorrect", 5, taxonNodeService.count(TaxonNode.class));
		assertEquals("Number of derived units is incorrect", 11, occurrenceService.count(DerivedUnit.class));
		assertEquals("Number of field units is incorrect", 11, occurrenceService.count(FieldUnit.class));
		assertEquals("Number of field units is incorrect", 1, referenceService.count(Reference.class));
	}
}
