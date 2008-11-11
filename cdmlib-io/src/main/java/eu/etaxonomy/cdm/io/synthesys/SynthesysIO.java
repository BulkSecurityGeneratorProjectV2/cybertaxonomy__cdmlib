package eu.etaxonomy.cdm.io.synthesys;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.transaction.TransactionStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DataSourceNotFoundException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.init.TermNotFoundException;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

public class SynthesysIO  extends SpecimenIoBase  implements ICdmIO {


	private static final Logger logger = Logger.getLogger(SynthesysIO.class);

	protected String fullScientificNameString;
	protected String nomenclatureCode;
	protected String institutionCode;
	protected String collectionCode;
	protected String unitID;
	protected String recordBasis;
	protected String accessionNumber;
	protected String collectorsNumber;
	protected String fieldNumber;
	protected Double longitude;
	protected Double latitude;
	protected String locality;
	protected String languageIso = null;
	protected String country;
	protected String isocountry;
	protected int depth;
	protected int altitude;
	protected ArrayList<String> gatheringAgentList;
	protected ArrayList<String> identificationList;
	protected ArrayList<String> namedAreaList;

	protected HSSFWorkbook hssfworkbook = null;


	public SynthesysIO() {
		super();
	}


	/*
	 * Store the Excel's data into variables
	 * @param fileName: the location of the Excel file
	 * @return the list of units data
	 */
	private static ArrayList<Hashtable<String, String>> parseXLS(String fileName) {
		ArrayList<Hashtable<String, String>> units = new ArrayList<Hashtable<String,String>>();

		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fileName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(0);
			HSSFRow row;
			HSSFCell cell;

			int rows; // No of rows
			rows = sheet.getPhysicalNumberOfRows();

			int cols = 0; // No of columns
			int tmp = 0;

			// This trick ensures that we get the data properly even if it doesn't start from first few rows
			for(int i = 0; i < 10 || i < rows; i++) {
				row = sheet.getRow(i);
				if(row != null) {
					tmp = sheet.getRow(i).getPhysicalNumberOfCells();
					if(tmp > cols) cols = tmp;
				}
			}
			Hashtable<String, String> headers = null;
			ArrayList<String> columns = new ArrayList<String>();
			row = sheet.getRow(0);
			for (int c =0; c<cols; c++){
				cell = row.getCell(c);
				columns.add(cell.toString());
			}
			for(int r = 1; r < rows; r++) {
				row = sheet.getRow(r);
				headers = new Hashtable<String, String>();
				if(row != null) {
					for(int c = 0; c < cols; c++) {
						cell = row.getCell((short)c);
						if(cell != null) {
							headers.put(columns.get(c),cell.toString());
						}
					}
				}
				units.add(headers);
			}

		} catch(Exception ioe) {
			ioe.printStackTrace();
		}
		return units;
	}

	/*
	 * Store the unit's properties into variables
	 * @param unit: the hashmap containing the splitted Excel line (Key=column name, value=value)
	 */
	private void setUnitPropertiesExcel(Hashtable<String,String> unit){
		String author = unit.get("author");
		author=author.replaceAll("None","");
		String taxonName = unit.get("taxonName");
		taxonName = taxonName.replaceAll("None", "");

		try {
			this.institutionCode = unit.get("institution").replaceAll("None", null);
		} catch (Exception e) {
		}

		try {this.collectionCode = unit.get("collection").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.unitID = unit.get("unitID").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.recordBasis = unit.get("recordBasis").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.accessionNumber = null;
		} catch (Exception e) {
		}
		try {this.locality = unit.get("locality").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.longitude = Double.valueOf(unit.get("longitude"));
		} catch (Exception e) {
		}
		try {this.latitude = Double.valueOf(unit.get("latitude"));
		} catch (Exception e) {
		}
		try {this.country = unit.get("country").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.isocountry = unit.get("isoCountry").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.fieldNumber = unit.get("field number").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {this.collectorsNumber = unit.get("collector number").replaceAll("None", null);
		} catch (Exception e) {
		}
		try {String coll =unit.get("collector");
		coll=coll.replaceAll("None", null);
		this.gatheringAgentList.add(coll);
		} catch (Exception e) {
		}
		try {this.identificationList.add(taxonName+" "+author);
		} catch (Exception e) {System.out.println(e);
		}
	}

	private Institution getInstitution(String institutionCode, CdmApplicationController app){
		Institution institution;
		List<Institution> institutions;
		try{
			System.out.println(this.institutionCode);
			institutions= app.getAgentService().searchInstitutionByCode(this.institutionCode);
		}catch(Exception e){
			System.out.println("BLI "+e);
			institutions=new ArrayList<Institution>();
		}
		if (institutions.size() ==0){
			System.out.println("Institution (agent) unknown");
			//create institution
			institution = Institution.NewInstance();
			institution.setCode(this.institutionCode);				
		}
		else{
			System.out.println("Institution (agent) already in the db");
			institution = institutions.get(0);
		}
		return institution;
	}

	/*
	 * Look if the Collection does already exists
	 * @param collectionCode: a string
	 * @param institution: the current Institution
	 * @param app
	 * @return the Collection (existing or new)
	 */
	private Collection getCollection(String collectionCode, Institution institution, CdmApplicationController app){
		Collection collection = Collection.NewInstance();
		List<Collection> collections;
		try{
			collections = app.getOccurrenceService().searchCollectionByCode(this.collectionCode);
		}catch(Exception e){
			System.out.println("BLA"+e);
			collections=new ArrayList<Collection>();
		}
		if (collections.size() ==0){
			System.out.println("Collection not found "+this.collectionCode);
			//create new collection
			collection.setCode(this.collectionCode);
			collection.setCodeStandard("GBIF");
			collection.setInstitute(institution);
		}
		else{
			boolean collectionFound=false;
			for (int i=0; i<collections.size(); i++){
				collection = collections.get(i);
				try {
					if (collection.getInstitute().getCode().equalsIgnoreCase(institution.getCode())){ 
						//found a collection with the same code and the same institution
						collectionFound=true;
					}
				} catch (NullPointerException e) {}
			}
			if (!collectionFound){ 
				collection.setCode(this.collectionCode);
				collection.setCodeStandard("GBIF");
				collection.setInstitute(institution);
			}

		}
		return collection;
	}

	/*
	 * 
	 * @param app
	 * @param derivedThing
	 * @param sec
	 */
	private void setTaxonNameBase(CdmApplicationController app, DerivedUnitBase derivedThing, ReferenceBase sec){
		TaxonNameBase taxonName = null;
		String fullScientificNameString;
		Taxon taxon = null;
		DeterminationEvent determinationEvent = null;
		List<TaxonNameBase> names = null;
		NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
		String scientificName="";
		boolean preferredFlag=false;

		for (int i = 0; i < this.identificationList.size(); i++) {
			fullScientificNameString = this.identificationList.get(i);
			fullScientificNameString = fullScientificNameString.replaceAll(" et ", " & ");
			if (fullScientificNameString.indexOf("_preferred_") != -1){
				scientificName = fullScientificNameString.split("_preferred_")[0];
				String pTmp = fullScientificNameString.split("_preferred_")[1].split("_code_")[0];
				if (pTmp == "1" || pTmp.toLowerCase().indexOf("true") != -1)
					preferredFlag=true;
				else
					preferredFlag=false;
			}
			else scientificName = fullScientificNameString;
			if (fullScientificNameString.indexOf("_code_") != -1){
				this.nomenclatureCode = fullScientificNameString.split("_code_")[1];
			}

			System.out.println("nomenclature: "+this.nomenclatureCode);
			if (this.nomenclatureCode == "Zoological"){
				taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICZN(),null);
				if (taxonName.hasProblem())
					System.out.println("pb ICZN");}
			if (this.nomenclatureCode == "Botanical"){
				taxonName  = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICBN(),null);
				if (taxonName.hasProblem())
					System.out.println("pb ICBN");}
			if (this.nomenclatureCode == "Bacterial"){
				taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICNB(), null);
				if (taxonName.hasProblem())
					System.out.println("pb ICNB");
			}
			if (this.nomenclatureCode == "Cultivar"){
				taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICNCP(), null);
				if (taxonName.hasProblem())
					System.out.println("pb ICNCP");
			}
			if (this.nomenclatureCode == "Viral"){
				taxonName = nvnpi.parseFullName(this.fullScientificNameString,NomenclaturalCode.ICVCN(), null);
				if (taxonName.hasProblem())
					System.out.println("pb ICVCN");
			}
			try{taxonName.hasProblem();}
			catch (Exception e) {
				taxonName = nvnpi.parseFullName(scientificName);
			}
			if (taxonName.hasProblem())
				taxonName = nvnpi.parseFullName(scientificName);
			if (true){
				names = app.getNameService().getNamesByName(scientificName);
				if (names.size() == 0){
					System.out.println("Name not found: " + scientificName);
				}else{
					if (names.size() > 1){
						System.out.println("More then 1 name found: " + scientificName);
					}
					System.out.println("Name found");
					taxonName = names.get(0);
				}
			}

			app.getNameService().saveTaxonName(taxonName);
			taxon = Taxon.NewInstance(taxonName, sec); //TODO use real reference for sec

			determinationEvent = DeterminationEvent.NewInstance();
			determinationEvent.setTaxon(taxon);
			determinationEvent.setPreferredFlag(preferredFlag);
			derivedThing.addDetermination(determinationEvent);
		}

	}

	/*
	 * Store the unit with its Gathering informations in the CDM
	 */
	public boolean start(IImportConfigurator config){
		boolean result = true;
		boolean withCdm = true;
		CdmApplicationController app = null;
		TransactionStatus tx = null;

		try {
			app = CdmApplicationController.NewInstance(config.getDestination(), config.getDbSchemaValidation());
		} catch (DataSourceNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("DataSourceNotFoundException "+e1);
		} catch (TermNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("TermNotFoundException " +e1);
		}

		tx = app.startTransaction();
		try {
			ReferenceBase sec = Database.NewInstance();
			sec.setTitleCache("XML DATA");

			/**
			 * SPECIMEN OR OBSERVATION OR LIVING
			 */
			DerivedUnitBase derivedThing = null;
			//create specimen
			if (this.recordBasis != null){
				if (this.recordBasis.toLowerCase().startsWith("s")) {//specimen
					derivedThing = Specimen.NewInstance();				
				}
				else if (this.recordBasis.toLowerCase().startsWith("o")) {//observation
					derivedThing = Observation.NewInstance();				
				}
				else if (this.recordBasis.toLowerCase().startsWith("l")) {//living -> fossil, herbarium sheet....???
					derivedThing = LivingBeing.NewInstance();
				}
			}
			if (derivedThing == null) 
				derivedThing = Observation.NewInstance();

			this.setTaxonNameBase(app, derivedThing, sec);


			//set catalogue number (unitID)
			derivedThing.setCatalogNumber(this.unitID);
			derivedThing.setAccessionNumber(this.accessionNumber);
			derivedThing.setCollectorsNumber(this.collectorsNumber);


			/**
			 * INSTITUTION & COLLECTION
			 */
			//manage institution
			Institution institution = this.getInstitution(this.institutionCode,app);
			//manage collection
			Collection collection = this.getCollection(this.collectionCode, institution, app); 
			//link specimen & collection
			derivedThing.setCollection(collection);

			/**
			 * GATHERING EVENT
			 */

			UnitsGatheringEvent unitsGatheringEvent = new UnitsGatheringEvent(app, this.locality, this.languageIso, this.longitude, 
					this.latitude, this.gatheringAgentList);
			UnitsGatheringArea unitsGatheringArea = new UnitsGatheringArea(this.isocountry, this.country,app);
			NamedArea areaCountry = unitsGatheringArea.getArea();
			unitsGatheringEvent.addArea(areaCountry);
			unitsGatheringArea = new UnitsGatheringArea(this.namedAreaList);
			ArrayList<NamedArea> nas = unitsGatheringArea.getAreas();
			for (int i=0; i<nas.size();i++)
				unitsGatheringEvent.addArea(nas.get(i));


			//create field/observation
			FieldObservation fieldObservation = FieldObservation.NewInstance();
			//add fieldNumber
			fieldObservation.setFieldNumber(this.fieldNumber);
			//join gatheringEvent to fieldObservation
			fieldObservation.setGatheringEvent(unitsGatheringEvent.getGatheringEvent());

//			//link fieldObservation and specimen
			DerivationEvent derivationEvent = DerivationEvent.NewInstance();
			derivationEvent.addOriginal(fieldObservation);
			derivedThing.addDerivationEvent(derivationEvent);

			/**
			 * SAVE AND STORE DATA
			 */			

			app.getTermService().saveTerm(areaCountry);//save it sooner
			for (int i=0; i<nas.size();i++)
				app.getTermService().saveTerm(nas.get(i));//save it sooner (foreach area)
			app.getTermService().saveLanguageData(unitsGatheringEvent.getLocality());//save it sooner
			app.getOccurrenceService().saveSpecimenOrObservationBase(derivedThing);

			logger.info("saved new specimen ...");


		} catch (Exception e) {
			logger.warn("Error when reading record!!");
			e.printStackTrace();
			result = false;
		}
		app.commitTransaction(tx);
		System.out.println("commit done");
		app.close();
		return result;
	}


	public boolean invoke(IImportConfigurator config){
		System.out.println("INVOKE Specimen Import From Excel File (Synthesys Cache format");
		SynthesysIO test = new SynthesysIO();
		String sourceName = config.getSourceNameString();

		ArrayList<Hashtable<String,String>> unitsList = parseXLS(sourceName);
		if (unitsList != null){
			Hashtable<String,String> unit=null;
			for (int i=0; i<unitsList.size();i++){
				unit = unitsList.get(i);
				test.setUnitPropertiesExcel(unit);//and then invoke
				test.start(config);
				config.setDbSchemaValidation(DbSchemaValidation.UPDATE);
			}
		}

		return false;

	}


	public boolean invoke(IImportConfigurator config, Map stores) {
		invoke(config);
		return false;
	}




}
