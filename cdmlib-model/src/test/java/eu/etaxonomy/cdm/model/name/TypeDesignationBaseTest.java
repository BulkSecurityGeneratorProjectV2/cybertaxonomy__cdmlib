/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 18.04.2018
 */
public class TypeDesignationBaseTest extends EntityTestBase {

    @SuppressWarnings("cast")
    @Test
    public void testClone(){
        DerivedUnit specimen = DerivedUnit.NewInstance(SpecimenOrObservationType.PreservedSpecimen);
        TaxonName name1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        SpecimenTypeDesignationStatus status = SpecimenTypeDesignationStatus.HOLOTYPE();
        Reference citation = ReferenceFactory.newBook();
        String microCitation = "p. 123";
        String originalInfo = "orig. info";
        boolean isNotDesignated = true;
        SpecimenTypeDesignation originalDesignation = name1.addSpecimenTypeDesignation(specimen, status,
                citation, microCitation, originalInfo, isNotDesignated, false);
        Registration registration1 = Registration.NewInstance();
        registration1.addTypeDesignation(originalDesignation);

        //pre assert
        Assert.assertTrue(originalDesignation instanceof TypeDesignationBase);
        Assert.assertEquals(1, name1.getTypeDesignations().size());
        Assert.assertTrue(name1.getTypeDesignations().contains(originalDesignation));
        Assert.assertTrue(originalDesignation.getRegistrations().contains(registration1));
        Assert.assertNotNull(status);

        //clone
        SpecimenTypeDesignation clonedDesignation = originalDesignation.clone();

        //post assert
        Assert.assertEquals(2, name1.getTypeDesignations().size());
        Assert.assertTrue(name1.getTypeDesignations().contains(clonedDesignation));
        Assert.assertTrue(clonedDesignation.getTypifiedNames().contains(name1));
        Assert.assertEquals(2, registration1.getTypeDesignations().size());
        Assert.assertTrue(registration1.getTypeDesignations().contains(clonedDesignation));
        Assert.assertTrue(clonedDesignation.getRegistrations().contains(registration1));

        Assert.assertSame(citation, clonedDesignation.getCitation());
        Assert.assertSame(status, clonedDesignation.getTypeStatus());
    }
}