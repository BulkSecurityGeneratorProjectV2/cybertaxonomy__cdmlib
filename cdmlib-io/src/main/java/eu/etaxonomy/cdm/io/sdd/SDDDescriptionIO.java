/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.sdd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.IVersionableService;//rajout
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.mediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ICdmImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Address;//rajout
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author h.fradin
 * @created 24.10.2008
 * @version 1.0
 */
@Component("sddDescriptionIO")
public class SDDDescriptionIO extends CdmImportBase<SDDImportConfigurator, SDDImportState> implements ICdmImport<SDDImportConfigurator, SDDImportState> {
	private static final Logger logger = Logger.getLogger(SDDDescriptionIO.class);

	private static int modCount = 1000;

	private Map<String,Person> authors = new HashMap<String,Person>();
	private Map<String,String> citations = new HashMap<String,String>();
	private Map<String,String> defaultUnitPrefixes = new HashMap<String,String>();
	private Map<String,Person> editors = new HashMap<String,Person>();
	private Map<String,FeatureNode> featureNodes = new HashMap<String,FeatureNode>();
	private Map<String,Feature> features = new HashMap<String,Feature>();
	private Map<String,String> locations = new HashMap<String,String>();
	private Map<String,List<CdmBase>> mediaObject_ListCdmBase = new HashMap<String,List<CdmBase>>();
	private Map<String,String> mediaObject_Role = new HashMap<String,String>();
	private Map<String,ReferenceBase> publications = new HashMap<String,ReferenceBase>();
	private Map<String,StateData> stateDatas = new HashMap<String,StateData>();
	private Map<String,State> states = new HashMap<String,State>();
	private Map<String,TaxonDescription> taxonDescriptions = new HashMap<String,TaxonDescription>();
	private Map<String,NonViralName> taxonNameBases = new HashMap<String,NonViralName>();
	private Map<String,MeasurementUnit> units = new HashMap<String,MeasurementUnit>();
	private Map<String,TaxonNode> taxonNodes = new HashMap<String,TaxonNode>();
	private Map<String,NamedArea> namedAreas = new HashMap<String,NamedArea>();
	private Map<String,Specimen> specimens = new HashMap<String,Specimen>();
	private Map<String,Modifier> modifiers = new HashMap<String,Modifier>();
	
	private Set<MarkerType> markerTypes = new HashSet<MarkerType>();

	private Set<Feature> descriptiveConcepts = new HashSet<Feature>();
	private Set<TermVocabulary<Modifier>> termVocabularyStates = new HashSet<TermVocabulary<Modifier>>();
	private Set<AnnotationType> annotationTypes = new HashSet<AnnotationType>();
	private Set<Feature> featureSet = new HashSet<Feature>();
	ReferenceFactory refFactory = ReferenceFactory.newInstance();
	private ReferenceBase sec = refFactory.newDatabase();
	private ReferenceBase sourceReference = null;

	private Language datasetLanguage = null;

	private Namespace xmlNamespace = Namespace.getNamespace("xml","http://www.w3.org/XML/1998/namespace");

	private String generatorName = "";
	private String generatorVersion = "";

	private Set<StatisticalMeasure> statisticalMeasures = new HashSet<StatisticalMeasure>();
	private Set<VersionableEntity> featureData = new HashSet<VersionableEntity>();
	private Set<FeatureTree> featureTrees = new HashSet<FeatureTree>();
	private Set<TaxonomicTree> taxonomicTrees = new HashSet<TaxonomicTree>();

	private Rights copyright = null;

	private int taxonNamesCount = 0; //XIM ajout
	
	public SDDDescriptionIO(){
		super();
	}

	@Override
	public boolean doCheck(SDDImportState state){
		boolean result = true;
		logger.warn("No check implemented for SDD");
		return result;
	}

	//	@Override
	//	public boolean doInvoke(IImportConfigurator config, Map<String, MapWrapper<? extends CdmBase>> stores){
	@Override
	public boolean doInvoke(SDDImportState state){

		TransactionStatus ts = startTransaction();
		SDDImportConfigurator sddConfig = state.getConfig();

		logger.info("start Datasets ...");
		// <Datasets xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://rs.tdwg.org/UBIF/2006/" xsi:schemaLocation="http://rs.tdwg.org/UBIF/2006/ ../SDD.xsd">
		Element root = sddConfig.getSourceRoot();
		boolean success = true;
		Namespace sddNamespace = sddConfig.getSddNamespace();

		logger.info("start TechnicalMetadata ...");
		// <TechnicalMetadata created="2006-04-20T10:00:00">
		importTechnicalMetadata(root, sddNamespace, sddConfig);
		List<Element> elDatasets = root.getChildren("Dataset",sddNamespace);
		int i = 0;

		//for each Dataset
		logger.info("start Dataset ...");
		for (Element elDataset : elDatasets){
			importDataset(elDataset, sddNamespace, success, sddConfig);			
			if ((++i % modCount) == 0){ logger.info("Datasets handled: " + i);}
			logger.info(i + " Datasets handled");
		}
		commitTransaction(ts);
		return success;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(SDDImportState state){
		return false;
	}


	// associates the reference of a media object in SDD with a CdmBase Object
	protected void associateImageWithCdmBase(String refMO, CdmBase cb){
		if ((refMO != null) && (cb!=null)) {
			if (!refMO.equals("")) {
				if (!mediaObject_ListCdmBase.containsKey(refMO)) {
					List<CdmBase> lcb = new ArrayList<CdmBase>();
					lcb.add(cb);
					mediaObject_ListCdmBase.put(refMO,lcb);
				} else {
					List<CdmBase> lcb = mediaObject_ListCdmBase.get(refMO);
					lcb.add(cb);
					mediaObject_ListCdmBase.put(refMO,lcb);
				}
			}
		}
	}

	// imports information about the Dataset
	protected void importDatasetRepresentation(Element parent, Namespace sddNamespace){
		logger.info("start Representation ...");
		/* <Representation>
			<Label>The Genus Viola</Label>
			<Detail>This is an example for a very simple SDD file, representing a single description with categorical, quantitative, and text character. Compare also the "Fragment*" examples, which contain more complex examples in the form of document fragments. Intended for version="SDD 1.1".</Detail>
	       </Representation>
		 */
		Element elRepresentation = parent.getChild("Representation",sddNamespace);
		String label = (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace);
		String detail = (String)ImportHelper.getXmlInputValue(elRepresentation, "Detail",sddNamespace);

		sec.setTitleCache(label);

		if (detail != null) {
			Annotation annotation = Annotation.NewInstance(detail, datasetLanguage);
			annotation.setAnnotationType(AnnotationType.EDITORIAL());
			sec.addAnnotation(annotation);
		}

		List<Element> listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);

		for (Element elMediaObject : listMediaObjects) {
			String ref = null;
			String role = null;
			if (elMediaObject != null) {
				ref = elMediaObject.getAttributeValue("ref");
				role = elMediaObject.getAttributeValue("role");
			}
			if (ref != null) {
				if (!ref.equals("")) {
					this.associateImageWithCdmBase(ref,sourceReference);
					this.associateImageWithCdmBase(ref,sec);
					mediaObject_Role.put(ref,role);
				}
			}
		}
	}

	// imports the representation (label, detail, lang) of a particular SDD element
	protected void importRepresentation(Element parent, Namespace sddNamespace, VersionableEntity ve, String id, IImportConfigurator config){
		Element elRepresentation = parent.getChild("Representation",sddNamespace);
		// <Label xml:lang="la">Viola hederacea Labill.</Label>
		List<Element> listLabels = elRepresentation.getChildren("Label",sddNamespace);
		List<Element> listDetails = elRepresentation.getChildren("Detail",sddNamespace);
		Map<Language,List<String>> langLabDet = new HashMap<Language,List<String>>();

		for (Element elLabel : listLabels){
			String lang = elLabel.getAttributeValue("lang",xmlNamespace);
			Language language = null;
			if (lang != null) {
				if (!lang.equals("")) {
					language = getTermService().getLanguageByIso(lang.substring(0, 2));
				} else {
					language = datasetLanguage;
				}
			} else {
				language = datasetLanguage;
			}
			String label = elLabel.getText();
			List<String> labDet = new ArrayList<String>(3);
			labDet.add(label);
			langLabDet.put(language, labDet);
		}

		for (Element elDetail : listDetails){
			String lang = elDetail.getAttributeValue("lang",xmlNamespace);
			String role = elDetail.getAttributeValue("role");
			Language language = null;
			if (lang != null) {
				if (!lang.equals("")) {
					language = getTermService().getLanguageByIso(lang.substring(0, 2));
				} else {
					language = datasetLanguage;
				}
			} else {
				language = datasetLanguage;
			}
			String detail = elDetail.getText();
			List<String> labDet = langLabDet.get(language);
			labDet.add(detail);
			labDet.add(role);
			langLabDet.put(language, labDet);
		}

		if (ve instanceof TermBase) {
			TermBase tb = (TermBase) ve;

			for (Iterator<Language> l = langLabDet.keySet().iterator() ; l.hasNext() ;){
				Language lang = l.next();
				List<String> labDet = langLabDet.get(lang);
				if (labDet.size()>0){
					if (labDet.size()>1) {
						tb.addRepresentation(Representation.NewInstance(labDet.get(1), labDet.get(0), labDet.get(0), lang));
					} else {
						tb.addRepresentation(Representation.NewInstance(labDet.get(0), labDet.get(0), labDet.get(0), lang));
					}
				}
				ve = tb;
			}

		} else if (ve instanceof Media) {
			Media m = (Media) ve;

			for (Iterator<Language> l = langLabDet.keySet().iterator() ; l.hasNext() ;){
				Language lang = l.next();
				List<String> labDet = langLabDet.get(lang);
				if (labDet.get(0) != null){
					m.addTitle(LanguageString.NewInstance(labDet.get(0), lang));
				}
				if (labDet.size()>1) {
					m.addDescription(labDet.get(1), lang);
				}
				ve = m;
			}

		} 
		if (ve instanceof IdentifiableEntity<?>) {
			IdentifiableEntity<?> ie = (IdentifiableEntity<?>) ve;
			List<String> labDet = null;

			if (ve instanceof TaxonNameBase) {
				if (langLabDet.keySet().contains(getTermService().getLanguageByIso("la"))) {
					labDet = langLabDet.get(getTermService().getLanguageByIso("la"));
				} else if (langLabDet.keySet().contains(datasetLanguage)) {
					labDet = langLabDet.get(datasetLanguage);
					logger.info("TaxonName " + (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace) + " is not specified as a latin name.");
				} else {
					labDet = langLabDet.get(langLabDet.keySet().iterator().next());
					logger.info("TaxonName " + (String)ImportHelper.getXmlInputValue(elRepresentation, "Label",sddNamespace) + " is not specified as a latin name.");
				}
			} else {
				labDet = langLabDet.get(langLabDet.keySet().iterator().next());
			}

			ie.setTitleCache(labDet.get(0));

			if (labDet.size()>1) {
				Annotation annotation = null;
				if (labDet.get(1) != null) {
					if (labDet.get(2) != null) {
						annotation = Annotation.NewInstance(labDet.get(2) + " - " + labDet.get(1), datasetLanguage);
					} else {
						annotation = Annotation.NewInstance(labDet.get(1), datasetLanguage);
					}
				}
				ie.addAnnotation(annotation);
			}

			ve = ie;

		}
		
		if (ve instanceof IdentifiableMediaEntity<?>){
			IdentifiableMediaEntity<?> ime = (IdentifiableMediaEntity<?>) ve;
			Element elLinks = parent.getChild("Links",sddNamespace);

			if (elLinks != null) {

				//  <Link rel="Alternate" href="http://www.diversitycampus.net/people/hagedorn"/>
				List<Element> listLinks = elLinks.getChildren("Link", sddNamespace);
				Media link = Media.NewInstance();
				MediaRepresentation mr = MediaRepresentation.NewInstance();
				int k = 0;
				//for each Link
				for (Element elLink : listLinks){

					try {

						String rel = elLink.getAttributeValue("rel");
						String href = elLink.getAttributeValue("href");

						mr.addRepresentationPart(MediaRepresentationPart.NewInstance(href, null));
						link.addRepresentation(mr);
						ime.addMedia(link);

					} catch (Exception e) {
						//FIXME
						logger.warn("Import of Link " + k + " failed.");
					}

					if ((++k % modCount) == 0){ logger.info("Links handled: " + k);}

				}
			}
		}

		List <Element> listMediaObjects = elRepresentation.getChildren("MediaObject",sddNamespace);
		for (Element elMediaObject : listMediaObjects) {
			String ref = null;
			String role = null;
			if (elMediaObject != null) {
				ref = elMediaObject.getAttributeValue("ref");
				role = elMediaObject.getAttributeValue("role");
			}
			if (ref != null) {
				if (!ref.equals("")) {
					if (ref != null) {
						if (ve instanceof TaxonDescription) {
							TaxonDescription td = (TaxonDescription) ve;
							//TODO: ensure that all images are imported
							if (td.getDescriptionSources().toArray().length > 0) {
								this.associateImageWithCdmBase(ref,(ReferenceBase) td.getDescriptionSources().toArray()[0]);
							} else {
								ReferenceBase descriptionSource = refFactory.newGeneric();
								td.addDescriptionSource(descriptionSource);
								this.associateImageWithCdmBase(ref,descriptionSource);
							}
						} else {
							this.associateImageWithCdmBase(ref,ve);
						}
					}

				}
			}
		}

	}
	

	// imports the representation (label, detail, lang) of a particular SDD element
	protected void importTechnicalMetadata(Element root, Namespace sddNamespace, SDDImportConfigurator sddConfig){
		Element elTechnicalMetadata = root.getChild("TechnicalMetadata", sddNamespace);
		String nameCreated = elTechnicalMetadata.getAttributeValue("created");
		sourceReference = sddConfig.getSourceReference();

		if (nameCreated != null) {
			if (!nameCreated.equals("")) {
				int year = Integer.parseInt(nameCreated.substring(0,4));
				int monthOfYear = Integer.parseInt(nameCreated.substring(5,7));
				int dayOfMonth = Integer.parseInt(nameCreated.substring(8,10));
				int hourOfDay = Integer.parseInt(nameCreated.substring(11,13));
				int minuteOfHour = Integer.parseInt(nameCreated.substring(14,16));
				int secondOfMinute = Integer.parseInt(nameCreated.substring(17,19));
				DateTime created = new DateTime(year,monthOfYear,dayOfMonth,hourOfDay,minuteOfHour,secondOfMinute,0);
				sourceReference.setCreated(created);
				sec.setCreated(created);
			}
		}

		// <Generator name="n/a, handcrafted instance document" version="n/a"/>
		Element elGenerator = elTechnicalMetadata.getChild("Generator", sddNamespace);
		generatorName = elGenerator.getAttributeValue("name");
		generatorVersion = elGenerator.getAttributeValue("version");

		sec.addAnnotation(Annotation.NewDefaultLanguageInstance(generatorName + " - " + generatorVersion));
		sourceReference.addAnnotation(Annotation.NewDefaultLanguageInstance(generatorName + " - " + generatorVersion));

	}

	// imports the complete dataset information
	protected void importDataset(Element elDataset, Namespace sddNamespace, boolean success, SDDImportConfigurator sddConfig){			// <Dataset xml:lang="en-us">

		importDatasetLanguage(elDataset,sddConfig);
		importDatasetRepresentation(elDataset, sddNamespace);
		importRevisionData(elDataset, sddNamespace);
		importIPRStatements(elDataset, sddNamespace, sddConfig);
		importTaxonNames(elDataset, sddNamespace, sddConfig);
		importDescriptiveConcepts(elDataset, sddNamespace, sddConfig);
		importCharacters(elDataset, sddNamespace, sddConfig, success);
		importCharacterTrees(elDataset, sddNamespace, sddConfig, success);
		importCodedDescriptions(elDataset, sddNamespace, sddConfig, success);
		importAgents(elDataset, sddNamespace, sddConfig, success);
		importPublications(elDataset, sddNamespace, sddConfig, success);
		importMediaObjects(elDataset, sddNamespace, sddConfig, success);
		importTaxonHierarchies(elDataset, sddNamespace, sddConfig, success);
		importGeographicAreas(elDataset, sddNamespace, sddConfig);
		importSpecimens(elDataset,sddNamespace, sddConfig);
			
		MarkerType editorMarkerType = MarkerType.NewInstance("Editor", "editor", "edt") ;
		MarkerType geographicAreaMarkerType = MarkerType.NewInstance("", "SDDGeographicArea", "ga");
		MarkerType descriptiveConceptMarkerType = MarkerType.NewInstance("Descriptive Concept", "DescriptiveConcept", "DC");
		markerTypes.add(editorMarkerType);
		markerTypes.add(geographicAreaMarkerType);
		markerTypes.add(descriptiveConceptMarkerType);
		
		
		if ((authors != null)||(editors != null)) {
			Team team = Team.NewInstance();
			if (authors != null) {
			for (Iterator<Person> author = authors.values().iterator() ; author.hasNext() ;){
				team.addTeamMember(author.next());
			}
			}
			if (editors != null) {
				Marker marker = Marker.NewInstance();
				marker.setMarkerType(editorMarkerType);
				for (Iterator<Person> editor = editors.values().iterator() ; editor.hasNext() ;){
					Person edit = editor.next();
					edit.addMarker(marker);
					team.addTeamMember(edit);
				}
				}
			sec.setAuthorTeam(team);
			sourceReference.setAuthorTeam(team);
		}

		if (copyright != null) {
			sourceReference.addRights(copyright);
			sec.addRights(copyright);
		}
		
		// Returns a CdmApplicationController created by the values of this configuration.
		IDescriptionService descriptionService = getDescriptionService();

		for (Iterator<TaxonDescription> k = taxonDescriptions.values().iterator() ; k.hasNext() ;){
			TaxonDescription taxonDescription = k.next();
			// Persists a Description
			descriptionService.save(taxonDescription);
		}

		
		for (Iterator<String> refCD = taxonDescriptions.keySet().iterator() ; refCD.hasNext() ;){
			String ref = refCD.next();
			TaxonDescription td = taxonDescriptions.get(ref);
			if (citations.containsKey(ref)) {
				IArticle publication = (IArticle) publications.get(citations.get(ref));
				if (locations.containsKey(ref)) {
					Annotation location = Annotation.NewInstance(locations.get(ref), datasetLanguage);
					AnnotationType annotationType = AnnotationType.NewInstance("", "location", "");
					annotationTypes.add(annotationType);
					location.setAnnotationType(annotationType);
					((ReferenceBase)publication).addAnnotation(location);
				}
				td.addDescriptionSource((ReferenceBase)publication);
			}
		}
		logger.info("end makeTaxonDescriptions ...");
		
		
//		for (Iterator<TermVocabulary<Modifier>> k = termVocabularyStates.iterator() ; k.hasNext() ;){
//			TermVocabulary<Modifier> termVocabulary = k.next();
//			getVocabularyService().save(termVocabulary); //XIM
//		}
		

		//sddConfig.setSourceReference(sourceReference);

		//saving of all imported data into the CDM db
		ITermService termService = getTermService();
		
//		Modifier odif = new Modifier("zoub","zab","zib");
//		Modifier odif2 = new Modifier("zouba","zaba","ziba");
//		modifiers.put("yy", odif);
//		State st = State.NewInstance();
//		StateData stat = StateData.NewInstance();
		//StateData stat2 = StateData.NewInstance();
		//termService.save(stat2);
//		stat2.setState(st);
//		stat.setState(st);
//		stateDatas.put("yy", stat);
//		stateDatas.put("yy", stat2);
//		stat.addModifier(odif);
//		stat.addModifier(odif2);
//		stat2.addModifier(odif);
		
		for (Iterator<Modifier> k = modifiers.values().iterator() ; k.hasNext() ;){
			Modifier modifier = k.next();
			termService.save(modifier);
		}
		
		//termService.save(descriptiveConceptMarkerType);
		if (descriptiveConcepts != null) {
			for (Iterator<Feature> feat = descriptiveConcepts.iterator() ; feat.hasNext() ;) {
				Marker marker = Marker.NewInstance();
				marker.setMarkerType(descriptiveConceptMarkerType);
				Feature feature = feat.next();
				feature.addMarker(marker);
			}
		}
		
		for (Iterator<State> k = states.values().iterator() ; k.hasNext() ;){
			State state = k.next();
			termService.save(state);
		}
		
		/*Marker markerd = Marker.NewInstance();
		markerd.setMarkerType(descriptiveConceptMarker);
		Feature fiture = Feature.NewInstance("Fitoure","Fitoure","Fitoure");
		fiture.addMarker(markerd);
		TermVocabulary<Modifier> termVocabularyState = new TermVocabulary<Modifier>("test","test","test","test");
		Modifier modif = new Modifier("zoub","zab","zib");
		termVocabularyState.addTerm(modif);
		getVocabularyService().save(termVocabularyState);
		fiture.addRecommendedModifierEnumeration(termVocabularyState);
		termService.save(modif);
		termService.save(fiture);*/
		
		for (Iterator<Feature> k = features.values().iterator() ; k.hasNext() ;){
			Feature feature = k.next();
			termService.save(feature); 
		}
		
		for(Iterator<MarkerType> k = markerTypes.iterator() ; k.hasNext() ;){
			MarkerType markerType = k.next();
			termService.save(markerType);
		}
		
		//XIMtermService.save(editorMarkerType);
		
		//XIMtermService.save(geographicAreaMarkerType);
		for (Iterator<NamedArea> k = namedAreas.values().iterator() ; k.hasNext() ;) {
			Marker marker = Marker.NewInstance();
			marker.setMarkerType(geographicAreaMarkerType);
			NamedArea area = k.next();
			area.addMarker(marker);
			//getTermService().save(area);
			termService.save(area);
		}		
		
		if (units != null) {
			for (Iterator<MeasurementUnit> k = units.values().iterator() ; k.hasNext() ;){
				MeasurementUnit unit = k.next();
				if (unit != null) {
					termService.save(unit); 
				}
			}
		}
		for (Iterator<StatisticalMeasure> k = statisticalMeasures.iterator() ; k.hasNext() ;) {
			StatisticalMeasure sm = k.next();
			termService.save(sm); 
		}
		for (Iterator<AnnotationType> at = annotationTypes.iterator() ; at.hasNext() ;) {
			AnnotationType annotationType = at.next();
			termService.save(annotationType); 
		}

		IReferenceService referenceService = getReferenceService();
		// referenceService.saveReference(sourceReference); 
		for (Iterator<ReferenceBase> k = publications.values().iterator() ; k.hasNext() ;){
			ReferenceBase publication = (ReferenceBase) k.next();
			referenceService.save(publication); 
		}

		for (Iterator<FeatureTree> k = featureTrees.iterator() ; k.hasNext() ;) {
			FeatureTree tree = k.next();
			getFeatureTreeService().save(tree);
		}
		for (Iterator<TaxonomicTree> k = taxonomicTrees.iterator() ; k.hasNext() ;) {
			TaxonomicTree tree = k.next();
			getTaxonTreeService().save(tree);
		}
		for (Iterator<Specimen> k = specimens.values().iterator() ; k.hasNext() ;) {
			Specimen specimen = k.next();
			getOccurrenceService().save(specimen);
		}
		logger.info("end of persistence ...");
		
	}

	// imports the default language of the dataset
	protected void importDatasetLanguage(Element elDataset, SDDImportConfigurator sddConfig){
		String nameLang = elDataset.getAttributeValue("lang",xmlNamespace);

		if (!nameLang.equals("")) {
			String iso = nameLang.substring(0, 2);
			datasetLanguage = getTermService().getLanguageByIso(iso);
		} else {
			datasetLanguage = Language.ENGLISH();
		}
		if (datasetLanguage == null) {
			datasetLanguage = Language.ENGLISH();
		}
	}
	
	// imports the specimens
	protected void importSpecimens(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig) {
		logger.info("start Specimens ...");
		/*	<Specimens>
        		<Specimen id="sp1">
           			<Representation>
              			<Label>TJM45337</Label>
           			</Representation>
        		</Specimen>
     		</Specimens>
		 */
		Element elSpecimens = elDataset.getChild("Specimens",sddNamespace);
		if (elSpecimens != null){
			List<Element> listSpecimens = elSpecimens.getChildren("Specimen", sddNamespace);
			int j = 0;
			for (Element elSpecimen : listSpecimens) {
				String id = elSpecimen.getAttributeValue("id");
				Specimen specimen = null;
				if (!id.equals("")) {
					specimen = Specimen.NewInstance();
					specimens.put(id,specimen);
					importRepresentation(elSpecimen, sddNamespace, specimen, id, sddConfig);
				}
			}

		}
	}

	// imports the revision data associated with the Dataset (authors, modifications)
	protected void importRevisionData(Element elDataset, Namespace sddNamespace){
		// <RevisionData>
		logger.info("start RevisionData ...");
		Element elRevisionData = elDataset.getChild("RevisionData",sddNamespace);
		if (elRevisionData != null){
			// <Creators>
			Element elCreators = elRevisionData.getChild("Creators",sddNamespace);

			// <Agent role="aut" ref="a1"/>
			List<Element> listAgents = elCreators.getChildren("Agent", sddNamespace);

			int j = 0;
			//for each Agent
			for (Element elAgent : listAgents){

				String role = elAgent.getAttributeValue("role");
				String ref = elAgent.getAttributeValue("ref");
				if (role.equals("aut")) {
					if(!ref.equals("")) {
						authors.put(ref, null);
					}
				}
				if (role.equals("edt")) {
					if(!ref.equals("")) {
						editors.put(ref, null);
					}
				}
				if ((++j % modCount) == 0){ logger.info("Agents handled: " + j);}

			}

			// <DateModified>2006-04-08T00:00:00</DateModified>
			String stringDateModified = (String)ImportHelper.getXmlInputValue(elRevisionData, "DateModified",sddNamespace);

			if (stringDateModified != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
				Date d = null;
				try {
					d = sdf.parse(stringDateModified);
				} catch(Exception e) {
					System.err.println("Exception :");
					e.printStackTrace();
				}

				DateTime updated = null;
				if (d != null) {
					updated = new DateTime(d);
					sourceReference.setUpdated(updated);
					sec.setUpdated(updated);
				}
			}
		}
	}

	// imports ipr statements associated with a dataset
	protected void importIPRStatements(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig){
		// <IPRStatements>
		logger.info("start IPRStatements ...");
		Element elIPRStatements = elDataset.getChild("IPRStatements",sddNamespace);
		// <IPRStatement role="Copyright">
		if (elIPRStatements != null) {
			List<Element> listIPRStatements = elIPRStatements.getChildren("IPRStatement", sddNamespace);
			int j = 0;
			//for each IPRStatement

			for (Element elIPRStatement : listIPRStatements){

				String role = elIPRStatement.getAttributeValue("role");
				// <Label xml:lang="en-au">(c) 2003-2006 Centre for Occasional Botany.</Label>
				Element elLabel = elIPRStatement.getChild("Label",sddNamespace);
				String lang = "";
				if (elLabel != null) {
					lang = elLabel.getAttributeValue("lang",xmlNamespace);
				}
				String label = (String)ImportHelper.getXmlInputValue(elIPRStatement, "Label",sddNamespace);

				if (role.equals("Copyright")) {
					Language iprLanguage = null;
					if (lang != null) {
						if (!lang.equals("")) {
							iprLanguage = getTermService().getLanguageByIso(lang.substring(0, 2));
						} else {
							iprLanguage = datasetLanguage;
						}
					}
					if (iprLanguage == null) {
						iprLanguage = datasetLanguage;
					}
					copyright = Rights.NewInstance(label, iprLanguage);
				}

				if (copyright != null) {
					sourceReference.addRights(copyright);
					sec.addRights(copyright);
				}

				if ((++j % modCount) == 0){ logger.info("IPRStatements handled: " + j);}

			}
		}
	}

	// imports the taxon names
	protected void importTaxonNames(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig){
		// <TaxonNames>
		logger.info("start TaxonNames ...");
		Element elTaxonNames = elDataset.getChild("TaxonNames",sddNamespace);
		// <TaxonName id="t1" uri="urn:lsid:authority:namespace:my-own-id">
		if (elTaxonNames != null) {
			List<Element> listTaxonNames = elTaxonNames.getChildren("TaxonName", sddNamespace);
			int j = 0;
			//for each TaxonName
			for (Element elTaxonName : listTaxonNames){

				String id = elTaxonName.getAttributeValue("id");
				String uri = elTaxonName.getAttributeValue("uri");

				NonViralName tnb = null;
				if (!id.equals("")) {
					tnb = NonViralName.NewInstance(null);
					IdentifiableSource source = null;
					if (uri != null) {
						if (!uri.equals("")) {
							source = IdentifiableSource.NewInstance(id, "TaxonName", refFactory.newGeneric(), uri);
						}
					} else {
						source = IdentifiableSource.NewInstance(id, "TaxonName");
					}
					tnb.addSource(source);
					taxonNameBases.put(id,tnb);
				}

				// <Representation>
				// <Label xml:lang="la">Viola hederacea Labill.</Label>
				importRepresentation(elTaxonName, sddNamespace, tnb, id, sddConfig);

				if ((++j % modCount) == 0){ logger.info("TaxonNames handled: " + j);}

			}
		}
	}

	// imports the characters (categorical, quantitative and text ; sequence characters not supported) which correspond to CDM Features
	protected void importCharacters(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig, boolean success){
		// <Characters>
		logger.info("start Characters ...");
		Element elCharacters = elDataset.getChild("Characters", sddNamespace);

		// <CategoricalCharacter id="c1">
		if (elCharacters != null) {
			List<Element> elCategoricalCharacters = elCharacters.getChildren("CategoricalCharacter", sddNamespace);
			int j = 0;
			for (Element elCategoricalCharacter : elCategoricalCharacters){
				try {

					String idCC = elCategoricalCharacter.getAttributeValue("id");
					Feature categoricalCharacter = Feature.NewInstance();
					categoricalCharacter.setKindOf(Feature.DESCRIPTION());
					importRepresentation(elCategoricalCharacter, sddNamespace, categoricalCharacter, idCC, sddConfig);
					categoricalCharacter.setSupportsCategoricalData(true);

					// <States>
					Element elStates = elCategoricalCharacter.getChild("States",sddNamespace);

					// <StateDefinition id="s1">
					List<Element> elStateDefinitions = elStates.getChildren("StateDefinition",sddNamespace);
					TermVocabulary<State> termVocabularyState = new TermVocabulary<State>();
					int k = 0;
					//for each StateDefinition
					for (Element elStateDefinition : elStateDefinitions){

						if ((++k % modCount) == 0){ logger.info("StateDefinitions handled: " + (k-1));}

						String idS = elStateDefinition.getAttributeValue("id");
						State state = State.NewInstance();
						importRepresentation(elStateDefinition, sddNamespace, state, idS, sddConfig);

						//StateData stateData = StateData.NewInstance();
						//stateData.setState(state);
						termVocabularyState.addTerm(state);
						states.put(idS,state);
					}
					categoricalCharacter.addSupportedCategoricalEnumeration(termVocabularyState);
					features.put(idCC, categoricalCharacter);

				} catch (Exception e) {
					logger.warn("Import of CategoricalCharacter " + j + " failed.");
					success = false; 
				}

				if ((++j % modCount) == 0){ logger.info("CategoricalCharacters handled: " + j);}

			}

			// <QuantitativeCharacter id="c2">
			List<Element> elQuantitativeCharacters = elCharacters.getChildren("QuantitativeCharacter", sddNamespace);
			j = 0;
			//for each QuantitativeCharacter
			for (Element elQuantitativeCharacter : elQuantitativeCharacters){

				try {

					String idQC = elQuantitativeCharacter.getAttributeValue("id");

					// <Representation>
					//  <Label>Leaf length</Label>
					// </Representation>
					Feature quantitativeCharacter = Feature.NewInstance();
					quantitativeCharacter.setKindOf(Feature.DESCRIPTION());
					importRepresentation(elQuantitativeCharacter, sddNamespace, quantitativeCharacter, idQC, sddConfig);

					quantitativeCharacter.setSupportsQuantitativeData(true);

					// <MeasurementUnit>
					//  <Label role="Abbrev">m</Label>
					// </MeasurementUnit>
					Element elMeasurementUnit = elQuantitativeCharacter.getChild("MeasurementUnit",sddNamespace);
					String label = "";
					String role = "";
					if (elMeasurementUnit != null) {
						Element elLabel = elMeasurementUnit.getChild("Label",sddNamespace);
						role = elLabel.getAttributeValue("role");
						label = (String)ImportHelper.getXmlInputValue(elMeasurementUnit, "Label",sddNamespace);
					}

					MeasurementUnit unit = null;
					if (!label.equals("")){
						if (role != null) {
							if (role.equals("Abbrev")){
								unit = MeasurementUnit.NewInstance(label,label,label);
							}
						} else {
							unit = MeasurementUnit.NewInstance(label,label,label);
						}
					}

					if (unit != null) {
						units.put(idQC, unit);
					}

					//<Default>
					//  <MeasurementUnitPrefix>milli</MeasurementUnitPrefix>
					//</Default>
					Element elDefault = elQuantitativeCharacter.getChild("Default",sddNamespace);
					if (elDefault != null) {
						String measurementUnitPrefix = (String)ImportHelper.getXmlInputValue(elDefault, "MeasurementUnitPrefix",sddNamespace);
						if (!measurementUnitPrefix.equals("")){
							defaultUnitPrefixes.put(idQC, measurementUnitPrefix);
						}
					}

					features.put(idQC, quantitativeCharacter);

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of QuantitativeCharacter " + j + " failed.");
					success = false; 
				}

				if ((++j % modCount) == 0){ logger.info("QuantitativeCharacters handled: " + j);}

			}

			// <TextCharacter id="c3">
			List<Element> elTextCharacters = elCharacters.getChildren("TextCharacter", sddNamespace);
			j = 0;
			//for each TextCharacter
			for (Element elTextCharacter : elTextCharacters){

				try {

					String idTC = elTextCharacter.getAttributeValue("id");

					// <Representation>
					//  <Label xml:lang="en">Leaf features not covered by other characters</Label>
					// </Representation>
					Feature textCharacter = Feature.NewInstance();
					textCharacter.setKindOf(Feature.DESCRIPTION());
					importRepresentation(elTextCharacter, sddNamespace, textCharacter, idTC, sddConfig);

					textCharacter.setSupportsTextData(true);

					features.put(idTC, textCharacter);

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of TextCharacter " + j + " failed.");
					success = false; 
				}

				if ((++j % modCount) == 0){ logger.info("TextCharacters handled: " + j);}

			}

		}

		/*for (Iterator<Feature> f = features.values().iterator() ; f.hasNext() ;){
			featureSet.add(f.next()); //XIM Why this line ?
		}*/

	}

	// imports the descriptions of taxa
	protected void importCodedDescriptions(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig, boolean success){
		// <CodedDescriptions>
		logger.info("start CodedDescriptions ...");
		Element elCodedDescriptions = elDataset.getChild("CodedDescriptions",sddNamespace);
		// <CodedDescription id="D101">

		if (elCodedDescriptions != null) {
			List<Element> listCodedDescriptions = elCodedDescriptions.getChildren("CodedDescription", sddNamespace);
			int j = 0;
			//for each CodedDescription

			for (Element elCodedDescription : listCodedDescriptions){

				try {

					String idCD = elCodedDescription.getAttributeValue("id");

					// <Representation>
					//  <Label>&lt;i&gt;Viola hederacea&lt;/i&gt; Labill. as revised by R. Morris April 8, 2006</Label>
					// </Representation>
					TaxonDescription taxonDescription = TaxonDescription.NewInstance();
					importRepresentation(elCodedDescription, sddNamespace, taxonDescription, idCD, sddConfig);

					// <Scope>
					//  <TaxonName ref="t1"/>
					//  <Citation ref="p1" location="p. 30"/>
					// </Scope>
					Element elScope = elCodedDescription.getChild("Scope",sddNamespace);
					String ref = "";
					Taxon taxon = null;
					if (elScope != null) {
						Element elTaxonName = elScope.getChild("TaxonName",sddNamespace);
						ref = elTaxonName.getAttributeValue("ref");

						NonViralName taxonNameBase = taxonNameBases.get(ref);
						taxon = Taxon.NewInstance(taxonNameBase, sec);
					}
					else {//XIM what is it ?
						NonViralName tnb = NonViralName.NewInstance(null);
						String id = new String(""+taxonNamesCount);
						IdentifiableSource source = IdentifiableSource.NewInstance(id, "TaxonName");
						importRepresentation(elCodedDescription, sddNamespace, tnb, id, sddConfig);
						tnb.addSource(source);
						taxonNameBases.put(id ,tnb);
						taxonNamesCount++;
						taxon = Taxon.NewInstance(tnb, sec);
					}

					String refCitation = "";
					String location = "";

					if (elScope != null) {
						Element elCitation = elScope.getChild("Citation",sddNamespace);
						if (elCitation != null) {
							refCitation = elCitation.getAttributeValue("ref");
							location = elCitation.getAttributeValue("location");
						}
					}

					// <SummaryData>
					Element elSummaryData = elCodedDescription.getChild("SummaryData",sddNamespace);
					if (elSummaryData != null) {

						// <Categorical ref="c4">
						List<Element> elCategoricals = elSummaryData.getChildren("Categorical", sddNamespace);
						int k = 0;
						//for each Categorical
						for (Element elCategorical : elCategoricals){
							if ((++k % modCount) == 0){ logger.warn("Categorical handled: " + (k-1));}
							ref = elCategorical.getAttributeValue("ref");
							Feature feature = features.get(ref);
							CategoricalData categoricalData = CategoricalData.NewInstance();
							categoricalData.setFeature(feature);

							// <State ref="s3"/>
							List<Element> elStates = elCategorical.getChildren("State", sddNamespace);
							int l = 0;
							
							//for each State
							for (Element elState : elStates){
								if ((++l % modCount) == 0){ logger.info("States handled: " + (l-1));}
								ref = elState.getAttributeValue("ref");
								State state = states.get(ref);
								if (state!=null) {
									StateData stateData = StateData.NewInstance();
									stateData.setState(state);
									List<Element> elModifiers = elState.getChildren("Modifier", sddNamespace);
									for (Element elModifier : elModifiers){
										ref = elModifier.getAttributeValue("ref");
										Modifier modifier = modifiers.get(ref);
										if (modifier!=null) {
											stateData.addModifier(modifier);
										}
									}
								categoricalData.addState(stateData);
							}
							taxonDescription.addElement(categoricalData);
						}
						}
						// <Quantitative ref="c2">
						List<Element> elQuantitatives = elSummaryData.getChildren("Quantitative", sddNamespace);
						k = 0;
						//for each Quantitative
						for (Element elQuantitative : elQuantitatives){
							if ((++k % modCount) == 0){ logger.warn("Quantitative handled: " + (k-1));}
							ref = elQuantitative.getAttributeValue("ref");
							Feature feature = features.get(ref);
							QuantitativeData quantitativeData = QuantitativeData.NewInstance();
							quantitativeData.setFeature(feature);

							MeasurementUnit unit = units.get(ref);
							String prefix = defaultUnitPrefixes.get(ref);
							if (unit != null) {
								String u = unit.getLabel();
								if (prefix != null) {
									u = prefix + u;
								}
								unit.setLabel(u);
								quantitativeData.setUnit(unit);
							}

							// <Measure type="Min" value="2.3"/>
							List<Element> elMeasures = elQuantitative.getChildren("Measure", sddNamespace);
							int l = 0;
							
							//for each State
							for (Element elMeasure : elMeasures){
								if ((++l % modCount) == 0){ logger.info("States handled: " + (l-1));}
								String type = elMeasure.getAttributeValue("type");
								String value = elMeasure.getAttributeValue("value");
								if (value.contains(",")) {
									value = value.replace(',', '.');
								}
								Float v = Float.parseFloat(value);
								//Float v = new Float(0);
								StatisticalMeasure t = null;
								if (type.equals("Min")) {
									t = StatisticalMeasure.MIN();
								} else if (type.equals("Mean")) {
									t = StatisticalMeasure.AVERAGE();
								} else if (type.equals("Max")) {
									t = StatisticalMeasure.MAX();
								} else if (type.equals("SD")) {
									t = StatisticalMeasure.STANDARD_DEVIATION();
								} else if (type.equals("N")) {
									t = StatisticalMeasure.SAMPLE_SIZE();
								} else if (type.equals("UMethLower")) {
									t = StatisticalMeasure.TYPICAL_LOWER_BOUNDARY();
								} else if (type.equals("UMethUpper")) {
									t = StatisticalMeasure.TYPICAL_UPPER_BOUNDARY();
								} else if (type.equals("Var")) {
									t = StatisticalMeasure.VARIANCE();
								} else {
									t = StatisticalMeasure.NewInstance(type,type,type);
									statisticalMeasures.add(t);
								}

								StatisticalMeasurementValue statisticalValue = StatisticalMeasurementValue.NewInstance();
								statisticalValue.setValue(v);
								statisticalValue.setType(t);
								quantitativeData.addStatisticalValue(statisticalValue);
								featureData.add(statisticalValue);
							}
							taxonDescription.addElement(quantitativeData);
						}

						// <TextChar ref="c3">
						List<Element> elTextChars = elSummaryData.getChildren("TextChar", sddNamespace);
						k = 0;
						//for each TextChar
						for (Element elTextChar : elTextChars){
							if ((++k % modCount) == 0){ logger.info("TextChar handled: " + (k-1));}
							ref = elTextChar.getAttributeValue("ref");
							Feature feature = features.get(ref);
							TextData textData = TextData.NewInstance();
							textData.setFeature(feature);

							// <Content>Free form text</Content>
							String content = (String)ImportHelper.getXmlInputValue(elTextChar, "Content",sddNamespace);
							textData.putText(content, datasetLanguage);
							taxonDescription.addElement(textData);
						}

					}

					if (taxon != null) {
						taxon.addDescription(taxonDescription);
					}

					if (!refCitation.equals("")){
						citations.put(idCD,refCitation);
					}

					if (!location.equals("")){
						locations.put(idCD, location);
					}
					
					taxonDescription.setDescriptiveSystem(featureSet);

					taxonDescriptions.put(idCD, taxonDescription);//FIXME

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of CodedDescription " + j + " failed.");
					success = false;
				}
				if ((++j % modCount) == 0){ logger.info("CodedDescriptions handled: " + j);}

			}

		}
	}

	// imports the persons associated with the dataset creation, modification, related publications
	protected void importAgents(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig, boolean success){
		// <Agents>
		logger.info("start Agents ...");
		Element elAgents = elDataset.getChild("Agents",sddNamespace);
		if (elAgents != null) {
			// <Agent id="a1">
			List <Element> listAgents = elAgents.getChildren("Agent", sddNamespace);
			int j = 0;
			//for each Agent
			for (Element elAgent : listAgents){

				try {

					String idA = elAgent.getAttributeValue("id");

					//  <Representation>
					//   <Label>Kevin Thiele</Label>
					//   <Detail role="Description">Ali Baba is also known as r.a.m.</Detail>
					//  </Representation>
					Person person = Person.NewInstance();
					importRepresentation(elAgent, sddNamespace, person, idA, sddConfig);
					person.addSource(IdentifiableSource.NewInstance(idA, "Agent"));

					/*XIM <Links>
					Element elLinks = elAgent.getChild("Links",sddNamespace);

					if (elLinks != null) {

						//  <Link rel="Alternate" href="http://www.diversitycampus.net/people/hagedorn"/>
						List<Element> listLinks = elLinks.getChildren("Link", sddNamespace);
						int k = 0;
						//for each Link
						for (Element elLink : listLinks){

							try {

								String rel = elLink.getAttributeValue("rel");
								String href = elLink.getAttributeValue("href");

								Media link = Media.NewInstance();
								MediaRepresentation mr = MediaRepresentation.NewInstance();
								mr.addRepresentationPart(MediaRepresentationPart.NewInstance(href, null));
								link.addRepresentation(mr);
								person.addMedia(link);

							} catch (Exception e) {
								//FIXME
								logger.warn("Import of Link " + k + " failed.");
								success = false; 
							}

							if ((++k % modCount) == 0){ logger.info("Links handled: " + k);}

						}
					}
					*/
					if (authors.containsKey(idA)) {
						authors.put(idA,person);
					}

					if (editors.containsKey(idA)) {
						editors.put(idA, person);
					}

				} catch (Exception e) {
					//FIXME
					logger.warn("Import of Agent " + j + " failed.");
					success = false; 
				}

				if ((++j % modCount) == 0){ logger.info("Agents handled: " + j);}

			}
		}
	}

	// imports publications related with the data set
	protected void importPublications(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig, boolean success){
		/* <Publications>
			  <Publication id="p112">
			    <Representation>
			      <Label>Gee, X. & Haa, Y. (2003). How to be happy in five minutes. Instant Gratifications, Palm Beach.</Label>
			    </Representation>
			    <Links>
			    <Link rel="BasedOn" href="doi:10.1992/32311"/>
			    <Link rel="Alternate" href="http://some.service.net/providing/bibliographic.data"/>
			    </Links>
			</Publications>
*/
		logger.info("start Publications ...");
		Element elPublications = elDataset.getChild("Publications",sddNamespace);

		if (elPublications != null) {
			List<Element> listPublications = elPublications.getChildren("Publication", sddNamespace);
			int j = 0;
			for (Element elPublication : listPublications){

				try {

					String idP = elPublication.getAttributeValue("id");
					ReferenceBase publication = refFactory.newArticle();
					importRepresentation(elPublication, sddNamespace, publication, idP, sddConfig);

					publications.put(idP,publication);

				} catch (Exception e) {
					logger.warn("Import of Publication " + j + " failed.");
					success = false; 
				}

				if ((++j % modCount) == 0){ logger.info("Publications handled: " + j);}

			}
		}
	}

	// imports media objects such as images //FIXME check mediaobj
	protected void importMediaObjects(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig, boolean success){
		// <MediaObjects>
		logger.info("start MediaObjects ...");
		Element elMediaObjects = elDataset.getChild("MediaObjects",sddNamespace);

		if (elMediaObjects != null) {
			// <MediaObject id="m1">
			List<Element> listMediaObjects = elMediaObjects.getChildren("MediaObject", sddNamespace);
			int j = 0;
			for (Element elMO : listMediaObjects){

				String id = "";

				try {
					String idMO = elMO.getAttributeValue("id");
					id = idMO;

					//  <Representation>
					//   <Label>Image description, e.g. to be used for alt-attribute in html.</Label>
					//  </Representation>
					Media media = Media.NewInstance();
					importRepresentation(elMO, sddNamespace, media, idMO, sddConfig);

					// <Type>Image</Type>
					// <Source href="http://test.edu/test.jpg"/>
					String type = (String)ImportHelper.getXmlInputValue(elMO,"Type",sddNamespace);

					if ((type != null) && (type.equals("Image"))) {
						Element elSource = elMO.getChild("Source",sddNamespace);
						String href = elSource.getAttributeValue("href");

						ImageMetaData imageMetaData = ImageMetaData.newInstance();
						ImageFile image = null;
						if (href.substring(0,7).equals("http://")) {
							try{
								URL url = new URL(href);
								
								imageMetaData.readMetaData(url.toURI(), 0);
								image = ImageFile.NewInstance(url.toString(), null, imageMetaData);
							} catch (MalformedURLException e) {
								logger.error("Malformed URL", e);
							}
						} else {
							String sns = sddConfig.getSourceNameString();
							File f = new File(sns);
							File parent = f.getParentFile();
							String fi = href;
							//String fi = parent.toString() + File.separator + href; //TODO erase file:/
							File file = new File(fi);
							imageMetaData.readMetaData(file.toURI(), 0);
							image = ImageFile.NewInstance(file.toString(), null, imageMetaData);
						}
						MediaRepresentation representation = MediaRepresentation.NewInstance(imageMetaData.getMimeType(), null);
						representation.addRepresentationPart(image);

						media.addRepresentation(representation);

						ArrayList<CdmBase> lcb = (ArrayList<CdmBase>) mediaObject_ListCdmBase.get(idMO);
						if (lcb != null) {
							for (int k = 0; k < lcb.size(); k++) {
								if (lcb.get(k) instanceof DefinedTermBase) {
									DefinedTermBase dtb = (DefinedTermBase) lcb.get(k);
									// if (lcb.get(0) instanceof DefinedTermBase) {
									// DefinedTermBase dtb = (DefinedTermBase) lcb.get(0);
									//									if (dtb!=null) {
									//										if (k == 0) {
									dtb.addMedia(media);
									//System.out.println(dtb.getLabel());
									//										} else {
									//											Media me = (Media) media.clone();
									//											dtb.addMedia(me);
									//										}
									//									}
								} else if (lcb.get(k) instanceof ReferenceBase) {
									ReferenceBase rb = (ReferenceBase) lcb.get(k);
									//} else if (lcb.get(0) instanceof ReferenceBase) {
									//ReferenceBase rb = (ReferenceBase) lcb.get(0);
									// rb.setTitleCache(label);
									//									if (rb!=null) {
									//										if (k == 0) {
									rb.addMedia(media);
									//System.out.println(rb.getTitle());
									//										} else {
									//											Media me = (Media) media.clone();
									//											rb.addMedia(me);
									//										}
									//									}
								}/* else if (lcb.get(k) instanceof TaxonNameBase){
									TaxonNameBase tb = (TaxonNameBase) lcb.get(k);
									tb.addMedia(media);
							}*/
							}
						}
					}

				} catch (Exception e) {
					//FIXME
					logger.warn("Could not attach MediaObject " + j + "(SDD: " + id + ") to several objects.");
					success = false; 
				}

				if ((++j % modCount) == 0){ logger.info("MediaObjects handled: " + j);

				}
			}
		}
	}

	// imports the <DescriptiveConcepts> block ; DescriptiveConcepts are used as nodes in CharacterTrees and Characters as leaves
	// but since Modifiers can be linked to DescriptiveConcepts they are stored as features with a particular Marker
	protected void importDescriptiveConcepts(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig){
		/* <DescriptiveConcepts>
		      <DescriptiveConcept id="dc0">
			        <Representation>
			          <Label>Fixed set of modifiers supported in Lucid3</Label>
			        </Representation>
			        <Modifiers>
			          <Modifier id="mod1">
			            <Representation>
			              <Label>rarely</Label>
			            </Representation>
			            <ModifierClass>Frequency</ModifierClass>
			            <ProportionRange lowerestimate="0.0" upperestimate="0.25"/>
			          </Modifier>
		          </Modifiers>
		        </DescriptiveConcept>
	         </DescriptiveConcepts>
		 */
		logger.info("start DescriptiveConcepts ...");
		Element elDescriptiveConcepts = elDataset.getChild("DescriptiveConcepts",sddNamespace);
		if (elDescriptiveConcepts != null) {
			List<Element> listDescriptiveConcepts = elDescriptiveConcepts.getChildren("DescriptiveConcept", sddNamespace);
			int j = 0;

			for (Element elDescriptiveConcept : listDescriptiveConcepts){
				try {
				String id = elDescriptiveConcept.getAttributeValue("id");
					Feature feature = Feature.NewInstance();
					feature.setKindOf(Feature.DESCRIPTION());
					if (!id.equals("")) {
					//	 <Representation>
					//       <Label>Body</Label>
					importRepresentation(elDescriptiveConcept, sddNamespace, feature, id, sddConfig);
						features.put(id, feature);
						getTermService().save(feature);//XIM
						descriptiveConcepts.add(feature);
						// imports the modifiers
						Element elModifiers = elDescriptiveConcept.getChild("Modifiers", sddNamespace);
					if (elModifiers !=null){
						List<Element> listModifiers = elModifiers.getChildren("Modifier", sddNamespace);
							TermVocabulary<Modifier> termVocabularyState = new TermVocabulary<Modifier>();
						for (Element elModifier : listModifiers) {
								Modifier modif = Modifier.NewInstance();
								String idmod = elModifier.getAttributeValue("id");
								importRepresentation(elModifier, sddNamespace, modif, idmod, sddConfig);
								termVocabularyState.addTerm(modif);
								//termVocabularyStates.add(termVocabularyState);
								getVocabularyService().save(termVocabularyState);//XIM
								modifiers.put(idmod, modif);
						}
							feature.addRecommendedModifierEnumeration(termVocabularyState);
				}

					}
				}
				catch (Exception e) {
					logger.warn("Import of DescriptiveConcept " + j + " failed.");
				}
				if ((++j % modCount) == 0){ logger.info("DescriptiveConcepts handled: " + j);}

			}
		}
	}

	// imports the <CharacterTrees> block
	protected void importCharacterTrees(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig, boolean success){
		// <CharacterTrees>
		logger.info("start CharacterTrees ...");
		Element elCharacterTrees = elDataset.getChild("CharacterTrees",sddNamespace);

		if (elCharacterTrees != null) {
			List<Element> listCharacterTrees = elCharacterTrees.getChildren("CharacterTree", sddNamespace);
			int j = 0;
			for (Element elCharacterTree : listCharacterTrees){
				try {
					Element elRepresentation = elCharacterTree.getChild("Representation",sddNamespace);
					String label = (String)ImportHelper.getXmlInputValue(elRepresentation,"Label",sddNamespace);
					//Element elDesignedFor = elCharacterTree.getChild("DesignedFor",sddNamespace);//TODO ?

						FeatureTree feattree =  FeatureTree.NewInstance();
						importRepresentation(elCharacterTree, sddNamespace, feattree, "", sddConfig);
						FeatureNode root = feattree.getRoot();
						List<Element> listelNodes = elCharacterTree.getChildren("Nodes", sddNamespace);

					//Nodes of CharacterTrees in SDD always refer to DescriptiveConcepts
						for (Element elNodes : listelNodes) {
							List<Element> listNodes = elNodes.getChildren("Node", sddNamespace);
							if (listNodes != null) {
								for (Element elNode : listNodes){
									String idN = elNode.getAttributeValue("id");
									FeatureNode fn = null;
								Feature dc = null;
								if (idN!=null) {
									// DescriptiveConcepts are used as nodes in CharacterTrees
										Element elDescriptiveConcept = elNode.getChild("DescriptiveConcept", sddNamespace);
										if (elDescriptiveConcept != null){
											String refDC = elDescriptiveConcept.getAttributeValue("ref");
										dc = features.get(refDC);
										fn = FeatureNode.NewInstance(dc);
										}
									if (fn==null){
											fn = FeatureNode.NewInstance();
										}
										Element elParent = elNode.getChild("Parent", sddNamespace);
									// in SDD links between Nodes are referenced by the <Parent> tag
										if (elParent!=null){
											String refP = elParent.getAttributeValue("ref");
										if (refP!=null) {
											FeatureNode parent = featureNodes.get(refP);
											if (parent==null){
												root.addChild(fn); // if no parent found or the reference is broken, add the node to the root of the tree
											}
											else {
												parent.addChild(fn);
											}
										}
									}
										else {
										root.addChild(fn); // if no parent found or the reference is broken, add the node to the root of the tree
										}
									}
								featureNodes.put(idN, fn);
								}
						}

						// Leaves of CharacterTrees in SDD are always CharNodes (referring to Characters)
								List<Element> listCharNodes = elNodes.getChildren("CharNode", sddNamespace);
						if (listCharNodes != null) {
								for (Element elCharNode : listCharNodes){
									Element elParent = elCharNode.getChild("Parent", sddNamespace);
									Element elCharacter = elCharNode.getChild("Character", sddNamespace);							
									FeatureNode fn = FeatureNode.NewInstance();
									if (elParent!=null){
										String refP = elParent.getAttributeValue("ref");
										if ((refP!=null)&&(!refP.equals(""))) {
										FeatureNode parent = featureNodes.get(refP);
											if (parent==null){
											parent = root; // if no parent found or the reference is broken, add the node to the root of the tree
											}
											parent.addChild(fn);
										}
									}
									String refC = elCharacter.getAttributeValue("ref");
									if ((refC!=null)&&(!refC.equals(""))){
										Feature character = features.get(refC);
										fn.setFeature(character);
								featureNodes.put(refC, fn);
									}
							}		
									}
								}
						featureTrees.add(feattree);
					}

				catch (Exception e) {
					logger.warn("Import of Character tree " + j + " failed.");
					success = false; 
				}
				if ((++j % modCount) == 0){ logger.info("CharacterTrees handled: " + j);}

			}

		}
	}

	// imports the <TaxonHierarchies> block
	protected void importTaxonHierarchies(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig, boolean success){

		logger.info("start TaxonHierarchies ...");
		Element elTaxonHierarchies = elDataset.getChild("TaxonHierarchies",sddNamespace);

		if (elTaxonHierarchies != null) {
			List<Element> listTaxonHierarchies = elTaxonHierarchies.getChildren("TaxonHierarchy", sddNamespace);
			int j = 0;
			for (Element elTaxonHierarchy : listTaxonHierarchies){
				try {
					Element elRepresentation = elTaxonHierarchy.getChild("Representation",sddNamespace);
					String label = (String)ImportHelper.getXmlInputValue(elRepresentation,"Label",sddNamespace);
						TaxonomicTree taxonomicTree =  TaxonomicTree.NewInstance(label);
						importRepresentation(elTaxonHierarchy, sddNamespace, taxonomicTree, "", sddConfig);
					
						Set<TaxonNode> root = taxonomicTree.getRootNodes();
						Element elNodes = elTaxonHierarchy.getChild("Nodes", sddNamespace); // There can be only one <Nodes> block for TaxonHierarchies
						List<Element> listNodes = elNodes.getChildren("Node", sddNamespace);
						
						for (Element elNode : listNodes){
							String idN = elNode.getAttributeValue("id");
							TaxonNameBase tnb = null;
							if (!idN.equals("")) {
								Element elTaxonName = elNode.getChild("TaxonName", sddNamespace);
								String refTN = elTaxonName.getAttributeValue("ref");
								tnb = taxonNameBases.get(refTN);
								Taxon taxon = (Taxon) tnb.getTaxa().iterator().next() ;
								Element elParent = elNode.getChild("Parent", sddNamespace);
								if (elParent!=null){
									String refP = elParent.getAttributeValue("ref");
									if (!refP.equals("")) {
										TaxonNode parent = taxonNodes.get(refP);
										TaxonNode child = parent.addChildTaxon(taxon, sec, "", Synonym.NewInstance(tnb, sec));
										taxonNodes.put(idN,child);
									}
								}
								else {
									TaxonNode tn = taxonomicTree.addChildTaxon(taxon, sec, "", Synonym.NewInstance(tnb, sec)); // if no parent found or the reference is broken, add the node to the root of the tree
									taxonNodes.put(idN,tn);
								}
							}
						}

						taxonomicTrees.add(taxonomicTree);
					}

				catch (Exception e) {
					//FIXME
					logger.warn("Import of Taxon Hierarchy " + j + " failed.");
					success = false; 
				}

				if ((++j % modCount) == 0){ logger.info("TaxonHierarchies handled: " + j);}

			}

		}
	}
	
	
	// imports the <GeographicAreas> block 
	protected void importGeographicAreas(Element elDataset, Namespace sddNamespace, SDDImportConfigurator sddConfig) {
		Element elGeographicAreas = elDataset.getChild("GeographicAreas",sddNamespace);
		if (elGeographicAreas != null) {
			List<Element> listGeographicAreas = elGeographicAreas.getChildren("GeographicArea", sddNamespace);
			int j = 0;
						
			for (Element elGeographicArea : listGeographicAreas){

				String id = elGeographicArea.getAttributeValue("id");
				NamedArea na = new NamedArea();
				importRepresentation(elGeographicArea, sddNamespace, na, id, sddConfig);
				namedAreas.put(id,na);
								}
			if ((++j % modCount) == 0){ logger.info("GeographicAreas handled: " + j);}
							}
							}
}
