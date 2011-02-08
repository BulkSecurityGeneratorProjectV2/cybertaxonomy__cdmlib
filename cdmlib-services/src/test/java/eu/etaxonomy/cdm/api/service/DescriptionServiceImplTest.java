/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 09.02.2009
 * @version 1.0
 */
public class DescriptionServiceImplTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DescriptionServiceImplTest.class);
	
	@SpringBeanByType
	private IDescriptionService service;
	
	

	@Test
	public void testGetDefaultFeatureVocabulary() {
		
		service.getDefaultFeatureVocabulary();
	}
	
	@Test
	@DataSet("CommonServiceImplTest.xml")
	public void testChangeDescriptionElement(){
		DescriptionBase descBase = service.find(UUID.fromString("eb17b80a-9be6-4642-a6a8-b19a318925e6"));
		Set<DescriptionElementBase> elements = descBase.getElements();
		Iterator iterator = elements.iterator();
		while (iterator.hasNext()){
			DescriptionElementBase base = (DescriptionElementBase) iterator.next();
			if (base instanceof TextData){
				TextData textdata = (TextData) base;
				Set <Entry<Language,LanguageString>> entries = textdata.getMultilanguageText().entrySet();
				Iterator entryIterator = entries.iterator();
				while (entryIterator.hasNext()){
					Entry <Language, LanguageString> entry = (Entry<Language, LanguageString>) entryIterator.next();
					LanguageString langString = entry.getValue();
					System.out.println(langString);
					langString.setText("blablubber");
				}
			}
			
		}
		service.saveOrUpdate(descBase);
		Pager<DescriptionElementBase> allElements = service.getDescriptionElements(null, null, null, null, null, null);
		Assert.assertEquals(1, allElements.getCount().intValue());
		DescriptionElementBase test = allElements.getRecords().get(0);
		if (test instanceof TextData){
		
			Set <Entry<Language,LanguageString>> entries = ((TextData) test).getMultilanguageText().entrySet();
			Iterator entryIterator = entries.iterator();
			while (entryIterator.hasNext()){
				Entry <Language, LanguageString> entry = (Entry<Language, LanguageString>) entryIterator.next();
				LanguageString langString = entry.getValue();
				System.out.println(langString);
			}
		}
	}
	
	@Test
	public void testMoveDescriptionElementsToTaxon(){
		
		TaxonDescription sourceDescription = TaxonDescription.NewInstance();
		
		TextData element = TextData.NewInstance();
		sourceDescription.addElement(element);
		TextData element2 = TextData.NewInstance();
		sourceDescription.addElement(element2);
		Collection<DescriptionElementBase> sourceCollection = new HashSet<DescriptionElementBase>();
		sourceCollection.addAll(sourceDescription.getElements());
		TextData element3 = TextData.NewInstance();
		sourceDescription.addElement(element3);

		Assert.assertEquals(3, sourceDescription.getElements().size());
		
		TaxonDescription targetDescription = TaxonDescription.NewInstance();
		this.service.save(sourceDescription);
		this.service.save(targetDescription);
		
		service.moveDescriptionElementsToDescription(sourceCollection, targetDescription, false);
		
		Assert.assertEquals("Source descirption should have 1 element left", 1, sourceDescription.getElements().size());
		Assert.assertEquals("Target descriptoin should have 2 new elements", 2, targetDescription.getElements().size());
		Assert.assertTrue("The moved element should be in the new description", targetDescription.getElements().contains(element));
		Assert.assertTrue("The moved element2 should be in the new description", targetDescription.getElements().contains(element2));
		Assert.assertFalse("Element3 should not be in the new description", targetDescription.getElements().contains(element3));
		Assert.assertTrue("Element3 should remain in the old description", targetDescription.getElements().contains(element));
		this.service.save(sourceDescription);
		this.service.save(targetDescription);
		
		try {
			service.moveDescriptionElementsToDescription(targetDescription.getElements(), sourceDescription, false);
		} catch (Exception e) {
			//asserting that no ConcurrentModificationException is thrown when the elements collection is passed as a parameter
			Assert.fail();
		}
		
		Assert.assertEquals("Source descirption should have 3 elements again", 3, sourceDescription.getElements().size());
		Assert.assertEquals("Destination descirption should have no elements again", 0, targetDescription.getElements().size());
		this.service.save(sourceDescription);
		this.service.save(targetDescription);
		
		//test paste
		sourceCollection.add(sourceDescription.getElements().iterator().next());
		service.moveDescriptionElementsToDescription(sourceCollection, targetDescription, true);

		Assert.assertEquals("Source descirption should still have 3 elements", 3, sourceDescription.getElements().size());
		Assert.assertEquals("Destination descirption should have 1 element again", 1, targetDescription.getElements().size());
		for (DescriptionElementBase targetElement : targetDescription.getElements()){
			Assert.assertFalse("Target elements may not be in sourced description as they are only clones (but not same).", sourceDescription.getElements().contains(targetElement));
		}
		this.service.save(targetDescription);
		this.service.save(sourceDescription);
		

	}
}
