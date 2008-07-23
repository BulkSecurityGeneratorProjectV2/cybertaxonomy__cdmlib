/* just for testing */


package eu.etaxonomy.cdm.test.function;

import java.util.Locale;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.aspectj.PropertyChangeTest;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class TestModel {
	private static final UUID TEST_TAXON_UUID = UUID.fromString("b3084573-343d-4279-ba92-4ab01bb47db5");
	static Logger logger = Logger.getLogger(TestModel.class);
	
	
	public void testSomething(){
		
		logger.info("Create name objects...");
		logger.info(NomenclaturalStatusType.NUDUM().getRepresentation(Language.LATIN()).getAbbreviatedLabel());
		NonViralName tn = NonViralName.NewInstance(Rank.SPECIES());
		BotanicalName tn3 = BotanicalName.NewInstance(Rank.SUBSPECIES());
		ZoologicalName parentName = ZoologicalName.NewInstance(Rank.FAMILY());
		
		logger.info("Create reference objects...");
		ReferenceBase sec = Journal.NewInstance();
		sec.setTitleCache("TestJournal");
		
		logger.info("Create taxon objects...");
		Taxon childTaxon = Taxon.NewInstance(tn, sec);
		Synonym syn = Synonym.NewInstance(tn3, sec);
		childTaxon.addSynonym(syn, SynonymRelationshipType.SYNONYM_OF());
		Taxon parentTaxon = Taxon.NewInstance(parentName, sec);
		parentTaxon.setUuid(TEST_TAXON_UUID);
		parentTaxon.addTaxonomicChild(childTaxon, sec, null);
		
		// setup listeners
		PropertyChangeTest listener = new PropertyChangeTest();
		tn.addPropertyChangeListener(listener);
		tn3.addPropertyChangeListener(listener);

		// test listeners
		tn.setGenusOrUninomial("tn1-Genus1");
		tn3.setGenusOrUninomial("tn3-genus");
		tn3.getGenusOrUninomial();
		
		logger.info("Create new Author agent...");
		Person team= Person.NewInstance();
		team.addPropertyChangeListener(listener);
		team.setTitleCache("AuthorAgent1");
		tn.setCombinationAuthorTeam(team);
	}
	
	public void testParentRelation(){
		TaxonNameBase taxonName = BotanicalName.NewInstance(Rank.SPECIES());
		ReferenceBase ref = Journal.NewInstance();
		Taxon parent = Taxon.NewInstance(taxonName, ref);
		Taxon child = Taxon.NewInstance(taxonName, null);
		parent.addTaxonomicChild(child, null, null);
		if (child.getTaxonomicParent() != parent){
			logger.warn("Error");
		}
	}
	
	public void testDescription(){
		ReferenceBase ref = Journal.NewInstance();
		Taxon taxon = Taxon.NewInstance(null, ref);
		TaxonDescription desc = TaxonDescription.NewInstance();
		taxon.addDescription(desc);
		taxon.removeDescription(desc);
	}

	public void testTDWG(){
		NamedArea tdwgArea = TdwgArea.getAreaByTdwgLabel("GER");
		System.out.println(tdwgArea.getLabel());
	}
	
	private void test(){
		System.out.println("Start ...");
		TestModel sc = new TestModel();
		//sc.testSomething();
		//sc.testParentRelation();
		//sc.testDescription();
		sc.testTDWG();
		System.out.println("\nEnd ...");
	}
	
	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestModel sc = new TestModel();
		
		 DateTime dt = new DateTime();
		  String monthName = dt.monthOfYear().getAsText();
		  String frenchShortName = dt.monthOfYear().getAsShortText(Locale.FRENCH);
		  boolean isLeapYear = dt.year().isLeap();
		  DateTime rounded = dt.monthOfYear().roundHalfFloorCopy();
		  
			System.out.println(rounded + "\nEnd ...");
		//sc.test();
	}

}
