// $Id$
/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author pplitzner
 * @date 05.11.2018
 *
 */
public class TermVocabularyDto extends AbstractTermDto {

    private static final long serialVersionUID = 6053392236860675874L;

    private Set<TermDto> terms;

    public TermVocabularyDto(UUID uuid, Set<Representation> representations, TermType termType) {
        super(uuid, representations);
        terms = new HashSet<>();
        setTermType(termType);
    }

    public Set<TermDto> getTerms() {
        return terms;
    }

    public void addTerm(TermDto term){
        terms.add(term);
    }

    public static String getTermDtoSelect(){
        return getTermDtoSelect("TermVocabulary");
    }

    public static String getTermDtoSelect(String fromTable){
        return ""
                + "select a.uuid, "
                + "r, "
                + "a.termType "

                + "FROM "+fromTable+" as a "
                + "LEFT JOIN a.representations AS r ";
    }

    public static List<TermVocabularyDto> termDtoListFrom(List<Object[]> results) {
        List<TermVocabularyDto> dtos = new ArrayList<>(); // list to ensure order
        // map to handle multiple representations because of LEFT JOIN
        Map<UUID, TermVocabularyDto> dtoMap = new HashMap<>(results.size());
        for (Object[] elements : results) {
            UUID uuid = (UUID)elements[0];
            if(dtoMap.containsKey(uuid)){
                // multiple results for one voc -> multiple (voc) representation
                if(elements[1]!=null){
                    dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
                }

            } else {
                // voc representation
                Set<Representation> representations = new HashSet<>();
                if(elements[1] instanceof Representation) {
                    representations = new HashSet<Representation>(1);
                    representations.add((Representation)elements[1]);
                }


                TermVocabularyDto termVocDto = new TermVocabularyDto(
                        uuid,
                        representations,
                        (TermType)elements[2]);


                dtoMap.put(uuid, termVocDto);
                dtos.add(termVocDto);
            }
        }
        return dtos;
    }


}
