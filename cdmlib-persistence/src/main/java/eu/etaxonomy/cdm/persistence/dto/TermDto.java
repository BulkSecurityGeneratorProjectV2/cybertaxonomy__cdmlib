/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author andreas
 * @since Mar 25, 2015
 *
 */
public class TermDto extends AbstractTermDto{

    private static final long serialVersionUID = 5627308906985438034L;

    private UUID kindOfUuid = null;
    private UUID partOfUuid = null;
    private UUID vocabularyUuid = null;
    private Integer orderIndex = null;
    private String idInVocabulary = null;
    private Collection<TermDto> includes;
    private Collection<TermDto> generalizationOf;

    public TermDto(UUID uuid, Set<Representation> representations, Integer orderIndex) {
        super(uuid, representations);
        this.setOrderIndex(orderIndex);
    }

    public TermDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID vocabularyUuid, Integer orderIndex) {
        this(uuid, representations, partOfUuid, null, vocabularyUuid, orderIndex, null);
    }

    public TermDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID kindOfUuid, UUID vocabularyUuid, Integer orderIndex, String idInVocabulary) {
        super(uuid, representations);
        this.partOfUuid = partOfUuid;
        this.kindOfUuid = kindOfUuid;
        this.vocabularyUuid = vocabularyUuid;
        this.orderIndex = orderIndex;
        this.idInVocabulary = idInVocabulary;
    }

    static public TermDto fromNamedArea(NamedArea namedArea) {
        TermDto dto = new TermDto(namedArea.getUuid(), namedArea.getRepresentations(), namedArea.getOrderIndex());
        return dto;
    }

    public UUID getVocabularyUuid() {
        return vocabularyUuid;
    }

    public void setVocabularyUuid(UUID vocabularyUuid) {
        this.vocabularyUuid = vocabularyUuid;
    }

    public UUID getPartOfUuid() {
        return partOfUuid;
    }

    public UUID getKindOfUuid() {
        return kindOfUuid;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getIdInVocabulary() {
        return idInVocabulary;
    }

    public void setIdInVocabulary(String idInVocabulary) {
        this.idInVocabulary = idInVocabulary;
    }

    public Collection<TermDto> getIncludes() {
        return includes;
    }

    public void setIncludes(Collection<TermDto> includes) {
        this.includes = includes;
    }

    public Collection<TermDto> getGeneralizationOf() {
        return generalizationOf;
    }

    public void setGeneralizationOf(Collection<TermDto> generalizationOf) {
        this.generalizationOf = generalizationOf;
    }

    public static String getTermDtoSelect(){
        return ""
                + "select a.uuid, r, p.uuid, k.uuid, v.uuid, a.orderIndex, a.idInVocabulary "
                + "from DefinedTermBase as a "
                + "LEFT JOIN a.partOf as p "
                + "LEFT JOIN a.kindOf as k "
                + "LEFT JOIN a.representations AS r "
                + "LEFT JOIN a.vocabulary as v ";
    }

    public static List<TermDto> termDtoListFrom(List<Object[]> results) {
        Map<UUID, TermDto> dtoMap = new HashMap<>(results.size());
        for (Object[] elements : results) {
            UUID uuid = (UUID)elements[0];
            if(dtoMap.containsKey(uuid)){
                dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
            } else {
                Set<Representation> representations;
                if(elements[1] instanceof Representation) {
                    representations = new HashSet<Representation>(1);
                    representations.add((Representation)elements[1]);
                } else {
                    representations = (Set<Representation>)elements[1];
                }
                dtoMap.put(uuid, new TermDto(uuid, representations, (UUID)elements[2], (UUID)elements[3], (UUID)elements[4], (Integer)elements[5], (String)elements[6]));
            }
        }
        return new ArrayList<>(dtoMap.values());
    }

}
