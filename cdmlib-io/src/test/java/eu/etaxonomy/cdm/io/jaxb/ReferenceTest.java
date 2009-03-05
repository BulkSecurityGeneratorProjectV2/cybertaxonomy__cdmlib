package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Journal;

public class ReferenceTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/ReferenceTest.xml";
	    
	    @Test
	    public void testUnmarshalReference() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Article article = (Article)dataSet.getReferences().get(0);	
			assertNotNull("Article must not be null",article);
			
			Journal journal = (Journal)dataSet.getReferences().get(1);
			assertNotNull("Journal must not be null", journal);
			assertEquals("Journal must equal Article.inJournal",journal,article.getInJournal());
	    }
}
