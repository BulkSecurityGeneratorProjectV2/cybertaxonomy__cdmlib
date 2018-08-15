/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.taxon.TaxonRelationshipFormatter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * DTO to transfer a list of taxon relationships for a given taxon.
 *
 * @author a.mueller
 * @since 15.08.2018
 */
public class TaxonRelationshipsDTO {

    public class TaxonRelation{

        private UUID relationUuid;
        private boolean doubtful = false;
        private boolean misapplication = false;
        private boolean synonym = false;
        private Direction direction;
        private UUID taxonUuid;
        private String cache;
        private List<TaggedText> taggedText;
        //TODO maybe this will be changed in future
        private TermDto type;


        public TaxonRelation(TaxonRelationship relation, Direction direction, List<Language> languages) {
            Taxon relatedTaxon = direction == Direction.relatedTo? relation.getToTaxon()
                    : relation.getFromTaxon();
            this.taxonUuid = relatedTaxon.getUuid();
            this.doubtful = relation.isDoubtful();
            this.relationUuid = relation.getUuid();
            this.direction = direction;
            TaxonRelationshipType relType = relation.getType();
            if (relType != null){
                this.misapplication = relType.isAnyMisappliedName();
                this.synonym = relType.isAnySynonym();
                //TODO there must be a better DTO which also includes
                Set<Representation> representations = direction.isDirect() ? relType.getRepresentations() : relType.getInverseRepresentations();
                this.setType(new TermDto(relType.getUuid(), representations, relType.getOrderIndex()));
                this.misapplication = relation.getType().isAnyMisappliedName();
            }
            List<TaggedText> tags = new TaxonRelationshipFormatter().getTaggedText(
                    relation, direction == Direction.relatedFrom, languages);
            this.taggedText = tags;
            this.setCache(TaggedCacheHelper.createString(tags));
        }


        public UUID getTaxonUuid() {
            return taxonUuid;
        }
        public void setTaxonUuid(UUID taxonUuid) {
            this.taxonUuid = taxonUuid;
        }
        public boolean isDoubtful() {
            return doubtful;
        }
        public void setDoubtful(boolean doubtful) {
            this.doubtful = doubtful;
        }
        public UUID getRelationUuid() {
            return relationUuid;
        }
        public void setRelationUuid(UUID relationUuid) {
            this.relationUuid = relationUuid;
        }

        public Direction getDirection() {
            return direction;
        }
        public void setDirection(Direction direction) {
            this.direction = direction;
        }



        @Override
        public String toString(){
            return taxonUuid == null? super.toString() : taxonUuid.toString();
        }

        public String getCache() {
            return cache;
        }
        public void setCache(String cache) {
            this.cache = cache;
        }

        public List<TaggedText> getTaggedText() {
            return taggedText;
        }
//        public void setTaggedText(List<TaggedText> taggedText) {
//            this.taggedText = taggedText;
//        }

        public boolean isMisapplication() {
            return misapplication;
        }
        public void setMisapplication(boolean misapplication) {
            this.misapplication = misapplication;
        }

        public boolean isSynonym() {
            return synonym;
        }
        public void setSynonym(boolean synonym) {
            this.synonym = synonym;
        }

        public TermDto getType() {
            return type;
        }
        public void setType(TermDto type) {
            this.type = type;
        }

    }

    private List<TaxonRelation> relations = new ArrayList<>();

    private List<List<TaggedText>> misapplications = new ArrayList<>();

    private DateTime date = DateTime.now();

    //** ******************* CONSTRUCTOR **************************/

    public TaxonRelationshipsDTO() {}

//    public TaxonRelationshipsDTO(UUID taxonUuid) {
//        IncludedTaxon originalTaxon = new TaxonRelationshipsDTO(taxonUuid, false);
//        includedTaxa.add(originalTaxon);
//    }

 // ************************** GETTER / SETTER  ***********************/

    public List<TaxonRelation> getRelations() {
        return relations;
    }

    public void setIncludedTaxa(List<TaxonRelation> relations) {
        this.relations = relations;
    }

    public void addRelation(TaxonRelation relation){
        relations.add(relation);
    }

    /**
     * @param relation
     * @param direction
     */
    public void addRelation(TaxonRelationship relation, Direction direction, List<Language> languages) {
        TaxonRelation newRelation = new TaxonRelation(relation, direction, languages);
        relations.add(newRelation);
    }


    /**
     *
     */
    public void createMisapplicationString() {
        List<List<TaggedText>> result = new ArrayList<>();

        for (TaxonRelation relation: relations){
            if (relation.isMisapplication()){
                List<TaggedText> tags = relation.getTaggedText();

                boolean isDuplicate = false;;
                for (List<TaggedText> existing: result){
                    isDuplicate = mergeIfDuplicate(existing, tags);
                    if (isDuplicate){
                        break;
                    }
                }
                if (!isDuplicate){
                    List<TaggedText> newTags = new ArrayList<>(tags.size());
                    newTags.addAll(tags);
                    result.add(newTags);
                }
            }
        }
        this.setMisapplications(result);
    }

    /**
     * Checks if all tags are equal, except for the sensuReference tags
     * @param existing
     * @param tags
     * @return
     */
    private boolean mergeIfDuplicate(List<TaggedText> first, List<TaggedText> second) {
        int i = 0;
        int j = 0;
        int sensuEndInFirst = -1;
        int sensuStartInSecond = -1;
        int lastEndInSecond = -1;

        while (i < first.size() && j< second.size()){
            if (tagEqualsMisapplied(first.get(i), second.get(i))){
                i++;j++;
            }else{
                while (i < first.size() && tagIsSensu(first.get(i))){
                    i++;
                }
                sensuEndInFirst = i;
                sensuStartInSecond = j;
                while (j< second.size() && tagIsSensu(second.get(j))){
                    j++;
                }
                lastEndInSecond = j;
            }
        }
        boolean isDuplicate = (i == first.size() || j == second.size());
        if (isDuplicate && sensuEndInFirst > -1 && sensuStartInSecond > -1){
            first.addAll(sensuEndInFirst, second.subList(sensuStartInSecond, lastEndInSecond));
            return true;
        }else{
            return false;
        }
    }

    /**
     * @param taggedText
     * @return
     */
    private boolean tagIsSensu(TaggedText tag) {
        if (tag.getType() == TagEnum.sensuReference ||
                tag.getType() == TagEnum.sensuMicroReference){
            return true;
        }
        return false;
    }

    /**
     * @param x
     * @param y
     * @return
     */
    private boolean tagEqualsMisapplied(TaggedText x, TaggedText y) {
        if (CdmUtils.nullSafeEqual(x.getText(),y.getText())
                && x.getType().equals(y.getType())){
            //TODO entity
            return true;
        }else{
            return false;
        }
    }

    public DateTime getDate() {
        return date;
    }
    public void setDate(DateTime date) {
        this.date = date;
    }

    public int getSize(){
        return relations.size();
    }
//
//    public boolean contains(UUID taxonUuid) {
//        for (TaxonRelation relation: relations){
//            if (taxon.taxonUuid.equals(taxonUuid)){
//                return true;
//            }
//        }
//        return false;
//    }

    @Override
    public String toString(){
        String result = "";
        for (TaxonRelation relation : relations){
            result += relation.toString() + ",";
        }
        if (result.length() > 0){
            result = result.substring(0, result.length() - 1);
        }

        result = "[" + result + "]";
        return result;
    }

    /**
     * @return the misapplications
     */
    public List<List<TaggedText>> getMisapplications() {
        return misapplications;
    }

    /**
     * @param misapplications the misapplications to set
     */
    public void setMisapplications(List<List<TaggedText>> misapplications) {
        this.misapplications = misapplications;
    }


}
