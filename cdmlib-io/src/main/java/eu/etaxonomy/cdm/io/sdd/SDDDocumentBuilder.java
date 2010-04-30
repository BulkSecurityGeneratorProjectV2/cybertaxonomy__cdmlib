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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xml.sax.SAXException;

import org.apache.xerces.impl.xpath.regex.ParseException;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import eu.etaxonomy.cdm.io.jaxb.CdmMarshallerListener;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.IDatabase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.api.service.NaturalLanguageGenerator;

/**
 * Writes the SDD XML file. 
 * 
 * @author h.fradin
 * @created 10.12.2008
 * @version 1.0
 */

public class SDDDocumentBuilder {

	private DocumentImpl document;
	private XMLSerializer xmlserializer;
	private Writer writer;
	private DOMSerializer domi;
	private SDDDataSet cdmSource;

	private Map<Person,String> agents = new HashMap<Person,String>();
	private Map<TaxonNameBase,String> taxonNames = new HashMap<TaxonNameBase,String>();
	private Map<Feature,String> characters = new HashMap<Feature,String>();
	private Map<FeatureNode,String> featureNodes = new HashMap<FeatureNode,String>();
	private Map<Feature,String> descriptiveConcepts = new HashMap<Feature,String>();
	private Map<TaxonDescription,String> codedDescriptions = new HashMap<TaxonDescription,String>();
	private Map<Media,String> medias = new HashMap<Media,String>();
	private Map<State,String> states = new HashMap<State,String>();
	private Map<ReferenceBase, String> articles = new HashMap<ReferenceBase, String>();
	private Map<VersionableEntity, String> featuretrees = new HashMap<VersionableEntity, String>();
	private Map<Modifier, String> modifiers = new HashMap<Modifier, String>();
	private Map<TaxonNode, String> taxonNodes = new HashMap<TaxonNode,String>();
	private Map<NamedArea, String> namedAreas = new HashMap<NamedArea,String>();
	private Map<Specimen, String> specimens = new HashMap<Specimen,String>();
	private ReferenceFactory refFactory = ReferenceFactory.newInstance();
	
	private Map<VersionableEntity,String> features = new HashMap<VersionableEntity,String>();
	private int agentsCount = 0;
	private int articlesCount = 0;
	private int codedDescriptionsCount = 0;
	private int taxonNamesCount = 0;
	private int charactersCount = 0;
	private int textcharactersCount = 0;
	private int mediasCount = 0;
	private int statesCount = 0;
	private int featureNodesCount = 0;
	private int chartreeCount = 0;
	private int charnodeCount = 0;
	private int taxonNodesCount = 0;
	private int namedAreasCount = 0;
	private int specimenCount = 0;
	private int descriptiveConceptCount = 0;
	private int modifiersCount = 0;
	
	private String AGENT = "Agent";
	private String AGENTS = "Agents";
	private String CATEGORICAL = "Categorical";
	private String CATEGORICAL_CHARACTER = "CategoricalCharacter";
	private String CHARACTER = "Character";
	private String CHARACTERS = "Characters";
	private String CHARACTER_TREE = "CharacterTree";
	private String CHARACTER_TREES = "CharacterTrees";
	private String CHAR_NODE = "CharNode";
	private String CITATION = "Citation";
	private String CODED_DESCRIPTION = "CodedDescription";
	private String CODED_DESCRIPTIONS = "CodedDescriptions";
	private String CONTENT = "Content";
	private String CREATORS = "Creators";
	private String DATASET = "Dataset";
	private String DATASETS = "Datasets";
	private String DATE_CREATED = "DateCreated";
	private String DATE_MODIFIED = "DateModified";
	private String DEPENDENCY_RULES = "DependencyRules";
	private String DESCRIPTIVE_CONCEPT = "DescriptiveConcept";
	private String DESCRIPTIVE_CONCEPTS = "DescriptiveConcepts";
	private String DETAIL = "Detail";
	private String GENERATOR = "Generator";
	private String ID = "id";
	private String IMAGE = "Image";
	private String INAPPLICABLE_IF = "InapplicableIf";
	private String IPR_STATEMENT = "IPRStatement";
	private String IPR_STATEMENTS = "IPRStatements";
	private String LABEL = "Label";
	private String MEASURE = "Measure";
	private String MEDIA_OBJECT = "MediaObject";
	private String MEDIA_OBJECTS = "MediaObjects";
	private String NODE = "Node";
	private String NODES = "Nodes";
	private String NOTE = "Note";
	private String PARENT = "Parent";
	private String PUBLICATIONS = "Publications";
	private String QUANTITATIVE = "Quantitative";
	private String QUANTITATIVE_CHARACTER = "QuantitativeCharacter";
	private String REF = "ref";
	private String REPRESENTATION = "Representation";
	private String REVISION_DATA = "RevisionData";
	private String ROLE = "role";
	private String SCOPE = "Scope";
	private String SHOULD_CONTAIN_ALL_CHARACTERS = "ShouldContainAllCharacters";
	private String SOURCE = "Source";
	private String STATE = "State";
	private String STATE_DEFINITION = "StateDefinition";
	private String STATES = "States";
	private String STATUS = "Status";
	private String SUMMARY_DATA = "SummaryData";
	private String TAXON_NAME = "TaxonName";
	private String TAXON_NAMES = "TaxonNames";
	private String TECHNICAL_METADATA = "TechnicalMetadata";
	private String TEXT = "text";
	private String TEXT_CHAR = "TextChar";
	private String TEXT_CHARACTER = "TextCharacter";
	private String TYPE = "Type";
	private String URI = "uri";

	private Language defaultLanguage = Language.DEFAULT();

	private static final Logger logger = Logger.getLogger(SDDDocumentBuilder.class);
	
	private boolean natlang = true; // Only for tests on natural language generation ; to be deleted after
	private String NEWLINE = System.getProperty("line.separator");

	public SDDDocumentBuilder() throws SAXException, IOException {

		document = new DocumentImpl();

	}

	public void marshal(SDDDataSet cdmSource, File sddDestination) throws IOException {

		this.cdmSource = cdmSource;
		Marshaller marshaller;		
		CdmMarshallerListener marshallerListener = new CdmMarshallerListener();
		logger.info("Start marshalling");
		writeCDMtoSDD(sddDestination);

	}

	/**Write the DOM document.
	 * @param base
	 * @throws IOException
	 */
	public void writeCDMtoSDD(File sddDestination) throws IOException {

		try {
			buildDocument();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		OutputFormat format = new OutputFormat(document, "UTF-8", true);

		FileOutputStream fos = new FileOutputStream(sddDestination);

		writer = new OutputStreamWriter(fos, "UTF-8");

		xmlserializer = new XMLSerializer(writer, format);
		domi = xmlserializer.asDOMSerializer(); // As a DOM Serializer

		domi.serialize(document.getDocumentElement());

		writer.close();
	}

	//	#############
	//	# BUILD DOM	#
	//	#############	

	/**
	 * Builds the whole document.
	 * @param base the Base
	 * @throws ParseException 
	 */
	public void buildDocument() throws ParseException {

		//create <Datasets> = root node
		ElementImpl baselement = new ElementImpl(document, DATASETS);
		if (natlang) {
			buildNaturalLanguageDescription(baselement);
		}
		else {
			baselement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			baselement.setAttribute("xmlns", "http://rs.tdwg.org/UBIF/2006/");
			baselement.setAttribute("xsi:schemaLocation", "http://rs.tdwg.org/UBIF/2006 http://rs.tdwg.org/UBIF/2006/Schema/1.1/SDD.xsd");

			buildTechnicalMetadata(baselement);

			List<ReferenceBase> references = cdmSource.getReferences();
			Iterator<ReferenceBase> iterator = references.iterator();
			IDatabase d = refFactory.newDatabase();
			while (iterator.hasNext()) {
				ReferenceBase reference = (ReferenceBase) iterator.next();
				if (reference.getType().equals(ReferenceType.Database)) {
					buildDataset(baselement, reference);
				}
			}
		}
		document.appendChild(baselement);
	}
	
	// Only for tests on natural language generation ; to be deleted after
	public void buildNaturalLanguageDescription(ElementImpl dataset) {
		List<TextData> listTextData = null;
		Set<TaxonDescription> descriptions = null;
		TaxonDescription description = null;
		FeatureTree featureTree = null;
		for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa().iterator() ; tb.hasNext() ;){
			Taxon taxon = (Taxon) tb.next();
			if (taxon.generateTitle().contains("Podosperm")) {
				descriptions = taxon.getDescriptions();
				description = descriptions.iterator().next();
				System.out.println("P. canum");
			}
		}
		for (int i = 0; i < cdmSource.getFeatureData().size(); i++) {
			VersionableEntity featu = cdmSource.getFeatureData().get(i);
			if (featu instanceof FeatureTree){
				FeatureTree ft = (FeatureTree) featu;
				if (ft.getLabel().contains("natural language")) {
					featureTree = ft;
					System.out.println("natural language");
				}
			}
		}
		NaturalLanguageGenerator natlgen = new NaturalLanguageGenerator();
		listTextData = natlgen.generateNaturalLanguageDescription(featureTree, description);
		for (Iterator<TextData> td = listTextData.iterator() ; td.hasNext();) {
			TextData textD = td.next();
			System.out.println(textD.getText(Language.ENGLISH()));
		}

	}
	
	//	#############
	//	# BUILD DOM	#
	//	#############	

	/**
	 * Builds TechnicalMetadata associated with the SDD file
	 */
	public void buildTechnicalMetadata(ElementImpl baselement) throws ParseException {
		//create TechnicalMetadata
		ElementImpl technicalMetadata = new ElementImpl(document, TECHNICAL_METADATA);
		//select different databases associated to different descriptions TODO
		List<ReferenceBase> references = cdmSource.getReferences();
		Iterator<ReferenceBase> iterator = references.iterator();
		boolean database = false;
		IDatabase d = refFactory.newDatabase();
		while ((iterator.hasNext()) && (!database)) {
			ReferenceBase reference = (ReferenceBase) iterator.next();
			if (reference.getType().equals(ReferenceType.Database)) {
				d = reference;
			}
		}
		DateTime dt = d.getCreated();
		String date = dt.toString().substring(0, 19);
		technicalMetadata.setAttribute("created", date);

		ElementImpl generator = new ElementImpl(document, GENERATOR);
		generator.setAttribute("name", "EDIT CDM");
		generator.setAttribute("version", "v1");
		generator.setAttribute("notes","This SDD file has been generated by the SDD export functionality of the EDIT platform for Cybertaxonomy - Copyright (c) 2008");
		technicalMetadata.appendChild(generator);

		baselement.appendChild(technicalMetadata);
	}

	// Builds the information associated with a dataset
	public void buildDataset(ElementImpl baselement,IDatabase reference) throws ParseException {
		// create Dataset and language
		ElementImpl dataset = new ElementImpl(document, DATASET);
		// no default language associated with a dataset in the CDM
		dataset.setAttribute("xml:lang", Language.DEFAULT().getIso639_1());
		baselement.appendChild(dataset);
		buildRepresentation(dataset, reference);
		buildRevisionData(dataset, reference);
		buildIPRStatements(dataset, reference);
		buildTaxonNames(dataset);
		buildDescriptiveConcepts(dataset);
		buildCharacters(dataset);
		buildCodedDescriptions(dataset);
		buildAgents(dataset);
		buildPublications(dataset);
		buildMediaObjects(dataset);
		buildCharacterTrees(dataset);
		buildTaxonomicTrees(dataset);
		buildGeographicAreas(dataset);
		buildSpecimens(dataset);
	}

	/**
	 * Builds a Representation element using a ReferenceBase
	 */
	public void buildRepresentation(ElementImpl element, IDatabase reference) throws ParseException {

		//	create <Representation> element
		ElementImpl representation = new ElementImpl(document, REPRESENTATION);
		element.appendChild(representation);
		buildLabel(representation, reference.getTitleCache());

		Set<Annotation> annotations = ((ReferenceBase)reference).getAnnotations();
		Iterator iterator = annotations.iterator();
		String detailText = null;
		if (iterator.hasNext()) {
			Annotation annotation = (Annotation) iterator.next();
			detailText = annotation.getText();
		}

		if (detailText != null && !detailText.equals("")) {
			ElementImpl detail = new ElementImpl(document, DETAIL);
			detail.appendChild(document.createTextNode(detailText));
			representation.appendChild(detail);
		}

		Set<Media> rm = ((ReferenceBase)reference).getMedia();

		if (rm != null && rm.size() > 0) {
			ElementImpl mediaObject;

			for (int i = 0; i < rm.size(); i++) {
				mediaObject = new ElementImpl(document, MEDIA_OBJECT);
				mediasCount = buildReference((Media) rm.toArray()[i], medias, REF, mediaObject, "m", mediasCount);
				representation.appendChild(mediaObject);
			}
		}

	}
	
	
	/**
	 * Builds a Representation element using a Feature
	 */
	public void buildRepresentation(ElementImpl element, TermBase tb) throws ParseException {

		//			create <Representation> element
		ElementImpl representation = new ElementImpl(document, REPRESENTATION);
		element.appendChild(representation);

		Set<Representation> representations = tb.getRepresentations();
		if (representations != null) {
			if (!representations.isEmpty()) {
				String label = ((Representation) representations.toArray()[0]).getLabel();
				buildLabel(representation, label);
				String detailText = tb.getDescription();

				if (detailText != null && !detailText.equals("")) {
					if (!detailText.equals(label)) {
						ElementImpl detail = new ElementImpl(document, DETAIL);
						detail.appendChild(document.createTextNode(detailText));
						representation.appendChild(detail);
					}
				}

			}
		}		

		if (tb instanceof DefinedTermBase) {
			DefinedTermBase dtb = (DefinedTermBase) tb;
			Set<Media> rm = dtb.getMedia();

			if (rm != null && rm.size() > 0) {
				ElementImpl mediaObject;

				for (int i = 0; i < rm.size(); i++) {
					mediaObject = new ElementImpl(document, MEDIA_OBJECT);
					mediasCount = buildReference((Media) rm.toArray()[i], medias, REF, mediaObject, "m", mediasCount);
					representation.appendChild(mediaObject);
				}
			}
		}
	}
	
	/**
	 * Builds a Representation element using an IdentifiableEntity 
	 */
	public void buildRepresentation(ElementImpl element, IdentifiableEntity ie) throws ParseException {

		//			create <Representation> element
		ElementImpl representation = new ElementImpl(document, REPRESENTATION);
		element.appendChild(representation);
		buildLabel(representation, ie.getTitleCache());

		Set<Annotation> annotations = ie.getAnnotations();
		Iterator iterator = annotations.iterator();
		String detailText = null;
		if (iterator.hasNext()) {
			Annotation annotation = (Annotation) iterator.next();
			detailText = annotation.getText();
		}

		if (detailText != null && !detailText.equals("")) {
			ElementImpl detail = new ElementImpl(document, DETAIL);
			detail.appendChild(document.createTextNode(detailText));
			representation.appendChild(detail);
		}

		if (ie instanceof DefinedTermBase) {
			DefinedTermBase dtb = (DefinedTermBase) ie;
			Set<Media> rm = dtb.getMedia();

			if (rm != null && rm.size() > 0) {
				ElementImpl mediaObject;

				for (int i = 0; i < rm.size(); i++) {
					mediaObject = new ElementImpl(document, MEDIA_OBJECT);
					mediasCount = buildReference((Media) rm.toArray()[i], medias, REF, mediaObject, "m", mediasCount);
					representation.appendChild(mediaObject);
				}
			}
		}
		if (ie instanceof IdentifiableMediaEntity) {
			IdentifiableMediaEntity ime = (IdentifiableMediaEntity) ie;
			Set<Media> medias = ime.getMedia();
			if (medias != null){
				ElementImpl elLinks = new ElementImpl(document, "Links");
				for (Iterator<Media> m = medias.iterator() ; m.hasNext() ;){
					Media media = m.next();
					Set<MediaRepresentation> smr = media.getRepresentations();
					for (Iterator<MediaRepresentation> mr = smr.iterator() ; mr.hasNext();){
						MediaRepresentation mediarep = mr.next();
						List<MediaRepresentationPart> lmrp = mediarep.getParts();
						for (Iterator<MediaRepresentationPart> mrp = lmrp.iterator();mrp.hasNext();){
							MediaRepresentationPart mediareppart = mrp.next();
							ElementImpl elLink = new ElementImpl(document, "Link");
							elLink.setAttribute("href",mediareppart.getUri());
							elLinks.appendChild(elLink);
						}
					}
				}
				element.appendChild(elLinks);
			}
		}

	}

	/**
	 * Builds RevisionData associated with the Dataset
	 */
	public void buildRevisionData(ElementImpl dataset, IDatabase database) throws ParseException {

		// <RevisionData>
		//  <Creators>
		//    <Agent role="aut" ref="a1"/>
		//    <Agent role="aut" ref="a2"/>
		//    <Agent role="edt" ref="a3"/>
		//  </Creators>
		//  <DateModified>2006-04-08T00:00:00</DateModified>
		// </RevisionData>

		ElementImpl revisionData = new ElementImpl(document, REVISION_DATA);

		// authors
		TeamOrPersonBase authors = database.getAuthorTeam();
		//TeamOrPersonBase editors = database.getUpdatedBy();

		if ((authors != null)) { // || (editors != null)) {
			ElementImpl creators = new ElementImpl(document, CREATORS);
			if (authors != null) {
				buildRefAgent(creators, authors, "aut");
			}
			//			if (editors != null) {
			//						buildRefAgent(creators, editors, "edt");
			//			}
			revisionData.appendChild(creators);
		}

		buildDateModified(revisionData, database);

		dataset.appendChild(revisionData);
	}

	/**
	 * Builds ModifiedDate associated with RevisionData
	 */
	public void buildDateModified(ElementImpl revisionData, IDatabase database) throws ParseException {

		//  <DateModified>2006-04-08T00:00:00</DateModified>

		if (((ReferenceBase)database).getUpdated() != null) {
			ElementImpl dateModified = new ElementImpl(document, DATE_MODIFIED);

			DateTime c = ((ReferenceBase)database).getUpdated();
			DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

			String date = fmt.print(c);
			dateModified.appendChild(document.createTextNode(date));

			revisionData.appendChild(dateModified);
		}

	}

	/**
	 * Builds IPRStatements associated with the Dataset
	 */
	public void buildIPRStatements(ElementImpl dataset, IDatabase database) throws ParseException {

		// <IPRStatements>
		//  <IPRStatement role="Copyright">
		//    <Label xml:lang="en-au">(c) 2003-2006 Centre for Occasional Botany.</Label>
		//  </IPRStatement>
		// </IPRStatements>

		if (database.getRights() != null) {
			//create IPRStatements
			ElementImpl iprStatements = new ElementImpl(document, IPR_STATEMENTS);
			dataset.appendChild(iprStatements);

			//mapping between IPRStatement Copyright (SDD) and first Right in the list of Rights
			ElementImpl iprStatement = new ElementImpl(document, IPR_STATEMENT);
			iprStatement.setAttribute("role", "Copyright");
			iprStatements.appendChild(iprStatement);
			if (!database.getRights().isEmpty()) {
				buildLabel(iprStatement, ((Rights) database.getRights().toArray()[0]).getText());
			}
		}

	}
	
	/**
	 * Creates a Label element 
	 * @param base
	 * @param element
	 */
	public void buildLabel(ElementImpl element, String text) {
		ElementImpl label = new ElementImpl(document, LABEL);
		label.appendChild(document.createTextNode(text));
		element.appendChild(label);
	}
	
	//	################
	//	# GENERIC BRICKS       #
	//	################

	/**
	 * Builds TaxonNames associated with the Dataset
	 */
	public void buildTaxonNames(ElementImpl dataset) throws ParseException {

		// <TaxonNames>
		//  <TaxonName id="t1" uri="urn:lsid:authority:namespace:my-own-id">
		//    <Representation>
		//      <Label xml:lang="la">Viola hederacea Labill.</Label>
		//    </Representation>
		//  </TaxonName>
		// </TaxonNames>

		if (cdmSource.getTaxonomicNames() != null) {
			ElementImpl elTaxonNames = new ElementImpl(document, TAXON_NAMES);

			for (int i = 0; i < cdmSource.getTaxonomicNames().size(); i++) {
				ElementImpl elTaxonName = new ElementImpl(document, TAXON_NAME);
				TaxonNameBase tnb = cdmSource.getTaxonomicNames().get(i);

				taxonNamesCount = buildReference(tnb, taxonNames, ID, elTaxonName, "t", taxonNamesCount);

				buildRepresentation(elTaxonName, tnb);

				elTaxonNames.appendChild(elTaxonName);
			}

			dataset.appendChild(elTaxonNames);
		}

	}
	

	public void buildDescriptiveConcepts(ElementImpl dataset) throws ParseException {

		if (cdmSource.getFeatureData() != null) {
			ElementImpl elFeatures = new ElementImpl(document, DESCRIPTIVE_CONCEPTS);
			int f = cdmSource.getTerms().size();
			for (int i = 0; i < f; i++) {
				DefinedTermBase dtb = cdmSource.getTerms().get(i);
				if (dtb instanceof Feature) {
					ElementImpl elFeat = new ElementImpl(document, DESCRIPTIVE_CONCEPT);
					Feature feature = (Feature) dtb;
					if (feature.getMarkers()!= null){
						Set<Marker> markers = feature.getMarkers();
						for(Iterator<Marker> m = markers.iterator() ; m.hasNext();){
							Marker marker = m.next();
							if (marker.getMarkerType().getLabel().equals("DescriptiveConcept")){
								descriptiveConceptCount = buildReference(dtb, descriptiveConcepts, ID, elFeat, "dc", descriptiveConceptCount);
								buildRepresentation(elFeat, feature);
								if (!feature.getRecommendedModifierEnumeration().isEmpty()){
									ElementImpl elModifiers = new ElementImpl(document, "Modifiers");
									for(Iterator<TermVocabulary<Modifier>> menum = feature.getRecommendedModifierEnumeration().iterator() ; menum.hasNext() ;) {
										TermVocabulary<Modifier> termVoc = menum.next();
										Set<Modifier> sm = termVoc.getTerms() ;
										for (Iterator<Modifier> modif = sm.iterator() ; modif.hasNext() ;) {
											Modifier modifier = modif.next();
											ElementImpl elModifier = new ElementImpl(document, "Modifier");
											modifiersCount = buildReference(modifier, modifiers, ID, elModifier, "mod", modifiersCount);
											buildRepresentation(elModifier,modifier);
											elModifiers.appendChild(elModifier);
										}
									}
									elFeat.appendChild(elModifiers);
								}
								elFeatures.appendChild(elFeat);
							}
						}
					}
				}
			}
			dataset.appendChild(elFeatures);
		}
	}


	/**
	 * Builds Characters associated with the Dataset
	 */
	public void buildCharacters(ElementImpl dataset) throws ParseException {

		if (cdmSource.getTerms() != null) {
			ElementImpl elCharacters = new ElementImpl(document, CHARACTERS);

			int f = cdmSource.getTerms().size();
			for (int i = 0; i < f; i++) {
				if (cdmSource.getTerms().get(i) instanceof Feature) {
					Feature character = (Feature) cdmSource.getTerms().get(i);
					if (character.isSupportsQuantitativeData()) {
						ElementImpl elQuantitativeCharacter = new ElementImpl(document, QUANTITATIVE_CHARACTER);
						charactersCount = buildReference(character, characters, ID, elQuantitativeCharacter, "c", charactersCount);
						buildRepresentation(elQuantitativeCharacter, character);
						elCharacters.appendChild(elQuantitativeCharacter);
					}

					if (character.isSupportsCategoricalData()) {
						ElementImpl elCategoricalCharacter = new ElementImpl(document, CATEGORICAL_CHARACTER);
						charactersCount = buildReference(character, characters, ID, elCategoricalCharacter, "c", charactersCount);
						buildRepresentation(elCategoricalCharacter, character);

						Set<TermVocabulary<State>> enumerations = character.getSupportedCategoricalEnumerations();
						if (enumerations != null) {
							if (enumerations.size()>0) {
								ElementImpl elStates = new ElementImpl(document, STATES);
								TermVocabulary tv = (TermVocabulary) enumerations.toArray()[0];
								Set<State> stateList = tv.getTerms();
								for (int j = 0; j < stateList.size(); j++) {
									ElementImpl elStateDefinition = new ElementImpl(document, STATE_DEFINITION);
									State state = (State) stateList.toArray()[j];
									statesCount = buildReference(state, states, ID, elStateDefinition, "s", statesCount);
									buildRepresentation(elStateDefinition, state);
									elStates.appendChild(elStateDefinition);
								}
								elCategoricalCharacter.appendChild(elStates);
								elCharacters.appendChild(elCategoricalCharacter);
							}
						}
					}
					if (character.isSupportsTextData()) {
						ElementImpl elTextCharacter = new ElementImpl(document, TEXT_CHARACTER);
						textcharactersCount = buildReference(character, characters, ID, elTextCharacter, TEXT, textcharactersCount);
						buildRepresentation(elTextCharacter, character);
						elCharacters.appendChild(elTextCharacter);
					}
				}
			}

			dataset.appendChild(elCharacters);
		}

	}

	public void buildCodedDescriptions(ElementImpl dataset) throws ParseException {

		if (cdmSource.getTaxa() != null) {
			ElementImpl elCodedDescriptions = new ElementImpl(document, CODED_DESCRIPTIONS);

			for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa().iterator() ; tb.hasNext() ;){
				Taxon taxon = (Taxon) tb.next();
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (Iterator<TaxonDescription> td = descriptions.iterator() ; td.hasNext() ;){
					TaxonDescription taxonDescription = td.next();
					ElementImpl elCodedDescription = new ElementImpl(document, CODED_DESCRIPTION);
					codedDescriptionsCount = buildReference(taxonDescription, codedDescriptions, ID, elCodedDescription, "D", codedDescriptionsCount);
					buildRepresentation(elCodedDescription, taxonDescription);
					buildScope(elCodedDescription, taxonDescription);
					buildSummaryData(elCodedDescription, taxonDescription);
					elCodedDescriptions.appendChild(elCodedDescription);
				}
			}

			dataset.appendChild(elCodedDescriptions);
		}

	}
	
	/**
	 * Builds Scope associated with a CodedDescription
	 */
	public void buildScope(ElementImpl element, TaxonDescription taxonDescription) throws ParseException {

		//		  <Scope>
		//         <TaxonName ref="t1"/>
		//         <Citation ref="p1" location="p. 30"/>
		//        </Scope>

		ElementImpl scope = new ElementImpl(document, SCOPE);

		Taxon taxon = taxonDescription.getTaxon();
		if (taxon != null) {
			TaxonNameBase taxonNameBase = taxon.getName();
			if (taxonNameBase != null) {
				String ref = taxonNames.get(taxonNameBase);
				if (!ref.equals("")) {
					ElementImpl taxonName = new ElementImpl(document, TAXON_NAME);
					taxonName.setAttribute(REF, ref);
					scope.appendChild(taxonName);
				}
			}
		}

		Set<ReferenceBase> descriptionSources = taxonDescription.getDescriptionSources();
		for (Iterator<ReferenceBase> rb = descriptionSources.iterator() ; rb.hasNext() ;){
			ReferenceBase descriptionSource = rb.next();
			if (descriptionSource.getType().equals(ReferenceType.Article)) {

				ElementImpl citation = new ElementImpl(document, CITATION);
				articlesCount = buildReference(descriptionSource, articles, REF, citation, "p", articlesCount);

				Set<Annotation> annotations = descriptionSource.getAnnotations();
				for (Iterator<Annotation> a = annotations.iterator() ; a.hasNext() ;){
					Annotation annotation = a.next();
					AnnotationType annotationType = annotation.getAnnotationType();
					if (annotationType != null) {
						String type = annotationType.getLabel();
						if (type.equals("location")) {
							citation.setAttribute("location", annotation.getText());
						}
					}
				}

				scope.appendChild(citation);
			}
		}

		element.appendChild(scope);
	}

	/**
	 * Builds SummaryData associated with a CodedDescription
	 */
	public void buildSummaryData(ElementImpl element, TaxonDescription taxonDescription) throws ParseException {

		//			<SummaryData>
		//	          <Categorical ref="c4">
		//	            <State ref="s3"/>
		//	            <State ref="s4"/>
		//	          </Categorical>

		ElementImpl summaryData = new ElementImpl(document, SUMMARY_DATA);
		Set<DescriptionElementBase> elements = taxonDescription.getElements();
		for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){
			DescriptionElementBase descriptionElement = deb.next();
			if (descriptionElement instanceof CategoricalData) {
				CategoricalData categoricalData = (CategoricalData) descriptionElement;
				buildCategorical(summaryData, categoricalData);
			}
			if (descriptionElement instanceof QuantitativeData) {
				QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
				buildQuantitative(summaryData, quantitativeData);
			}
			if (descriptionElement instanceof TextData) {
				TextData textData = (TextData) descriptionElement;
				buildTextChar(summaryData, textData);
			}
		}
		element.appendChild(summaryData);
	}

	/**
	 * Builds Categorical associated with a SummaryData
	 */
	public void buildCategorical(ElementImpl element, CategoricalData categoricalData) throws ParseException {

		//			<SummaryData>
		//	          <Categorical ref="c4">
		//	            <State ref="s3"/>
		//	            <State ref="s4"/>
		//	          </Categorical>

		ElementImpl categorical = new ElementImpl(document, CATEGORICAL);
		Feature feature = categoricalData.getFeature();
		buildReference(feature, characters, REF, categorical, "c", charactersCount);
		List<StateData> states = categoricalData.getStates();
		for (Iterator<StateData> sd = states.iterator() ; sd.hasNext() ;){
			StateData stateData = sd.next();
			State s = stateData.getState();
			buildState(categorical, s);
		}
		element.appendChild(categorical);
	}

	/**
	 * Builds State associated with a Categorical
	 */
	public void buildState(ElementImpl element, State s) throws ParseException {

		//			<SummaryData>
		//	          <Categorical ref="c4">
		//	            <State ref="s3"/>
		//	            <State ref="s4"/>
		//	          </Categorical>

		ElementImpl state = new ElementImpl(document, STATE);
		buildReference(s, states, REF, state, "s", statesCount);
		element.appendChild(state);
	}

	/**
	 * Builds Quantitative associated with a SummaryData
	 */
	public void buildQuantitative(ElementImpl element, QuantitativeData quantitativeData) throws ParseException {

		//		<Quantitative ref="c2">
		//        <Measure type="Min" value="2.3"></Measure>
		//        <Measure type="Mean" value="5.1"/>
		//        <Measure type="Max" value="7.9"/>
		//        <Measure type="SD" value="1.3"/>
		//        <Measure type="N" value="20"/>
		//      </Quantitative>

		ElementImpl quantitative = new ElementImpl(document, QUANTITATIVE);
		Feature feature = quantitativeData.getFeature();
		buildReference(feature, characters, REF, quantitative, "c", charactersCount);
		Set<StatisticalMeasurementValue> statisticalValues = quantitativeData.getStatisticalValues();
		for (Iterator<StatisticalMeasurementValue> smv = statisticalValues.iterator() ; smv.hasNext() ;){
			StatisticalMeasurementValue statisticalValue = smv.next();
			buildMeasure(quantitative, statisticalValue);
		}
		element.appendChild(quantitative);
	}

	/**
	 * Builds Measure associated with a Quantitative XIMCHECK
	 */
	public void buildMeasure(ElementImpl element, StatisticalMeasurementValue statisticalValue) throws ParseException {

		//		<Quantitative ref="c2">
		//        <Measure type="Min" value="2.3"></Measure>
		//        <Measure type="Mean" value="5.1"/>
		//        <Measure type="Max" value="7.9"/>
		//        <Measure type="SD" value="1.3"/>
		//        <Measure type="N" value="20"/>
		//      </Quantitative>

		ElementImpl measure = new ElementImpl(document, MEASURE);
		StatisticalMeasure type = statisticalValue.getType();
		String label = type.getLabel();
		if (label.equals("Average")) {
			measure.setAttribute("type", "Mean");
		} else if (label.equals("StandardDeviation")) {
			measure.setAttribute("type", "SD");
		} else if (label.equals("SampleSize")) {
			measure.setAttribute("type", "N");
		} else {
			measure.setAttribute("type", label);
		}
		float value = statisticalValue.getValue();
		measure.setAttribute("value", String.valueOf(value));
		element.appendChild(measure);
	}

	/**
	 * Builds TextChar associated with a SummaryData
	 */
	public void buildTextChar(ElementImpl element, TextData textData) throws ParseException {

		//			<TextChar ref="c3">
		//            <Content>Free form text</Content>
		//          </TextChar>

		ElementImpl textChar = new ElementImpl(document, TEXT_CHAR);
		Feature feature = textData.getFeature();
		buildReference(feature, characters, REF, textChar, "c", charactersCount);
		Map<Language,LanguageString> multilanguageText = textData.getMultilanguageText();
		for (Iterator<Language> l = multilanguageText.keySet().iterator() ; l.hasNext() ;){
			Language language = l.next();
			LanguageString languageString = multilanguageText.get(language);
			buildContent(textChar,languageString);
		}
		element.appendChild(textChar);
	}

	/**
	 * Builds Content associated with a TextChar
	 */
	public void buildContent(ElementImpl element, LanguageString languageString) throws ParseException {

		//			<TextChar ref="c3">
		//            <Content>Free form text</Content>
		//          </TextChar>

		ElementImpl content = new ElementImpl(document, CONTENT);
		Language language = languageString.getLanguage();
		String text = languageString.getText();
		if (!language.getIso639_1().equals(defaultLanguage.getIso639_1())) {
			content.setAttribute("xml:lang", language.getIso639_1());
		}
		content.setTextContent(text);
		element.appendChild(content);
	}

	/**
	 * Builds an element Agent referring to Agent defined later in the SDD file
	 */
	public void buildRefAgent(ElementImpl element, TeamOrPersonBase ag, String role) throws ParseException {
		if (ag instanceof Person) {
			Person p = (Person) ag;
			ElementImpl agent = new ElementImpl(document, AGENT);
			if (ag.getMarkers()!= null){
				Set<Marker> markers = ag.getMarkers();
				for(Iterator<Marker> m = markers.iterator() ; m.hasNext();){
					Marker marker = m.next();
					if (marker.getMarkerType().getLabel().equals("editor")){
						agent.setAttribute(ROLE, "edt");
					}
				}
			}
			else {
				agent.setAttribute(ROLE, role);
			}
			agentsCount = buildReference(p, agents, REF, agent, "a", agentsCount);
			element.appendChild(agent);
		}

		if (ag instanceof Team) {
			Team team = (Team) ag;
			for (int i = 0; i < team.getTeamMembers().size(); i++) {
				Person author = team.getTeamMembers().get(i);
				ElementImpl agent = new ElementImpl(document, AGENT);
				if (author.getMarkers()!= null){
					Set<Marker> markers = author.getMarkers();
					if (!markers.isEmpty()){
						for(Iterator<Marker> m = markers.iterator() ; m.hasNext();){
							Marker marker = m.next();
							if (marker.getMarkerType().getLabel().equals("editor")){
								agent.setAttribute(ROLE, "edt");
							}
						}
					}
						else {
							agent.setAttribute(ROLE, role);
						}
				}
				else {
					agent.setAttribute(ROLE, role);
				}
				if (author.getSources() != null) {
					IdentifiableSource os = (IdentifiableSource) author.getSources().toArray()[0];
					String id = os.getIdInSource();
					if (id != null) {
						if (!id.equals("")) {
							if (!agents.containsValue(id)) {
								agent.setAttribute(REF, id);
							} else if (!agents.containsValue("a" + (agentsCount+1))) {
								agent.setAttribute(REF, "a" + (agentsCount+1));
								agentsCount++;
							} else {
								agent.setAttribute(REF, id + (agentsCount+1));
								agentsCount++;
							}
						} else {
							agent.setAttribute(REF, "a" + (agentsCount+1));
							agentsCount++;
						}
					} else {
						agent.setAttribute(REF, "a" + (agentsCount+1));
						agentsCount++;
					}
				} else {
					agent.setAttribute(REF, "a" + (agentsCount+1));
					agentsCount++;
				}
				agents.put(author, agent.getAttribute(REF));
				element.appendChild(agent);
			}
		}
	}


	/**
	 * Builds Agents associated with the Dataset
	 */
	public void buildAgents(ElementImpl dataset) throws ParseException {

		if (cdmSource.getAgents() != null) {
			ElementImpl elAgents = new ElementImpl(document, AGENTS);

			for (int i = 0; i < cdmSource.getAgents().size(); i++) {
				ElementImpl elAgent = new ElementImpl(document, AGENT);
				AgentBase personagent = (AgentBase)cdmSource.getAgents().get(i);
				if (personagent  instanceof Person){
					if (personagent.getMarkers()!= null){
						Set<Marker> markers = personagent.getMarkers();
						for(Iterator<Marker> m = markers.iterator() ; m.hasNext();){
							Marker marker = m.next();
							if (marker.getMarkerType().getLabel().equals("editor")){
								agentsCount = buildReference(personagent, agents, ID, elAgent, "a", agentsCount);
							}
						}
					}
					agentsCount = buildReference(personagent, agents, ID, elAgent, "a", agentsCount);
					buildRepresentation(elAgent, personagent);
					elAgents.appendChild(elAgent);
				}
			}

			dataset.appendChild(elAgents);
		}
	}
	
	public void buildCharacterTrees(ElementImpl dataset) throws ParseException {

		if (cdmSource.getFeatureData() != null) {
			ElementImpl elChartrees = new ElementImpl(document, CHARACTER_TREES);

			for (int i = 0; i < cdmSource.getFeatureData().size(); i++) {
				VersionableEntity featu = cdmSource.getFeatureData().get(i);
				if (featu instanceof FeatureTree){
					FeatureTree ft = (FeatureTree) featu;
					ElementImpl elChartree = new ElementImpl(document, CHARACTER_TREE);
					chartreeCount = buildReference(featu, featuretrees, ID, elChartree, "ct", chartreeCount);
					buildRepresentation(elChartree, ft);
					elChartrees.appendChild(elChartree);
					ElementImpl elNodes = new ElementImpl(document, NODES);
					elChartree.appendChild(elNodes);
					List<FeatureNode> roots = ft.getRootChildren();
					for (Iterator<FeatureNode> fn = roots.iterator(); fn.hasNext();) {
						FeatureNode featureNode = fn.next();
						buildBranches(featureNode,elNodes,true);				
					}
				}
			}
			dataset.appendChild(elChartrees);
		}
	}
	
	public void buildTaxonomicTrees(ElementImpl dataset) throws ParseException {

		if (cdmSource.getTaxa() != null) {
			ElementImpl elTaxonHierarchies = new ElementImpl(document, "TaxonHierarchies");
			ElementImpl elTaxonHierarchy = new ElementImpl(document, "TaxonHierarchy");
			for (Iterator<? extends TaxonBase> tb = cdmSource.getTaxa().iterator() ; tb.hasNext() ;){
				Taxon taxon = (Taxon) tb.next();
				if (taxon.getTaxonNodes()!=null){
					for (Iterator<TaxonNode> tn = taxon.getTaxonNodes().iterator() ; tn.hasNext() ;){
						TaxonNode taxonnode = tn.next();
							if (taxonnode.isTopmostNode()) {
								ElementImpl elNode = new ElementImpl(document, "Node");
								taxonNodesCount = buildReference(taxonnode, taxonNodes, ID, elNode, "tn", taxonNodesCount);
								ElementImpl elTaxonName = new ElementImpl(document, TAXON_NAME);
								taxonNamesCount = buildReference(taxonnode.getTaxon().getName(), taxonNames, REF, elTaxonName, "t", taxonNamesCount);
								elNode.appendChild(elTaxonName);
								elTaxonHierarchy.appendChild(elNode);
								if (taxonnode.hasChildNodes()){
									buildTaxonBranches(taxonnode.getChildNodes(),taxonnode, elTaxonHierarchy);
								}
							}
					}
				}
			}
			elTaxonHierarchies.appendChild(elTaxonHierarchy);
			dataset.appendChild(elTaxonHierarchies);
		}
	}

	private void buildTaxonBranches(Set<TaxonNode> children, TaxonNode parent, ElementImpl elTaxonHierarchy){
		if (children != null){
			for (Iterator<TaxonNode> tn = children.iterator() ; tn.hasNext();){
				TaxonNode taxonnode = tn.next();
				ElementImpl elNode = new ElementImpl(document, "Node");
				ElementImpl elParent = new ElementImpl(document, PARENT);
				ElementImpl elTaxonName = new ElementImpl(document, TAXON_NAME);
				if (taxonnode.hasChildNodes()){
					buildTaxonBranches(taxonnode.getChildNodes(),taxonnode, elTaxonHierarchy);
				}
				taxonNodesCount = buildReference(taxonnode, taxonNodes, ID, elNode, "tn", taxonNodesCount);
				taxonNodesCount = buildReference(parent, taxonNodes, REF, elParent, "tn", taxonNodesCount);
				taxonNamesCount = buildReference(taxonnode.getTaxon().getName(), taxonNames, REF, elTaxonName, "t", taxonNamesCount);
				elNode.appendChild(elParent);
				elNode.appendChild(elTaxonName);
				elTaxonHierarchy.appendChild(elNode);
			}
		}
	}
	
	public void buildBranches(FeatureNode parent, ElementImpl element, boolean isRoot) {
		List<FeatureNode> children = parent.getChildren();
		if (!parent.isLeaf()){
			ElementImpl elCharNode = new ElementImpl(document, NODE);
			charnodeCount = buildReference(parent, featuretrees, ID, elCharNode, "cn", charnodeCount);
			FeatureNode grandparent = parent.getParent();
			if ((grandparent !=null)&&(!isRoot))	{
				ElementImpl elParent = new ElementImpl(document, PARENT);
				charnodeCount = buildReference(grandparent, featuretrees, REF, elParent, "cn", charnodeCount);
				elCharNode.appendChild(elParent);
			}
			ElementImpl elDescriptiveConcept = new ElementImpl(document, DESCRIPTIVE_CONCEPT);
			Feature fref = parent.getFeature();
			descriptiveConceptCount = buildReference(fref, descriptiveConcepts, REF, elDescriptiveConcept, "dc", descriptiveConceptCount);
			elCharNode.appendChild(elDescriptiveConcept);
			element.appendChild(elCharNode);
			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				FeatureNode fn = ifn.next();
				buildBranches(fn,element,false);
			}
		}
		else {
			ElementImpl elCharNode = new ElementImpl(document, CHAR_NODE);
			ElementImpl elParent = new ElementImpl(document, PARENT);
			FeatureNode grandparent = parent.getParent();
			charnodeCount = buildReference(grandparent, featuretrees, REF, elParent, "cn", charnodeCount);
			charnodeCount = buildReference(parent, featuretrees, ID, elCharNode, "cn", charnodeCount);
			ElementImpl elCharacter = new ElementImpl(document, CHARACTER);
			Feature fref = parent.getFeature();
			charactersCount = buildReference(fref, characters, REF, elCharacter, "c", charactersCount);
			elCharNode.appendChild(elCharacter);
			elCharNode.appendChild(elParent);
			element.appendChild(elCharNode);
		}
	}

	public void buildMediaObjects(ElementImpl dataset) throws ParseException {

		if (cdmSource.getMedia() != null) {
			ElementImpl elMediaObjects = new ElementImpl(document, MEDIA_OBJECTS);

			for (int i = 0; i < cdmSource.getMedia().size(); i++) {
				ElementImpl elMediaObject = new ElementImpl(document, MEDIA_OBJECT);
				Media mediobj = (Media) cdmSource.getMedia().get(i);
				mediasCount = buildReference(mediobj, medias, ID, elMediaObject, "t", mediasCount);
				buildRepresentation(elMediaObject, mediobj);
				Set<MediaRepresentation> smr = mediobj.getRepresentations();
				for (Iterator<MediaRepresentation> mr = smr.iterator() ; mr.hasNext();){
					MediaRepresentation mediarep = mr.next();
					ElementImpl elType = new ElementImpl(document, "Type");
					elType.appendChild(document.createTextNode(mediarep.getMimeType()));
					elMediaObject.appendChild(elType);
					List<MediaRepresentationPart> lmrp = mediarep.getParts();
					for (Iterator<MediaRepresentationPart> mrp = lmrp.iterator();mrp.hasNext();){
						MediaRepresentationPart mediareppart = mrp.next();
						ElementImpl elSource = new ElementImpl(document, "Source");
						elSource.setAttribute("href",mediareppart.getUri());
						elMediaObject.appendChild(elSource);
					}
				}
				elMediaObjects.appendChild(elMediaObject);
			}
			dataset.appendChild(elMediaObjects);
		}
	}

	public void buildPublications(ElementImpl dataset) throws ParseException {

		if (cdmSource.getReferences() != null) {
			ElementImpl elPublications = new ElementImpl(document, PUBLICATIONS);
			boolean editorial = false;
			for (int i = 0; i < cdmSource.getReferences().size(); i++) {
				ElementImpl elPublication = new ElementImpl(document, "Publication");
				ReferenceBase publication = cdmSource.getReferences().get(i);
				Set<Annotation> annotations = publication.getAnnotations();
				for (Iterator<Annotation> a = annotations.iterator() ; a.hasNext() ;){
					Annotation annotation = a.next();
					AnnotationType annotationType = annotation.getAnnotationType();
					if (annotationType.equals(AnnotationType.EDITORIAL())) {
						editorial = true;
					}
					else {
						editorial = false;
					}
				}
				if (!editorial){
					articlesCount = buildReference(publication, articles, ID, elPublication, "p", articlesCount);
					buildRepresentation(elPublication, (IDatabase) publication);
					elPublications.appendChild(elPublication);
				}
			}
			dataset.appendChild(elPublications);
		}
	}

	public int buildReference(VersionableEntity ve, Map references, String refOrId, ElementImpl element, String prefix, int count) throws ParseException {
		if (references.containsKey(ve)) {
			element.setAttribute(refOrId,(String) references.get(ve));
		} else {
			if (ve instanceof IdentifiableEntity) {
				IdentifiableEntity ie = (IdentifiableEntity) ve;
				if (ie.getSources().size() > 0) {
					IdentifiableSource os = (IdentifiableSource) ie.getSources().toArray()[0];
					String id = os.getIdInSource();
					String uri = os.getCitationMicroReference();
					if (uri != null) {element.setAttribute(URI, uri);}
					if (id != null) {
						if (!id.equals("")) {
							if (!references.containsValue(id)) {
								element.setAttribute(refOrId, id);
							} else while (element.getAttribute(refOrId).equals("")) {
								if (!references.containsValue(prefix + (count+1))) {
									element.setAttribute(refOrId, prefix + (count+1));
								}
								count++;
							}
						} else while (element.getAttribute(refOrId).equals("")) {
							if (!references.containsValue(prefix + (count+1))) {
								element.setAttribute(refOrId, prefix + (count+1));
							}
							count++;
						}
					} else while (element.getAttribute(refOrId).equals("")) {
						if (!references.containsValue(prefix + (count+1))) {
							element.setAttribute(refOrId, prefix + (count+1));
						}
						count++;
					}
				} else while (element.getAttribute(refOrId).equals("")) {
					if (!references.containsValue(prefix + (count+1))) {
						element.setAttribute(refOrId, prefix + (count+1));
					}
					count++;
				}
			} else while (element.getAttribute(refOrId).equals("")) {
				if (!references.containsValue(prefix + (count+1))) {
					element.setAttribute(refOrId, prefix + (count+1));
				}
				count++;
			}
			references.put(ve, element.getAttribute(refOrId));
		}
		return count;
	}

	public void buildGeographicAreas(ElementImpl dataset) {
		if (cdmSource.getTerms() != null) {
			ElementImpl elGeographicAreas = new ElementImpl(document, "GeographicAreas");

			int f = cdmSource.getTerms().size();
			for (int i = 0; i < f; i++) {
				if (cdmSource.getTerms().get(i) instanceof NamedArea) {
					NamedArea na = (NamedArea) cdmSource.getTerms().get(i);
					for (Iterator<Marker> mark = na.getMarkers().iterator() ; mark.hasNext();) {
						Marker marker = mark.next();
						if (marker.getMarkerType().getLabel().equals("SDDGeographicArea")) {
							ElementImpl elGeographicArea = new ElementImpl(document, "GeographicArea");
							namedAreasCount = buildReference(na, namedAreas, ID, elGeographicArea, "a", namedAreasCount);
							buildRepresentation(elGeographicArea,na);
							elGeographicAreas.appendChild(elGeographicArea);
						}
					}

				}
			}
			dataset.appendChild(elGeographicAreas);
		}
	}
	
	public void buildSpecimens(ElementImpl dataset) throws ParseException {

		if (cdmSource.getOccurrences() != null) {
			ElementImpl elSpecimens = new ElementImpl(document, "Specimens");

			for (int i = 0; i < cdmSource.getOccurrences().size(); i++) {
				ElementImpl elSpecimen = new ElementImpl(document, "Specimen");
				SpecimenOrObservationBase sob = cdmSource.getOccurrences().get(i);
				if (sob instanceof Specimen){
					specimenCount = buildReference(sob, specimens, ID, elSpecimen, "s", specimenCount);
					buildRepresentation(elSpecimen, sob);
					elSpecimens.appendChild(elSpecimen);
				}
			}
			dataset.appendChild(elSpecimens);
		}

	}

	
}