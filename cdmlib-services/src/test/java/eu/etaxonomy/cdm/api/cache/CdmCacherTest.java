package eu.etaxonomy.cdm.api.cache;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

import eu.etaxonomy.cdm.api.cache.CdmCacher;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;

public class CdmCacherTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmCacherTest.class);

	@SpringBeanByType
	private CdmCacher cdmCacher;
	
    @SpringBeanByType
    private IReferenceService referenceService;
    
    @SpringBeanByType
    private ITaxonService taxonService;
	
	@Test
	public void testLanguageCache() {
		Language defaultLanguage = Language.getDefaultLanguage();	
		
		Language defaultLanguageInCache = (Language)cdmCacher.getFromCache(defaultLanguage.getUuid());
		Assert.assertEquals("Loaded Language Term should match Language Term in Cache",defaultLanguage,defaultLanguageInCache);		
		
		Language language = Language.getLanguageFromUuid(Language.uuidFrench);
		Language languageInCache = (Language)cdmCacher.getFromCache(language.getUuid());
		Assert.assertEquals("Loaded Language Term should match Language Term in Cache",language,languageInCache);
		
		// Following test is just to make sure no exception is raised when saving a taxon corresponding
		// to a taxon name with no name cache to begin with 
		Reference sec = ReferenceFactory.newDatabase();
        referenceService.save(sec);
		Taxon taxon = Taxon.NewInstance(NonViralName.NewInstance(Rank.SERIES()), sec);
        taxon.setTitleCache("Tax" + "CdmCacher", true);
        taxonService.save(taxon);
        NonViralName nvn = (NonViralName)taxon.getName();
        String nameCache = nvn.getNameCache();
        logger.warn("name cache : " + nameCache);
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
        
    }

}
