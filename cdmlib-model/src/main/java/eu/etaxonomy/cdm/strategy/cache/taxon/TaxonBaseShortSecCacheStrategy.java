/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.taxon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

public class TaxonBaseShortSecCacheStrategy<T extends TaxonBase>
        extends StrategyBase
        implements ITaxonCacheStrategy<T> {

    private static final long serialVersionUID = -2831618484053675222L;
    final static UUID uuid = UUID.fromString("931e48f0-2033-11de-8c30-0800200c9a66");

	@Override
	protected UUID getUuid() {
		return uuid;
	}


	@Override
    public String getTitleCache(T taxonBase) {
		String title;
		if (taxonBase.getName() != null && taxonBase.getName().getTitleCache() != null){
			title = getNamePart(taxonBase);
		}else{
		    title = "???";
		}
        boolean isSynonym = taxonBase.isInstanceOf(Synonym.class);
        String secSeparator =  (isSynonym? " syn." : "") + " sec. ";
		title += secSeparator;  //TODO check if separator is required before, e.g. for nom. status. see TaxonBaseDefaultCacheStrategy
		title += getSecundumPart(taxonBase);
		if (taxonBase.isDoubtful()){
			title = "?" + title;
		}
		return title;
	}

	private String getSecundumPart(T taxonBase) {
		String result = "???";
		Reference sec = HibernateProxyHelper.deproxy(taxonBase.getSec());
		if (sec != null){
			if (sec.isProtectedTitleCache()){
				return sec.getTitleCache();
			}
			if (sec.getAuthorship() != null){

				if (sec.getAuthorship().isInstanceOf(Team.class)){
					Team authorTeam = CdmBase.deproxy(sec.getAuthorship(), Team.class);
					if (authorTeam.getTeamMembers().size() > 2){
						if (authorTeam.getTeamMembers().get(0).getFamilyName() != null){
					        result = authorTeam.getTeamMembers().get(0).getFamilyName() + " & al.";
					    } else {
					        result = authorTeam.getTeamMembers().get(0).getTitleCache();
					        result = result + " & al.";
					    }
					} else if (authorTeam.getTeamMembers().size() == 2){
						String firstAuthor;
						if (authorTeam.getTeamMembers().get(0).getFamilyName() != null){
							firstAuthor = authorTeam.getTeamMembers().get(0).getFamilyName();
						}else{
							firstAuthor = authorTeam.getTeamMembers().get(0).getTitleCache();
						}
						String secondAuthor;
						if (authorTeam.getTeamMembers().get(1).getFamilyName() != null){
							secondAuthor = authorTeam.getTeamMembers().get(1).getFamilyName();
						}else{
							secondAuthor = authorTeam.getTeamMembers().get(1).getTitleCache();
						}
						result = firstAuthor + " & " + secondAuthor;

					} else{
						if (authorTeam.getTeamMembers().get(0).getFamilyName() != null){
					        result = authorTeam.getTeamMembers().get(0).getFamilyName();
					    } else {
					        result = authorTeam.getTeamMembers().get(0).getTitleCache();
					    }
					}

				} else {
					Person author = HibernateProxyHelper.deproxy(sec.getAuthorship(), Person.class);
					if (author.getFamilyName() != null){
						result = author.getFamilyName();
					} else{
						result = author.getTitleCache();
					}
				}
				if (result != null){
					result = result.replaceAll("[A-Z]\\.", "");
				}
				if (sec.getYear() != null && result != null){
					result = result.concat(" (" + sec.getYear()+")");
				}
			}else{
			    result = sec.getTitleCache();
			}
		}
		return result;
	}

	/**
	 * @param name
	 */
	private String getNamePart(TaxonBase<?> taxonBase) {
		TaxonName taxonName = taxonBase.getName();
		String result = taxonName.getTitleCache();
		//use name cache instead of title cache if required
		if (taxonBase.isUseNameCache()){
			result = taxonName.getNameCache();
		}
		if (isNotBlank(taxonBase.getAppendedPhrase())){
			result = result.trim() + " " +  taxonBase.getAppendedPhrase().trim();
		}
		return result;
	}

    @Override
    public List<TaggedText> getTaggedTitle(T taxonBase) {
        if (taxonBase == null){
            return null;
        }

        List<TaggedText> tags = new ArrayList<>();

        if (taxonBase.isProtectedTitleCache()){
            //protected title cache
            tags.add(new TaggedText(TagEnum.name, taxonBase.getTitleCache()));
            return tags;
        }else{
            //name
            TaxonName name = taxonBase.getName();
            if (name != null){
                //TODO
                List<TaggedText> nameTags = name.getCacheStrategy().getTaggedTitle(name);
                tags.addAll(nameTags);
            }

            //ref.
            List<TaggedText> secTags;
            Reference ref = taxonBase.getSec();
            ref = HibernateProxyHelper.deproxy(ref, Reference.class);
            if (ref != null){
                secTags = getSecReferenceTags(ref);
            }else{
                secTags = new ArrayList<>();
                if (isBlank(taxonBase.getAppendedPhrase())){
                    secTags.add(new TaggedText(TagEnum.reference, "???"));
                }
            }
            if(! secTags.isEmpty()){
                //sec.
                boolean isSynonym = taxonBase.isInstanceOf(Synonym.class);
                String secSeparator =  (isSynonym? " syn." : "") + " sec. ";
                tags.add(new TaggedText(TagEnum.separator, secSeparator));
                tags.addAll(secTags);
            }
        }
        return tags;
    }

    /**
     * @param ref
     */
    private List<TaggedText> getSecReferenceTags(Reference sec) {
        List<TaggedText> tags = new ArrayList<>();

        if (sec.isProtectedTitleCache()){
            tags.add(new TaggedText(TagEnum.reference, sec.getTitleCache()));
        }else{
            if (sec.getAuthorship() != null){
                List<TaggedText> authorTags;
                if (sec.getAuthorship().isInstanceOf(Team.class)){
                    authorTags = handleTeam(sec);
                } else {
                    authorTags = handlePerson(sec);
                }
                tags.addAll(authorTags);

                //FIXME why did we have this normalization? For removing first names??
//                if (result != null){
//                    result = result.replaceAll("[A-Z]\\.", "");
//                }

                //year
                String year = sec.getYear();
                if (isNotBlank(year) && ! authorTags.isEmpty()){
                    tags.add(new TaggedText(TagEnum.separator, "("));
                    tags.add(new TaggedText(TagEnum.year, year));
                    tags.add(new TaggedText(TagEnum.postSeparator, ")"));
                }
            }else{

            }
        }

        return tags;
    }

    private List<TaggedText>  handlePerson(Reference sec) {
        List<TaggedText> tags = new ArrayList<>();

        Person author = HibernateProxyHelper.deproxy(sec.getAuthorship(), Person.class);
        String authorStr;
        if (author.getFamilyName() != null){
            authorStr = author.getFamilyName();
        } else{
            authorStr = author.getTitleCache();
        }
        tags.add(new TaggedText(TagEnum.authors, authorStr));
        return tags;
    }

    private List<TaggedText> handleTeam(Reference sec) {
        List<TaggedText> tags = new ArrayList<>();

        Team authorTeam = HibernateProxyHelper.deproxy(sec.getAuthorship(), Team.class);
        if (authorTeam.isProtectedTitleCache() || authorTeam.getTeamMembers().isEmpty()){
            String authorStr = authorTeam.getTitleCache();
            tags.add(new TaggedText(TagEnum.authors, authorStr));
        }else if (authorTeam.getTeamMembers().size() > 2){
            //>2 members
            if (authorTeam.getTeamMembers().get(0).getFamilyName() != null){
                String authorStr = authorTeam.getTeamMembers().get(0).getFamilyName() + " & al.";
                tags.add(new TaggedText(TagEnum.authors, authorStr));
            } else {
                String authorStr = authorTeam.getTeamMembers().get(0).getTitleCache();
                authorStr = authorStr + " & al.";
                tags.add(new TaggedText(TagEnum.authors, authorStr));
            }
        } else if (authorTeam.getTeamMembers().size() == 2){
            //2 members
            String firstAuthor;
            if (authorTeam.getTeamMembers().get(0).getFamilyName() != null){
                firstAuthor = authorTeam.getTeamMembers().get(0).getFamilyName();
            }else{
                firstAuthor = authorTeam.getTeamMembers().get(0).getTitleCache();
            }
            String secondAuthor;
            if (authorTeam.getTeamMembers().get(1).getFamilyName() != null){
                secondAuthor = authorTeam.getTeamMembers().get(1).getFamilyName();
            }else{
                secondAuthor = authorTeam.getTeamMembers().get(1).getTitleCache();
            }
            String authorStr = firstAuthor + " & " + secondAuthor;
            tags.add(new TaggedText(TagEnum.authors, authorStr));
        } else{
            //1 member
            String authorStr;
            if (authorTeam.getTeamMembers().get(0).getFamilyName() != null){
                authorStr = authorTeam.getTeamMembers().get(0).getFamilyName();
            } else {
                authorStr = authorTeam.getTeamMembers().get(0).getTitleCache();
            }
            tags.add(new TaggedText(TagEnum.authors, authorStr));
        }
        return tags;
    }

    @Override
    public String getTitleCache(T taxonBase, HTMLTagRules htmlTagRules) {
        List<TaggedText> tags = getTaggedTitle(taxonBase);
        if (tags == null){
            return null;
        }else{
            String result = TaggedCacheHelper.createString(tags, htmlTagRules);
            return result;
        }
    }

}
