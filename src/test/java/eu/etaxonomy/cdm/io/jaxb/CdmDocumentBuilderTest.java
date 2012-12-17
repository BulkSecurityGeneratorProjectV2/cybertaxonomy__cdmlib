/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import java.io.InputStreamReader;
import java.net.URI;

import junit.framework.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;

public class CdmDocumentBuilderTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/CdmDocumentBuilderTest.xml";
	    
	    @Test
	    public void testXInclude() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Person person = (Person)dataSet.getAgents().get(0);
			Assert.assertNotNull(person);
	    }
}
