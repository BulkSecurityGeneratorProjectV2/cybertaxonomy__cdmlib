package eu.etaxonomy.cdm.io.abcd206;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.CdmApplicationAwareDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class SpecimenImportConfiguratorTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByName
	CdmApplicationAwareDefaultImport<?> defaultImport;

	@SpringBeanByType
	INameService nameService;

	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		String inputFile = "/eu/etaxonomy/cdm/io/abcd206/SpecimenImportConfiguratorTest-input.xml";
		URL url = this.getClass().getResource(inputFile);
		assertNotNull("URL for the test file '" + inputFile + "' does not exist", url);
		configurator = SpecimenImportConfigurator.NewInstance(url.toString(), null);
		assertNotNull("Configurator could not be created", configurator);
	}
	
	@Test
	public void testInit() {
		assertNotNull("cdmTcsXmlImport should not be null", defaultImport);
		assertNotNull("nameService should not be null", nameService);
	}
	
	@Test
	public void testDoInvoke() {
		boolean result = defaultImport.invoke(configurator);
		assertTrue("Return value for import.invoke should be true", result);
		assertEquals("Number of TaxonNames should be 5", 5, nameService.count());
	}

}
