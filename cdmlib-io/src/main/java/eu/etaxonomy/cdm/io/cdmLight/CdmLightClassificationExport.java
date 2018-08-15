/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmLight;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ExportResult.ExportResultState;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HomotypicalGroupNameComparator;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeComparator;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author k.luther
 * @since 15.03.2017
 */
@Component
public class CdmLightClassificationExport
            extends CdmExportBase<CdmLightExportConfigurator, CdmLightExportState, IExportTransformer, File>
            implements ICdmExport<CdmLightExportConfigurator, CdmLightExportState>{


    private static final long serialVersionUID = 2518643632756927053L;
    private static final String STD_TEAM_CONCATINATION = ", ";
    private static final String FINAL_TEAM_CONCATINATION = " & ";


    private static final String IPNI_NAME_IDENTIFIER = "Ipni Name Identifier";
    private static final String TROPICOS_NAME_IDENTIFIER = "Tropicos Name Identifier";
    private static final String WFO_NAME_IDENTIFIER = "WFO Name Identifier";

    public CdmLightClassificationExport() {
        super();
        this.ioName = this.getClass().getSimpleName();

    }

    @Override
    public long countSteps(CdmLightExportState state) {
        TaxonNodeFilter filter = state.getConfig().getTaxonNodeFilter();
        return taxonNodeService.count(filter);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(CdmLightExportState state) {
        try {

            IProgressMonitor monitor = state.getConfig().getProgressMonitor();
            CdmLightExportConfigurator config = state.getConfig();
            config.setFieldsTerminatedBy(",");

//            if (config.getTaxonNodeFilter().getTaxonNodesFilter().isEmpty() && config.getTaxonNodeFilter().getClassificationFilter().isEmpty()){
//                //TODO
//                state.setEmptyData();
//                return;
//            }



//            for (LogicFilter<Classification> classificationFilter : config.getTaxonNodeFilter().getClassificationFilter()){
//                UUID classificationUuid = classificationFilter.getUuid();
//                Classification classification = getClassificationService().find(classificationUuid);
//                if (classification == null){
//                    String message = String.format("Classification for given classification UUID not found. No data imported for %s", classificationUuid.toString());
//                    state.getResult().addWarning(message);
//                }else{
//                    TaxonNode root = classification.getRootNode();
//                    UUID uuid = root.getUuid();
//                    root = getTaxonNodeService().load(uuid);
//                    handleSingleClassification(state, root.getUuid());
//                }
//            }


            @SuppressWarnings("unchecked")
            TaxonNodeOutStreamPartitioner<XmlExportState> partitioner
                  = TaxonNodeOutStreamPartitioner.NewInstance(
                          this, state, state.getConfig().getTaxonNodeFilter(),
                          100, monitor, null);


                monitor.subTask("Start partitioning");

                TaxonNode node = partitioner.next();
                while (node != null){
                    handleTaxonNode(state, node);
                    node = partitioner.next();
                }


//            for (LogicFilter<TaxonNode> taxonNodeFilter : config.getTaxonNodeFilter().getTaxonNodesFilter()){
//                UUID nodeUuid = taxonNodeFilter.getUuid();
//                handleSingleClassification(state, nodeUuid);
//            }
            state.getProcessor().createFinalResult(state);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred in main method doInvoke() " +
                    e.getMessage());
        }
    }

    /**
     * @param state
     * @param classificationUuid
     */
    private void handleTaxonNode(CdmLightExportState state, TaxonNode taxonNode) {

            if (taxonNode == null){
                String message = "TaxonNode for given taxon node UUID not found. ";
                //TODO
                state.getResult().addWarning(message);
            }else{
                try {
                    TaxonNode root = taxonNode;
                    if (root.hasTaxon()){
                        handleTaxon(state, root);
                    }else{
    //                    for (TaxonNode child : root.getChildNodes()){
    //                        handleTaxon(state, child);
    //                        //TODO progress monitor
    //                    }
                    }
                } catch (Exception e) {
                    state.getResult().addException(e, "An unexpected error occurred when handling classification " +
                            taxonNode.getUuid() + ": " + e.getMessage() + e.getStackTrace());
                }
            }
    }

    /**
     * @param state
     * @param taxon
     */
    private void handleTaxon(CdmLightExportState state, TaxonNode taxonNode) {
        try{
      //  Taxon taxon = taxonNode.getTaxon();
        if (taxonNode == null){
            state.getResult().addError ("The taxonNode was null.", "handleTaxon");
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
            return;
        }
        if (taxonNode.getTaxon() == null){
            state.getResult().addError ("There was a taxon node without a taxon: " + taxonNode.getUuid(), "handleTaxon");
            state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
        }else{
            Taxon taxon = HibernateProxyHelper.deproxy(taxonNode.getTaxon(), Taxon.class);

             try{
                TaxonName name = taxon.getName();
                handleName(state, name);
                for (Synonym syn : taxon.getSynonyms()){
                    handleSynonym(state, syn);
                }
                for (TaxonRelationship rel : taxon.getProParteAndPartialSynonymRelations()){
                    handleProPartePartialMisapplied(state, rel);
                }
                for (TaxonRelationship rel : taxon.getMisappliedNameRelations()){
                    handleProPartePartialMisapplied(state, rel);
                }

                CdmLightExportTable table = CdmLightExportTable.TAXON;
                String[] csvLine = new String[table.getSize()];

                csvLine[table.getIndex(CdmLightExportTable.TAXON_ID)] = getId(state, taxon);
                csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, name);
                Taxon parent = (taxonNode.getParent()==null) ? null : taxonNode.getParent().getTaxon();
                csvLine[table.getIndex(CdmLightExportTable.PARENT_FK)] = getId(state, parent);
                csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE_FK)] = getId(state, taxon.getSec());
                if (taxon.getSec()!= null && taxon.getSec().getDatePublished() != null && taxon.getSec().getDatePublished().getFreeText() != null){
                    String sec_string = taxon.getSec().getTitleCache() + ". " + taxon.getSec().getDatePublished().getFreeText();
                    sec_string = sec_string.replace("..", ".");
                    csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE)] = sec_string;
                }else{
                    csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE)] = getTitleCache(taxon.getSec());
                }
                if (state.getReferenceFromStore(taxon.getSec().getId()) == null){
                    handleReference(state, taxon.getSec());
                }
                csvLine[table.getIndex(CdmLightExportTable.CLASSIFICATION_ID)] = getId(state, taxonNode.getClassification());
                csvLine[table.getIndex(CdmLightExportTable.CLASSIFICATION_TITLE)] = taxonNode.getClassification().getTitleCache();

                state.getProcessor().put(table, taxon, csvLine);
                handleDescriptions(state, taxon);
             }catch(Exception e){
                 state.getResult().addException (e, "An unexpected problem occurred when trying to export "
                         + "taxon with id " + taxon.getId());
                 state.getResult().setState(ExportResultState.INCOMPLETE_WITH_ERROR);
             }
       }

       taxonNode.removeNullValueFromChildren();
//       for (TaxonNode child: taxonNode.getChildNodes()){
//           handleTaxon(state, child);
//       }
        }catch (Exception e){
            state.getResult().addException(e, "An unexpected error occurred when handling the taxon node of " +
                    cdmBaseStr(taxonNode.getTaxon()) + ": " + e.getMessage());
        }
    }


    /**
     * @param state
     * @param taxon
     */
    private void handleDescriptions(CdmLightExportState state, CdmBase cdmBase) {
        try{
        if (cdmBase instanceof Taxon){
            Taxon taxon = HibernateProxyHelper.deproxy(cdmBase, Taxon.class);
            Set<TaxonDescription> descriptions = taxon.getDescriptions();
            List<DescriptionElementBase> simpleFacts = new ArrayList<>();
            List<DescriptionElementBase> specimenFacts = new ArrayList<>();
            List<DescriptionElementBase> distributionFacts = new ArrayList<>();
            List<DescriptionElementBase> commonNameFacts = new ArrayList<>();
            List<DescriptionElementBase> usageFacts = new ArrayList<>();
            for (TaxonDescription description: descriptions){
                if (description.getElements() != null){
                    for (DescriptionElementBase element: description.getElements()){
                        element = CdmBase.deproxy(element);
                        if (element.getFeature().equals(Feature.COMMON_NAME())){
                            commonNameFacts.add(element);
                        }else if (element.getFeature().equals(Feature.DISTRIBUTION())){
                            distributionFacts.add(element);
                        }else if (element instanceof IndividualsAssociation || isSpecimenFeature(element.getFeature())){
                            specimenFacts.add(element);
                        }else{
                            simpleFacts.add(element);
                        }
                    }
                }
            }
            if (!commonNameFacts.isEmpty()){
                handleCommonNameFacts(state, taxon, commonNameFacts);
            }
            if (!distributionFacts.isEmpty()){
                handleDistributionFacts(state, taxon, distributionFacts);
            }
            if (!specimenFacts.isEmpty()){
                handleSpecimenFacts(state, taxon, specimenFacts);
            }
            if (!simpleFacts.isEmpty()){
                handleSimpleFacts(state, taxon, simpleFacts);
            }
        } else if (cdmBase instanceof TaxonName){
            TaxonName name = CdmBase.deproxy(cdmBase, TaxonName.class);
            Set<TaxonNameDescription> descriptions = name.getDescriptions();
            List<DescriptionElementBase> simpleFacts = new ArrayList<>();
            for (TaxonNameDescription description: descriptions){
                if (description.getElements() != null){
                    for (DescriptionElementBase element: description.getElements()){
                        if (!element.getFeature().equals(Feature.PROTOLOGUE())){
                            simpleFacts.add(element);
                        }
                    }
                }
             }
            if (!simpleFacts.isEmpty()){
                handleSimpleFacts(state, name, simpleFacts);
            }
        }
        }catch (Exception e){
            state.getResult().addException(e, "An unexpected error occurred when handling description of" +
                    cdmBaseStr(cdmBase) + ": " + e.getMessage());
        }
    }


    /**
     * @param feature
     * @return
     */
    private boolean isSpecimenFeature(Feature feature) {
        //TODO allow user defined specimen features
        if (feature == null){
            return false;
        }else if (feature.isSupportsIndividualAssociation()){
            return true;
        }else{
            return feature.equals(Feature.SPECIMEN()) || feature.equals(Feature.INDIVIDUALS_ASSOCIATION())
                    || feature.equals(Feature.MATERIALS_EXAMINED()) || feature.equals(Feature.OBSERVATION())
                    || feature.equals(Feature.OCCURRENCE())
                     ;
        }
    }

    /**
     * @param state
     * @param taxon
     * @param simpleFacts
     */
    private void handleSimpleFacts(CdmLightExportState state, CdmBase cdmBase,
            List<DescriptionElementBase> simpleFacts) {
        try {
            CdmLightExportTable table = CdmLightExportTable.SIMPLE_FACT;
            CdmLightExportTable tableMedia = CdmLightExportTable.MEDIA;
            for (DescriptionElementBase element: simpleFacts){
                if (element.getModifyingText().isEmpty() && !element.getMedia().isEmpty()){
                    handleSimpleMediaFact(state, cdmBase, tableMedia, element);
                }else{
                    handleSingleSimpleFact(state, cdmBase, table, element);
                }
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling simple facts for " +
                    cdmBaseStr(cdmBase) + ": " + e.getMessage());
        }
    }

    /**
     * @param state
     * @param cdmBase
     * @param tableMedia
     * @param element
     */
    private void handleSimpleMediaFact(CdmLightExportState state, CdmBase cdmBase, CdmLightExportTable table,
            DescriptionElementBase element) {
        try {
            String[] csvLine;
            handleSource(state, element, CdmLightExportTable.MEDIA);

            if (element instanceof TextData){
               TextData textData = (TextData)element;
               csvLine = new String[table.getSize()];
               csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
               if (cdmBase instanceof Taxon){
                   csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, cdmBase);
                   csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = "";
               }else if (cdmBase instanceof TaxonName){
                   csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = "";
                   csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, cdmBase);
               }


               String mediaUris = "";
               for (Media media: textData.getMedia()){
                   String mediaString = extractMediaUris(media.getRepresentations().iterator());
                   if (!StringUtils.isBlank(mediaString)){
                       mediaUris +=  mediaString + ";";
                   }
                   else{
                       state.getResult().addWarning("Empty Media object for "
                               + cdmBase.getUserFriendlyTypeName() + " " + cdmBase.getUuid()
                               + " (media: " + media.getUuid() + ")");
                   }
               }
               csvLine[table.getIndex(CdmLightExportTable.MEDIA_URI)] = mediaUris;

            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single simple fact " +
                    cdmBaseStr(element) + ": " + e.getMessage());
        }

    }

    /**
     * @param state
     * @param cdmBase
     * @param table
     * @param element
     */
    private void handleSingleSimpleFact(CdmLightExportState state, CdmBase cdmBase, CdmLightExportTable table,
            DescriptionElementBase element) {
        try {
            String[] csvLine;
            handleSource(state, element, CdmLightExportTable.SIMPLE_FACT);

            if (element instanceof TextData){
               TextData textData = (TextData)element;
               csvLine = new String[table.getSize()];
               csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
               if (cdmBase instanceof Taxon){
                   csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, cdmBase);
                   csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = "";
               }else if (cdmBase instanceof TaxonName){
                   csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = "";
                   csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, cdmBase);
               }
               csvLine[table.getIndex(CdmLightExportTable.FACT_CATEGORY)] = textData.getFeature().getLabel();

               String mediaUris = "";
               for (Media media: textData.getMedia()){
                   String mediaString = extractMediaUris(media.getRepresentations().iterator());
                   if (!StringUtils.isBlank(mediaString)){
                       mediaUris +=  mediaString + ";";
                   }
                   else{
                       state.getResult().addWarning("Empty Media object for uuid: " +
                               cdmBase.getUuid() + " uuid of media: " + media.getUuid());
                   }
               }
               csvLine[table.getIndex(CdmLightExportTable.MEDIA_URI)] = mediaUris;
               if (textData.getFeature().equals(Feature.CITATION())){
                  // csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, cdmBase);
                   state.getProcessor().put(table, textData, csvLine);
               }else if (!textData.getMultilanguageText().isEmpty()){
                   for (Language language: textData.getMultilanguageText().keySet()){
                       String[] csvLineLanguage = csvLine.clone();
                       LanguageString langString = textData.getLanguageText(language);
                       String text = langString.getText();
                       if (state.getConfig().isFilterIntextReferences()){
                           text = filterIntextReferences(langString.getText());
                       }
                       csvLineLanguage[table.getIndex(CdmLightExportTable.FACT_TEXT)] = text;
                       csvLineLanguage[table.getIndex(CdmLightExportTable.LANGUAGE)] = language.getLabel();
                       state.getProcessor().put(table, textData, csvLineLanguage);
                   }
               } else{
                   state.getProcessor().put(table, textData, csvLine);
               }
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single simple fact " +
                    cdmBaseStr(element) + ": " + e.getMessage());
        }
    }


    /**
     * @param text
     * @return
     */
    private String filterIntextReferences(String text) {
        /*
         * (<cdm:reference cdmId='fbd19251-efee-4ded-b780-915000f66d41' intextId='1352d42c-e201-4155-a02a-55360d3b563e'>Ridley in Fl. Malay Pen. 3 (1924) 22</cdm:reference>)
         */

       String newText = text.replaceAll("<cdm:reference cdmId='[a-z0-9\\-]*' intextId='[a-z0-9\\-]*'>","");
       newText = newText.replaceAll("</cdm:reference>","");

       newText = newText.replaceAll("<cdm:key cdmId='[a-z0-9\\-]*' intextId='[a-z0-9\\-]*'>","");
       newText = newText.replaceAll("</cdm:key>","");
       return newText;
    }

    /**
     * @param state
     * @param specimenFacts
     */
    private void handleSpecimenFacts(CdmLightExportState state, Taxon taxon, List<DescriptionElementBase> specimenFacts) {
        CdmLightExportTable table = CdmLightExportTable.SPECIMEN_FACT;

        for (DescriptionElementBase element: specimenFacts){
            try {
                String[] csvLine = new String[table.getSize()];
                csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, taxon);
                handleSource(state, element, table);
                csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_NOTES)] = createAnnotationsString(element.getAnnotations());

                if (element instanceof IndividualsAssociation){

                    IndividualsAssociation indAssociation = (IndividualsAssociation)element;
                    if (indAssociation.getAssociatedSpecimenOrObservation() == null){
                        state.getResult().addWarning("There is an individual association with no specimen associated (Taxon "+ taxon.getTitleCache() + "(" + taxon.getUuid() +"). Could not be exported.");
                        continue;
                    }else{
                        if (state.getSpecimenFromStore(indAssociation.getAssociatedSpecimenOrObservation().getId()) == null){
                            SpecimenOrObservationBase<?> specimenBase = HibernateProxyHelper.deproxy(indAssociation.getAssociatedSpecimenOrObservation());

                            if (specimenBase instanceof SpecimenOrObservationBase){
                                SpecimenOrObservationBase derivedUnit = specimenBase;
                                handleSpecimen(state, derivedUnit);
                                csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_FK)] = getId(state, indAssociation.getAssociatedSpecimenOrObservation());
                            }else{
                                //field units are not supported
                                state.getResult().addError("The associated Specimen of taxon " + taxon.getUuid() + " is not an DerivedUnit. Could not be exported.");

                            }
                        }
                    }
                } else if (element instanceof TextData){
                    TextData textData = HibernateProxyHelper.deproxy(element, TextData.class);
                    csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_DESCRIPTION)] = createMultilanguageString(textData.getMultilanguageText());
                }
                state.getProcessor().put(table, element, csvLine);
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single specimen fact " +
                        cdmBaseStr(element) + ": " + e.getMessage());
            }
        }
    }

    /**
     * @param multilanguageText
     * @return
     */
    private String createMultilanguageString(Map<Language, LanguageString> multilanguageText) {
       String text = "";
       int index = multilanguageText.size();
       for(LanguageString langString: multilanguageText.values()){
           text += langString.getText();
           if (index > 1){
               text += "; ";
           }
           index --;
       }

        return text;
    }

    /**
     * @param annotations
     * @return
     */
    private String createAnnotationsString(Set<Annotation> annotations) {
        StringBuffer strBuff = new StringBuffer();

        for (Annotation ann:annotations){
            if (ann.getAnnotationType() == null ||!ann.getAnnotationType().equals(AnnotationType.TECHNICAL())){
                strBuff.append(ann.getText());
                strBuff.append("; ");
            }
        }

        if (strBuff.length() > 2){
            return strBuff.substring(0, strBuff.length()-2);
        }else{
            return null;
        }
    }

    /**
     * @param state
     * @param taxon
     * @param element
     */
    private void handleSource(CdmLightExportState state, DescriptionElementBase element, CdmLightExportTable factsTable) {
        CdmLightExportTable table = CdmLightExportTable.FACT_SOURCES;
        try {
        Set<DescriptionElementSource> sources = element.getSources();

        for (DescriptionElementSource source: sources){

                String[] csvLine = new  String[table.getSize()];
                Reference ref = source.getCitation();
                if ((ref == null) && (source.getNameUsedInSource() == null)){
                    continue;
                }
                if (ref != null){
                    if (state.getReferenceFromStore(ref.getId()) == null){
                        handleReference(state, ref);

                    }
                    csvLine[table.getIndex(CdmLightExportTable.REFERENCE_FK)] = getId(state, ref);
                }
                csvLine[table.getIndex(CdmLightExportTable.FACT_FK)] = getId(state, element);

                csvLine[table.getIndex(CdmLightExportTable.NAME_IN_SOURCE_FK)] = getId(state, source.getNameUsedInSource());
                csvLine[table.getIndex(CdmLightExportTable.FACT_TYPE)] = factsTable.getTableName();
                if ( StringUtils.isBlank(csvLine[table.getIndex(CdmLightExportTable.REFERENCE_FK)])  && StringUtils.isBlank(csvLine[table.getIndex(CdmLightExportTable.NAME_IN_SOURCE_FK)])){
                    continue;
                }
                state.getProcessor().put(table, source, csvLine);

        }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling single source " +
                    cdmBaseStr(element) + ": " + e.getMessage());
        }

    }

    /**
     * @param state
     * @param distributionFacts
     */
    private void handleDistributionFacts(CdmLightExportState state, Taxon taxon, List<DescriptionElementBase> distributionFacts) {
        CdmLightExportTable table = CdmLightExportTable.GEOGRAPHIC_AREA_FACT;

        for (DescriptionElementBase element: distributionFacts){
            try {
                if (element instanceof Distribution){
                    String[] csvLine = new  String[table.getSize()];
                    Distribution distribution = (Distribution)element;
                    csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                    handleSource(state, element, table);
                    csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, taxon);
                    if (distribution.getArea() != null){
                        csvLine[table.getIndex(CdmLightExportTable.AREA_LABEL)] = distribution.getArea().getLabel();
                    }
                    if (distribution.getStatus() != null){
                        csvLine[table.getIndex(CdmLightExportTable.STATUS_LABEL)] = distribution.getStatus().getLabel();
                    }
                    state.getProcessor().put(table, distribution, csvLine);
                } else{
                    state.getResult().addError("The distribution description for the taxon " + taxon.getUuid() + " is not of type distribution. Could not be exported. UUID of the description element: " + element.getUuid());
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single distribution " +
                        cdmBaseStr(element) + ": " + e.getMessage());
            }
        }
    }

    /**
     * @param state
     * @param commonNameFacts
     */
    private void handleCommonNameFacts(CdmLightExportState state, Taxon taxon, List<DescriptionElementBase> commonNameFacts) {
        CdmLightExportTable table = CdmLightExportTable.COMMON_NAME_FACT;

        for (DescriptionElementBase element: commonNameFacts){
            try {
                if (element instanceof CommonTaxonName){
                    String[] csvLine = new  String[table.getSize()];
                    CommonTaxonName commonName = (CommonTaxonName)element;
                    csvLine[table.getIndex(CdmLightExportTable.FACT_ID)] = getId(state, element);
                    handleSource(state, element, table);
                    csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, taxon);
                    if (commonName.getName() != null){csvLine[table.getIndex(CdmLightExportTable.FACT_TEXT)] = commonName.getName();}
                    if (commonName.getLanguage() != null){csvLine[table.getIndex(CdmLightExportTable.LANGUAGE)] = commonName.getLanguage().getLabel();}
                    if (commonName.getArea() != null){ csvLine[table.getIndex(CdmLightExportTable.AREA_LABEL)] = commonName.getArea().getLabel();}
                    state.getProcessor().put(table, commonName, csvLine);
                } else{
                    state.getResult().addError("The distribution description for the taxon " + taxon.getUuid() + " is not of type distribution. Could not be exported. UUID of the description element: " + element.getUuid());
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when handling single common name " +
                        cdmBaseStr(element) + ": " + e.getMessage());
            }
        }
    }

    /**
     * @param sec
     * @return
     */
    private String getTitleCache(IIdentifiableEntity identEntity) {
        if (identEntity == null){
            return "";
        }
        //TODO refresh?
        return identEntity.getTitleCache();
    }

    /**
     * @param state
     * @param taxon
     * @return
     */
    private String getId(CdmLightExportState state, ICdmBase cdmBase) {
        if (cdmBase == null){
            return "";
        }
        //TODO make configurable
        return cdmBase.getUuid().toString();
    }

    /**
     * @param state
     * @param synonym
     */
    private void handleSynonym(CdmLightExportState state, Synonym synonym) {
       try {
           if (isUnpublished(state.getConfig(), synonym)){
               return;
           }
           TaxonName name = synonym.getName();
           handleName(state, name);

           CdmLightExportTable table = CdmLightExportTable.SYNONYM;
           String[] csvLine = new String[table.getSize()];

           csvLine[table.getIndex(CdmLightExportTable.SYNONYM_ID)] = getId(state, synonym);
           csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, synonym.getAcceptedTaxon());
           csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, name);
           csvLine[table.getIndex(CdmLightExportTable.SYN_SEC_REFERENCE_FK)] = getId(state, synonym.getSec());
           csvLine[table.getIndex(CdmLightExportTable.SYN_SEC_REFERENCE)] = getTitleCache(synonym.getSec());

           state.getProcessor().put(table, synonym, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling synonym " +
                    cdmBaseStr(synonym) + ": " + e.getMessage());
        }
    }


    /**
     * Handles Misapplied names (including pro parte and partial as well as
     * pro parte and partial synonyms
     * @param state
     * @param rel
     */
    private void handleProPartePartialMisapplied(CdmLightExportState state, TaxonRelationship rel) {
        try {
            Taxon ppSyonym = rel.getFromTaxon();
            if (isUnpublished(state.getConfig(), ppSyonym)){
                return;
            }
            TaxonName name = ppSyonym.getName();
            handleName(state, name);

            CdmLightExportTable table = CdmLightExportTable.SYNONYM;
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(CdmLightExportTable.SYNONYM_ID)] = getId(state, rel);
            csvLine[table.getIndex(CdmLightExportTable.TAXON_FK)] = getId(state, rel.getToTaxon());
            csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId(state, name);

            Reference secRef = ppSyonym.getSec();
            csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE_FK)] = getId(state, secRef);
            csvLine[table.getIndex(CdmLightExportTable.SEC_REFERENCE)] = getTitleCache(secRef);
            Reference synSecRef = rel.getCitation();
            csvLine[table.getIndex(CdmLightExportTable.SYN_SEC_REFERENCE_FK)] = getId(state, synSecRef);
            csvLine[table.getIndex(CdmLightExportTable.SYN_SEC_REFERENCE)] = getTitleCache(synSecRef);

            //pro parte type
            TaxonRelationshipType type = rel.getType();
            csvLine[table.getIndex(CdmLightExportTable.IS_PRO_PARTE)] = type.isProParte()? "1":"0";
            csvLine[table.getIndex(CdmLightExportTable.IS_PARTIAL)] = type.isPartial()? "1":"0";
            csvLine[table.getIndex(CdmLightExportTable.IS_MISAPPLIED)] = type.isAnyMisappliedName()? "1":"0";

            state.getProcessor().put(table, ppSyonym, csvLine);
         } catch (Exception e) {
             state.getResult().addException(e, "An unexpected error occurred when handling "
                     + "pro parte/partial synonym relationship " +
                     cdmBaseStr(rel) + ": " + e.getMessage());
         }

    }


    /**
     * @param state
     * @param name
     */
    private void handleName(CdmLightExportState state, TaxonName name) {
        if (name == null){
            return;
        }
        try {
            Rank rank = name.getRank();
            CdmLightExportTable table = CdmLightExportTable.SCIENTIFIC_NAME;
            name = HibernateProxyHelper.deproxy(name);
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(CdmLightExportTable.NAME_ID)] = getId(state, name);
            if (name.getLsid() != null){
                csvLine[table.getIndex(CdmLightExportTable.LSID)] = name.getLsid().getLsid();
            }else{
                csvLine[table.getIndex(CdmLightExportTable.LSID)] = "";
            }

            handleIdentifier(state, name);
            handleDescriptions(state, name);

            csvLine[table.getIndex(CdmLightExportTable.RANK)] = getTitleCache(rank);
            if (rank != null){
                csvLine[table.getIndex(CdmLightExportTable.RANK_SEQUENCE)] = String.valueOf(rank.getOrderIndex());
                if (rank.isInfraGeneric()){
                    try {
                        csvLine[table.getIndex(CdmLightExportTable.INFRAGENERIC_RANK)] = name.getRank().getInfraGenericMarker();
                    } catch (UnknownCdmTypeException e) {
                        state.getResult().addError("Infrageneric marker expected but not available for rank " + name.getRank().getTitleCache());
                    }
                }
                if (rank.isInfraSpecific()){
                    csvLine[table.getIndex(CdmLightExportTable.INFRASPECIFIC_RANK)] = name.getRank().getAbbreviation();
                }
            }else{
                csvLine[table.getIndex(CdmLightExportTable.RANK_SEQUENCE)] = "";
            }
            if (name.isProtectedTitleCache()){
                csvLine[table.getIndex(CdmLightExportTable.FULL_NAME_WITH_AUTHORS)] =name.getTitleCache();
            }else{
                //TODO: adapt the tropicos titlecache creation
                csvLine[table.getIndex(CdmLightExportTable.FULL_NAME_WITH_AUTHORS)] = name.getTitleCache();
            }
            csvLine[table.getIndex(CdmLightExportTable.FULL_NAME_NO_AUTHORS)] = name.getNameCache();
            csvLine[table.getIndex(CdmLightExportTable.GENUS_UNINOMIAL)] = name.getGenusOrUninomial();

            csvLine[table.getIndex(CdmLightExportTable.INFRAGENERIC_EPITHET)] = name.getInfraGenericEpithet();
            csvLine[table.getIndex(CdmLightExportTable.SPECIFIC_EPITHET)] = name.getSpecificEpithet();

            csvLine[table.getIndex(CdmLightExportTable.INFRASPECIFIC_EPITHET)] = name.getInfraSpecificEpithet();
            csvLine[table.getIndex(CdmLightExportTable.BAS_AUTHORTEAM_FK)] = getId(state,name.getBasionymAuthorship());
            if (name.getBasionymAuthorship() != null){
                if (state.getAuthorFromStore(name.getBasionymAuthorship().getId()) == null) {
                    handleAuthor(state, name.getBasionymAuthorship());
                }
            }
            csvLine[table.getIndex(CdmLightExportTable.BAS_EX_AUTHORTEAM_FK)] = getId(state, name.getExBasionymAuthorship());
            if (name.getExBasionymAuthorship() != null){
                if (state.getAuthorFromStore(name.getExBasionymAuthorship().getId()) == null) {
                    handleAuthor(state, name.getExBasionymAuthorship());
                }

            }
            csvLine[table.getIndex(CdmLightExportTable.COMB_AUTHORTEAM_FK)] = getId(state,name.getCombinationAuthorship());
            if (name.getCombinationAuthorship() != null){
                if (state.getAuthorFromStore(name.getCombinationAuthorship().getId()) == null) {
                    handleAuthor(state, name.getCombinationAuthorship());
                }
            }
            csvLine[table.getIndex(CdmLightExportTable.COMB_EX_AUTHORTEAM_FK)] = getId(state, name.getExCombinationAuthorship());
            if (name.getExCombinationAuthorship() != null){
                if (state.getAuthorFromStore(name.getExCombinationAuthorship().getId()) == null) {
                    handleAuthor(state, name.getExCombinationAuthorship());
                }

            }

            csvLine[table.getIndex(CdmLightExportTable.AUTHOR_TEAM_STRING)] = name.getAuthorshipCache();

            Reference nomRef = name.getNomenclaturalReference();

            if (nomRef != null){
                if (state.getReferenceFromStore(nomRef.getId()) == null){
                    handleReference(state, nomRef);
                }
                csvLine[table.getIndex(CdmLightExportTable.REFERENCE_FK)] = getId(state, nomRef);
                csvLine[table.getIndex(CdmLightExportTable.PUBLICATION_TYPE)] = nomRef.getType().name();
                if (nomRef.getVolume() != null){
                    csvLine[table.getIndex(CdmLightExportTable.VOLUME_ISSUE)] = nomRef.getVolume();
                    csvLine[table.getIndex(CdmLightExportTable.COLLATION)] = createCollatation(name);
                }
                if (nomRef.getDatePublished() != null){
                    csvLine[table.getIndex(CdmLightExportTable.DATE_PUBLISHED)] = nomRef.getTimePeriodPublishedString();
                    csvLine[table.getIndex(CdmLightExportTable.YEAR_PUBLISHED)] = nomRef.getDatePublished().getYear();
                    csvLine[table.getIndex(CdmLightExportTable.VERBATIM_DATE)] = nomRef.getDatePublished().getVerbatimDate();
                }
                if (name.getNomenclaturalMicroReference() != null){
                    csvLine[table.getIndex(CdmLightExportTable.DETAIL)] = name.getNomenclaturalMicroReference();
                }
                nomRef = HibernateProxyHelper.deproxy(nomRef);
                if (nomRef.getInReference() != null){
                    Reference inReference = nomRef.getInReference();
                    if (inReference.getDatePublished() != null && nomRef.getDatePublished() == null){
                        csvLine[table.getIndex(CdmLightExportTable.DATE_PUBLISHED)] = inReference.getDatePublishedString();
                        csvLine[table.getIndex(CdmLightExportTable.YEAR_PUBLISHED)] = inReference.getDatePublished().getYear();
                    }
                    if (nomRef.getVolume() == null && inReference.getVolume() != null){
                        csvLine[table.getIndex(CdmLightExportTable.VOLUME_ISSUE)] = inReference.getVolume();
                        csvLine[table.getIndex(CdmLightExportTable.COLLATION)] = createCollatation(name);
                    }
                    if (inReference.getInReference() != null){
                        inReference = inReference.getInReference();
                    }
                    if (inReference.getAbbrevTitle() == null){
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_TITLE)] = CdmUtils.Nz(inReference.getAbbrevTitleCache());
                    }else{
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_TITLE)] = CdmUtils.Nz(inReference.getAbbrevTitle());
                    }
                    if (inReference.getTitle() == null){
                        csvLine[table.getIndex(CdmLightExportTable.FULL_TITLE)] = CdmUtils.Nz(inReference.getTitleCache());
                    }else{
                        csvLine[table.getIndex(CdmLightExportTable.FULL_TITLE)] = CdmUtils.Nz(inReference.getTitle());
                    }


                    TeamOrPersonBase<?> author = inReference.getAuthorship();
                    if (author != null && (nomRef.isOfType(ReferenceType.BookSection) || nomRef.isOfType(ReferenceType.Section))){
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_AUTHOR)] = CdmUtils.Nz(author.getNomenclaturalTitle());
                        csvLine[table.getIndex(CdmLightExportTable.FULL_REF_AUTHOR)] = CdmUtils.Nz(author.getTitleCache());
                    }else{
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_AUTHOR)] = "";
                        csvLine[table.getIndex(CdmLightExportTable.FULL_REF_AUTHOR)] = "";
                    }
                }else{
                    if (nomRef.getAbbrevTitle() == null){
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_TITLE)] = CdmUtils.Nz(nomRef.getAbbrevTitleCache());
                    }else{
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_TITLE)] = CdmUtils.Nz(nomRef.getAbbrevTitle());
                    }
                    if (nomRef.getTitle() == null){
                        csvLine[table.getIndex(CdmLightExportTable.FULL_TITLE)] = CdmUtils.Nz(nomRef.getTitleCache());
                    }else{
                        csvLine[table.getIndex(CdmLightExportTable.FULL_TITLE)] = CdmUtils.Nz(nomRef.getTitle());
                    }
                    TeamOrPersonBase<?> author = nomRef.getAuthorship();
                    if (author != null ){
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_AUTHOR)] = CdmUtils.Nz(author.getNomenclaturalTitle());
                        csvLine[table.getIndex(CdmLightExportTable.FULL_REF_AUTHOR)] = CdmUtils.Nz(author.getTitleCache());
                    }else{
                        csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_AUTHOR)] = "";
                        csvLine[table.getIndex(CdmLightExportTable.FULL_REF_AUTHOR)] = "";
                    }

                }
            }else{
                csvLine[table.getIndex(CdmLightExportTable.PUBLICATION_TYPE)] = "";
            }



            /*
            * Collation

            Detail


            TitlePageYear
            */
            Set<TaxonNameDescription> descriptions = name.getDescriptions();
            String protologueUriString = extractURIs(state, descriptions, Feature.PROTOLOGUE());

            csvLine[table.getIndex(CdmLightExportTable.PROTOLOGUE_URI)] = protologueUriString;

            if (name.getStatus() == null || name.getStatus().isEmpty()){
                csvLine[table.getIndex(CdmLightExportTable.NOM_STATUS)] = "";
                csvLine[table.getIndex(CdmLightExportTable.NOM_STATUS_ABBREV)] = "";
            }else{

                String statusStringAbbrev = extractStatusString(state, name, true);
                String statusString = extractStatusString(state, name, false);

                csvLine[table.getIndex(CdmLightExportTable.NOM_STATUS)] = statusString.trim();
                csvLine[table.getIndex(CdmLightExportTable.NOM_STATUS_ABBREV)] = statusStringAbbrev.trim();
            }

            HomotypicalGroup group =name.getHomotypicalGroup();

            if (state.getHomotypicalGroupFromStore(group.getId()) == null){
                handleHomotypicalGroup(state, group);
            }
            csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_FK)] = getId(state, group);
            List<TaxonName> typifiedNames = new ArrayList<>();
            typifiedNames.addAll(group.getTypifiedNames());
            Collections.sort(typifiedNames, new HomotypicalGroupNameComparator(null, true));
            Integer  seqNumber= typifiedNames.indexOf(name);
            csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_SEQ)] = String.valueOf(seqNumber);
            state.getProcessor().put(table, name, csvLine);

            /*
             *
            Tropicos_ID
            IPNI_ID


            InfragenericRank


            InfraspecificRank
            Collation
            Volume (Issue)
            Detail
            DatePublished
            YearPublished
            TitlePageYear



            HomotypicGroupSequenceNumber


             *
             */
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling synonym " +
                    cdmBaseStr(name) + ": " + e.getMessage());
        }
    }

    /**
     * @return
     */
    private String createCollatation(TaxonName name) {
        String collation = "";
        if (name.getNomenclaturalReference() != null){
            Reference ref = name.getNomenclaturalReference();
            collation = getVolume(ref);
        }
        if (name.getNomenclaturalMicroReference() != null){
            if (!StringUtils.isBlank(collation)){
                collation += ":";
            }
            collation +=name.getNomenclaturalMicroReference();
        }

        return collation;
    }

    /**
     * @param nomenclaturalReference
     * @return
     */
    private String getVolume(Reference reference) {
        if (reference.getVolume() != null){
            return reference.getVolume();
        }else if (reference.getInReference() != null){
            if (reference.getInReference().getVolume() != null){
                return reference.getInReference().getVolume();
            }
        }
        return null;
    }

    /**
     * @param state
     * @param name
     */
    private void handleIdentifier(CdmLightExportState state, TaxonName name) {
        CdmLightExportTable table = CdmLightExportTable.IDENTIFIER;
        String[] csvLine;
        try {
            Set<String>  IPNIidentifiers = name.getIdentifiers(DefinedTerm.IDENTIFIER_NAME_IPNI());
            Set<String>  tropicosIdentifiers = name.getIdentifiers(DefinedTerm.IDENTIFIER_NAME_TROPICOS());
            Set<String>  WFOIdentifiers = name.getIdentifiers(DefinedTerm.uuidWfoNameIdentifier);
            if (!IPNIidentifiers.isEmpty()){
                csvLine = new String[table.getSize()];
                csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId( state, name);
                csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = IPNI_NAME_IDENTIFIER;
                csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_IDS)] = extractIdentifier(IPNIidentifiers);
                state.getProcessor().put(table, name, csvLine);
            }
            if (!tropicosIdentifiers.isEmpty()){
                csvLine = new String[table.getSize()];
                csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId( state, name);
                csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = TROPICOS_NAME_IDENTIFIER;
                csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_IDS)] = extractIdentifier(tropicosIdentifiers);
                state.getProcessor().put(table, name, csvLine);
            }
            if (!WFOIdentifiers.isEmpty()){
                csvLine = new String[table.getSize()];
                csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = getId( state, name);
                csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_TYPE)] = WFO_NAME_IDENTIFIER;
                csvLine[table.getIndex(CdmLightExportTable.IDENTIFIER_IDS)] = extractIdentifier(WFOIdentifiers);
                state.getProcessor().put(table, name, csvLine);
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling identifiers for " +
                    cdmBaseStr(name) + ": " + e.getMessage());

        }
    }

    /**
     * @param tropicosIdentifiers
     */
    private String extractIdentifier(Set<String> identifierSet) {

        String identifierString = "";
        for (String identifier: identifierSet){
            if (!StringUtils.isBlank(identifierString)){
                identifierString += ", ";
            }
            identifierString += identifier;
        }

        return identifierString;
    }

    /**
     * @param state
     * @param descriptions
     * @return
     */
    private String extractURIs(CdmLightExportState state,
            Set<? extends DescriptionBase<?>> descriptionsSet, Feature feature) {
        String mediaUriString = "";
        SpecimenDescription specimenDescription;
        TaxonDescription taxonDescription;
        TaxonNameDescription nameDescription;
        Set<DescriptionElementBase> elements = new HashSet<>();
        for (DescriptionBase<?> description : descriptionsSet){
            try {
                if (!description.getElements().isEmpty()){
                    if (description instanceof SpecimenDescription){
                        specimenDescription = (SpecimenDescription)description;
                        elements = specimenDescription.getElements();
                    }else if (description instanceof TaxonDescription){
                        taxonDescription = (TaxonDescription) description;
                        elements = taxonDescription.getElements();
                    } else if (description instanceof TaxonNameDescription){
                        nameDescription = (TaxonNameDescription) description;
                        elements = nameDescription.getElements();
                    }

                    for (DescriptionElementBase element : elements){
                        Feature entityFeature = HibernateProxyHelper.deproxy(element.getFeature());
                        if (entityFeature.equals(feature)){
                            if (!element.getMedia().isEmpty()){
                                List<Media> media = element.getMedia();
                                for (Media mediaElement: media){
                                    Iterator<MediaRepresentation> it =  mediaElement.getRepresentations().iterator();
                                    mediaUriString = extractMediaUris(it);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                state.getResult().addException(e, "An unexpected error occurred when extracting media URIs for " +
                        cdmBaseStr(description) + ": " + e.getMessage());
            }
        }
        return mediaUriString;
    }

    /**
     * @param state
     * @param basionymAuthorship
     */
    private void handleAuthor(CdmLightExportState state, TeamOrPersonBase<?> author) {
        try {
            if (state.getAuthorFromStore(author.getId()) != null){
                return;
            }
            state.addAuthorToStore(author);
            CdmLightExportTable table = CdmLightExportTable.NOMENCLATURAL_AUTHOR;
            String[] csvLine = new String[table.getSize()];
            CdmLightExportTable tableAuthorRel = CdmLightExportTable.NOMENCLATURAL_AUTHOR_TEAM_RELATION;
            String[] csvLineRel = new String[tableAuthorRel.getSize()];
            String[] csvLineMember = new String[table.getSize()];
            csvLine[table.getIndex(CdmLightExportTable.AUTHOR_ID)] = getId(state, author);
            csvLine[table.getIndex(CdmLightExportTable.ABBREV_AUTHOR)] = author.getNomenclaturalTitle();
            csvLine[table.getIndex(CdmLightExportTable.AUTHOR_TITLE)] = author.getTitleCache();
            author = HibernateProxyHelper.deproxy(author);
            if (author instanceof Person){
                Person authorPerson = (Person)author;
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_GIVEN_NAME)] = authorPerson.getGivenName();
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_FAMILY_NAME)] = authorPerson.getFamilyName();
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_PREFIX)] = authorPerson.getPrefix();
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_SUFFIX)] = authorPerson.getSuffix();
            } else{
                // create an entry in rel table and all members in author table, check whether the team members already in author table

                Team authorTeam = (Team)author;
                int index = 0;
                for (Person member: authorTeam.getTeamMembers()){
                    csvLineRel = new String[tableAuthorRel.getSize()];
                    csvLineRel[tableAuthorRel.getIndex(CdmLightExportTable.AUTHOR_TEAM_FK)] = getId(state, authorTeam);
                    csvLineRel[tableAuthorRel.getIndex(CdmLightExportTable.AUTHOR_FK)] = getId(state, member);
                    csvLineRel[tableAuthorRel.getIndex(CdmLightExportTable.AUTHOR_TEAM_SEQ_NUMBER)] = String.valueOf(index);
                    state.getProcessor().put(tableAuthorRel, authorTeam.getId() +":" +member.getId(), csvLineRel);

                    if (state.getAuthorFromStore(member.getId()) == null){
                        state.addAuthorToStore(member);
                        csvLineMember = new String[table.getSize()];
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_ID)] = getId(state, member);
                        csvLineMember[table.getIndex(CdmLightExportTable.ABBREV_AUTHOR)] = member.getNomenclaturalTitle();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_TITLE)] = member.getTitleCache();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_GIVEN_NAME)] = member.getGivenName();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_FAMILY_NAME)] = member.getFamilyName();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_PREFIX)] = member.getPrefix();
                        csvLineMember[table.getIndex(CdmLightExportTable.AUTHOR_SUFFIX)] = member.getSuffix();
                        state.getProcessor().put(table, member, csvLineMember);
                    }
                    index++;

                }
            }
            state.getProcessor().put(table, author, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling author " +
                    cdmBaseStr(author) + ": " + e.getMessage());
        }
    }

    /**
     * @param state
     * @param name
     * @param statusString
     * @return
     */
    private String extractStatusString(CdmLightExportState state, TaxonName name, boolean abbrev) {
        try {
            Set<NomenclaturalStatus> status = name.getStatus();
            if (status.isEmpty()){
                return "";
            }
            String statusString = "";
            for (NomenclaturalStatus nameStatus: status){
                if (nameStatus != null){
                    if (abbrev){
                        if (nameStatus.getType() != null){
                            statusString += nameStatus.getType().getIdInVocabulary();
                        }
                    }else{
                        if (nameStatus.getType() != null){
                            statusString += nameStatus.getType().getTitleCache();
                        }
                    }
                    if (!abbrev){

                        if (nameStatus.getRuleConsidered() != null && !StringUtils.isBlank(nameStatus.getRuleConsidered())){
                            statusString += " " + nameStatus.getRuleConsidered();
                        }
                        if (nameStatus.getCitation() != null){
                            statusString += " " + nameStatus.getCitation().getTitleCache();
                        }
                        if (nameStatus.getCitationMicroReference() != null && !StringUtils.isBlank(nameStatus.getCitationMicroReference())){
                            statusString += " " + nameStatus.getCitationMicroReference();
                        }
                    }
                    statusString += " ";
                }
            }
            return statusString;
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when extracting status string for " +
                    cdmBaseStr(name) + ": " + e.getMessage());
            return "";
        }
    }

    /**
     * @param group
     */
    private void handleHomotypicalGroup(CdmLightExportState state, HomotypicalGroup group) {
        try {
            state.addHomotypicalGroupToStore(group);
            CdmLightExportTable table = CdmLightExportTable.HOMOTYPIC_GROUP;
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_ID)] = getId(state, group);
            List<TaxonName> typifiedNames = new ArrayList<>();
            typifiedNames.addAll(group.getTypifiedNames());
            Collections.sort(typifiedNames, new HomotypicalGroupNameComparator(null, true));
            String typifiedNamesString = "";
            for (TaxonName name: typifiedNames){
                //Concatenated output string for homotypic group (names and citations) + status + some name relations (e.g. “non”)
                //TODO: nameRelations, which and how to display


                typifiedNamesString += name.getTitleCache()+ extractStatusString(state, name, true) + "; ";
            }
            typifiedNamesString = typifiedNamesString.substring(0, typifiedNamesString.length()-2);
            if (typifiedNamesString != null){
                csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_STRING)] = typifiedNamesString.trim();
            }else{
                csvLine[table.getIndex(CdmLightExportTable.HOMOTYPIC_GROUP_STRING)] = "";
            }
            Set<TypeDesignationBase> typeDesigantions = group.getTypeDesignations();
            List<TypeDesignationBase> designationList = new ArrayList<>();
            designationList.addAll(typeDesigantions);
            Collections.sort(designationList, new TypeComparator());
            StringBuffer typeDesignationString = new StringBuffer();
            for (TypeDesignationBase typeDesignation: typeDesigantions){
                if (typeDesignation != null && typeDesignation.getTypeStatus() != null){
                    typeDesignationString.append(typeDesignation.getTypeStatus().getTitleCache() + ": ");
                }
                if (typeDesignation instanceof SpecimenTypeDesignation){
                    if (((SpecimenTypeDesignation)typeDesignation).getTypeSpecimen() != null){
                        typeDesignationString.append(((SpecimenTypeDesignation)typeDesignation).getTypeSpecimen().getTitleCache());
                        handleSpecimen(state, ((SpecimenTypeDesignation)typeDesignation).getTypeSpecimen());
                    }
                }else{
                    if (((NameTypeDesignation)typeDesignation).getTypeName() != null){
                        typeDesignationString.append(((NameTypeDesignation)typeDesignation).getTypeName().getTitleCache());
                    }
                }
                if(typeDesignation.getCitation() != null ){
                    typeDesignationString.append(", "+typeDesignation.getCitation().getTitleCache());
                }
                //TODO...
                /*
                 * Sortierung:
                1.  Status der Typen: a) holo, lecto, neo, syn, b) epi, paralecto, c) para (wenn überhaupt) – die jeweiligen iso immer direct mit dazu
                2.  Land
                3.  Sammler
                4.  Nummer

                Aufbau der Typusinformationen:
                Land: Lokalität mit Höhe und Koordinaten; Datum; Sammler Nummer (Herbar/Barcode, Typusart; Herbar/Barcode, Typusart …)

                 */
            }
            String typeDesignations = typeDesignationString.toString();
            if (typeDesignations != null){
                csvLine[table.getIndex(CdmLightExportTable.TYPE_STRING)] = typeDesignations;
            }else{
                csvLine[table.getIndex(CdmLightExportTable.TYPE_STRING)] = "";
            }
            state.getProcessor().put(table, String.valueOf(group.getId()), csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling homotypic group " +
                    cdmBaseStr(group) + ": " + e.getMessage());
        }
    }

    /**
     * @param name
     * @return
     */
    private String getTropicosTitleCache(CdmLightExportState state, TaxonName name) {
        try {
            String basionymStart = "(";
            String basionymEnd = ") ";
            String exAuthorSeperator = " ex ";
            TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
            TeamOrPersonBase<?> exCombinationAuthor = name.getExCombinationAuthorship();
            TeamOrPersonBase<?> basionymAuthor = name.getBasionymAuthorship();
            TeamOrPersonBase<?> exBasionymAuthor = name.getExBasionymAuthorship();

            String combinationAuthorString = "";
            if (combinationAuthor != null){
                combinationAuthor = HibernateProxyHelper.deproxy(combinationAuthor);
                if (combinationAuthor instanceof Team){
                    combinationAuthorString = createTropicosTeamTitle(combinationAuthor);
                }else{
                    Person person = HibernateProxyHelper.deproxy(combinationAuthor, Person.class);
                    combinationAuthorString = createTropicosAuthorString(person);
                }
            }
            String exCombinationAuthorString = "";
            if (exCombinationAuthor != null){
                exCombinationAuthor = HibernateProxyHelper.deproxy(exCombinationAuthor);
                if (exCombinationAuthor instanceof Team){
                   exCombinationAuthorString = createTropicosTeamTitle(exCombinationAuthor);
                }else{
                    Person person = HibernateProxyHelper.deproxy(exCombinationAuthor, Person.class);
                    exCombinationAuthorString = createTropicosAuthorString(person);
                }
            }

            String basionymAuthorString = "";
            if (basionymAuthor != null){
                basionymAuthor = HibernateProxyHelper.deproxy(basionymAuthor);
                if (basionymAuthor instanceof Team){
                    basionymAuthorString =  createTropicosTeamTitle(basionymAuthor);
                }else{
                    Person person = HibernateProxyHelper.deproxy(basionymAuthor, Person.class);
                    basionymAuthorString = createTropicosAuthorString(person);
                }
            }

            String exBasionymAuthorString = "";

            if (exBasionymAuthor != null){
                exBasionymAuthor = HibernateProxyHelper.deproxy(exBasionymAuthor);
                if (exBasionymAuthor instanceof Team){
                    exBasionymAuthorString = createTropicosTeamTitle(exBasionymAuthor);

                }else{
                    Person person = HibernateProxyHelper.deproxy(exBasionymAuthor, Person.class);
                    exBasionymAuthorString = createTropicosAuthorString(person);
                }
            }
            String completeAuthorString =  name.getNameCache() + " ";

            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString) || !CdmUtils.isBlank(basionymAuthorString)) ? basionymStart: "";
            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString)) ? (CdmUtils.Nz(exBasionymAuthorString) + exAuthorSeperator): "" ;
            completeAuthorString += (!CdmUtils.isBlank(basionymAuthorString))? CdmUtils.Nz(basionymAuthorString):"";
            completeAuthorString += (!CdmUtils.isBlank(exBasionymAuthorString) || !CdmUtils.isBlank(basionymAuthorString)) ?  basionymEnd:"";
            completeAuthorString += (!CdmUtils.isBlank(exCombinationAuthorString)) ? (CdmUtils.Nz(exCombinationAuthorString) + exAuthorSeperator): "" ;
            completeAuthorString += (!CdmUtils.isBlank(combinationAuthorString))? CdmUtils.Nz(combinationAuthorString):"";


            return completeAuthorString;
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling tropicos title cache for " +
                    cdmBaseStr(name) + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * @param combinationAuthor
     * @return
     */
    private String createTropicosTeamTitle(TeamOrPersonBase<?> combinationAuthor) {
        String combinationAuthorString;
        Team team = HibernateProxyHelper.deproxy(combinationAuthor, Team.class);
        Team tempTeam = Team.NewInstance();
        for (Person teamMember:team.getTeamMembers()){
            combinationAuthorString = createTropicosAuthorString(teamMember);
            Person tempPerson = Person.NewTitledInstance(combinationAuthorString);
            tempTeam.addTeamMember(tempPerson);
        }
        combinationAuthorString = tempTeam.generateTitle();
        return combinationAuthorString;
    }

    /**
     * @param teamMember
     */
    private String createTropicosAuthorString(Person teamMember) {
        String nomAuthorString = "";
        String[] splittedAuthorString = null;
        if (teamMember == null){
            return nomAuthorString;
        }

        if (teamMember.getGivenName() != null){
            String givenNameString = teamMember.getGivenName().replaceAll("\\.", "\\. ");
            splittedAuthorString = givenNameString.split("\\s");
            for (String split: splittedAuthorString){
                if (!StringUtils.isBlank(split)){
                    nomAuthorString += split.substring(0, 1);
                    nomAuthorString += ".";
                }
            }
        }
        if (teamMember.getFamilyName() != null){
            String familyNameString = teamMember.getFamilyName().replaceAll("\\.", "\\. ");
            splittedAuthorString = familyNameString.split("\\s");
            for (String split: splittedAuthorString){
                nomAuthorString += " " +split;
            }
        }
        if (StringUtils.isBlank(nomAuthorString.trim())){
            if (teamMember.getTitleCache() != null) {
                String titleCacheString = teamMember.getTitleCache().replaceAll("\\.", "\\. ");
                splittedAuthorString = titleCacheString.split("\\s");
            }


            int index = 0;
            for (String split: splittedAuthorString){
                if ( index < splittedAuthorString.length-1 && (split.length()==1 || split.endsWith("."))){
                    nomAuthorString += split;
                }else{
                    nomAuthorString = nomAuthorString +" "+ split;
                }
                index++;
            }
        }
        return nomAuthorString.trim();
    }

    /**
     * @param state
     * @param name
     */
    private void handleReference(CdmLightExportState state, Reference reference) {
        try {
            state.addReferenceToStore(reference);
            CdmLightExportTable table = CdmLightExportTable.REFERENCE;

            String[] csvLine = new String[table.getSize()];
            csvLine[table.getIndex(CdmLightExportTable.REFERENCE_ID)] = getId(state, reference);
            //TODO short citations correctly
            String shortCitation = createShortCitation(reference);  //Should be Author(year) like in Taxon.sec
            csvLine[table.getIndex(CdmLightExportTable.BIBLIO_SHORT_CITATION)] = shortCitation;
            //TODO get preferred title
            csvLine[table.getIndex(CdmLightExportTable.REF_TITLE)] = reference.getTitle();
            csvLine[table.getIndex(CdmLightExportTable.ABBREV_REF_TITLE)] = reference.getAbbrevTitle();
            csvLine[table.getIndex(CdmLightExportTable.DATE_PUBLISHED)] = reference.getDatePublishedString();
            //TBC
            csvLine[table.getIndex(CdmLightExportTable.EDITION)] = reference.getEdition();
            csvLine[table.getIndex(CdmLightExportTable.EDITOR)] = reference.getEditor();
            csvLine[table.getIndex(CdmLightExportTable.ISBN)] = reference.getIsbn();
            csvLine[table.getIndex(CdmLightExportTable.ISSN)] = reference.getIssn();
            csvLine[table.getIndex(CdmLightExportTable.ORGANISATION)] = reference.getOrganization();
            csvLine[table.getIndex(CdmLightExportTable.PAGES)] = reference.getPages();
            csvLine[table.getIndex(CdmLightExportTable.PLACE_PUBLISHED)] = reference.getPlacePublished();
            csvLine[table.getIndex(CdmLightExportTable.PUBLISHER)] = reference.getPublisher();
            csvLine[table.getIndex(CdmLightExportTable.REF_ABSTRACT)] = reference.getReferenceAbstract();
            csvLine[table.getIndex(CdmLightExportTable.SERIES_PART)] = reference.getSeriesPart();
            csvLine[table.getIndex(CdmLightExportTable.VOLUME)] = reference.getVolume();
            csvLine[table.getIndex(CdmLightExportTable.YEAR)] = reference.getYear();

            if ( reference.getAuthorship() != null){
                csvLine[table.getIndex(CdmLightExportTable.AUTHORSHIP_TITLE)] = createFullAuthorship(reference);
                csvLine[table.getIndex(CdmLightExportTable.AUTHOR_FK)] = getId(state,reference.getAuthorship());
            }

            csvLine[table.getIndex(CdmLightExportTable.IN_REFERENCE)] = getId(state, reference.getInReference());
            if (reference.getInReference() != null && state.getReferenceFromStore(reference.getInReference().getId()) == null){
                handleReference(state, reference.getInReference());
            }
            if ( reference.getInstitution() != null){ csvLine[table.getIndex(CdmLightExportTable.INSTITUTION)] = reference.getInstitution().getTitleCache();}
            if ( reference.getLsid() != null){ csvLine[table.getIndex(CdmLightExportTable.LSID)] = reference.getLsid().getLsid();}
            if ( reference.getSchool() != null){ csvLine[table.getIndex(CdmLightExportTable.SCHOOL)] = reference.getSchool().getTitleCache();}
            if ( reference.getUri() != null){ csvLine[table.getIndex(CdmLightExportTable.URI)] = reference.getUri().toString();}
            csvLine[table.getIndex(CdmLightExportTable.REF_TYPE)] = reference.getType().getKey();

            state.getProcessor().put(table, reference, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling reference " +
                    cdmBaseStr(reference) + ": " + e.getMessage());
        }

    }


    /**
     * @param reference
     * @return
     */
    private String createShortCitation(Reference reference) {
        TeamOrPersonBase<?> authorship = reference.getAuthorship();
        String shortCitation = "";
        if (authorship == null) {
            return null;
        }
        authorship = HibernateProxyHelper.deproxy(authorship);
        if (authorship instanceof Person){
            shortCitation = ((Person)authorship).getFamilyName();
            if (StringUtils.isBlank(shortCitation) ){
                shortCitation = ((Person)authorship).getTitleCache();
            }
        }
        else if (authorship instanceof Team){

            Team authorTeam = HibernateProxyHelper.deproxy(authorship, Team.class);
            int index = 0;

            for (Person teamMember : authorTeam.getTeamMembers()){
                index++;
                if (index == 3){
                    shortCitation += " & al.";
                    break;
                }
                String concat = concatString(authorTeam, authorTeam.getTeamMembers(), index);
                if (teamMember.getFamilyName() != null){
                    shortCitation += concat + teamMember.getFamilyName();
                }else{
                    shortCitation += concat + teamMember.getTitleCache();
                }

            }
            if (StringUtils.isBlank(shortCitation)){
                shortCitation = authorTeam.getTitleCache();
            }

        }
        if (!StringUtils.isBlank(reference.getDatePublished().getFreeText())){
            shortCitation = shortCitation + " (" + reference.getDatePublished().getFreeText() + ")";
        }else if (!StringUtils.isBlank(reference.getYear()) ){
            shortCitation = shortCitation + " (" + reference.getYear() + ")";
        }
        return shortCitation;
    }

    /**
     * @param reference
     * @return
     */
    private String createFullAuthorship(Reference reference) {
        TeamOrPersonBase<?> authorship = reference.getAuthorship();
        String fullAuthorship = "";
        if (authorship == null) {
            return null;
        }
        authorship = HibernateProxyHelper.deproxy(authorship);
        if (authorship instanceof Person){
            fullAuthorship = ((Person)authorship).getTitleCache();

        }
        else if (authorship instanceof Team){

            Team authorTeam = HibernateProxyHelper.deproxy(authorship, Team.class);
            int index = 0;

            for (Person teamMember : authorTeam.getTeamMembers()){
                index++;
                String concat = concatString(authorTeam, authorTeam.getTeamMembers(), index);
                fullAuthorship += concat + teamMember.getTitleCache();
            }

        }

        return fullAuthorship;
    }

    private static String concatString(Team team, List<Person> teamMembers, int i) {
        String concat;
        if (i <= 1){
            concat = "";
        }else if (i < teamMembers.size() || ( team.isHasMoreMembers() && i == teamMembers.size())){
            concat = STD_TEAM_CONCATINATION;
        }else{
            concat = FINAL_TEAM_CONCATINATION;
        }
        return concat;
    }

    /*
     * TypeDesignation table
     * Specimen_Fk
     *  EditName_Fk
     *   TypeVerbatimCitation
     *   TypeCategory
     *   TypeDesignatedByString
     *   TypeDesignatedByRef_Fk
     */

    private void handleSpecimenTypeDesignations(CdmLightExportState state, TaxonName name){
       try {
           Set<SpecimenTypeDesignation> typeDesignations = name.getSpecimenTypeDesignations();
           CdmLightExportTable table = CdmLightExportTable.TYPE_DESIGNATION;
           String nameId = getId(state, name);
           String[] csvLine = new String[table.getSize()];
            for (SpecimenTypeDesignation specimenType: typeDesignations){
                csvLine = new String[table.getSize()];
                DerivedUnit specimen = specimenType.getTypeSpecimen();
                if (state.getSpecimenFromStore(specimen.getId()) == null){
                    handleSpecimen(state, specimen);
                }
                csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_FK)] = getId(state, specimenType.getTypeSpecimen());
                csvLine[table.getIndex(CdmLightExportTable.NAME_FK)] = nameId;
                csvLine[table.getIndex(CdmLightExportTable.TYPE_VERBATIM_CITATION)] = specimenType.getTypeSpecimen().generateTitle();
                //TODO: add link to existing Vorcabulary
                csvLine[table.getIndex(CdmLightExportTable.TYPE_CATEGORY)] = "";
                csvLine[table.getIndex(CdmLightExportTable.TYPE_DESIGNATED_BY_STRING)] = specimenType.getCitation().getTitleCache();
                csvLine[table.getIndex(CdmLightExportTable.TYPE_DESIGNATED_BY_REF_FK)] = getId(state, specimenType.getCitation());
            }
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling specimen type designations for " +
                    cdmBaseStr(name) + ": " + e.getMessage());
        }
    }

    /**
     * @param state
     * @param specimen
     */
    private void handleSpecimen(CdmLightExportState state, SpecimenOrObservationBase specimen) {
        try {
            state.addSpecimenToStore(specimen);
            CdmLightExportTable table = CdmLightExportTable.SPECIMEN;
            String specimenId = getId(state, specimen);
            String[] csvLine = new String[table.getSize()];

            csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_ID)] = specimenId;
            csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_CITATION)] = specimen.getTitleCache();
            csvLine[table.getIndex(CdmLightExportTable.SPECIMEN_IMAGE_URIS)] = extractURIs(state, specimen.getDescriptions(), Feature.IMAGE());
            if (specimen instanceof DerivedUnit){
                    DerivedUnit derivedUnit = (DerivedUnit)specimen;
                    if (derivedUnit.getCollection() != null){ csvLine[table.getIndex(CdmLightExportTable.HERBARIUM_ABBREV)] = derivedUnit.getCollection().getCode();}

                if (specimen instanceof MediaSpecimen){
                    MediaSpecimen mediaSpecimen = (MediaSpecimen) specimen;
                    Iterator<MediaRepresentation> it = mediaSpecimen.getMediaSpecimen().getRepresentations().iterator();
                    String mediaUris = extractMediaUris(it);
                    csvLine[table.getIndex(CdmLightExportTable.MEDIA_SPECIMEN_URL)] = mediaUris;

                }

                if (derivedUnit.getDerivedFrom() != null){
                    for (SpecimenOrObservationBase<?> original: derivedUnit.getDerivedFrom().getOriginals()){
                        //TODO: What to do if there are more then one FieldUnit??
                        if (original instanceof FieldUnit){
                            FieldUnit fieldUnit = (FieldUnit)original;
                            csvLine[table.getIndex(CdmLightExportTable.COLLECTOR_NUMBER)] = fieldUnit.getFieldNumber();

                            GatheringEvent gathering = fieldUnit.getGatheringEvent();
                            if (gathering != null){
                                if (gathering.getLocality() != null){ csvLine[table.getIndex(CdmLightExportTable.LOCALITY)] = gathering.getLocality().getText();}
                                if (gathering.getCountry() != null){csvLine[table.getIndex(CdmLightExportTable.COUNTRY)] = gathering.getCountry().getLabel();}
                                csvLine[table.getIndex(CdmLightExportTable.COLLECTOR_STRING)] = createCollectorString(state, gathering, fieldUnit);
                                addCollectingAreas(state, gathering);
                                if (gathering.getGatheringDate() != null){csvLine[table.getIndex(CdmLightExportTable.COLLECTION_DATE)] = gathering.getGatheringDate().toString();}
                                if (!gathering.getCollectingAreas().isEmpty()){
                                    int index = 0;
                                    csvLine[table.getIndex(CdmLightExportTable.FURTHER_AREAS)] = "0";
                                    for (NamedArea area: gathering.getCollectingAreas()){
                                        if (index == 0){
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_CATEGORY1)] = area.getTermType().getKey();
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_NAME1)] = area.getLabel();
                                        }
                                        if (index == 1){
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_CATEGORY2)] = area.getTermType().getKey();
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_NAME2)] = area.getLabel();
                                        }
                                        if (index == 2){
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_CATEGORY3)] = area.getTermType().getKey();
                                            csvLine[table.getIndex(CdmLightExportTable.AREA_NAME3)] = area.getLabel();
                                        }
                                        if (index == 3){
                                            csvLine[table.getIndex(CdmLightExportTable.FURTHER_AREAS)] = "1";
                                            break;
                                        }
                                        index++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            state.getProcessor().put(table, specimen, csvLine);
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when handling specimen " +
                    cdmBaseStr(specimen) + ": " + e.getMessage());
        }
    }

    /**
     * @param it
     */
    private String extractMediaUris(Iterator<MediaRepresentation> it) {

        String mediaUriString = "";
        boolean first = true;
        while(it.hasNext()){
            MediaRepresentation rep = it.next();
            List<MediaRepresentationPart> parts = rep.getParts();
            for (MediaRepresentationPart part: parts){
                if (first){
                    if (part.getUri() != null){
                        mediaUriString += part.getUri().toString();
                        first = false;
                    }
                }else{
                    if (part.getUri() != null){
                        mediaUriString += ", " +part.getUri().toString();
                    }
                }
            }
        }

        return mediaUriString;
    }

    /**
     * @param state
     * @param gathering
     */
    private void addCollectingAreas(CdmLightExportState state, GatheringEvent gathering) {
        // TODO implement !!!

        if (!gathering.getCollectingAreas().isEmpty()){
            state.getResult().addWarning("Collecting areas not yet implemented but gathering " +
                    cdmBaseStr(gathering) + " has collecting areas.");
        }

    }

    /**
     * @param gathering
     * @return
     */
    private String createCollectorString(CdmLightExportState state, GatheringEvent gathering, FieldUnit fieldUnit) {
        try {
            String collectorString = "";
            AgentBase<?> collectorA = CdmBase.deproxy(gathering.getCollector());
            if (gathering.getCollector() != null){
               if (collectorA instanceof TeamOrPersonBase
                       && state.getConfig().isHighLightPrimaryCollector()){

                   Person primaryCollector = fieldUnit.getPrimaryCollector();
                   if (collectorA instanceof Team){
                       Team collectorTeam = (Team)collectorA;
                       boolean isFirst = true;
                       for (Person member: collectorTeam.getTeamMembers()){
                           if (!isFirst){
                               collectorString += "; ";
                           }
                           if (member.equals(primaryCollector)){
                               //highlight
                               collectorString += "<b>" + member.getTitleCache() + "</b>";
                           }else{
                               collectorString += member.getTitleCache();
                           }
                       }
                   }
               } else{
                   collectorString = collectorA.getTitleCache();
               }
           }
           return collectorString;
        } catch (Exception e) {
            state.getResult().addException(e, "An unexpected error occurred when creating collector string for " +
                    cdmBaseStr(fieldUnit) + ": " + e.getMessage());
            return "";
        }
    }


    /**
     * Returns a string representation of the {@link CdmBase cdmBase} object
     * for result messages.
     */
    private String cdmBaseStr(CdmBase cdmBase) {
        if (cdmBase == null){
            return "-no object available-";
        }else{
            return cdmBase.getClass().getSimpleName() + ": " + cdmBase.getUuid();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(CdmLightExportState state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(CdmLightExportState state) {
        return false;
    }

}
