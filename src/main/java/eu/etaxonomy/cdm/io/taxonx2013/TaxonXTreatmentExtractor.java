// $Id$
/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.taxonx2013;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author pkelbert
 * @date 2 avr. 2013
 *
 */
public class TaxonXTreatmentExtractor extends TaxonXExtractor{

    private static final String notMarkedUp = "Not marked-up";
    private static final UUID proIbioTreeUUID = UUID.fromString("2c49f506-c7f7-44de-a8b9-2e695de3769c");
    private static final UUID OtherUUID = UUID.fromString("6465f8aa-2175-446f-807e-7163994b120f");
    private static final UUID NotMarkedUpUUID = UUID.fromString("796fe3a5-2c9c-4a89-b298-7598ca944063");
    private static final boolean skippQuestion = true;

    private final NomenclaturalCode nomenclaturalCode;
    private Classification classification;

    private  String treatmentMainName,originalTreatmentName;

    private final HashMap<String,Map<String,String>> namesMap = new HashMap<String, Map<String,String>>();


    private final Pattern keypattern = Pattern.compile("^(\\d+.*|-\\d+.*)");
    private final Pattern keypatternend = Pattern.compile("^.+?\\d$");

    private boolean maxRankRespected =false;
    private final Map<String, Feature> featuresMap;

    private MyName currentMyName=new MyName();

    private final Reference<?> sourceUrlRef;

    private final TaxonXAddSources sourceHandler = new TaxonXAddSources();

    /**
     * @param nomenclaturalCode
     * @param classification
     * @param importer
     * @param configState
     */
    public TaxonXTreatmentExtractor(NomenclaturalCode nomenclaturalCode, Classification classification, TaxonXImport importer,
            TaxonXImportState configState,Map<String, Feature> featuresMap,  Reference<?> urlSource) {
        this.nomenclaturalCode=nomenclaturalCode;
        this.classification = classification;
        this.importer=importer;
        this.configState=configState;
        this.featuresMap=featuresMap;
        this.sourceUrlRef =urlSource;
        prepareCollectors(configState, importer.getAgentService());
        this.sourceHandler.setSourceUrlRef(sourceUrlRef);
        this.sourceHandler.setImporter(importer);
    }

    /**
     * extracts all the treament information and save them
     * @param treatmentnode: the XML Node
     * @param tosave: the list of object to save into the CDM
     * @param refMods: the reference extracted from the MODS
     * @param sourceName: the URI of the document
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    protected void extractTreatment(Node treatmentnode, List<Object> tosave, Reference<?> refMods, URI sourceName) {
        logger.info("extractTreatment");
        List<TaxonNameBase> nametosave = new ArrayList<TaxonNameBase>();
        NodeList children = treatmentnode.getChildNodes();
        Taxon acceptedTaxon =null;
        Taxon defaultTaxon =null;
        boolean refgroup=false;

        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group")) {
                refgroup=true;
            }
        }

        for (int i=0;i<children.getLength();i++){

            if (children.item(i).getNodeName().equalsIgnoreCase("tax:nomenclature")){
                NodeList nomenclature = children.item(i).getChildNodes();
                boolean containsName=false;
                for(int k=0;k<nomenclature.getLength();k++){
                    if(nomenclature.item(k).getNodeName().equalsIgnoreCase("tax:name")){
                        containsName=true;
                        break;
                    }
                }
                if (containsName){
                    reloadClassification();
                    //extract "main" the scientific name
                    try{
                        acceptedTaxon = extractNomenclature(children.item(i),nametosave,refMods);
                    }catch(ClassCastException e){e.printStackTrace();System.exit(0);}
                    //                    System.out.println("acceptedTaxon : "+acceptedTaxon);
                }
            }
            else if (children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group") && maxRankRespected){
                reloadClassification();
                //extract the References within the document
                extractReferences(children.item(i),nametosave,acceptedTaxon,refMods);
            }
            else if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("multiple") && maxRankRespected){
                File file = new File("/home/pkelbert/Bureau/multipleTaxonX.txt");
                FileWriter writer;
                try {
                    writer = new FileWriter(file ,true);
                    writer.write(sourceName+"\n");
                    writer.flush();
                    writer.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                //                String multiple = askMultiple(children.item(i));
                String multiple = "Other";
                if (multiple.equalsIgnoreCase("other")) {
                    extractSpecificFeatureNotStructured(children.item(i),acceptedTaxon, defaultTaxon,nametosave, refMods,multiple);
                }
                else
                    if (multiple.equalsIgnoreCase("synonyms")) {
                        extractSynonyms(children.item(i),acceptedTaxon, refMods);
                    }
                    else
                        if(multiple.equalsIgnoreCase("material examined")){
                            extractMaterials(children.item(i),acceptedTaxon, refMods, nametosave);
                        }
                        else
                            if (multiple.equalsIgnoreCase("distribution")){
                                extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods);
                            }
                            else
                                if (multiple.equalsIgnoreCase("type status")){
                                    extractDescriptionWithReference(children.item(i),acceptedTaxon,defaultTaxon,refMods, "TypeStatus");
                                }
                                else
                                    if (multiple.equalsIgnoreCase("vernacular name")){
                                        extractDescriptionWithReference(children.item(i),acceptedTaxon,defaultTaxon,refMods, Feature.COMMON_NAME().getTitleCache());

                                    }
                                    else{
                                        extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,multiple);
                                    }

            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("biology_ecology") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon, nametosave, refMods, Feature.BIOLOGY_ECOLOGY());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("vernacularName") && maxRankRespected){
                extractDescriptionWithReference(children.item(i),acceptedTaxon,defaultTaxon,refMods, Feature.COMMON_NAME().getTitleCache());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("description") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods, Feature.DESCRIPTION());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("diagnosis") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,Feature.DIAGNOSIS());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("discussion") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods, Feature.DISCUSSION());
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("note") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods, Feature.DESCRIPTION());
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("distribution") && maxRankRespected){
                extractDistribution(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("etymology") && maxRankRespected){
                extractFeature(children.item(i),acceptedTaxon,defaultTaxon,nametosave,refMods,Feature.ETYMOLOGY());
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("materials_examined") && maxRankRespected){
                extractMaterials(children.item(i),acceptedTaxon, refMods, nametosave);
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:figure") && maxRankRespected){
                extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon, nametosave, refMods, "Figure");
            }
            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("Other") && maxRankRespected){
                extractSpecificFeature(children.item(i),acceptedTaxon,defaultTaxon, nametosave, refMods, "table");
            }

            else if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("key") && maxRankRespected){
                //TODO IGNORE keys for the moment
                //extractKey(children.item(i),acceptedTaxon, nametosave,source, refMods);
                extractSpecificFeatureNotStructured(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,"Keys - unparsed");
            }
            else{
                if (!children.item(i).getNodeName().equalsIgnoreCase("tax:pb")){
                    logger.info("ANOTHER KIND OF NODES: "+children.item(i).getNodeName()+", "+children.item(i).getAttributes());
                    if (children.item(i).getAttributes() !=null) {
                        logger.info(children.item(i).getAttributes().item(0));
                    }
                    extractSpecificFeatureNotStructured(children.item(i),acceptedTaxon,defaultTaxon,nametosave, refMods,notMarkedUp);
                }
            }
        }
        //        logger.info("saveUpdateNames");
        if (maxRankRespected){
            importer.getNameService().saveOrUpdate(nametosave);
            importer.getClassificationService().saveOrUpdate(classification);
            logger.info("saveUpdateNames-ok");
        }

        buildFeatureTree();
    }


    protected Map<String,Feature> getFeaturesUsed(){
        return featuresMap;
    }
    /**
     *
     */
    private void buildFeatureTree() {
        FeatureTree proibiospheretree = importer.getFeatureTreeService().find(proIbioTreeUUID);
        if (proibiospheretree == null){
            proibiospheretree = FeatureTree.NewInstance();
            proibiospheretree.setUuid(proIbioTreeUUID);
        }
        //        FeatureNode root = proibiospheretree.getRoot();
        FeatureNode root2 = FeatureNode.NewInstance();
        proibiospheretree.setRoot(root2);
        for (Feature feature:featuresMap.values()) {
            root2.addChild(FeatureNode.NewInstance(feature));
        }
        importer.getFeatureTreeService().saveOrUpdate(proibiospheretree);

    }


    /**
     * @param keys
     * @param acceptedTaxon: the current acceptedTaxon
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     */
    /*   @SuppressWarnings("rawtypes")
    private void extractKey(Node keys, Taxon acceptedTaxon,List<TaxonNameBase> nametosave, Reference<?> refMods) {
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        NodeList children = keys.getChildNodes();
        String key="";
        PolytomousKey poly =  PolytomousKey.NewInstance();
        poly.addSource(OriginalSourceType.Import, null,null,refMods,null);
        poly.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
        poly.addTaxonomicScope(acceptedTaxon);
        poly.setTitleCache("bloup");
        //        poly.addCoveredTaxon(acceptedTaxon);
        PolytomousKeyNode root = poly.getRoot();
        PolytomousKeyNode previous = null,tmpKey=null;
        Taxon taxonKey=null;
        List<PolytomousKeyNode> polyNodes = new ArrayList<PolytomousKeyNode>();

        //        String fullContent = keys.getTextContent();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList paragraph = children.item(i).getChildNodes();
                key="";
                taxonKey=null;
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text")){
                        if (! paragraph.item(j).getTextContent().trim().isEmpty()){
                            key+=paragraph.item(j).getTextContent().trim();
                            //                            logger.info("KEY: "+j+"--"+key);
                        }
                    }
                    if(paragraph.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        taxonKey=getTaxonFromXML(paragraph.item(j),nametosave,refMods);
                    }
                }
                //                logger.info("keypattern.matcher(key).matches(): "+keypattern.matcher(key).matches());
                if (keypattern.matcher(key).matches()){
                    tmpKey = PolytomousKeyNode.NewInstance(key);
                    if (taxonKey!=null) {
                        tmpKey.setTaxon(taxonKey);
                    }
                    polyNodes.add(tmpKey);
                    if (previous == null) {
                        root.addChild(tmpKey);
                    } else {
                        previous.addChild(tmpKey);
                    }
                }else{
                    if (!key.isEmpty()){
                        tmpKey=PolytomousKeyNode.NewInstance(key);
                        if (taxonKey!=null) {
                            tmpKey.setTaxon(taxonKey);
                        }
                        polyNodes.add(tmpKey);
                        if (keypatternend.matcher(key).matches()) {
                            root.addChild(tmpKey);
                            previous=tmpKey;
                        } else{
                            previous.addChild(tmpKey);
                        }

                    }
                }
            }
        }
        importer.getPolytomousKeyNodeService().saveOrUpdate(polyNodes);
        importer.getPolytomousKeyService().saveOrUpdate(poly);
    }
     */
    //    /**
    //     * @param taxons: the XML Nodegroup
    //     * @param nametosave: the list of objects to save into the CDM
    //     * @param acceptedTaxon: the current accepted Taxon
    //     * @param refMods: the current reference extracted from the MODS
    //     *
    //     * @return Taxon object built
    //     */
    //    @SuppressWarnings({ "rawtypes", "unchecked" })
    //    private Taxon getTaxonFromXML(Node taxons, List<TaxonNameBase> nametosave, Reference<?> refMods) {
    //        //        logger.info("getTaxonFromXML");
    //        //        logger.info("acceptedTaxon: "+acceptedTaxon);
    //
    //        // TaxonNameBase nameToBeFilled = null;
    //
    //        currentMyName = new MyName();
    //        NomenclaturalStatusType statusType = null;
    //
    //        try {
    //            currentMyName = extractScientificName(taxons);
    //            if (!currentMyName.getStatus().isEmpty()){
    //                try {
    //                    statusType = nomStatusString2NomStatus(currentMyName.getStatus());
    //                } catch (UnknownCdmTypeException e) {
    //                    addProblematicStatusToFile(currentMyName.getStatus());
    //                    logger.warn("Problem with status");
    //                }
    //            }
    //
    //        } catch (TransformerFactoryConfigurationError e1) {
    //            logger.warn(e1);
    //        } catch (TransformerException e1) {
    //            logger.warn(e1);
    //        }
    //        /*  INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
    //
    //        nameToBeFilled = parser.parseFullName(currentMyName.getName(), nomenclaturalCode, currentMyName.getRank());
    //        if (nameToBeFilled.hasProblem() &&
    //                !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
    //            //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
    //            addProblemNameToFile(currentMyName.getName(),"",nomenclaturalCode,currentMyName.getRank());
    //            nameToBeFilled=solveNameProblem(currentMyName.getOriginalName(), currentMyName.getName(),parser, currentMyName.getAuthor(), currentMyName.getRank());
    //        }
    //
    //        nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
    //         */
    //        TaxonNameBase nameToBeFilled = currentMyName.getTaxonNameBase();
    //        Taxon t = currentMyName.getTaxon();
    //        //        importer.getNameService().saveOrUpdate(nametosave);
    //        /*    Taxon t = importer.getTaxonService().findBestMatchingTaxon(nameToBeFilled.getTitleCache());
    //         */
    //        boolean statusMatch=false;
    //        if(t !=null ){
    //            statusMatch=compareStatus(t, statusType);
    //        }
    //        if (t ==null || (t != null && !statusMatch)){
    //            if(statusType != null) {
    //                nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
    //            }
    //            t= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
    //            if (t.getSec() == null) {
    //                t.setSec(refMods);
    //            }
    //            if(!configState.getConfig().doKeepOriginalSecundum()) {
    //                t.setSec(configState.getConfig().getSecundum());
    //                logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
    //            }
    //            t.addSource(OriginalSourceType.Import,null,null,refMods,null);
    //            t.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
    //
    //
    //            if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
    //                setLSID(currentMyName.getIdentifier(), t);
    //            }
    //
    //            //            Taxon parentTaxon = currentMyName.getHigherTaxa();
    //            //            if (parentTaxon == null && !skippQuestion) {
    //            //                parentTaxon =  askParent(t, classification);
    //            //            }
    //            //            if (parentTaxon ==null){
    //            //                while (parentTaxon == null) {
    //            //                    System.out.println("parent is null");
    //            //                    parentTaxon = createParent(t, refMods);
    //            //                    classification.addParentChild(parentTaxon, t, refMods, null);
    //            //                }
    //            //            }else{
    //            //                classification.addParentChild(parentTaxon, t, refMods, null);
    //            //            }
    //        }
    //        else{
    //            t = CdmBase.deproxy(t, Taxon.class);
    //        }
    //        if (!configState.getConfig().doKeepOriginalSecundum()) {
    //            t.setSec(configState.getConfig().getSecundum());
    //            logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
    //        }
    //        return t;
    //    }




    //    private Taxon getTaxonFromTaxonNameBase(TaxonNameBase tnb,Reference<?> ref){
    //        Taxon taxon = null;
    ////        System.out.println(tnb.getTitleCache());
    //        Taxon cc= importer.getTaxonService().findBestMatchingTaxon(tnb.getTitleCache());
    //        if (cc != null){
    //            if ((cc.getSec() == null || cc.getSec().toString().isEmpty()) || (cc.getSec() != null &&
    //                    cc.getSec().getTitleCache().equalsIgnoreCase(ref.getTitleCache()))) {
    //                if(cc.getSec() == null || cc.getSec().toString().isEmpty()){
    //                    cc.setSec(ref);
    //                    importer.getTaxonService().saveOrUpdate(cc);
    //                }
    //                taxon=cc;
    //            }
    //        }
    //        else{
    //            //            List<TaxonBase> c = importer.getTaxonService().searchTaxaByName(tnb.getTitleCache(), ref);
    //            List<TaxonBase> c = importer.getTaxonService().list(TaxonBase.class, 0, 0, null, null);
    //            for (TaxonBase b : c) {
    //                try{
    //                    taxon = (Taxon) b;
    //                }catch(ClassCastException e){logger.warn("error while casting existing taxonnamebase");}
    //            }
    //        }
    //        if (taxon == null){
    ////            System.out.println("NEW TAXON HERE "+tnb.toString()+", "+ref.toString());
    //            taxon = Taxon.NewInstance(tnb, ref); //sec set null
    //            importer.getTaxonService().save(taxon);
    //
    //        }
    //        taxon = (Taxon) importer.getTaxonService().find(taxon.getUuid());
    //
    //        boolean exist = false;
    //        for (TaxonNode p : classification.getAllNodes()){
    //            if(p.getTaxon().equals(taxon)) {
    //                exist =true;
    //            }
    //        }
    //        if (!exist){
    //            taxon = (Taxon) importer.getTaxonService().find(taxon.getUuid());
    //            Taxon parentTaxon = currentMyName.getHigherTaxa();
    //            if (parentTaxon != null) {
    //                classification.addParentChild(parentTaxon, taxon, ref, null);
    //            } else {
    //                System.out.println("HERE???");
    //                classification.addChildTaxon(taxon, ref, null);
    //            }
    //            importer.getClassificationService().saveOrUpdate(classification);
    //            //                        refreshTransaction();
    //        }
    //        taxon = CdmBase.deproxy(taxon, Taxon.class);
    //        //        System.out.println("TAXON RETOURNE : "+taxon.getTitleCache());
    //        return taxon;
    //    }
    /**
     * @param taxons: the XML Nodegroup
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     *
     * @return Taxon object built
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    private TaxonNameBase getTaxonNameBaseFromXML(Node taxons, List<TaxonNameBase> nametosave, Reference<?> refMods) {
        //        logger.info("getTaxonFromXML");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);

        TaxonNameBase nameToBeFilled = null;

        currentMyName=new MyName();

        NomenclaturalStatusType statusType = null;
        try {
            currentMyName = extractScientificName(taxons,refMods);
        } catch (TransformerFactoryConfigurationError e1) {
            logger.warn(e1);
        } catch (TransformerException e1) {
            logger.warn(e1);
        }
        /* INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        nameToBeFilled = parser.parseFullName(currentMyName.getName(), nomenclaturalCode, currentMyName.getRank());
        if (nameToBeFilled.hasProblem() &&
                !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
            //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
            addProblemNameToFile(currentMyName.getName(),"",nomenclaturalCode,currentMyName.getRank());
            nameToBeFilled=solveNameProblem(currentMyName.getOriginalName(), currentMyName.getName(),parser,currentMyName.getAuthor(), currentMyName.getRank());
        }

        nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
         */
        nameToBeFilled = currentMyName.getTaxonNameBase();
        return nameToBeFilled;

    }

    //    @SuppressWarnings("rawtypes")
    //    private TaxonNameBase getTaxonNameBase (TaxonNameBase name, List<TaxonNameBase> nametosave, NomenclaturalStatusType statusType){
    //        List<TaxonNameBase> names = importer.getNameService().list(TaxonNameBase.class, null, null, null, null);
    //        for (TaxonNameBase tb : names){
    //            if (tb.getTitleCache().equalsIgnoreCase(name.getTitleCache())) {
    //                boolean statusMatch=false;
    //                if(tb !=null ){
    //                    statusMatch=compareStatus(tb, statusType);
    //                }
    //                if (!statusMatch){
    //                    if(statusType != null) {
    //                        name.addStatus(NomenclaturalStatus.NewInstance(statusType));
    //                    }
    //                }else
    //                {
    //                    logger.info("TaxonNameBase FOUND"+name.getTitleCache());
    //                    return  CdmBase.deproxy(tb, TaxonNameBase.class);
    //                }
    //            }
    //        }
    //        //        logger.info("TaxonNameBase NOT FOUND "+name.getTitleCache());
    //        //        System.out.println("add name "+name);
    //        nametosave.add(name);
    //        name = CdmBase.deproxy(name, TaxonNameBase.class);
    //        return name;
    //
    //    }



    //    /**
    //     * @param tb
    //     * @param statusType
    //     * @return
    //     */
    //    private boolean compareStatus(TaxonNameBase tb, NomenclaturalStatusType statusType) {
    //        boolean statusMatch=false;
    //        //found one taxon
    //        Set<NomenclaturalStatus> status = tb.getStatus();
    //        if (statusType!=null && status.size()>0){ //the statusType is known for both taxon
    //            for (NomenclaturalStatus st:status){
    //                NomenclaturalStatusType stype = st.getType();
    //                if (stype.toString().equalsIgnoreCase(statusType.toString())) {
    //                    statusMatch=true;
    //                }
    //            }
    //        }
    //        else{
    //            if(statusType == null && status.size()==0) {//there is no statusType, we can assume it's the same
    //                statusMatch=true;
    //            }
    //        }
    //        return statusMatch;
    //    }

    /**
     *
     */
    private void reloadClassification() {
        Classification cl = importer.getClassificationService().find(classification.getUuid());
        if (cl != null){
            classification=cl;
        }else{
            importer.getClassificationService().saveOrUpdate(classification);
            classification = importer.getClassificationService().find(classification.getUuid());
        }
    }

    //    /**
    //     * Create a Taxon for the current NameBase, based on the current reference
    //     * @param taxonNameBase
    //     * @param refMods: the current reference extracted from the MODS
    //     * @return Taxon
    //     */
    //    @SuppressWarnings({ "unused", "rawtypes" })
    //    private Taxon getTaxon(TaxonNameBase taxonNameBase, Reference<?> refMods) {
    //        Taxon t = new Taxon(taxonNameBase,null );
    //        if (!configState.getConfig().doKeepOriginalSecundum() || (t.getSec() == null)) {
    //            t.setSec(configState.getConfig().getSecundum());
    //            logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
    //        }
    //        /*<<<<<<< .courant
    //        boolean sourceExists=false;
    //        Set<IdentifiableSource> sources = t.getSources();
    //        for (IdentifiableSource src : sources){
    //            String micro = src.getCitationMicroReference();
    //            Reference r = src.getCitation();
    //            if (r.equals(refMods) && micro == null) {
    //                sourceExists=true;
    //            }
    //        }
    //        if(!sourceExists) {
    //            t.addSource(null,null,refMods,null);
    //        }
    //=======*/
    //        t.addSource(OriginalSourceType.Import,null,null,refMods,null);
    //        t.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
    //        return t;
    //    }

    private void  extractDescriptionWithReference(Node typestatus, Taxon acceptedTaxon, Taxon defaultTaxon, Reference<?> refMods,
            String featureName) {
        //        System.out.println("extractDescriptionWithReference !");
        NodeList children = typestatus.getChildNodes();

        Feature currentFeature=getFeatureObjectFromString(featureName);

        String r="";String s="";
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                s+=children.item(i).getTextContent().trim();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:bibref")){
                r+= children.item(i).getTextContent().trim();
            }
            if (s.indexOf(r)>-1) {
                s=s.split(r)[0];
            }
        }

        Reference<?> currentref =  ReferenceFactory.newGeneric();
        if(!r.isEmpty()) {
            currentref.setTitleCache(r);
        } else {
            currentref=refMods;
        }
        setParticularDescription(s,acceptedTaxon,defaultTaxon, currentref, refMods,currentFeature);
    }

    /**
     * @param nametosave
     * @param distribution: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings("rawtypes")
    private void extractDistribution(Node distribution, Taxon acceptedTaxon, Taxon defaultTaxon, List<TaxonNameBase> nametosave, Reference<?> refMods) {
        //        logger.info("DISTRIBUTION");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        NodeList children = distribution.getChildNodes();
        Map<Integer,List<MySpecimenOrObservation>> specimenOrObservations = new HashMap<Integer, List<MySpecimenOrObservation>>();
        Map<Integer,String> descriptionsFulltext = new HashMap<Integer,String>();

        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList paragraph = children.item(i).getChildNodes();
                for (int j=0;j<paragraph.getLength();j++){
                    if (paragraph.item(j).getNodeName().equalsIgnoreCase("#text")){
                        extractText(descriptionsFulltext, i, paragraph.item(j));
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        extractInLine(nametosave, refMods, descriptionsFulltext, i,paragraph.item(j));
                    }
                    else if (paragraph.item(j).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        MySpecimenOrObservation specimenOrObservation = new MySpecimenOrObservation();
                        DerivedUnit derivedUnitBase = null;
                        specimenOrObservation = extractSpecimenOrObservation(paragraph.item(j), derivedUnitBase, SpecimenOrObservationType.DerivedUnit);
                        extractTextFromSpecimenOrObservation(specimenOrObservations, descriptionsFulltext, i, specimenOrObservation);
                    }
                }
            }
        }

        int m=0;
        for (int k:descriptionsFulltext.keySet()) {
            if (k>m) {
                m=k;
            }
        }
        for (int k:specimenOrObservations.keySet()) {
            if (k>m) {
                m=k;
            }
        }


        TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
        Feature currentFeature = Feature.DISTRIBUTION();
        //        DerivedUnit derivedUnitBase=null;
        //        String descr="";
        for (int k=0;k<=m;k++){
            if(specimenOrObservations.keySet().contains(k)){
                for (MySpecimenOrObservation soo:specimenOrObservations.get(k) ) {
                    handleAssociation(acceptedTaxon, refMods, td, soo);
                }
            }

            if (descriptionsFulltext.keySet().contains(k)){
                if (!descriptionsFulltext.get(k).isEmpty() && (descriptionsFulltext.get(k).startsWith("Hab.") || descriptionsFulltext.get(k).startsWith("Habitat"))){
                    setParticularDescription(descriptionsFulltext.get(k),acceptedTaxon,defaultTaxon, refMods, Feature.HABITAT());
                    break;
                }
                else{
                    handleTextData(refMods, descriptionsFulltext, td, currentFeature, k);
                }
            }

            if (descriptionsFulltext.keySet().contains(k) || specimenOrObservations.keySet().contains(k)){
                acceptedTaxon.addDescription(td);
                sourceHandler.addAndSaveSource(refMods, td, null);
                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
            }
        }
    }

    /**
     * @param refMods
     * @param descriptionsFulltext
     * @param td
     * @param currentFeature
     * @param k
     */
    private void handleTextData(Reference<?> refMods, Map<Integer, String> descriptionsFulltext, TaxonDescription td,
            Feature currentFeature, int k) {
        TextData textData = TextData.NewInstance();
        textData.setFeature(currentFeature);
        textData.putText(Language.UNKNOWN_LANGUAGE(), descriptionsFulltext.get(k));
        sourceHandler.addSource(refMods, textData);
        td.addElement(textData);
    }

    /**
     * @param acceptedTaxon
     * @param refMods
     * @param td
     * @param soo
     */
    private void handleAssociation(Taxon acceptedTaxon, Reference<?> refMods, TaxonDescription td, MySpecimenOrObservation soo) {
        String descr=soo.getDescr();
        DerivedUnit derivedUnitBase = soo.getDerivedUnitBase();

        sourceHandler.addAndSaveSource(refMods, derivedUnitBase);

        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
        acceptedTaxon.addDescription(taxonDescription);

        Feature feature=null;
        feature = makeFeature(derivedUnitBase);
        if(!StringUtils.isEmpty(descr)) {
            derivedUnitBase.setTitleCache(descr, true);
        }

        IndividualsAssociation indAssociation = createIndividualAssociation(refMods, derivedUnitBase, feature);

        taxonDescription.addElement(indAssociation);
        taxonDescription.setTaxon(acceptedTaxon);
        sourceHandler.addAndSaveSource(refMods, taxonDescription,null);
        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        td.setDescribedSpecimenOrObservation(soo.getDerivedUnitBase());
    }

    /**
     * create an individualAssociation
     * @param refMods
     * @param derivedUnitBase
     * @param feature
     * @return
     */
    private IndividualsAssociation createIndividualAssociation(Reference<?> refMods, DerivedUnit derivedUnitBase,
            Feature feature) {
        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
        indAssociation.setFeature(feature);
        indAssociation = sourceHandler.addSource(refMods, indAssociation);
        return indAssociation;
    }

    /**
     * @param specimenOrObservations
     * @param descriptionsFulltext
     * @param i
     * @param specimenOrObservation
     */
    private void extractTextFromSpecimenOrObservation(Map<Integer, List<MySpecimenOrObservation>> specimenOrObservations,
            Map<Integer, String> descriptionsFulltext, int i, MySpecimenOrObservation specimenOrObservation) {
        List<MySpecimenOrObservation> speObsList = specimenOrObservations.get(i);
        if (speObsList == null) {
            speObsList=new ArrayList<MySpecimenOrObservation>();
        }
        speObsList.add(specimenOrObservation);
        specimenOrObservations.put(i,speObsList);

        String s = specimenOrObservation.getDerivedUnitBase().toString();
        if (descriptionsFulltext.get(i) !=null){
            s = descriptionsFulltext.get(i)+" "+s;
        }
        descriptionsFulltext.put(i, s);
    }

    /**
     * Extract the text with the inline link to a taxon
     * @param nametosave
     * @param refMods
     * @param descriptionsFulltext
     * @param i
     * @param paragraph
     */
    @SuppressWarnings("rawtypes")
    private void extractInLine(List<TaxonNameBase> nametosave, Reference<?> refMods, Map<Integer, String> descriptionsFulltext,
            int i, Node paragraph) {
        String inLine=getInlineText(nametosave, refMods, paragraph);
        if (descriptionsFulltext.get(i) !=null){
            inLine = descriptionsFulltext.get(i)+inLine;
        }
        descriptionsFulltext.put(i, inLine);
    }

    /**
     * Extract the raw text from a Node
     * @param descriptionsFulltext
     * @param node
     * @param j
     */
    private void extractText(Map<Integer, String> descriptionsFulltext, int i, Node node) {
        if(!node.getTextContent().trim().isEmpty()) {
            String s =node.getTextContent().trim();
            if (descriptionsFulltext.get(i) !=null){
                s = descriptionsFulltext.get(i)+" "+s;
            }
            descriptionsFulltext.put(i, s);
        }
    }


    /**
     * @param materials: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings("rawtypes")
    private void extractMaterials(Node materials, Taxon acceptedTaxon, Reference<?> refMods,List<TaxonNameBase> nametosave) {
        //        logger.info("EXTRACTMATERIALS");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        NodeList children = materials.getChildNodes();
        NodeList events = null;
        String descr="";

        DerivedUnit derivedUnitBase=null;
        MySpecimenOrObservation myspecimenOrObservation = null;

        for (int i=0;i<children.getLength();i++){
            String rawAssociation="";
            boolean added=false;
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                events = children.item(i).getChildNodes();
                for(int k=0;k<events.getLength();k++){
                    if (events.item(k).getNodeName().equalsIgnoreCase("tax:name")){
                        String inLine= getInlineText(nametosave, refMods, events.item(k));
                        if(!inLine.isEmpty()) {
                            rawAssociation+=inLine;
                        }
                    }
                    if (! events.item(k).getNodeName().equalsIgnoreCase("tax:name")
                            && !events.item(k).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        rawAssociation+= events.item(k).getTextContent().trim();
                    }
                    if(events.item(k).getNodeName().equalsIgnoreCase("tax:collection_event")){
                        if (!containsDistinctLetters(rawAssociation.replaceAll(";",""))) {
                            rawAssociation="no description text";
                        }
                        added=true;
                        DerivedUnitFacade derivedUnitFacade = getFacade(rawAssociation.replaceAll(";",""),SpecimenOrObservationType.DerivedUnit);
                        derivedUnitBase = derivedUnitFacade.innerDerivedUnit();

                        sourceHandler.addAndSaveSource(refMods, derivedUnitBase);

                        myspecimenOrObservation = extractSpecimenOrObservation(events.item(k),derivedUnitBase,SpecimenOrObservationType.DerivedUnit);
                        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();
                        descr=myspecimenOrObservation.getDescr();

                        sourceHandler.addAndSaveSource(refMods, derivedUnitBase);

                        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
                        acceptedTaxon.addDescription(taxonDescription);

                        Feature feature = makeFeature(derivedUnitBase);
                        featuresMap.put(feature.getTitleCache(),feature);
                        if(!StringUtils.isEmpty(descr)) {
                            derivedUnitBase.setTitleCache(descr, true);
                        }

                        IndividualsAssociation indAssociation = createIndividualAssociation(refMods, derivedUnitBase, feature);

                        taxonDescription.addElement(indAssociation);
                        taxonDescription.setTaxon(acceptedTaxon);
                        sourceHandler.addAndSaveSource(refMods, taxonDescription,null);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                    }
                    if (!rawAssociation.isEmpty() && !added){

                        Feature feature = Feature.MATERIALS_EXAMINED();
                        featuresMap.put(feature.getTitleCache(),feature);

                        TextData textData = TextData.NewInstance();
                        textData.setFeature(feature);
                        sourceHandler.addSource(refMods, textData);

                        textData.putText(Language.UNKNOWN_LANGUAGE(), rawAssociation);

                        if(! rawAssociation.isEmpty() && (acceptedTaxon!=null)){
                            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                            td.addElement(textData);
                            acceptedTaxon.addDescription(td);
                            sourceHandler.addAndSaveSource(refMods, td, null);
                        }
                        //                        DerivedUnitFacade derivedUnitFacade = getFacade(rawAssociation.replaceAll(";",""),SpecimenOrObservationType.DerivedUnit);
                        //                        derivedUnitBase = derivedUnitFacade.innerDerivedUnit();
                        //
                        //                        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
                        //                        acceptedTaxon.addDescription(taxonDescription);
                        //
                        //                        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
                        //
                        //                        Feature feature = Feature.MATERIALS_EXAMINED();
                        //                        featuresMap.put(feature.getTitleCache(),feature);
                        //                        if(!StringUtils.isEmpty(rawAssociation)) {
                        //                            derivedUnitBase.setTitleCache(rawAssociation, true);
                        //                        }
                        //                        indAssociation.setAssociatedSpecimenOrObservation(derivedUnitBase);
                        //                        indAssociation.setFeature(feature);
                        //                        indAssociation.addSource(OriginalSourceType.Import, null, null, refMods, null);
                        //
                        //                        /*boolean sourceExists=false;
                        //                        Set<DescriptionElementSource> dsources = indAssociation.getSources();
                        //                        for (DescriptionElementSource src : dsources){
                        //                            String micro = src.getCitationMicroReference();
                        //                            Reference r = src.getCitation();
                        //                            if (r.equals(refMods) && micro == null) {
                        //                                sourceExists=true;
                        //                            }
                        //                        }
                        //                        if(!sourceExists) {
                        //                            indAssociation.addSource(null, null, refMods, null);
                        //                        }*/
                        //                        taxonDescription.addElement(indAssociation);
                        //                        taxonDescription.setTaxon(acceptedTaxon);
                        //                        taxonDescription.addSource(OriginalSourceType.Import, null,null,refMods,null);
                        //
                        //                        /*sourceExists=false;
                        //                        Set<IdentifiableSource> sources = taxonDescription.getSources();
                        //                        for (IdentifiableSource src : sources){
                        //                            String micro = src.getCitationMicroReference();
                        //                            Reference r = src.getCitation();
                        //                            if (r.equals(refMods) && micro == null) {
                        //                                sourceExists=true;
                        //                            }
                        //                        }
                        //                        if(!sourceExists) {
                        //                            taxonDescription.addSource(OriginalSourceType.Import,null,null,refMods,null);
                        //                        }*/
                        //
                        //                        importer.getDescriptionService().saveOrUpdate(taxonDescription);
                        importer.getTaxonService().saveOrUpdate(acceptedTaxon);

                        rawAssociation="";
                    }
                }
            }
        }
    }



    /**
     * @param materials: the XML node group
     * @param acceptedTaxon: the current accepted Taxon
     * @param refMods: the current reference extracted from the MODS
     */
    private String extractMaterialsDirect(Node materials, Taxon acceptedTaxon, Reference<?> refMods, String event) {
        //        logger.info("EXTRACTMATERIALS");
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        String descr="";

        DerivedUnit derivedUnitBase=null;
        MySpecimenOrObservation myspecimenOrObservation = extractSpecimenOrObservation(materials,derivedUnitBase, SpecimenOrObservationType.DerivedUnit);
        derivedUnitBase = myspecimenOrObservation.getDerivedUnitBase();

        sourceHandler.addAndSaveSource(refMods, derivedUnitBase);

        TaxonDescription taxonDescription = importer.getTaxonDescription(acceptedTaxon, false, true);
        acceptedTaxon.addDescription(taxonDescription);

        Feature feature=null;
        if (event.equalsIgnoreCase("collection")){
            feature = makeFeature(derivedUnitBase);
        }
        else{
            feature = Feature.MATERIALS_EXAMINED();
        }
        featuresMap.put(feature.getTitleCache(),  feature);

        descr=myspecimenOrObservation.getDescr();
        if(!StringUtils.isEmpty(descr)) {
            derivedUnitBase.setTitleCache(descr);
        }

        IndividualsAssociation indAssociation = createIndividualAssociation(refMods, derivedUnitBase, feature);

        taxonDescription.addElement(indAssociation);
        taxonDescription.setTaxon(acceptedTaxon);
        sourceHandler.addAndSaveSource(refMods, taxonDescription,null);
        importer.getTaxonService().saveOrUpdate(acceptedTaxon);

        return derivedUnitBase.getTitleCache();

    }


    /**
     * @param description: the XML node group
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @param featureName: the feature name
     */
    @SuppressWarnings({ "rawtypes", "null" })
    private String extractSpecificFeature(Node description, Taxon acceptedTaxon, Taxon defaultTaxon,
            List<TaxonNameBase> nametosave, Reference<?> refMods, String featureName ) {
        //        System.out.println("GRUUUUuu");
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        NodeList trNodes;
        //        String descr ="";
        String localdescr="";
        List<String> blabla=null;
        List<String> text = new ArrayList<String>();

        String table="<table>";
        String head="";
        String line="";

        Feature currentFeature=getFeatureObjectFromString(featureName);

        //        String fullContent = description.getTextContent();
        for (int i=0;i<children.getLength();i++){
            //            localdescr="";
            if (children.item(i).getNodeName().equalsIgnoreCase("#text") && !children.item(i).getTextContent().trim().isEmpty()){
                text.add(children.item(i).getTextContent().trim());
            }
            if (featureName.equalsIgnoreCase("table")){
                if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                        children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("thead")){
                    head="<th>";
                    trNodes = children.item(i).getChildNodes();
                    for (int k=0;k<trNodes.getLength();k++){
                        if (trNodes.item(k).getNodeName().equalsIgnoreCase("tax:div")
                                && trNodes.item(k).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){
                            line = getTableLineContent(trNodes.item(k).getChildNodes());
                            head+=line;
                        }
                    }
                    head+="</th>";
                    table+=head;
                    line="<tr>";
                    if (children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){
                        line = getTableLineContent(children.item(i).getChildNodes());
                    }
                    line+="</tr>";
                    if (!line.equalsIgnoreCase("<tr></tr>")) {
                        table+=line;
                    }
                }
                if (children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                        children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("tr")){
                    line = getTableLineContent(children.item(i).getChildNodes());
                    if(!line.equalsIgnoreCase("<tr></tr>")) {
                        table+=line;
                    }
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                blabla= new ArrayList<String>();
                for (int j=0;j<insideNodes.getLength();j++){
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String inlinetext = getInlineText(nametosave, refMods, insideNodes.item(j));
                        if (!inlinetext.isEmpty()) {
                            blabla.add(inlinetext);
                        }
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {
                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                            //                            localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                    }
                }
                if (!blabla.isEmpty()) {
                    setParticularDescription(StringUtils.join(blabla," "),acceptedTaxon,defaultTaxon, refMods,currentFeature);
                }
                text.add(StringUtils.join(blabla," "));
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")){
                if(!children.item(i).getTextContent().trim().isEmpty()){
                    localdescr = children.item(i).getTextContent().trim();
                    setParticularDescription(localdescr,acceptedTaxon,defaultTaxon, refMods,currentFeature);
                }
            }
        }

        table+="</table>";
        if (!table.equalsIgnoreCase("<table></table>")){
            //            System.out.println("TABLE : "+table);
            text.add(table);
        }

        if (text !=null && !text.isEmpty()) {
            return StringUtils.join(text," ");
        } else {
            return "";
        }

    }

    /**
     * build a html table line, with td columns
     * @param tdNodes
     * @return an html coded line
     */
    private String getTableLineContent(NodeList tdNodes) {
        String line;
        line="<tr>";
        for (int l=0;l<tdNodes.getLength();l++){
            if (tdNodes.item(l).getNodeName().equalsIgnoreCase("tax:p")){
                line+="<td>"+tdNodes.item(l).getTextContent()+"</td>";
            }
        }
        line+="</tr>";
        return line;
    }

    /**
     * @param description: the XML node group
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @param featureName: the feature name
     */
    @SuppressWarnings({ "unused", "rawtypes" })
    private String extractSpecificFeatureNotStructured(Node description, Taxon acceptedTaxon, Taxon defaultTaxon,
            List<TaxonNameBase> nametosave, Reference<?> refMods, String featureName ) {
        NodeList children = description.getChildNodes();
        NodeList insideNodes ;
        List<String> blabla= new ArrayList<String>();


        Feature currentFeature = getFeatureObjectFromString(featureName);

        String fullContent = description.getTextContent();
        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                for (int j=0;j<insideNodes.getLength();j++){
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String inlineText =getInlineText(nametosave, refMods, insideNodes.item(j));
                        if(!inlineText.isEmpty()) {
                            blabla.add(inlineText);
                        }
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {
                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                        }
                    }
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("#text")){
                if(!children.item(i).getTextContent().trim().isEmpty()){
                    String localdescr = children.item(i).getTextContent().trim();
                    if(!localdescr.isEmpty())
                    {
                        blabla.add(localdescr);
                    }
                }
            }
        }

        if (blabla !=null && !blabla.isEmpty()) {
            setParticularDescription(StringUtils.join(blabla," "),acceptedTaxon,defaultTaxon, refMods,currentFeature);
            return StringUtils.join(blabla," ");
        } else {
            return "";
        }

    }

    /**
     * @param nametosave
     * @param refMods
     * @param insideNodes
     * @param blabla
     * @param j
     */
    @SuppressWarnings({ "rawtypes" })
    private String getInlineText(List<TaxonNameBase> nametosave, Reference<?> refMods, Node insideNode) {
        TaxonNameBase tnb = getTaxonNameBaseFromXML(insideNode, nametosave,refMods);
        //                        Taxon tax = getTaxonFromTxonNameBase(tnb, refMods);
        Taxon tax = currentMyName.getTaxon();
        if(tnb !=null){
            String linkedTaxon = tnb.toString().split("sec")[0];//TODO NOT IMPLEMENTED IN THE CDM YET
            return "<cdm:taxon uuid='"+tax.getUuid()+"'>"+linkedTaxon+"</cdm:taxon>";
        }
        return "";
    }

    /**
     * @param featureName
     * @return
     */
    @SuppressWarnings("rawtypes")
    private Feature getFeatureObjectFromString(String featureName) {
        List<DefinedTermBase> features = importer.getTermService().list(Feature.class, null,null,null,null);
        Feature currentFeature=null;
        for (DefinedTermBase feature: features){
            String tmpF = ((Feature)feature).getTitleCache();
            if (tmpF.equalsIgnoreCase(featureName)) {
                currentFeature=(Feature)feature;
                //                System.out.println("currentFeatureFromList "+currentFeature.getUuid());
            }
        }
        if (currentFeature == null) {
            currentFeature=Feature.NewInstance(featureName, featureName, featureName);
            if(featureName.equalsIgnoreCase("Other")){
                currentFeature.setUuid(OtherUUID);
            }
            if(featureName.equalsIgnoreCase(notMarkedUp)){
                currentFeature.setUuid(NotMarkedUpUUID);
            }
            importer.getTermService().saveOrUpdate(currentFeature);
        }
        return currentFeature;
    }




    /**
     * @param children: the XML node group
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current acceptedTaxon
     * @param refMods: the current reference extracted from the MODS
     * @param fullContent :the parsed XML content
     * @return a list of description (text)
     */
    @SuppressWarnings({ "unused", "rawtypes" })
    private List<String> parseParagraph(List<TaxonNameBase> nametosave, Taxon acceptedTaxon, Reference<?> refMods, Node paragraph, Feature feature){
        List<String> fullDescription=  new ArrayList<String>();
        //        String localdescr;
        String descr="";
        NodeList insideNodes ;
        boolean collectionEvent = false;
        List<Node>collectionEvents = new ArrayList<Node>();

        NodeList children = paragraph.getChildNodes();

        for (int i=0;i<children.getLength();i++){
            //            localdescr="";
            if (children.item(i).getNodeName().equalsIgnoreCase("#text") && !children.item(i).getTextContent().trim().isEmpty()){
                descr += children.item(i).getTextContent().trim();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                insideNodes=children.item(i).getChildNodes();
                List<String> blabla= new ArrayList<String>();
                for (int j=0;j<insideNodes.getLength();j++){
                    boolean nodeKnown = false;
                    //                    System.out.println("insideNodes.item(j).getNodeName() : "+insideNodes.item(j).getNodeName());
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        String inlineText = getInlineText(nametosave, refMods, insideNodes.item(j));
                        if (!inlineText.isEmpty()) {
                            blabla.add(inlineText);
                        }
                        nodeKnown=true;
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("#text")) {
                        if(!insideNodes.item(j).getTextContent().trim().isEmpty()){
                            blabla.add(insideNodes.item(j).getTextContent().trim());
                            //                            localdescr += insideNodes.item(j).getTextContent().trim();
                        }
                        nodeKnown=true;
                    }
                    if (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:bibref")) {
                        String ref = insideNodes.item(j).getTextContent().trim();
                        if (ref.endsWith(";")  && ((ref.length())>1)) {
                            ref=ref.substring(0, ref.length()-1)+".";
                        }
                        Reference<?> reference = ReferenceFactory.newGeneric();
                        reference.setTitleCache(ref, true);
                        blabla.add(reference.getTitleCache());
                        nodeKnown=true;
                    }
                    if  (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:figure")){
                        String figure = extractSpecificFeature(insideNodes.item(j),acceptedTaxon,acceptedTaxon, nametosave, refMods, "figure");
                        blabla.add(figure);
                    }
                    if(insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:div") &&
                            insideNodes.item(j).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("Other") &&
                            insideNodes.item(j).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("table")){
                        String table = extractSpecificFeature(insideNodes.item(j),acceptedTaxon,acceptedTaxon, nametosave, refMods, "table");
                        blabla.add(table);
                    }
                    if  (insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:collection_event")) {
                        //                        logger.warn("SEEMS TO BE COLLECTION EVENT INSIDE A "+feature.toString());
                        String titlecache  = extractMaterialsDirect(insideNodes.item(j), acceptedTaxon, refMods, "collection");
                        blabla.add(titlecache);
                        collectionEvent=true;
                        collectionEvents.add(insideNodes.item(j));
                        nodeKnown=true;
                    }
                    //                    if (!nodeKnown && !insideNodes.item(j).getNodeName().equalsIgnoreCase("tax:pb")) {
                    //                        logger.info("Node not handled yet : "+insideNodes.item(j).getNodeName());
                    //                    }

                }
                if (!blabla.isEmpty()) {
                    fullDescription.add(StringUtils.join(blabla," "));
                }
            }
            if  (children.item(i).getNodeName().equalsIgnoreCase("tax:figure")){
                String figure = extractSpecificFeature(children.item(i),acceptedTaxon,acceptedTaxon, nametosave, refMods, "Figure");
                fullDescription.add(figure);
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:div") &&
                    children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("Other") &&
                    children.item(i).getAttributes().getNamedItem("otherType").getNodeValue().equalsIgnoreCase("table")){
                String table = extractSpecificFeature(children.item(i),acceptedTaxon,acceptedTaxon, nametosave, refMods, "table");
                fullDescription.add(table);
            }
        }

        if(descr.length()>0){


            Feature currentFeature= getNotMarkedUpFeatureObject();
            setParticularDescription(descr,acceptedTaxon,acceptedTaxon, refMods,currentFeature);
        }
        //        if (collectionEvent) {
        //            logger.warn("SEEMS TO BE COLLECTION EVENT INSIDE A "+feature.toString());
        //            for (Node coll:collectionEvents){
        //                = extractMaterialsDirect(coll, acceptedTaxon, refMods, "collection");
        //            }
        //        }
        return fullDescription;
    }


    /**
     * @param description: the XML node group
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @param feature: the feature to link the data with
     */
    @SuppressWarnings("rawtypes")
    private void extractFeature(Node description, Taxon acceptedTaxon, Taxon defaultTaxon, List<TaxonNameBase> nametosave, Reference<?> refMods, Feature feature){
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
        List<String> fullDescription= parseParagraph( nametosave, acceptedTaxon, refMods, description,feature);

        //        System.out.println("Feature : "+feature.toString()+", "+fullDescription.toString());
        if (!fullDescription.isEmpty()) {
            setParticularDescription(StringUtils.join(fullDescription," "),acceptedTaxon,defaultTaxon, refMods,feature);
        }

    }


    /**
     * @param descr: the XML Nodegroup to parse
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param refMods: the current reference extracted from the MODS
     * @param currentFeature: the feature name
     * @return
     */
    private void setParticularDescription(String descr, Taxon acceptedTaxon, Taxon defaultTaxon, Reference<?> refMods, Feature currentFeature) {
        logger.info("setParticularDescription "+currentFeature.getTitleCache()+", \n blabla : "+descr);
        //        System.out.println("setParticularDescription "+currentFeature.getTitleCache()+", \n blabla : "+descr);
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        featuresMap.put(currentFeature.getTitleCache(),currentFeature);

        TextData textData = TextData.NewInstance();
        textData.setFeature(currentFeature);
        sourceHandler.addSource(refMods, textData);

        textData.putText(Language.UNKNOWN_LANGUAGE(), descr);

        if(! descr.isEmpty() && (acceptedTaxon!=null)){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            td.addElement(textData);
            acceptedTaxon.addDescription(td);

            sourceHandler.addAndSaveSource(refMods, td, null);
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

        if(! descr.isEmpty() && (acceptedTaxon == null) && (defaultTaxon != null)){
            try{
                Taxon tmp =(Taxon) importer.getTaxonService().find(defaultTaxon.getUuid());
                if (tmp!=null) {
                    defaultTaxon=CdmBase.deproxy(tmp,Taxon.class);
                }else{
                    importer.getTaxonService().saveOrUpdate(defaultTaxon);
                }
            }catch(Exception e){
                logger.debug("TAXON EXISTS"+defaultTaxon);
            }

            TaxonDescription td =importer.getTaxonDescription(defaultTaxon, false, true);
            defaultTaxon.addDescription(td);
            td.addElement(textData);
            sourceHandler.addAndSaveSource(refMods, td, null);
            importer.getTaxonService().saveOrUpdate(defaultTaxon);
        }
    }



    /**
     * @param descr: the XML Nodegroup to parse
     * @param acceptedTaxon: the current acceptedTaxon
     * @param defaultTaxon: the current defaultTaxon, only used if there is no accepted name
     * @param refMods: the current reference extracted from the MODS
     * @param currentFeature: the feature name
     * @return
     */
    private void setParticularDescription(String descr, Taxon acceptedTaxon, Taxon defaultTaxon,Reference<?> currentRef, Reference<?> refMods, Feature currentFeature) {
        //        System.out.println("setParticularDescriptionSPecial "+currentFeature);
        //        logger.info("acceptedTaxon: "+acceptedTaxon);
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        featuresMap.put(currentFeature.getTitleCache(),currentFeature);
        TextData textData = TextData.NewInstance();
        textData.setFeature(currentFeature);
        sourceHandler.addSource(refMods, textData);

        textData.putText(Language.UNKNOWN_LANGUAGE(), descr);

        if(! descr.isEmpty() && (acceptedTaxon!=null)){
            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
            td.addElement(textData);
            acceptedTaxon.addDescription(td);

            sourceHandler.addAndSaveSource(refMods, td, currentRef);
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }

        if(! descr.isEmpty() && (acceptedTaxon == null) && (defaultTaxon != null)){
            try{
                Taxon tmp =(Taxon) importer.getTaxonService().find(defaultTaxon.getUuid());
                if (tmp!=null) {
                    defaultTaxon=CdmBase.deproxy(tmp,Taxon.class);
                }else{
                    importer.getTaxonService().saveOrUpdate(defaultTaxon);
                }
            }catch(Exception e){
                logger.debug("TAXON EXISTS"+defaultTaxon);
            }

            TaxonDescription td =importer.getTaxonDescription(defaultTaxon, false, true);
            defaultTaxon.addDescription(td);
            td.addElement(textData);
            sourceHandler.addAndSaveSource(currentRef, td,currentRef);
            importer.getTaxonService().saveOrUpdate(defaultTaxon);
        }
    }



    /**
     * @param synonyms: the XML Nodegroup to parse
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current acceptedTaxon
     * @param refMods: the current reference extracted from the MODS
     */
    @SuppressWarnings({ "rawtypes" })
    private void extractSynonyms(Node synonyms, Taxon acceptedTaxon,Reference<?> refMods) {
        //        logger.info("extractSynonyms: "+acceptedTaxon);
        Taxon ttmp = (Taxon) importer.getTaxonService().find(acceptedTaxon.getUuid());
        if (ttmp != null) {
            acceptedTaxon = CdmBase.deproxy(ttmp,Taxon.class);
        }
        else{
            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
        }
        NodeList children = synonyms.getChildNodes();
        TaxonNameBase nameToBeFilled = null;
        List<MyName> names = new ArrayList<MyName>();

        if(synonyms.getNodeName().equalsIgnoreCase("tax:name")){
            MyName myName;
            try {
                myName = extractScientificName(synonyms,refMods);
                names.add(myName);
            } catch (TransformerFactoryConfigurationError e) {
                logger.warn(e);
            } catch (TransformerException e) {
                logger.warn(e);
            }
        }


        for (int i=0;i<children.getLength();i++){
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList tmp = children.item(i).getChildNodes();
                //                String fullContent = children.item(i).getTextContent();
                for (int j=0; j< tmp.getLength();j++){
                    if(tmp.item(j).getNodeName().equalsIgnoreCase("tax:name")){
                        MyName myName;
                        try {
                            myName = extractScientificName(tmp.item(j),refMods);
                            names.add(myName);
                        } catch (TransformerFactoryConfigurationError e) {
                            logger.warn(e);
                        } catch (TransformerException e) {
                            logger.warn(e);
                        }

                    }
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                MyName myName;
                try {
                    myName = extractScientificName(children.item(i),refMods);
                    names.add(myName);
                } catch (TransformerFactoryConfigurationError e) {
                    logger.warn(e);
                } catch (TransformerException e) {
                    logger.warn(e);
                }

            }
        }
        NomenclaturalStatusType statusType = null;

        for(MyName name:names){
            //            System.out.println("HANDLE NAME "+name);

            statusType = null;

            nameToBeFilled = currentMyName.getTaxonNameBase();

            Synonym synonym = null;

            if (!name.getStatus().isEmpty()){
                try {
                    statusType = nomStatusString2NomStatus(name.getStatus());
                    nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
                    synonym = Synonym.NewInstance(nameToBeFilled, refMods);
                } catch (UnknownCdmTypeException e) {
                    addProblematicStatusToFile(name.getStatus());
                    logger.warn("Problem with status");
                    synonym = Synonym.NewInstance(nameToBeFilled, refMods);
                    synonym.setAppendedPhrase(name.getStatus());
                }
            }
            else{
                synonym =  Synonym.NewInstance(nameToBeFilled, refMods);
            }
            /* INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
            nameToBeFilled = parser.parseFullName(name.getName(), nomenclaturalCode, name.getRank());
            if (nameToBeFilled.hasProblem() &&
                    !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                addProblemNameToFile(name.getName(),"",nomenclaturalCode,name.getRank());
                nameToBeFilled = solveNameProblem(name.getOriginalName(), name.getName(), parser,name.getAuthor(), name.getRank());
            }
            nameToBeFilled = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
             */
            if (!name.getIdentifier().isEmpty() && (name.getIdentifier().length()>2)){
                setLSID(name.getIdentifier(), synonym);
            }

            Set<Synonym> synonymsSet= acceptedTaxon.getSynonyms();
            //            System.out.println(synonym.getName()+" -- "+synonym.getSec());
            boolean synoExist = false;
            for (Synonym syn: synonymsSet){
                System.out.println(syn.getName()+" -- "+syn.getSec());
                boolean a =syn.getName().equals(synonym.getName());
                boolean b = syn.getSec().equals(synonym.getSec());
                if (a && b) {
                    synoExist=true;
                }
            }
            if (!synonymsSet.contains(synonym) && ! (synoExist)) {
                //                System.out.println("SYNONYM");
                sourceHandler.addSource(refMods, synonym);

                acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(),refMods, null);

            }
        }
        importer.getTaxonService().saveOrUpdate(acceptedTaxon);
    }


    /**
     * @param refgroup: the XML nodes
     * @param nametosave: the list of objects to save into the CDM
     * @param acceptedTaxon: the current acceptedTaxon
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @return the acceptedTaxon (why?)
     * handle cases where the bibref are inside <p> and outside
     */
    @SuppressWarnings({ "rawtypes" })
    private Taxon extractReferences(Node refgroup, List<TaxonNameBase> nametosave, Taxon acceptedTaxon, Reference<?> refMods) {
        //        logger.info("extractReferences");
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        NodeList children = refgroup.getChildNodes();
        NonViralName<?> nameToBeFilled = getNonViralNameAccNomenclature();

        ReferenceBuilder refBuild = new ReferenceBuilder();
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:bibref")){
                String ref = children.item(i).getTextContent().trim();
                refBuild.builReference(ref, treatmentMainName, nomenclaturalCode,  acceptedTaxon, refMods);
                if (!refBuild.isFoundBibref()){
                    extractReferenceRawText(children.item(i).getChildNodes(), nameToBeFilled, refMods, acceptedTaxon);
                }
            }

            if(children.item(i).getNodeName().equalsIgnoreCase("tax:p")){
                NodeList references = children.item(i).getChildNodes();
                String descr="";
                for (int j=0;j<references.getLength();j++){
                    if(references.item(j).getNodeName().equalsIgnoreCase("tax:bibref")){
                        String ref = references.item(j).getTextContent().trim();
                        refBuild.builReference(ref, treatmentMainName,  nomenclaturalCode,  acceptedTaxon, refMods);
                    }
                    else
                        if (references.item(j).getNodeName().equalsIgnoreCase("#text")
                                && !references.item(j).getTextContent().trim().isEmpty()){
                            descr += references.item(j).getTextContent().trim();
                        }

                }
                if (!refBuild.isFoundBibref()){
                    //if it's not tagged, put it as row information.
                    //                    extractReferenceRawText(references, nameToBeFilled, nametosave, refMods, acceptedTaxon);
                    //then put it as a not markup feature if not empty
                    if (descr.length()>0){
                        Feature currentFeature= getNotMarkedUpFeatureObject();
                        setParticularDescription(descr,acceptedTaxon,acceptedTaxon, refMods,currentFeature);
                    }
                }
            }
        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;

    }

    /**
     * get the non viral name according to the current nomenclature
     * @return
     */
    private NonViralName<?> getNonViralNameAccNomenclature() {
        NonViralName<?> nameToBeFilled = null;
        if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
            nameToBeFilled = BotanicalName.NewInstance(null);
        }
        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
            nameToBeFilled = ZoologicalName.NewInstance(null);
        }
        if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
            nameToBeFilled = BacterialName.NewInstance(null);
        }
        return nameToBeFilled;
    }

    /**
     * @return the feature object for the category "not marked up"
     */
    @SuppressWarnings("rawtypes")
    private Feature getNotMarkedUpFeatureObject() {
        List<DefinedTermBase> features = importer.getTermService().list(Feature.class, null,null,null,null);
        Feature currentFeature =null;
        for (DefinedTermBase feat: features){
            String tmpF = ((Feature)feat).getTitleCache();
            if (tmpF.equalsIgnoreCase(notMarkedUp)) {
                currentFeature=(Feature)feat;
            }
        }
        if (currentFeature == null) {
            currentFeature=Feature.NewInstance(notMarkedUp, notMarkedUp, notMarkedUp);
            currentFeature.setUuid(NotMarkedUpUUID);
            importer.getTermService().saveOrUpdate(currentFeature);
        }
        return currentFeature;
    }

    /**
     * @param references
     * handle cases where the bibref are inside <p> and outside
     */
    @SuppressWarnings("rawtypes")
    private void extractReferenceRawText(NodeList references, NonViralName<?> nameToBeFilled, Reference<?> refMods,
            Taxon acceptedTaxon) {
        String refString="";
        NomenclaturalStatusType statusType = null;
        currentMyName= new MyName();
        for (int j=0;j<references.getLength();j++){
            acceptedTaxon=CdmBase.deproxy(acceptedTaxon, Taxon.class);
            //no bibref tag inside
            //            System.out.println("references.item(j).getNodeName()"+references.item(j).getNodeName());
            if (references.item(j).getNodeName().equalsIgnoreCase("tax:name")){

                try {
                    currentMyName = extractScientificName(references.item(j),refMods);
                    //                    if (myName.getNewName().isEmpty()) {
                    //                        name=myName.getOriginalName()+"---"+myName.getRank()+"---"+myName.getIdentifier()+"---"+myName.getStatus();
                    //                    } else {
                    //                        name=myName.getNewName()+"---"+myName.getRank()+"---"+myName.getIdentifier()+"---"+myName.getStatus();
                    //                    }
                } catch (TransformerFactoryConfigurationError e) {
                    logger.warn(e);
                } catch (TransformerException e) {
                    logger.warn(e);
                }

                //                name=name.trim();
            }
            if (references.item(j).getNodeName().equalsIgnoreCase("#text")){
                refString = references.item(j).getTextContent().trim();
            }
            if(references.item(j).getNodeName().equalsIgnoreCase("#text") && !references.item(j).getTextContent().trim().isEmpty()){
                //
                statusType = null;
                if (!currentMyName.getStatus().isEmpty()){
                    try {
                        statusType = nomStatusString2NomStatus(currentMyName.getStatus());
                    } catch (UnknownCdmTypeException e) {
                        addProblematicStatusToFile(currentMyName.getStatus());
                        logger.warn("Problem with status");
                    }
                }


                /*INonViralNameParser parser = NonViralNameParserImpl.NewInstance();*/
                String fullLineRefName = references.item(j).getTextContent().trim();
                int nameOrRefOrOther=2;
                nameOrRefOrOther=askIfNameContained(fullLineRefName);
                //                System.out.println("NAMEORREFOR?? "+nameOrRefOrOther);
                if (nameOrRefOrOther==0){
                    /*TaxonNameBase nameTBF = parser.parseFullName(fullLineRefName, nomenclaturalCode, Rank.UNKNOWN_RANK());
                    if (nameTBF.hasProblem() &&
                            !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                        addProblemNameToFile(fullLineRefName,"",nomenclaturalCode,Rank.UNKNOWN_RANK());
                        nameTBF=solveNameProblem(fullLineRefName, fullLineRefName,parser,currentMyName.getAuthor(), currentMyName.getRank());
                    }
                    nameTBF = getTaxonNameBase(nameTBF,nametosave,statusType);
                     */
                    TaxonNameBase nameTBF = currentMyName.getTaxonNameBase();
                    Synonym synonym = null;
                    if (!currentMyName.getStatus().isEmpty()){
                        try {
                            statusType = nomStatusString2NomStatus(currentMyName.getStatus());
                            nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
                            synonym = Synonym.NewInstance(nameTBF, refMods);
                        } catch (UnknownCdmTypeException e) {
                            addProblematicStatusToFile(currentMyName.getStatus());
                            logger.warn("Problem with status");
                            synonym = Synonym.NewInstance(nameTBF, refMods);
                            synonym.setAppendedPhrase(currentMyName.getStatus());
                        }
                    }
                    else{
                        synonym =  Synonym.NewInstance(nameTBF, refMods);
                    }

                    Set<Synonym> synonymsSet= acceptedTaxon.getSynonyms();
                    //                    System.out.println(synonym.getName()+" -- "+synonym.getSec());
                    boolean synoExist = false;
                    for (Synonym syn: synonymsSet){
                        //                        System.out.println(syn.getName()+" -- "+syn.getSec());
                        boolean a =syn.getName().equals(synonym.getName());
                        boolean b = syn.getSec().equals(synonym.getSec());
                        if (a && b) {
                            synoExist=true;
                        }
                    }
                    if (!synonymsSet.contains(synonym) && ! (synoExist)) {
                        sourceHandler.addSource(refMods, synonym);

                        acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(),refMods, null);
                    }
                }

                if (nameOrRefOrOther==1){
                    Reference<?> re = ReferenceFactory.newGeneric();
                    re.setTitleCache(fullLineRefName);

                    /* TaxonNameBase nameTBF = parser.parseFullName(currentMyName.getName(), nomenclaturalCode, currentMyName.getRank());
                    if (nameTBF.hasProblem() &&
                            !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                        addProblemNameToFile(currentMyName.getName(),"",nomenclaturalCode,currentMyName.getRank());
                        nameTBF=solveNameProblem(currentMyName.getName(), currentMyName.getName(),parser,currentMyName.getAuthor(), currentMyName.getRank());
                    }
                    nameTBF = getTaxonNameBase(nameTBF,nametosave,statusType);
                     */
                    TaxonNameBase nameTBF = currentMyName.getTaxonNameBase();
                    Synonym synonym = null;
                    if (!currentMyName.getStatus().isEmpty()){
                        try {
                            statusType = nomStatusString2NomStatus(currentMyName.getStatus());
                            nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
                            synonym = Synonym.NewInstance(nameTBF, refMods);
                        } catch (UnknownCdmTypeException e) {
                            addProblematicStatusToFile(currentMyName.getStatus());
                            logger.warn("Problem with status");
                            synonym = Synonym.NewInstance(nameTBF, refMods);
                            synonym.setAppendedPhrase(currentMyName.getStatus());
                        }
                    }
                    else{
                        synonym =  Synonym.NewInstance(nameTBF, refMods);
                    }

                    Set<Synonym> synonymsSet= acceptedTaxon.getSynonyms();
                    //                    System.out.println(synonym.getName()+" -- "+synonym.getSec());
                    boolean synoExist = false;
                    for (Synonym syn: synonymsSet){
                        //                        System.out.println(syn.getName()+" -- "+syn.getSec());
                        boolean a =syn.getName().equals(synonym.getName());
                        boolean b = syn.getSec().equals(synonym.getSec());
                        if (a && b) {
                            synoExist=true;
                        }
                    }
                    if (!synonymsSet.contains(synonym) && ! (synoExist)) {
                        sourceHandler.addSource(refMods, synonym);

                        acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(),re, null);
                    }

                }


                if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                    setLSID(currentMyName.getIdentifier(), acceptedTaxon);
                }
            }

            if(!currentMyName.getName().isEmpty()){
                logger.info("acceptedTaxon and name: *"+acceptedTaxon.getTitleCache()+"*, *"+currentMyName.getName()+"*");
                if (acceptedTaxon.getTitleCache().split("sec")[0].trim().equalsIgnoreCase(currentMyName.getName().trim())){
                    Reference<?> refS = ReferenceFactory.newGeneric();
                    refS.setTitleCache(refString, true);
                    //                            TaxonDescription td =importer.getTaxonDescription(acceptedTaxon, false, true);
                    //                            acceptedTaxon.addDescription(td);
                    //                            acceptedTaxon.addSource(refSource);
                    //
                    //                            TextData textData = TextData.NewInstance(Feature.CITATION());
                    //
                    //                            textData.addSource(null, null, refS, null);
                    //                            td.addElement(textData);
                    //                            td.addSource(refSource);
                    //                            importer.getDescriptionService().saveOrUpdate(td);


                    if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                        setLSID(currentMyName.getIdentifier(), acceptedTaxon);

                    }

                    acceptedTaxon.getName().setNomenclaturalReference(refS);
                }
                else{
                    /* INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
                    TaxonNameBase nameTBF = parser.parseFullName(currentMyName.getName(), nomenclaturalCode, currentMyName.getRank());
                    if (nameTBF.hasProblem() &&
                            !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                        //            if (nameToBeFilled.hasProblem() && nameToBeFilled.getParsingProblems().contains(ParserProblem.UnparsableNamePart)){
                        addProblemNameToFile(currentMyName.getName(),"",nomenclaturalCode,currentMyName.getRank());
                        nameTBF=solveNameProblem(currentMyName.getOriginalName(), currentMyName.getName(),parser,currentMyName.getAuthor(), currentMyName.getRank());
                    }
                    nameTBF = getTaxonNameBase(nameTBF,nametosave,statusType);
                     */
                    TaxonNameBase nameTBF = currentMyName.getTaxonNameBase();
                    Synonym synonym = null;
                    if (!currentMyName.getStatus().isEmpty()){
                        try {
                            statusType = nomStatusString2NomStatus(currentMyName.getStatus());
                            nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
                            synonym = Synonym.NewInstance(nameTBF, refMods);
                        } catch (UnknownCdmTypeException e) {
                            addProblematicStatusToFile(currentMyName.getStatus());
                            logger.warn("Problem with status");
                            synonym = Synonym.NewInstance(nameTBF, refMods);
                            synonym.setAppendedPhrase(currentMyName.getStatus());
                        }
                    }
                    else{
                        synonym =  Synonym.NewInstance(nameTBF, refMods);
                    }


                    if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                        setLSID(currentMyName.getIdentifier(), synonym);
                    }

                    Set<Synonym> synonymsSet= acceptedTaxon.getSynonyms();
                    //                    System.out.println(synonym.getName()+" -- "+synonym.getSec());
                    boolean synoExist = false;
                    for (Synonym syn: synonymsSet){
                        //                        System.out.println(syn.getName()+" -- "+syn.getSec());
                        boolean a =syn.getName().equals(synonym.getName());
                        boolean b = syn.getSec().equals(synonym.getSec());
                        if (a && b) {
                            synoExist=true;
                        }
                    }
                    if (!synonymsSet.contains(synonym) && ! (synoExist)) {
                        sourceHandler.addSource(refMods, synonym);

                        acceptedTaxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF(),refMods, null);
                    }
                }
            }
            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
        }
    }



    /**
     * @param identifier
     * @param acceptedTaxon
     */
    @SuppressWarnings("rawtypes")
    private void setLSID(String identifier, TaxonBase<?> taxon) {
        //        boolean lsidok=false;
        String id = identifier.split("__")[0];
        String source = identifier.split("__")[1];
        if (id.indexOf("lsid")>-1){
            try {
                LSID lsid = new LSID(id);
                taxon.setLsid(lsid);
                //                lsidok=true;
            } catch (MalformedLSIDException e) {
                logger.warn("Malformed LSID");
            }

        }

        //  if ((id.indexOf("lsid")<0) || !lsidok){
        //ADD ORIGINAL SOURCE ID EVEN IF LSID
        Reference<?> re = null;
        List<Reference> references = importer.getReferenceService().list(Reference.class, null, null, null, null);
        for (Reference<?> refe: references) {
            if (refe.getTitleCache().equalsIgnoreCase(source)) {
                re =refe;
            }
        }

        if(re == null){
            re = ReferenceFactory.newGeneric();
            re.setTitleCache(source);
            importer.getReferenceService().saveOrUpdate(re);
        }
        re=CdmBase.deproxy(re, Reference.class);

        Set<IdentifiableSource> sources = taxon.getSources();
        boolean lsidinsource=false;
        boolean urlinsource=false;
        for (IdentifiableSource src:sources){
            if (id.equalsIgnoreCase(src.getIdInSource()) && re.getTitleCache().equals(src.getCitation().getTitleCache())) {
                lsidinsource=true;
            }
            if (src.getIdInSource() == null && re.getTitleCache().equals(sourceUrlRef.getTitleCache())) {
                urlinsource=true;
            }
        }
        if(!lsidinsource) {
            taxon.addSource(OriginalSourceType.Import, id,null,re,null);
        }
        if(!urlinsource)
        {
            taxon.addSource(OriginalSourceType.Import, null,null,sourceUrlRef,null);
            // }
        }

    }

    /**
     * try to solve a parsing problem for a scientific name
     * @param original : the name from the OCR document
     * @param name : the tagged version
     * @param parser
     * @return the corrected TaxonNameBase
     */
    /*   @SuppressWarnings({ "unchecked", "rawtypes" })
    private TaxonNameBase<?,?> solveNameProblem(String original, String name, INonViralNameParser parser, String author, Rank rank) {
        Map<String,String> ato = namesMap.get(original);
        if (ato == null) {
            ato = namesMap.get(original+" "+author);
        }


        if (ato == null && rank.equals(Rank.UNKNOWN_RANK())){
            rank=askForRank(original, Rank.UNKNOWN_RANK(), nomenclaturalCode);
        }
        if (ato != null && rank.equals(Rank.UNKNOWN_RANK())){
            rank = getRank(ato);
        }
        //        TaxonNameBase<?,?> nameTBF = parser.parseFullName(name, nomenclaturalCode, rank);
        TaxonNameBase<?,?> nameTBF = parser.parseSimpleName(name, nomenclaturalCode, rank);
        //                logger.info("RANK: "+rank);
        int retry=0;
        List<ParserProblem> problems = nameTBF.getParsingProblems();
        for (ParserProblem pb:problems) {
            System.out.println(pb.toString());
        }
        while (nameTBF.hasProblem() && (retry <1) && !((nameTBF.getParsingProblems().size()==1) && nameTBF.getParsingProblems().contains(ParserProblem.CheckRank))){
            addProblemNameToFile(name,author,nomenclaturalCode,rank);
            String fullname=name;
            if(! skippQuestion) {
                fullname =  getFullReference(name,nameTBF.getParsingProblems());
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
                nameTBF = BotanicalName.NewInstance(null);
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                nameTBF = ZoologicalName.NewInstance(null);
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
                nameTBF= BacterialName.NewInstance(null);
            }
            parser.parseReferencedName(nameTBF, fullname, rank, false);
            retry++;
        }
        if (retry == 1){
            if(author != null){
                if (name.indexOf(author)>-1) {
                    nameTBF = parser.parseSimpleName(name.substring(0, name.indexOf(author)), nomenclaturalCode, rank);
                } else {
                    nameTBF = parser.parseSimpleName(name, nomenclaturalCode, rank);
                }
                if (nameTBF.hasProblem()){
                    if (name.indexOf(author)>-1) {
                        addProblemNameToFile(name.substring(0, name.indexOf(author)),author,nomenclaturalCode,rank);
                    } else {
                        addProblemNameToFile(name,author,nomenclaturalCode,rank);
                    }
                    //                    System.out.println("TBF still has problems "+nameTBF.hasProblem());
                    problems = nameTBF.getParsingProblems();
                    for (ParserProblem pb:problems) {
                        System.out.println(pb.toString());
                    }
                    nameTBF.setFullTitleCache(name, true);
                }else{
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)) {
                        ((BotanicalName) nameTBF).setAuthorshipCache(currentMyName.getAuthor());
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
                        ((ZoologicalName) nameTBF).setAuthorshipCache(currentMyName.getAuthor());
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
                        ((BacterialName) nameTBF).setAuthorshipCache(currentMyName.getAuthor());
                    }
                }
                //                    logger.info("FULL TITLE CACHE "+name);
            }else{
                nameTBF.setFullTitleCache(name, true);
            }
        }
        return nameTBF;
    }

     */

    /**
     * @param nomenclatureNode: the XML nodes
     * @param nametosave: the list of objects to save into the CDM
     * @param refMods: the current reference extracted from the MODS
     * @return
     */
    @SuppressWarnings({ "rawtypes", "null" })
    private Taxon extractNomenclature(Node nomenclatureNode,  List<TaxonNameBase> nametosave, Reference<?> refMods) throws ClassCastException{
        //        logger.info("extractNomenclature");
        NodeList children = nomenclatureNode.getChildNodes();
        String freetext="";
        NonViralName<?> nameToBeFilled = null;
        Taxon acceptedTaxon = null;
        //   INonViralNameParser parser = NonViralNameParserImpl.NewInstance();

        //        String fullContent = nomenclatureNode.getTextContent();

        NomenclaturalStatusType statusType = null;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:status")){
                String status = children.item(i).getTextContent().trim();
                if (!status.isEmpty()){
                    try {
                        statusType = nomStatusString2NomStatus(status);
                    } catch (UnknownCdmTypeException e) {
                        addProblematicStatusToFile(currentMyName.getStatus());
                        logger.warn("Problem with status");
                    }
                }
            }
        }

        boolean containsSynonyms=false;
        for (int i=0;i<children.getLength();i++){

            if (children.item(i).getNodeName().equalsIgnoreCase("#text")) {
                freetext=children.item(i).getTextContent();
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:collection_event")) {
                //                System.out.println("COLLECTION EVENT INSIDE NOMENCLATURE");
                extractMaterialsDirect(children.item(i), acceptedTaxon, refMods, "collection");
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:name")){
                if(!containsSynonyms){
                    currentMyName = new MyName();
                    try {
                        currentMyName = extractScientificName(children.item(i),refMods);
                        treatmentMainName = currentMyName.getNewName();
                        originalTreatmentName = currentMyName.getOriginalName();

                    } catch (TransformerFactoryConfigurationError e1) {
                        logger.warn(e1);
                    } catch (TransformerException e1) {
                        logger.warn(e1);
                    }

                    if (currentMyName.getRank().equals(Rank.UNKNOWN_RANK()) || currentMyName.getRank().isLower(configState.getConfig().getMaxRank()) || currentMyName.getRank().equals(configState.getConfig().getMaxRank())){
                        maxRankRespected=true;
                        /* if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
                            nameToBeFilled = BotanicalName.NewInstance(null);
                        }
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                            nameToBeFilled = ZoologicalName.NewInstance(null);
                        }
                        if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
                            nameToBeFilled = BacterialName.NewInstance(null);
                        }
                         */
                        nameToBeFilled=currentMyName.getTaxonNameBase();

                        //                        acceptedTaxon = importer.getTaxonService().findBestMatchingTaxon(treatmentMainName);
                        acceptedTaxon=currentMyName.getTaxon();
                        System.out.println("TreatmentName "+treatmentMainName+" - "+acceptedTaxon);


                        boolean statusMatch=false;
                        if(acceptedTaxon !=null ){
                            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
                            statusMatch=compareStatus(acceptedTaxon, statusType);
                            System.out.println("statusMatch: "+statusMatch);
                        }
                        if (acceptedTaxon ==null || (acceptedTaxon != null && !statusMatch)){
                            //                            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
                            /*
                            if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)) {
                                nameToBeFilled = (BotanicalName) parser.parseFullName(treatmentMainName, nomenclaturalCode, null);
                            }
                            if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
                                nameToBeFilled = (ZoologicalName) parser.parseFullName(treatmentMainName, nomenclaturalCode, null);
                            }
                            if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
                                nameToBeFilled = (BacterialName) parser.parseFullName(treatmentMainName, nomenclaturalCode, null);
                            }


                            if (nameToBeFilled.hasProblem() &&
                                    !((nameToBeFilled.getParsingProblems().size()==1) && nameToBeFilled.getParsingProblems().contains(ParserProblem.CheckRank)) ) {
                                addProblemNameToFile(treatmentMainName,"",nomenclaturalCode,null);
                                if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)) {
                                    nameToBeFilled = (BotanicalName) solveNameProblem(originalTreatmentName,treatmentMainName,parser,currentMyName.getAuthor(), currentMyName.getRank());
                                }
                                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
                                    nameToBeFilled = (ZoologicalName)solveNameProblem(originalTreatmentName,treatmentMainName,parser,currentMyName.getAuthor(), currentMyName.getRank());
                                }
                                if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
                                    nameToBeFilled = (BacterialName) solveNameProblem(originalTreatmentName,treatmentMainName,parser,currentMyName.getAuthor(), currentMyName.getRank());
                                }

                            }
                             */
                            nameToBeFilled=currentMyName.getTaxonNameBase();
                            /*
                            try{
                                if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)) {
                                    nameToBeFilled = (BotanicalName) getTaxonNameBase(nameToBeFilled,nametosave,statusType);
                                }
                                if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
                                    nameToBeFilled = (ZoologicalName)getTaxonNameBase(nameToBeFilled,nametosave,statusType);
                                }
                                if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
                                    nameToBeFilled = (BacterialName)getTaxonNameBase(nameToBeFilled,nametosave,statusType);
                                }
                            }catch(Exception e){
                                TaxonNameBase n = getTaxonNameBase(nameToBeFilled,nametosave,statusType);
                                System.out.println(n.getClass());
                                nameToBeFilled = (NonViralName<?>) getTaxonNameBase(nameToBeFilled,nametosave,statusType);
                            }
                             */

                            if (!originalTreatmentName.isEmpty()) {
                                TaxonNameDescription td = TaxonNameDescription.NewInstance();
                                td.setTitleCache(originalTreatmentName);
                                nameToBeFilled.addDescription(td);
                            }

                            if(statusType != null) {
                                nameToBeFilled.addStatus(NomenclaturalStatus.NewInstance(statusType));
                            }
                            sourceHandler.addSource(refMods, nameToBeFilled);

                            if (nameToBeFilled.getNomenclaturalReference() == null) {
                                acceptedTaxon= new Taxon(nameToBeFilled,refMods);
                                System.out.println("NEW ACCEPTED HERE "+nameToBeFilled);
                            }
                            else {
                                acceptedTaxon= new Taxon(nameToBeFilled,(Reference<?>) nameToBeFilled.getNomenclaturalReference() );//TODO TOFIX reference
                                System.out.println("NEW ACCEPTED HERE2 "+nameToBeFilled);
                            }

                            sourceHandler.addSource(refMods, acceptedTaxon);

                            if(!configState.getConfig().doKeepOriginalSecundum()) {
                                acceptedTaxon.setSec(configState.getConfig().getSecundum());
                                logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
                            }

                            if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                                setLSID(currentMyName.getIdentifier(), acceptedTaxon);
                            }


                            importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

                            //                            Taxon parentTaxon = currentMyName.getHigherTaxa();
                            //                            if (parentTaxon == null && !skippQuestion) {
                            //                                parentTaxon =  askParent(acceptedTaxon, classification);
                            //                            }
                            //                            if (parentTaxon ==null){
                            //                                while (parentTaxon == null) {
                            //                                    parentTaxon = createParent(acceptedTaxon, refMods);
                            //                                    classification.addParentChild(parentTaxon, acceptedTaxon, refMods, null);
                            //                                }
                            //                            }else{
                            //                                classification.addParentChild(parentTaxon, acceptedTaxon, refMods, null);
                            //                            }

                        }else{
                            acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);
                            Set<IdentifiableSource> sources = acceptedTaxon.getSources();
                            boolean sourcelinked=false;
                            for (IdentifiableSource source:sources){
                                if (source.getCitation().getTitleCache().equalsIgnoreCase(refMods.getTitleCache())) {
                                    sourcelinked=true;
                                }
                            }
                            if (!configState.getConfig().doKeepOriginalSecundum()) {
                                acceptedTaxon.setSec(configState.getConfig().getSecundum());
                                logger.info("SET SECUNDUM "+configState.getConfig().getSecundum());
                            }
                            if (!sourcelinked){
                                sourceHandler.addSource(refMods, acceptedTaxon);
                            }
                            if (!sourcelinked || !configState.getConfig().doKeepOriginalSecundum()){

                                if (!currentMyName.getIdentifier().isEmpty() && (currentMyName.getIdentifier().length()>2)){
                                    setLSID(currentMyName.getIdentifier(), acceptedTaxon);
                                }
                                importer.getTaxonService().saveOrUpdate(acceptedTaxon);
                            }
                        }
                    }else{
                        maxRankRespected=false;
                    }
                    containsSynonyms=true;
                }else{
                    extractSynonyms(children.item(i), acceptedTaxon, refMods);
                }
            }
            if (children.item(i).getNodeName().equalsIgnoreCase("tax:ref_group") && maxRankRespected){
                reloadClassification();
                //extract the References within the document
                extractReferences(children.item(i),nametosave,acceptedTaxon,refMods);
            }
            if(!freetext.isEmpty()) {
                setParticularDescription(freetext,acceptedTaxon,acceptedTaxon, refMods,getNotMarkedUpFeatureObject());
            }

        }
        //        importer.getClassificationService().saveOrUpdate(classification);
        return acceptedTaxon;
    }


    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean compareStatus(Taxon t, NomenclaturalStatusType statusType) {
        boolean statusMatch=false;
        //found one taxon
        Set<NomenclaturalStatus> status = t.getName().getStatus();
        if (statusType!=null && status.size()>0){ //the statusType is known for both taxon
            for (NomenclaturalStatus st:status){
                NomenclaturalStatusType stype = st.getType();
                if (stype.toString().equalsIgnoreCase(statusType.toString())) {
                    statusMatch=true;
                }
            }
        }
        else{
            if(statusType == null && status.size()==0) {//there is no statusType, we can assume it's the same
                statusMatch=true;
            }
        }
        return statusMatch;
    }

    /**
     * @param acceptedTaxon: the current acceptedTaxon
     * @param ref: the current reference extracted from the MODS
     * @return the parent for the current accepted taxon
     */
    /*  private Taxon createParent(Taxon acceptedTaxon, Reference<?> ref) {
        acceptedTaxon = CdmBase.deproxy(acceptedTaxon, Taxon.class);

        List<Rank> rankList = new ArrayList<Rank>();
        rankList = importer.getTermService().listByTermClass(Rank.class, null, null, null, null);

        List<String> rankListStr = new ArrayList<String>();
        for (Rank r:rankList) {
            rankListStr.add(r.toString());
        }
        String r="";
        String s = acceptedTaxon.getTitleCache();
        Taxon tax = null;
        if(!skippQuestion){
            int addTaxon = askAddParent(s);
            logger.info("ADD TAXON: "+addTaxon);
            if (addTaxon == 0 ){
                Taxon tmp = askParent(acceptedTaxon, classification);
                if (tmp == null){
                    s = askSetParent(s);
                    r = askRank(s,rankListStr);

                    NonViralName<?> nameToBeFilled = null;
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)){
                        nameToBeFilled = BotanicalName.NewInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)){
                        nameToBeFilled = ZoologicalName.NewInstance(null);
                    }
                    if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)){
                        nameToBeFilled = BacterialName.NewInstance(null);
                    }
                    nameToBeFilled.setTitleCache(s);
                    nameToBeFilled.setRank(getRank(r));

                    tax = Taxon.NewInstance(nameToBeFilled, ref);
                }
                else{
                    tax=tmp;
                }

                createParent(tax, ref);
                //            logger.info("add parent child "+tax.getTitleCache()+", "+acceptedTaxon.getTitleCache());
                classification.addParentChild(tax, acceptedTaxon, ref, null);
            }
            else{
                classification.addChildTaxon(acceptedTaxon, ref, null);
                tax=acceptedTaxon;
            }
        } else{
            classification.addChildTaxon(acceptedTaxon, ref, null);
            tax=acceptedTaxon;
        }
        //        logger.info("RETURN: "+tax );
        return tax;

    }

     */

    /**
     * @param name
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerException
     * @return a list of possible names
     */
    @SuppressWarnings({ "null", "rawtypes" })
    private MyName extractScientificName(Node name, Reference<?> refMods) throws TransformerFactoryConfigurationError, TransformerException {
        //        System.out.println("extractScientificName");

        String[] rankListToPrint_tmp ={"dwc:genus","dwc:specificepithet","dwc:species","dwc:subspecies", "dwc:infraspecificepithet","dwc:scientificnameauthorship"};
        List<String> rankListToPrint = new ArrayList<String>();
        for (String r : rankListToPrint_tmp) {
            rankListToPrint.add(r.toLowerCase());
        }

        Rank rank = Rank.UNKNOWN_RANK();
        NodeList children = name.getChildNodes();
        String fullName = "";
        String newName="";
        String identifier="";
        HashMap<String, String> atomisedMap = new HashMap<String, String>();
        List<String> atomisedName= new ArrayList<String>();

        String rankStr = "";
        Rank tmpRank ;

        String status="";
        //        NomenclaturalStatusType statusType = null;
        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:status") ||
                    (children.item(i).getNodeName().equalsIgnoreCase("tax:namePart") &&
                            children.item(i).getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase("status"))){
                status = children.item(i).getTextContent().trim();
            }
        }

        for (int i=0;i<children.getLength();i++){
            if(children.item(i).getNodeName().equalsIgnoreCase("tax:xmldata")){
                NodeList atom = children.item(i).getChildNodes();
                for (int k=0;k<atom.getLength();k++){
                    if (atom.item(k).getNodeName().equalsIgnoreCase("tax:xid")){
                        try{
                            identifier = atom.item(k).getAttributes().getNamedItem("identifier").getNodeValue();
                        }catch(Exception e){
                            System.out.println("pb with identifier, maybe empty");
                        }
                        try{
                            identifier+="__"+atom.item(k).getAttributes().getNamedItem("source").getNodeValue();
                        }catch(Exception e){
                            System.out.println("pb with identifier, maybe empty");
                        }
                    }
                    tmpRank = null;
                    rankStr = atom.item(k).getNodeName().toLowerCase();
                    //                    logger.info("RANKSTR:*"+rankStr+"*");
                    if (rankStr.equalsIgnoreCase("dwc:taxonRank")) {
                        rankStr=atom.item(k).getTextContent().trim();
                        tmpRank = getRank(rankStr);
                    }
                    //                    if ((tmpRank != null) && (tmpRank.isLower(rank) || rank.equals(Rank.UNKNOWN_RANK()))) {
                    if (tmpRank != null){
                        rank=tmpRank;
                    }
                    atomisedMap.put(rankStr.toLowerCase(),atom.item(k).getTextContent().trim());
                }
                for (int k=0;k<atom.getLength();k++){
                    if (!atom.item(k).getNodeName().equalsIgnoreCase("dwc:taxonRank") ) {
                        if (atom.item(k).getNodeName().equalsIgnoreCase("dwc:subgenus") || atom.item(k).getNodeName().equalsIgnoreCase("dwcranks:subgenus")) {
                            atomisedName.add("("+atom.item(k).getTextContent().trim()+")");
                        } else{
                            if(atom.item(k).getNodeName().equalsIgnoreCase("dwcranks:varietyepithet") || atom.item(k).getNodeName().equalsIgnoreCase("dwc:Subspecies")) {
                                if(atom.item(k).getNodeName().equalsIgnoreCase("dwcranks:varietyepithet")){
                                    atomisedName.add("var. "+atom.item(k).getTextContent().trim());
                                }
                                if(atom.item(k).getNodeName().equalsIgnoreCase("dwc:Subspecies") || atom.item(k).getNodeName().equalsIgnoreCase("dwc:infraspecificepithet")) {
                                    atomisedName.add("subsp. "+atom.item(k).getTextContent().trim());
                                }
                            }
                            else{
                                if(rankListToPrint.contains(atom.item(k).getNodeName().toLowerCase())) {
                                    atomisedName.add(atom.item(k).getTextContent().trim());
                                }
                                else{
                                    //                                    System.out.println("rank : "+rank.toString());
                                    if (rank.isHigher(Rank.GENUS()) && (atom.item(k).getNodeName().indexOf("dwcranks:")>-1 || atom.item(k).getNodeName().indexOf("dwc:Family")>-1)) {
                                        atomisedName.add(atom.item(k).getTextContent().trim());
                                    }
                                    //                                    else{
                                    //                                      System.out.println("on a oublie qqn "+atom.item(k).getNodeName());
                                    //                                  }
                                }
                                //                                else{
                                //                                    System.out.println("on a oublie qqn "+atom.item(k).getNodeName());
                                //                                }
                            }
                        }
                    }
                }
            }
            if(children.item(i).getNodeName().equalsIgnoreCase("#text") && !StringUtils.isBlank(children.item(i).getTextContent())){
                //                logger.info("name non atomised: "+children.item(i).getTextContent());
                fullName = children.item(i).getTextContent().trim();
                //                logger.info("fullname: "+fullName);
            }
        }
        if (fullName != null){
            fullName = fullName.replace("( ", "(");
            fullName = fullName.replace(" )",")");

        }
        if (fullName.trim().isEmpty()){
            fullName=StringUtils.join(atomisedName," ");
        }

        while(fullName.contains("  ")) {
            fullName=fullName.replace("  ", " ");
            //            logger.info("while");
        }

        fullName=fullName.trim();

        namesMap.put(fullName,atomisedMap);
        String atomisedNameStr = StringUtils.join(atomisedName," ");
        while(atomisedNameStr.contains("  ")) {
            atomisedNameStr=atomisedNameStr.replace("  ", " ");
            //            logger.info("atomisedNameStr: "+atomisedNameStr);
        }
        atomisedNameStr=atomisedNameStr.trim();

        if (fullName != null){
            //            System.out.println("fullname: "+fullName);
            //            System.out.println("atomised: "+atomisedNameStr);
            if (!fullName.equalsIgnoreCase(atomisedNameStr)) {
                if (skippQuestion){
                    //                    String defaultN = "";
                    if (atomisedNameStr.length()>fullName.length()) {
                        newName=atomisedNameStr;
                    } else {
                        if (fullName.length()>atomisedNameStr.length() && (rank.isLower(Rank.SPECIES()) && fullName.length()>2 && !fullName.substring(0, 1).equals("."))) {
                            newName=getScientificName(fullName,atomisedNameStr,classification.getTitleCache(),name);
                        } else {
                            newName=fullName;
                        }
                    }
                } else {
                    newName=getScientificName(fullName,atomisedNameStr,classification.getTitleCache(),name);
                }
            } else {
                newName=fullName;
            }
        }
        //not really needed
        //        rank = askForRank(newName, rank, nomenclaturalCode);
        //        System.out.println("atomised: "+atomisedMap.toString());

        //        String[] names = new String[5];
        MyName myname = new MyName();

        System.out.println("\n\nBUILD "+newName+ "(rank: "+rank+")");
        //        System.out.println(atomisedMap.keySet());
        if (atomisedMap.get("dwc:scientificnameauthorship") == null && fullName!=null){
            //            System.out.println("rank : "+rank.toString());
            if(rank.isHigher(Rank.SPECIES())){
                try{
                    String author=null;
                    if(atomisedMap.get("dwcranks:subgenus") != null) {
                        author = fullName.split(atomisedMap.get("dwcranks:subgenus"))[1].trim();
                    }
                    if(atomisedMap.get("dwc:subgenus") != null) {
                        author = fullName.split(atomisedMap.get("dwc:subgenus"))[1].trim();
                    }
                    if(author == null) {
                        if(atomisedMap.get("dwc:genus") != null) {
                            author = fullName.split(atomisedMap.get("dwc:genus"))[1].trim();
                        }
                    }
                    if(author != null){
                        fullName = fullName.substring(0, fullName.indexOf(author));
                        myname.setAuthor(author);
                    }
                }catch(Exception e){
                    //could not extract the author
                }
            }
            if(rank.equals(Rank.SPECIES())){
                try{
                    String author=null;
                    if(author == null) {
                        if(atomisedMap.get("dwc:species") != null) {
                            String[] t = fullName.split(atomisedMap.get("dwc:species"));
                            //                            System.out.println("NB ELEMENTS "+t.length +"fullName "+fullName+", "+atomisedMap.get("dwc:species"));
                            author = fullName.split(atomisedMap.get("dwc:species"))[1].trim();
                            //                            System.out.println("AUTEUR "+author);
                        }
                    }
                    if(author != null){
                        fullName = fullName.substring(0, fullName.indexOf(author));
                        myname.setAuthor(author);
                    }
                }catch(Exception e){
                    //could not extract the author
                }
            }
        }else{
            myname.setAuthor(atomisedMap.get("dwc:scientificnameauthorship"));
        }
        myname.setOriginalName(fullName);
        myname.setNewName(newName);
        myname.setRank(rank);
        myname.setIdentifier(identifier);
        myname.setStatus(status);
        myname.setSource(refMods);

        //        boolean higherAdded=false;


        boolean parseNameManually=false;
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
        TaxonNameBase  nameToBeFilledTest = parser.parseFullName(atomisedNameStr, nomenclaturalCode, rank);
        if (nameToBeFilledTest.hasProblem()){
            addProblemNameToFile("ato",atomisedNameStr,nomenclaturalCode,rank, nameToBeFilledTest.getParsingProblems().toString());
            nameToBeFilledTest = parser.parseFullName(fullName, nomenclaturalCode,rank);
            if (nameToBeFilledTest.hasProblem()){
                addProblemNameToFile("full",fullName,nomenclaturalCode,rank, nameToBeFilledTest.getParsingProblems().toString());
                parseNameManually=true;
            }
        }

        if(parseNameManually){

            if(atomisedMap.get("dwc:family") != null && checkRankValidForImport(Rank.FAMILY())){
                myname.setFamily(myname.findOrCreateTaxon(atomisedMap.get("dwc:family"),newName, Rank.FAMILY(),rank));
            }
            if(atomisedMap.get("dwcranks:subfamily") != null  && checkRankValidForImport(Rank.SUBFAMILY())){
                myname.setSubfamily(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subfamily"), newName,Rank.SUBFAMILY(),rank));
            }
            if(atomisedMap.get("dwcranks:tribe") != null && checkRankValidForImport(Rank.TRIBE())){
                myname.setTribe(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:tribe"),newName, Rank.TRIBE(),rank));
            }
            if(atomisedMap.get("dwcranks:subtribe") != null && checkRankValidForImport(Rank.SUBTRIBE())){
                myname.setSubtribe(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subtribe"),newName, Rank.SUBTRIBE(),rank));
            }
            if(atomisedMap.get("dwc:genus") != null && checkRankValidForImport(Rank.GENUS())){
                myname.setGenus(myname.findOrCreateTaxon(atomisedMap.get("dwc:genus"),newName, Rank.GENUS(),rank));
            }
            if(atomisedMap.get("dwcranks:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS())){
                myname.setSubgenus(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:subgenus"),newName, Rank.SUBGENUS(),rank));
            }
            if(atomisedMap.get("dwc:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS())){
                myname.setSubgenus(myname.findOrCreateTaxon(atomisedMap.get("dwc:subgenus"),newName, Rank.SUBGENUS(),rank));
            }
            if(atomisedMap.get("dwc:species") != null && checkRankValidForImport(Rank.SPECIES())){
                String n=newName;
                if(atomisedMap.get("dwc:infraspecificepithet") != null) {
                    n=newName.split(atomisedMap.get("dwc:infraspecificepithet"))[0];
                    n=n.replace("subsp.","");
                }
                if(atomisedMap.get("dwc:subspecies") != null) {
                    n=newName.split(atomisedMap.get("dwc:subspecies"))[0];
                    n=n.replace("subsp.","");
                }
                if(atomisedMap.get("dwcranks:varietyepithet") != null) {
                    n=newName.split(atomisedMap.get("dwcranks:varietyepithet"))[0];
                    n=n.replace("var.","");
                    n=n.replace("v.","");
                }
                if(atomisedMap.get("dwcranks:formepithet") != null) {
                    //TODO
                    System.out.println("TODO FORMA");
                    n=newName.split(atomisedMap.get("dwcranks:formepithet"))[0];
                    n=n.replace("forma","");
                }
                n=n.trim();
                String author = myname.getAuthor();
                if(n.split(" ").length>2)
                {
                    String n2=n.split(" ")[0]+" "+n.split(" ")[1];
                    String a= n.split(n2)[1].trim();
                    myname.setAuthor(a);
                    System.out.println("FINDCREATESPECIES --"+n2+"--"+n+"**"+a+"##");
                    n=n2;

                }

                myname.setSpecies(myname.findOrCreateTaxon(atomisedMap.get("dwc:species"),n, Rank.SPECIES(),rank));
                myname.setAuthor(author);
            }
            if(atomisedMap.get("dwc:subspecies") != null && checkRankValidForImport(Rank.SUBSPECIES())){
                myname.setSubspecies(myname.findOrCreateTaxon(atomisedMap.get("dwc:subspecies"), newName,Rank.SUBSPECIES(),rank));
            }
            if(atomisedMap.get("dwc:infraspecificepithet") != null && checkRankValidForImport(Rank.SUBSPECIES())){
                myname.setSubspecies(myname.findOrCreateTaxon(atomisedMap.get("dwc:infraspecificepithet"),newName, Rank.SUBSPECIES(),rank));
            }
            if(atomisedMap.get("dwcranks:varietyepithet") != null && checkRankValidForImport(Rank.VARIETY())){
                myname.setVariety(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:varietyepithet"),newName, Rank.VARIETY(),rank));
            }
            if(atomisedMap.get("dwcranks:formepithet") != null && checkRankValidForImport(Rank.FORM())){
                myname.setForm(myname.findOrCreateTaxon(atomisedMap.get("dwcranks:formepithet"), newName,Rank.FORM(),rank));
            }
        }
        else{
            if(atomisedMap.get("dwc:family") != null && checkRankValidForImport(Rank.FAMILY())){
                myname.setFamilyStr(atomisedMap.get("dwc:family"));
            }
            if(atomisedMap.get("dwcranks:subfamily") != null  && checkRankValidForImport(Rank.SUBFAMILY())){
                myname.setSubfamilyStr(atomisedMap.get("dwcranks:subfamily"));
            }
            if(atomisedMap.get("dwcranks:tribe") != null && checkRankValidForImport(Rank.TRIBE())){
                myname.setTribeStr(atomisedMap.get("dwcranks:tribe"));
            }
            if(atomisedMap.get("dwcranks:subtribe") != null && checkRankValidForImport(Rank.SUBTRIBE())){
                myname.setSubtribeStr(atomisedMap.get("dwcranks:subtribe"));
            }
            if(atomisedMap.get("dwc:genus") != null && checkRankValidForImport(Rank.GENUS())){
                myname.setGenusStr(atomisedMap.get("dwc:genus"));
            }
            if(atomisedMap.get("dwcranks:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS())){
                myname.setSubgenusStr(atomisedMap.get("dwcranks:subgenus"));
            }
            if(atomisedMap.get("dwc:subgenus") != null && checkRankValidForImport(Rank.SUBGENUS())){
                myname.setSubgenusStr(atomisedMap.get("dwc:subgenus"));
            }
            if(atomisedMap.get("dwc:species") != null && checkRankValidForImport(Rank.SPECIES())){
                String n=newName;
                if(atomisedMap.get("dwc:infraspecificepithet") != null) {
                    n=newName.split(atomisedMap.get("dwc:infraspecificepithet"))[0];
                    n=n.replace("subsp.","");
                }
                if(atomisedMap.get("dwc:subspecies") != null) {
                    n=newName.split(atomisedMap.get("dwc:subspecies"))[0];
                    n=n.replace("subsp.","");
                }
                if(atomisedMap.get("dwcranks:varietyepithet") != null) {
                    n=newName.split(atomisedMap.get("dwcranks:varietyepithet"))[0];
                    n=n.replace("var.","");
                    n=n.replace("v.","");
                }
                if(atomisedMap.get("dwcranks:formepithet") != null) {
                    //TODO
                    System.out.println("TODO FORMA");
                    n=newName.split(atomisedMap.get("dwcranks:formepithet"))[0];
                    n=n.replace("forma","");
                }
                n=n.trim();
                String author = myname.getAuthor();
                if(n.split(" ").length>2)
                {
                    String n2=n.split(" ")[0]+" "+n.split(" ")[1];
                    String a= n.split(n2)[1].trim();
                    myname.setAuthor(a);
                    System.out.println("FINDCREATESPECIES --"+n2+"--"+n+"**"+a+"##");
                    n=n2;

                }

                myname.setSpeciesStr(atomisedMap.get("dwc:species"));
                myname.setAuthor(author);
            }
            if(atomisedMap.get("dwc:subspecies") != null && checkRankValidForImport(Rank.SUBSPECIES())){
                myname.setSubspeciesStr(atomisedMap.get("dwc:subspecies"));
            }
            if(atomisedMap.get("dwc:infraspecificepithet") != null && checkRankValidForImport(Rank.SUBSPECIES())){
                myname.setSubspeciesStr(atomisedMap.get("dwc:infraspecificepithet"));
            }
            if(atomisedMap.get("dwcranks:varietyepithet") != null && checkRankValidForImport(Rank.VARIETY())){
                myname.setVarietyStr(atomisedMap.get("dwcranks:varietyepithet"));
            }
            if(atomisedMap.get("dwcranks:formepithet") != null && checkRankValidForImport(Rank.FORM())){
                myname.setFormStr(atomisedMap.get("dwcranks:formepithet"));
            }
            myname.setParsedName(nameToBeFilledTest);
            myname.buildTaxon();


        }




        return myname;

    }

    /**
     * @return
     */
    private boolean checkRankValidForImport(Rank currentRank) {
        return currentRank.isLower(configState.getConfig().getMaxRank()) || currentRank.equals(configState.getConfig().getMaxRank());
    }



    /**
     * @param classification2
     */
    public void updateClassification(Classification classification2) {
        classification = classification2;
    }

    public class MyName {
        String originalName="";
        String newName="";
        Rank rank=Rank.UNKNOWN_RANK();
        String identifier="";
        String status="";
        String author=null;

        NonViralName<?> taxonnamebase;

        Reference<?> refMods ;

        Taxon family,subfamily,tribe,subtribe,genus,subgenus,species,subspecies, variety,form;
        NonViralName<?> familyName, subfamilyName, tribeName,subtribeName,genusName,subgenusName,speciesName,subspeciesName;
        String familyStr, subfamilyStr, tribeStr,subtribeStr,genusStr,subgenusStr,speciesStr,subspeciesStr,formStr,varietyStr;
        Taxon higherTaxa;
        Rank higherRank;
        private Taxon taxon;


        public void setSource(Reference<?> re){
            refMods=re;
        }
        /**
         * @param string
         */
        public void setFormStr(String string) {
            this.formStr=string;

        }
        /**
         * @param string
         */
        public void setVarietyStr(String string) {
            this.varietyStr=string;

        }
        /**
         * @param string
         */
        public void setSubspeciesStr(String string) {
            this.subspeciesStr=string;

        }
        /**
         * @param string
         */
        public void setSpeciesStr(String string) {
            this.speciesStr=string;

        }
        /**
         * @param string
         */
        public void setSubgenusStr(String string) {
            this.subgenusStr=string;

        }
        /**
         * @param string
         */
        public void setGenusStr(String string) {
            this.genusStr=string;

        }
        /**
         * @param string
         */
        public void setSubtribeStr(String string) {
            this.subtribeStr=string;

        }
        /**
         * @param string
         */
        public void setTribeStr(String string) {
            this.tribeStr=string;

        }
        /**
         * @param string
         */
        public void setSubfamilyStr(String string) {
            this.subfamilyStr=string;

        }
        /**
         * @param string
         */
        public void setFamilyStr(String string) {
            this.familyStr=string;

        }
        /**
         * @return the familyStr
         */
        public String getFamilyStr() {
            return familyStr;
        }
        /**
         * @return the subfamilyStr
         */
        public String getSubfamilyStr() {
            return subfamilyStr;
        }
        /**
         * @return the tribeStr
         */
        public String getTribeStr() {
            return tribeStr;
        }
        /**
         * @return the subtribeStr
         */
        public String getSubtribeStr() {
            return subtribeStr;
        }
        /**
         * @return the genusStr
         */
        public String getGenusStr() {
            return genusStr;
        }
        /**
         * @return the subgenusStr
         */
        public String getSubgenusStr() {
            return subgenusStr;
        }
        /**
         * @return the speciesStr
         */
        public String getSpeciesStr() {
            return speciesStr;
        }
        /**
         * @return the subspeciesStr
         */
        public String getSubspeciesStr() {
            return subspeciesStr;
        }
        /**
         * @return the formStr
         */
        public String getFormStr() {
            return formStr;
        }
        /**
         * @return the varietyStr
         */
        public String getVarietyStr() {
            return varietyStr;
        }
        /**
         *
         */
        public void buildTaxon() {
            System.out.println("BUILD TAXON");

            NomenclaturalStatusType statusType = null;
            if (!getStatus().isEmpty()){
                try {
                    statusType = nomStatusString2NomStatus(getStatus());
                    taxonnamebase.addStatus(NomenclaturalStatus.NewInstance(statusType));
                } catch (UnknownCdmTypeException e) {
                    addProblematicStatusToFile(getStatus());
                    logger.warn("Problem with status");
                }
            }
            importer.getNameService().save(taxonnamebase);
            Taxon tmptaxon = Taxon.NewInstance(taxonnamebase, refMods); //sec set null

            boolean exist = false;
            for (TaxonNode p : classification.getAllNodes()){
                if(p.getTaxon().getTitleCache().equalsIgnoreCase(tmptaxon.getTitleCache())) {
                    if(compareStatus(p.getTaxon(), statusType)){
                    tmptaxon=CdmBase.deproxy(p.getTaxon(), Taxon.class);
                    exist =true;
                    }
                }
            }
            if (!exist){
                if (taxonnamebase.getRank().equals(configState.getConfig().getMaxRank())) {
                    classification.addChildTaxon(tmptaxon, refMods, null);
                } else{
                    hierarchy = new HashMap<Rank, Taxon>();
                    lookForParentNode(taxonnamebase,tmptaxon, refMods,this);
                    System.out.println("HIERARCHY "+hierarchy);
                    Taxon parent = buildHierarchy();
                    if(!taxonExistsInClassification(parent,tmptaxon)){
                    classification.addParentChild(parent, tmptaxon, refMods, null);
                    importer.getClassificationService().saveOrUpdate(classification);
                    }
                    Set<TaxonNode> nodeList = classification.getAllNodes();
                    for(TaxonNode tn:nodeList) {
                        System.out.println(tn.getTaxon());
                    }
                }
                importer.getClassificationService().saveOrUpdate(classification);
                //            refreshTransaction();
            }
            taxon=CdmBase.deproxy(tmptaxon, Taxon.class);

        }
        /**
         *
         */
        private Taxon buildHierarchy() {
            Taxon higherTaxon = null;
            if(hierarchy.containsKey(configState.getConfig().getMaxRank())){
                if(!taxonExistsInClassification(higherTaxon, hierarchy.get(configState.getConfig().getMaxRank()))) {
                    classification.addChildTaxon(hierarchy.get(configState.getConfig().getMaxRank()), refMods, null);
                }
                higherTaxon = hierarchy.get(configState.getConfig().getMaxRank());
            }
            if(hierarchy.containsKey(Rank.SUBFAMILY())){
                if(!taxonExistsInClassification(higherTaxon, hierarchy.get(Rank.SUBFAMILY()))) {
                    classification.addParentChild(higherTaxon, hierarchy.get(Rank.SUBFAMILY()), refMods, null);
                }
                higherTaxon=hierarchy.get(Rank.SUBFAMILY());
            }
            if(hierarchy.containsKey(Rank.TRIBE())){
                if(!taxonExistsInClassification(higherTaxon, hierarchy.get(Rank.TRIBE()))) {
                    classification.addParentChild(higherTaxon, hierarchy.get(Rank.TRIBE()), refMods, null);
                }
                higherTaxon=hierarchy.get(Rank.TRIBE());
            }
            if(hierarchy.containsKey(Rank.SUBTRIBE())){
                if(!taxonExistsInClassification(higherTaxon, hierarchy.get(Rank.SUBTRIBE()))) {
                    classification.addParentChild(higherTaxon, hierarchy.get(Rank.SUBTRIBE()), refMods, null);
                }
                higherTaxon=hierarchy.get(Rank.SUBTRIBE());
            }
            if(hierarchy.containsKey(Rank.GENUS())){
                if(!taxonExistsInClassification(higherTaxon, hierarchy.get(Rank.GENUS()))) {
                    classification.addParentChild(higherTaxon, hierarchy.get(Rank.GENUS()), refMods, null);
                }
                higherTaxon=hierarchy.get(Rank.GENUS());
            }
            if(hierarchy.containsKey(Rank.SUBGENUS())){
                if(!taxonExistsInClassification(higherTaxon, hierarchy.get(Rank.SUBGENUS()))) {
                    classification.addParentChild(higherTaxon, hierarchy.get(Rank.SUBGENUS()), refMods, null);
                }
                higherTaxon=hierarchy.get(Rank.SUBGENUS());
            }
            importer.getClassificationService().saveOrUpdate(classification);
            return higherTaxon;
        }

        private boolean taxonExistsInClassification(Taxon parent, Taxon child){
            System.out.println("LOOK IF TAXA EXIST "+parent+", "+child);
            boolean found=false;
            if(parent !=null){
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                        for (TaxonNode c : p.getChildNodes()) {
                            if (c.getTaxon().getTitleCache().equalsIgnoreCase(child.getTitleCache())) {
                                found=true;
                                break;
                            }
                        }
                    }
                }
            }
            else{
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(child.getTitleCache())) {
                        found=true;
                        break;
                    }
                }
            }
            System.out.println("LOOK IF TAXA EXIST? "+found);
            return found;
        }
        /**
         * @param nameToBeFilledTest
         */
        @SuppressWarnings("rawtypes")
        public void setParsedName(TaxonNameBase nameToBeFilledTest) {
            this.taxonnamebase = (NonViralName<?>) nameToBeFilledTest;

        }
        //variety dwcranks:varietyEpithet
        /**
         * @return the author
         */
        public String getAuthor() {
            return author;
        }
        /**
         * @return
         */
        public Taxon getTaxon() {
            return taxon;
        }
        /**
         * @return
         */
        public NonViralName<?> getTaxonNameBase() {
            return taxonnamebase;
        }

        /**
         * @param findOrCreateTaxon
         */
        public void setForm(Taxon form) {
            this.form=form;

        }
        /**
         * @param findOrCreateTaxon
         */
        public void setVariety(Taxon variety) {
            this.variety=variety;

        }
        /**
         * @param string
         * @return
         */
        @SuppressWarnings("rawtypes")
        public Taxon findOrCreateTaxon(String partialname,String fullname, Rank rank, Rank globalrank) {
            if(fullname.indexOf("opulifolium")>-1) {
                System.out.println("\nLOOKFOR "+partialname+"--"+fullname+"( rank: "+rank+")");
            }
            List<TaxonBase> tmpList = importer.getTaxonService().list(Taxon.class, 0, 0, null, null);

            NomenclaturalStatusType statusType = null;
            if (!getStatus().isEmpty()){
                try {
                    statusType = nomStatusString2NomStatus(getStatus());
                } catch (UnknownCdmTypeException e) {
                    addProblematicStatusToFile(getStatus());
                    logger.warn("Problem with status");
                }
            }

            boolean foundIdentic=false;
            Taxon tmp=null;
            //            Taxon tmpPartial=null;
            for (TaxonBase tmpb:tmpList){
                if(tmpb !=null){
                    TaxonNameBase tnb =  tmpb.getName();
                    Rank crank=null;
                    if (tnb != null){
                        //                        System.out.println(tnb.getTitleCache());
                        //                        if (tnb.getTitleCache().split("sec.")[0].equals(partialname) ||tnb.getTitleCache().split("sec.")[0].equals(fullname) ){
                        if(globalrank.equals(rank) || (globalrank.isLower(Rank.SPECIES()) && rank.equals(Rank.SPECIES()))){
                            if (tnb.getTitleCache().split("sec.")[0].equals(fullname) ){
                                crank =tnb.getRank();
                                if (crank !=null && rank !=null){
                                    if (crank.equals(rank)){
                                        foundIdentic=true;
                                        try{
                                            tmp=(Taxon)tmpb;
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            if(fullname.indexOf(partialname)<0){ //for corrected names such as Anochetus -- A. blf-pat
                                if (tnb.getTitleCache().split("sec.")[0].equals(partialname) ){
                                    crank =tnb.getRank();
                                    if (crank !=null && rank !=null){
                                        if (crank.equals(rank)){
                                            foundIdentic=true;
                                            try{
                                                tmp=(Taxon)tmpb;
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            if (tnb.getTitleCache().split("sec.")[0].equals(partialname) ){
                                crank =tnb.getRank();
                                if (crank !=null && rank !=null){
                                    if (crank.equals(rank)){
                                        foundIdentic=true;
                                        try{
                                            tmp=(Taxon)tmpb;
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            boolean statusMatch=false;
            boolean appendedMatch=false;
            if(tmp !=null && foundIdentic){
                statusMatch=compareStatus(tmp, statusType);
                if (!getStatus().isEmpty() && ! (tmp.getAppendedPhrase() == null)) {
                    appendedMatch=tmp.getAppendedPhrase().equals(getStatus());
                }
                if (getStatus().isEmpty() && tmp.getAppendedPhrase() == null) {
                    appendedMatch=true;
                }
                if(fullname.indexOf("opulifolium")>-1) {
                    System.out.println("TMP: "+tmp+", "+statusMatch+", "+appendedMatch);
                }
            }
            if ((tmp == null || !foundIdentic) ||  (tmp != null && !statusMatch) ||  (tmp != null && !appendedMatch && !statusMatch)){
                if(fullname.indexOf("opulifolium")>-1) {
                    System.out.println("CREATE: --"+fullname+"--, **"+partialname+"**"+rank);
                }
                NonViralName<?> tnb = getNonViralNameAccNomenclature();
                tnb.setRank(rank);

                if(statusType != null) {
                    tnb.addStatus(NomenclaturalStatus.NewInstance(statusType));
                }
                if(getStatus()!=null) {
                    tnb.setAppendedPhrase(getStatus());
                }

                if(rank.equals(Rank.UNKNOWN_RANK())) {
                    tnb.setTitleCache(fullname);
                }

                if(rank.isHigher(Rank.SPECIES())) {
                    tnb.setTitleCache(partialname);
                }

                if (rank.equals(globalrank) && author != null) {
                    if(fullname.indexOf("opulifolium")>-1) {
                        System.out.println("AUTOR: "+author);
                    }
                    tnb.setCombinationAuthorTeam(findOrCreateAuthor(author));
                    if (getIdentifier() !=null && !getIdentifier().isEmpty()){
                        Taxon taxonLSID = getTaxonByLSID(getIdentifier());
                        if (taxonLSID !=null) {
                            tmp=taxonLSID;
                        }
                    }
                }

                if(tmp == null){
                    if (rank.equals(Rank.FAMILY())) {
                        tnb.generateTitle();
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        classification.addChildTaxon(tmp, null, null);
                    }
                    if (rank.equals(Rank.SUBFAMILY())) {
                        tnb.generateTitle();
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        if(family != null) {
                            classification.addParentChild(family, tmp, null, null);
                            higherRank=Rank.FAMILY();
                            higherTaxa=family;
                        } else {
                            classification.addChildTaxon(tmp, null, null);
                        }
                    }
                    if (rank.equals(Rank.TRIBE())) {
                        tnb.generateTitle();
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        if (subfamily !=null) {
                            classification.addParentChild(subfamily, tmp, null, null);
                            higherRank=Rank.SUBFAMILY();
                            higherTaxa=subfamily;
                        } else {
                            if(family != null) {
                                classification.addParentChild(family, tmp, null, null);
                                higherRank=Rank.FAMILY();
                                higherTaxa=family;
                            }
                            else{
                                classification.addChildTaxon(tmp, null, null);
                            }
                        }
                    }
                    if (rank.equals(Rank.SUBTRIBE())) {
                        tnb.generateTitle();
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        if(tribe != null) {
                            classification.addParentChild(tribe, tmp, null, null);
                            higherRank=Rank.TRIBE();
                            higherTaxa=tribe;
                        } else{
                            classification.addChildTaxon(tmp, null, null);
                        }
                    }
                    if (rank.equals(Rank.GENUS())) {
                        tnb.setGenusOrUninomial(partialname);
                        tnb.generateTitle();

                        if(partialname.equals("Anochetus")) {
                            System.out.println("CREATE NEW TAXON ANOCHETUS "+tnb.getTitleCache());
                        }
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        if(subtribe != null) {
                            classification.addParentChild(subtribe, tmp, null, null);
                            higherRank=Rank.SUBTRIBE();
                            higherTaxa=subtribe;
                        } else{
                            if(tribe !=null) {
                                classification.addParentChild(tribe, tmp, null, null);
                                higherRank=Rank.TRIBE();
                                higherTaxa=tribe;
                            } else{
                                if(subfamily !=null) {
                                    classification.addParentChild(subfamily, tmp, null, null);
                                    higherRank=Rank.SUBFAMILY();
                                    higherTaxa=subfamily;
                                } else
                                    if(family !=null) {
                                        classification.addParentChild(family, tmp, null, null);
                                        higherRank=Rank.FAMILY();
                                        higherTaxa=family;
                                    }
                                    else{classification.addChildTaxon(tmp, null, null);}
                            }
                        }
                    }

                    if (rank.equals(Rank.SUBGENUS())) {
                        tnb.setInfraGenericEpithet(partialname);
                        if (genus !=null) {
                            tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
                        }
                        tnb.generateTitle();
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        if(genus != null) {
                            classification.addParentChild(genus, tmp, null, null);
                            higherRank=Rank.GENUS();
                            higherTaxa=genus;
                        } else{
                            classification.addChildTaxon(tmp, null, null);
                        }
                    }
                    if (rank.equals(Rank.SPECIES())) {
                        if (genus !=null) {
                            tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
                        }
                        if (subgenus !=null) {
                            tnb.setInfraGenericEpithet(subgenusName.getInfraGenericEpithet());
                        }
                        tnb.setSpecificEpithet(partialname);
                        tnb.generateTitle();
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        if (subgenus !=null) {
                            classification.addParentChild(subgenus, tmp, null, null);
                            higherRank=Rank.SUBGENUS();
                            higherTaxa=subgenus;
                        } else {
                            if (genus !=null) {
                                classification.addParentChild(genus, tmp, null, null);
                                higherRank=Rank.GENUS();
                                higherTaxa=genus;
                            }
                            else{
                                classification.addChildTaxon(tmp, null, null);
                            }
                        }
                    }

                    if (rank.equals(Rank.SUBSPECIES())) {
                        if (genus !=null) {
                            tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
                        }
                        if (subgenus !=null) {
                            //                            System.out.println("SUB:"+subgenusName.getInfraGenericEpithet());
                            tnb.setInfraGenericEpithet(subgenusName.getInfraGenericEpithet());
                        }
                        if(species !=null) {
                            //                            System.out.println("SPE:"+speciesName.getSpecificEpithet());
                            tnb.setSpecificEpithet(speciesName.getSpecificEpithet());
                        }
                        tnb.setInfraSpecificEpithet(partialname);
                        tnb.generateTitle();
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);

                        if(species != null) {
                            classification.addParentChild(species, tmp, null, null);
                            higherRank=Rank.SPECIES();
                            higherTaxa=species;
                        }
                        else{
                            classification.addChildTaxon(tmp, null, null);
                        }
                    }

                    if (rank.equals(Rank.VARIETY())) {
                        if (genus !=null) {
                            tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
                        }
                        if (subgenus !=null) {
                            tnb.setInfraGenericEpithet(subgenusName.getInfraGenericEpithet());
                        }
                        if(species !=null) {
                            tnb.setSpecificEpithet(speciesName.getSpecificEpithet());
                        }
                        if(subspecies != null) {
                            tnb.setInfraSpecificEpithet(subspeciesName.getSpecificEpithet());
                        }
                        //TODO how to save variety?
                        tnb.setTitleCache(fullname, true);
                        //                    tnb.generateTitle();
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        if (subspecies !=null) {
                            classification.addParentChild(subspecies, tmp, null, null);
                            higherRank=Rank.SUBSPECIES();
                            higherTaxa=subspecies;
                        } else {
                            if(species !=null) {
                                classification.addParentChild(species, tmp, null, null);
                                higherRank=Rank.SPECIES();
                                higherTaxa=species;
                            }
                            else{
                                classification.addChildTaxon(tmp, null, null);
                            }
                        }
                    }

                    if (rank.equals(Rank.FORM())) {
                        if (genus !=null) {
                            tnb.setGenusOrUninomial(genusName.getGenusOrUninomial());
                        }
                        if (subgenus !=null) {
                            tnb.setInfraGenericEpithet(subgenusName.getInfraGenericEpithet());
                        }
                        if(species !=null) {
                            tnb.setSpecificEpithet(speciesName.getSpecificEpithet());
                        }
                        if(subspecies != null) {
                            tnb.setInfraSpecificEpithet(subspeciesName.getInfraSpecificEpithet());
                        }
                        tnb.generateTitle();
                        //TODO how to save form??
                        tnb.setTitleCache(fullname, true);
                        tmp = Taxon.NewInstance(tnb, sourceUrlRef);
                        sourceHandler.addSource(refMods, tmp);
                        if (subspecies !=null) {
                            classification.addParentChild(subspecies, tmp, null, null);
                            higherRank=Rank.SUBSPECIES();
                            higherTaxa=subspecies;
                        } else {
                            if (species !=null) {
                                classification.addParentChild(species, tmp, null, null);
                                higherRank=Rank.SPECIES();
                                higherTaxa=species;
                            }
                            else{
                                classification.addChildTaxon(tmp, null, null);
                            }
                        }
                    }

                    importer.getClassificationService().saveOrUpdate(classification);
                }
            }

            tmp = CdmBase.deproxy(tmp, Taxon.class);
            if (rank.equals(globalrank) && author != null) {
                if (!getIdentifier().isEmpty() && (getIdentifier().length()>2)){
                    setLSID(getIdentifier(), tmp);
                    importer.getTaxonService().saveOrUpdate(tmp);
                    tmp = CdmBase.deproxy(tmp, Taxon.class);
                }
            }
            TaxonNameBase tnb = CdmBase.deproxy(tmp.getName(), TaxonNameBase.class);

            this.taxon=tmp;
            castTaxonNameBase(tnb, taxonnamebase);
            return tmp;
        }
        /**
         * @param tnb
         * cast the current taxonnamebase into a botanical name or zoological or bacterial name
         * if errors, cast into a classis nonviralname
         * @param taxonnamebase2
         */
        @SuppressWarnings("rawtypes")
        private void castTaxonNameBase(TaxonNameBase tnb, NonViralName<?> taxonnamebase2) {
            if (nomenclaturalCode.equals(NomenclaturalCode.ICNAFP)) {
                try{
                    taxonnamebase2=(BotanicalName) tnb;
                }catch(Exception e){
                    taxonnamebase2= (NonViralName<?>) tnb;
                }
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICZN)) {
                try{
                    taxonnamebase2=(ZoologicalName) tnb;
                }catch(Exception e){
                    taxonnamebase2= (NonViralName<?>) tnb;
                }
            }
            if (nomenclaturalCode.equals(NomenclaturalCode.ICNB)) {
                try{
                    taxonnamebase2=(BacterialName) tnb;
                }catch(Exception e){
                    taxonnamebase2= (NonViralName<?>) tnb;
                }
            }
        }
        /**
         * @param identifier2
         * @return
         */
        @SuppressWarnings("rawtypes")
        private Taxon getTaxonByLSID(String identifier) {
            //            boolean lsidok=false;
            String id = identifier.split("__")[0];
            //            String source = identifier.split("__")[1];
            LSID lsid = null;
            if (id.indexOf("lsid")>-1){
                try {
                    lsid = new LSID(id);
                    //                    lsidok=true;
                } catch (MalformedLSIDException e) {
                    logger.warn("Malformed LSID");
                }
            }
            if (lsid !=null){
                List<TaxonBase> taxons = importer.getTaxonService().list(Taxon.class, 0, 0, null, null);
                LSID currentlsid=null;
                for (TaxonBase t:taxons){
                    currentlsid = t.getLsid();
                    if (currentlsid !=null){
                        if (currentlsid.getLsid().equals(lsid.getLsid())){
                            try{
                                return (Taxon) t;
                            }
                            catch(Exception e){logger.warn("Exception occurred while comparing LSIDs "+e );}
                        }
                    }
                }
            }
            return null;
        }
        /**
         * @param author2
         * @return
         */
        @SuppressWarnings("rawtypes")
        private Person findOrCreateAuthor(String author2) {
            List<UuidAndTitleCache<Person>> hiberPersons = importer.getAgentService().getPersonUuidAndTitleCache();
            for (UuidAndTitleCache<Person> hibernateP:hiberPersons){
                if(hibernateP.getTitleCache().equals(author2)) {
                    AgentBase existing = importer.getAgentService().find(hibernateP.getUuid());
                    return CdmBase.deproxy(existing, Person.class);
                }
            }
            Person p = Person.NewInstance();
            p.setTitleCache(author2,true);
            importer.getAgentService().saveOrUpdate(p);
            return CdmBase.deproxy(p, Person.class);
        }
        /**
         * @param author the author to set
         */
        public void setAuthor(String author) {
            this.author = author;
        }

        /**
         * @return the higherTaxa
         */
        public Taxon getHigherTaxa() {
            return higherTaxa;
        }
        /**
         * @param higherTaxa the higherTaxa to set
         */
        public void setHigherTaxa(Taxon higherTaxa) {
            this.higherTaxa = higherTaxa;
        }
        /**
         * @return the higherRank
         */
        public Rank getHigherRank() {
            return higherRank;
        }
        /**
         * @param higherRank the higherRank to set
         */
        public void setHigherRank(Rank higherRank) {
            this.higherRank = higherRank;
        }
        public String getName(){
            if (newName.isEmpty()) {
                return originalName;
            } else {
                return newName;
            }

        }
        /**
         * @return the fullName
         */
        public String getOriginalName() {
            return originalName;
        }
        /**
         * @param fullName the fullName to set
         */
        public void setOriginalName(String fullName) {
            this.originalName = fullName;
        }
        /**
         * @return the newName
         */
        public String getNewName() {
            return newName;
        }
        /**
         * @param newName the newName to set
         */
        public void setNewName(String newName) {
            this.newName = newName;
        }
        /**
         * @return the rank
         */
        public Rank getRank() {
            return rank;
        }
        /**
         * @param rank the rank to set
         */
        public void setRank(Rank rank) {
            this.rank = rank;
        }
        /**
         * @return the idenfitiger
         */
        public String getIdentifier() {
            return identifier;
        }
        /**
         * @param idenfitiger the idenfitiger to set
         */
        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
        /**
         * @return the status
         */
        public String getStatus() {
            if (status == null) {
                return "";
            }
            return status;
        }
        /**
         * @param status the status to set
         */
        public void setStatus(String status) {
            this.status = status;
        }
        /**
         * @return the family
         */
        public Taxon getFamily() {
            return family;
        }
        /**
         * @param family the family to set
         */
        @SuppressWarnings("rawtypes")
        public void setFamily(Taxon family) {
            this.family = family;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(family.getName(), TaxonNameBase.class);
            castTaxonNameBase(taxonNameBase,familyName);
        }
        /**
         * @return the subfamily
         */
        public Taxon getSubfamily() {
            return subfamily;
        }
        /**
         * @param subfamily the subfamily to set
         */
        @SuppressWarnings("rawtypes")
        public void setSubfamily(Taxon subfamily) {
            this.subfamily = subfamily;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(subfamily.getName(), TaxonNameBase.class);
            castTaxonNameBase(taxonNameBase,subfamilyName);
        }
        /**
         * @return the tribe
         */
        public Taxon getTribe() {
            return tribe;
        }
        /**
         * @param tribe the tribe to set
         */
        @SuppressWarnings("rawtypes")
        public void setTribe(Taxon tribe) {
            this.tribe = tribe;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(tribe.getName(), TaxonNameBase.class);
            castTaxonNameBase(taxonNameBase,tribeName);
        }
        /**
         * @return the subtribe
         */
        public Taxon getSubtribe() {
            return subtribe;
        }
        /**
         * @param subtribe the subtribe to set
         */
        @SuppressWarnings("rawtypes")
        public void setSubtribe(Taxon subtribe) {
            this.subtribe = subtribe;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(subtribe.getName(), TaxonNameBase.class);
            castTaxonNameBase(taxonNameBase,subtribeName);
        }
        /**
         * @return the genus
         */
        public Taxon getGenus() {
            return genus;
        }
        /**
         * @param genus the genus to set
         */
        @SuppressWarnings("rawtypes")
        public void setGenus(Taxon genus) {
            this.genus = genus;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(genus.getName(), TaxonNameBase.class);
            castTaxonNameBase(taxonNameBase,genusName);
        }
        /**
         * @return the subgenus
         */
        public Taxon getSubgenus() {
            return subgenus;
        }
        /**
         * @param subgenus the subgenus to set
         */
        @SuppressWarnings("rawtypes")
        public void setSubgenus(Taxon subgenus) {
            this.subgenus = subgenus;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(subgenus.getName(), TaxonNameBase.class);
            castTaxonNameBase(taxonNameBase,subgenusName);
        }
        /**
         * @return the species
         */
        public Taxon getSpecies() {
            return species;
        }
        /**
         * @param species the species to set
         */
        public void setSpecies(Taxon species) {
            this.species = species;
            @SuppressWarnings("rawtypes")
            TaxonNameBase taxonNameBase = CdmBase.deproxy(species.getName(), TaxonNameBase.class);
            castTaxonNameBase(taxonNameBase,speciesName);

        }
        /**
         * @return the subspecies
         */
        public Taxon getSubspecies() {
            return subspecies;
        }
        /**
         * @param subspecies the subspecies to set
         */
        @SuppressWarnings("rawtypes")
        public void setSubspecies(Taxon subspecies) {
            this.subspecies = subspecies;
            TaxonNameBase taxonNameBase = CdmBase.deproxy(subspecies.getName(), TaxonNameBase.class);
            castTaxonNameBase(taxonNameBase,subspeciesName);

        }



    }


    /**
     * @param status
     */
    private void addProblematicStatusToFile(String status) {
        try{
            FileWriter fstream = new FileWriter("/home/pkelbert/Bureau/StatusUnknown_"+classification.getTitleCache()+".txt",true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(status+"\n");
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }


    Map<Rank, Taxon> hierarchy = new HashMap<Rank, Taxon>();
    /**
     * @param taxonnamebase
     */
    @SuppressWarnings("rawtypes")
    public void lookForParentNode(NonViralName<?> taxonnamebase, Taxon tax, Reference<?> ref, MyName myName) {
        System.out.println("LOOK FOR PARENT NODE "+taxonnamebase.toString()+"; "+tax.toString()+"; "+taxonnamebase.getRank());
        INonViralNameParser parser = NonViralNameParserImpl.NewInstance();
        if (taxonnamebase.getRank().equals(Rank.SPECIES())){
            String parentStr = myName.getSubgenusStr();
            Rank r = Rank.SUBGENUS();

            if(parentStr==null){
                parentStr = myName.getGenusStr();
                r = Rank.GENUS();
            }

            if(parentStr==null){
                parentStr = myName.getSubtribeStr();
                r = Rank.SUBTRIBE();
            }
            if (parentStr == null){
                parentStr = myName.getTribeStr();
                r = Rank.TRIBE();
            }
            if (parentStr == null){
                parentStr = myName.getSubfamilyStr();
                r = Rank.SUBFAMILY();
            }
            if (parentStr == null){
                parentStr = myName.getFamilyStr();
                r = Rank.FAMILY();
            }
            if(parentStr!=null){
                NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
                Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
                //                    importer.getTaxonService().save(parent);
                //                    parent = CdmBase.deproxy(parent, Taxon.class);

                boolean parentDoesNotExists = true;
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                        System.out.println(p.getTaxon().getUuid());
                        System.out.println(parent.getUuid());
                        parentDoesNotExists = false;
                        parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                        break;
                    }
                }
                if(parentDoesNotExists) {
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                    lookForParentNode(parentNameName, parent, ref,myName);
                }

                hierarchy.put(r,parent);
            }
        }
        if (taxonnamebase.getRank().equals(Rank.SUBGENUS())){
            String parentStr = myName.getGenusStr();
            Rank r = Rank.GENUS();

            if(parentStr==null){
                parentStr = myName.getSubtribeStr();
                r = Rank.SUBTRIBE();
            }
            if (parentStr == null){
                parentStr = myName.getTribeStr();
                r = Rank.TRIBE();
            }
            if (parentStr == null){
                parentStr = myName.getSubfamilyStr();
                r = Rank.SUBFAMILY();
            }
            if (parentStr == null){
                parentStr = myName.getFamilyStr();
                r = Rank.FAMILY();
            }
            if(parentStr!=null){
                NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
                Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
                //                    importer.getTaxonService().save(parent);
                //                    parent = CdmBase.deproxy(parent, Taxon.class);

                boolean parentDoesNotExists = true;
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                        System.out.println(p.getTaxon().getUuid());
                        System.out.println(parent.getUuid());
                        parentDoesNotExists = false;
                        parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                        break;
                    }
                }
                if(parentDoesNotExists) {
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                    lookForParentNode(parentNameName, parent, ref,myName);
                }
                hierarchy.put(r,parent);
            }
        }

        if (taxonnamebase.getRank().equals(Rank.GENUS())){
            String parentStr = myName.getSubtribeStr();
            Rank r = Rank.SUBTRIBE();
            if (parentStr == null){
                parentStr = myName.getTribeStr();
                r = Rank.TRIBE();
            }
            if (parentStr == null){
                parentStr = myName.getSubfamilyStr();
                r = Rank.SUBFAMILY();
            }
            if (parentStr == null){
                parentStr = myName.getFamilyStr();
                r = Rank.FAMILY();
            }
            if(parentStr!=null){
                NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
                Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
                //                    importer.getTaxonService().save(parent);
                //                    parent = CdmBase.deproxy(parent, Taxon.class);

                boolean parentDoesNotExists = true;
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                        System.out.println(p.getTaxon().getUuid());
                        System.out.println(parent.getUuid());
                        parentDoesNotExists = false;
                        parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                        break;
                    }
                }
                if(parentDoesNotExists) {
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                    lookForParentNode(parentNameName, parent, ref,myName);
                }
                hierarchy.put(r,parent);
            }
        }
        if (taxonnamebase.getRank().equals(Rank.SUBTRIBE())){
            String parentStr = myName.getTribeStr();
            Rank r = Rank.TRIBE();
            if (parentStr == null){
                parentStr = myName.getSubfamilyStr();
                r = Rank.SUBFAMILY();
            }
            if (parentStr == null){
                parentStr = myName.getFamilyStr();
                r = Rank.FAMILY();
            }
            if(parentStr!=null){
                NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
                Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
                //                    importer.getTaxonService().save(parent);
                //                    parent = CdmBase.deproxy(parent, Taxon.class);

                boolean parentDoesNotExists = true;
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                        parentDoesNotExists = false;
                        parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);

                        break;
                    }
                }
                if(parentDoesNotExists) {
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                    lookForParentNode(parentNameName, parent, ref,myName);
                }
                hierarchy.put(r,parent);
            }
        }
        if (taxonnamebase.getRank().equals(Rank.TRIBE())){
            String parentStr = myName.getSubfamilyStr();
            Rank r = Rank.SUBFAMILY();
            if (parentStr == null){
                parentStr = myName.getFamilyStr();
                r = Rank.FAMILY();
            }
            if(parentStr!=null){
                NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
                Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
                //                    importer.getTaxonService().save(parent);
                //                    parent = CdmBase.deproxy(parent, Taxon.class);

                boolean parentDoesNotExists = true;
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                        parentDoesNotExists = false;
                        parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                        break;
                    }
                }
                if(parentDoesNotExists) {
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                    lookForParentNode(parentNameName, parent, ref,myName);
                }
                hierarchy.put(r,parent);
            }
        }

        if (taxonnamebase.getRank().equals(Rank.SUBFAMILY())){
            String parentStr = myName.getFamilyStr();
            Rank r = Rank.FAMILY();
            if(parentStr!=null){
                NonViralName<?> parentNameName =  (NonViralName<?>) parser.parseFullName(parentStr, nomenclaturalCode, r);
                Taxon parent = Taxon.NewInstance(parentNameName, ref); //sec set null
                //                    importer.getTaxonService().save(parent);
                //                    parent = CdmBase.deproxy(parent, Taxon.class);

                boolean parentDoesNotExists = true;
                for (TaxonNode p : classification.getAllNodes()){
                    if(p.getTaxon().getTitleCache().equalsIgnoreCase(parent.getTitleCache())) {
                        parentDoesNotExists = false;
                        parent=CdmBase.deproxy(p.getTaxon(),    Taxon.class);
                        break;
                    }
                }
                if(parentDoesNotExists) {
                    importer.getTaxonService().save(parent);
                    parent = CdmBase.deproxy(parent, Taxon.class);
                    lookForParentNode(parentNameName, parent, ref,myName);
                }
                hierarchy.put(r,parent);
            }
        }
    }

    /**
     * @param name
     * @param author
     * @param nomenclaturalCode2
     * @param rank
     */
    private void addProblemNameToFile(String name, String author, NomenclaturalCode nomenclaturalCode2, Rank rank) {
        try{
            FileWriter fstream = new FileWriter("/home/pkelbert/Bureau/NameNotParsed.txt",true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(name+"\t"+replaceNull(author)+"\t"+replaceNull(nomenclaturalCode2)+"\t"+replaceNull(rank)+"\n");
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private String replaceNull(Object in){
        if (in == null) {
            return "";
        }
        if (in.getClass().equals(NomenclaturalCode.class)) {
            return ((NomenclaturalCode)in).getTitleCache();
        }
        return in.toString();
    }

    /**
     * @param fullName
     * @param nomenclaturalCode2
     * @param rank
     */
    private void addProblemNameToFile(String type, String name, NomenclaturalCode nomenclaturalCode2, Rank rank, String problems) {
        try{
            FileWriter fstream = new FileWriter("/home/pkelbert/Bureau/NameNotParsed_"+classification.getTitleCache()+".txt",true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(type+"\t"+name+"\t"+replaceNull(nomenclaturalCode2)+"\t"+replaceNull(rank)+"\t"+problems+"\n");
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }


}



