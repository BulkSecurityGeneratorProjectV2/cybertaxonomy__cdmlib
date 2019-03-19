/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.config;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("configFileUtil-app-context.xml")
public class ConfigFileUtilTestWithContext2 {


	@Test
    public void testGetHomeDir() {
        String userHome = System.getProperty("user.home");
        Assert.assertEquals(userHome + File.separator + ".cdmLibrary", ConfigFileUtil.getCdmHomeDir().getAbsolutePath());
    }


}