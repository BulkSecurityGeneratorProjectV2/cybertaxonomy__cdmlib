/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

/**
 * #5833
 * The new single default cache strategy for {@link Reference references}.
 * As we do have only one {@link Reference} class left which implements multiple interfaces,
 * we may also only need 1 single cache strategy. However, care must be taken as the formatting
 * differs dependent on the type an the in-reference structure.
 *
 * Generally the cache strategy allows to compute 3 formats:<BR>
 *
 *  1.) for bibliographic references (stored in {@link Reference#getTitleCache() titleCache}).<BR>
 *
 *  2.) for nomenclatural references (stored in {@link Reference#getAbbrevTitleCache() abbrevTitleCache}),
 *      but without micro reference (detail).<BR>
 *
 *  3.) for nomenclatural references with micro reference, but not stored anywhere as the micro reference
 *      is part of the name, not of the reference<BR>
 *
 *  4.) for short citation (e.g. Author 2009) as defined in {@link IReferenceCacheStrategy#getCitation(Reference, String)}
 *  and {@link IReferenceCacheStrategy#createShortCitation(Reference, String, Boolean)}
 *
 * @author a.mueller
 * @since 25.05.2016
 */
public class ReferenceDefaultCacheStrategy
        extends StrategyBase
        implements IReferenceCacheStrategy {

    private static final long serialVersionUID = 6773742298840407263L;
    private static final Logger logger = Logger.getLogger(ReferenceDefaultCacheStrategy.class);

    private final static UUID uuid = UUID.fromString("63e669ca-c6be-4a8a-b157-e391c22580f9");

    //article
    public static final String UNDEFINED_JOURNAL = "- undefined journal -";
    private static final String afterAuthor = ", ";

    //book

    //(book?) section
    private String afterSectionAuthor = " - ";

    //in reference
    private String biblioInSeparator = UTF8.EN_DASH + " In: "; //#9529
    private String biblioArticleInSeparator = UTF8.EN_DASH + " "; //#9529

    //common
    private static final String blank = " ";
    public static final String beforeYear = ". ";
    private static final String afterYear = "";

    private static final boolean trim = true;

// ************************ FACTORY ****************************/

    public static ReferenceDefaultCacheStrategy NewInstance(){
        return new ReferenceDefaultCacheStrategy();
    }

// ******************************* Main methods ******************************/

    @Override
    protected UUID getUuid() {
        return uuid;
    }

    @Override
    public String getTitleCache(Reference reference) {
        if (reference == null){
            return null;
        }
        if (reference.isProtectedTitleCache()){
            return reference.getTitleCache();
        }
        boolean isNotAbbrev = false;

        String result;
        ReferenceType type = reference.getType();

        if (isRealInRef(reference)){
            result = titleCacheRealInRef(reference, isNotAbbrev);
        }else if(isNomRef(type)){
            //all Non-InRef NomRefs
            result =  getTitleWithoutYearAndAuthor(reference, isNotAbbrev, false);
            result = addPages(result, reference);
            result = addYear(result, reference, false);
            TeamOrPersonBase<?> team = reference.getAuthorship();

            if (type == ReferenceType.Article){
                String artTitle = CdmUtils.addTrailingDotIfNotExists(reference.getTitle());
                result = CdmUtils.concat(" ", artTitle, result);
                if (team != null && isNotBlank(team.getTitleCache())){
                    String authorSeparator = isNotBlank(reference.getTitle())? afterAuthor : " ";
                    result = team.getTitleCache() + authorSeparator + result;
                }
            }else{  //if Book, CdDvd, flat Generic, Thesis, WebPage
                if (team != null){
                    String teamTitle = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(),
                            team.getNomenclaturalTitle(), isNotAbbrev, trim);
                    if (teamTitle.length() > 0 ){
                        String concat = isNotBlank(result) ? afterAuthor : "";
                        result = teamTitle + concat + result;
                    }
                }
            }
        }else if (type == ReferenceType.Journal){
            result = titleCacheJournal(reference, isNotAbbrev);
        }else{
            result = titleCacheDefaultReference(reference, isNotAbbrev);
        }
        if (reference.getType() == ReferenceType.WebPage && reference.getUri() != null && !result.contains(reference.getUri().toString())){
            result = CdmUtils.concat(" "+UTF8.EN_DASH+" ", result, reference.getUri().toString());
        }
        if(reference.getAccessed() != null){
            //TODO still a bit preliminary, also brackets may change in future
            result = result + " [accessed " + getAccessedString(reference.getAccessed()) +"]";
        }
        return result;
    }

    private String getAccessedString(DateTime accessed) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        String result = formatter.print(accessed);
        if (result.endsWith(" 00:00")){
            result = result.replace(" 00:00", "");
        }
        return result;
    }

    private String addPages(String result, Reference reference) {
        //pages
        if (isNotBlank(reference.getPages())){
            //Removing trailing added just in case, maybe not necessary
            result = RemoveTrailingDot(Nz(result)).trim() + ": " + reference.getPages();
        }
        return result;
    }

    private static String RemoveTrailingDot(String str) {
        if (str != null && str.endsWith(".")){
            str = str.substring(0, str.length()-1);
        }
        return str;
    }

    @Override
    public String getFullAbbrevTitleString(Reference reference) {
        if (reference == null){
            return null;
        }
        String result;
        ReferenceType type = reference.getType();
        boolean isAbbrev = true;

        if (reference.isProtectedAbbrevTitleCache()){
            return reference.getAbbrevTitleCache();
        }

        if (type == ReferenceType.Article){
            result =  getTitleWithoutYearAndAuthor(reference, isAbbrev, false);
            boolean useFullDatePublished = false;
            result = addYear(result, reference, useFullDatePublished);
            TeamOrPersonBase<?> team = reference.getAuthorship();
            String articleTitle = CdmUtils.getPreferredNonEmptyString(reference.getTitle(),
                    reference.getAbbrevTitle(), isAbbrev, trim);
            result = CdmUtils.concat(" ", articleTitle, result);  //Article should maybe left out for nomenclatural references (?)
            if (team != null &&  isNotBlank(team.getNomenclaturalTitle())){
                String authorSeparator = isNotBlank(articleTitle) ? afterAuthor : " ";
                result = team.getNomenclaturalTitle() + authorSeparator + result;
            }
        }else if (isRealInRef(reference)){
            result = titleCacheRealInRef(reference, isAbbrev);
        }else if (isNomRef(type)){
            //FIXME same as titleCache => try to merge, but note article case
            result =  getTitleWithoutYearAndAuthor(reference, isAbbrev, false);
            boolean useFullDatePublished = false;
            result = addYear(result, reference, useFullDatePublished);
            TeamOrPersonBase<?> team = reference.getAuthorship();

            if (team != null){
                String teamTitle = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(),
                        team.getNomenclaturalTitle(), isAbbrev, trim);
                if (teamTitle.length() > 0 ){
                    String concat = isNotBlank(result) ? afterAuthor : "";
                    result = teamTitle + concat + result;
                }
            }
        }else if(type == ReferenceType.Journal){
            result = titleCacheJournal(reference, isAbbrev);
        }else{
            result = titleCacheDefaultReference(reference, isAbbrev);
        }

        return result;
    }

    //TODO see comment on createShortCitation(...)
    @Override
    public String getCitation(Reference reference, String microReference) {
        // mostly copied from nomRefCacheStrat, refCache, journalCache

        if (reference == null){
            return null;
        }
        StringBuilder result = new StringBuilder();
        TeamOrPersonBase<?> team = reference.getAuthorship();

        String nextConcat = "";

        if (team != null &&  isNotBlank(team.getTitleCache())){
            result.append(team.getTitleCache() );
            //here is the difference between nomRef and others
            if (isNomRef(reference.getType())) {
                nextConcat = afterAuthor;
            }else{
                //FIXME check if this really makes sense
                result.append(afterAuthor);
                nextConcat = beforeYear;
            }
        }

        String year = reference.getYear();
        if (isNotBlank(year)){
            result.append(nextConcat + year);
        }
        if (isNotBlank(microReference)){
            result.append(": " + microReference);
        }

        return result.toString();
    }

    //TODO this method should probably be unified with getCitation(Reference reference, String microReference)
    @Override
    public String createShortCitation(Reference reference, String citationDetail, Boolean withYearBrackets) {
        if (withYearBrackets == null){
            withYearBrackets = false;
        }
        if(reference.isProtectedTitleCache()){
            return handleCitationDetailInTitleCache(reference.getTitleCache(), citationDetail);
        }
        TeamOrPersonBase<?> authorship = reference.getAuthorship();
        String shortCitation = "";
        if (authorship == null) {
            return handleCitationDetailInTitleCache(reference.getTitleCache(), citationDetail);
        }
        authorship = CdmBase.deproxy(authorship);
        if (authorship instanceof Person){
            shortCitation = getPersonString((Person)authorship);
        }
        else if (authorship instanceof Team){

            Team team = CdmBase.deproxy(authorship, Team.class);
            if (team.isProtectedTitleCache()){
                shortCitation = team.getTitleCache();
            }else{
                List<Person> teamMembers = team.getTeamMembers();
                int etAlPosition = 2;
                for (int i = 1; i <= teamMembers.size() &&
                        (i < etAlPosition || teamMembers.size() == etAlPosition && !team.isHasMoreMembers()) ; i++){
                    Person teamMember = teamMembers.get(i-1);
                    if(teamMember == null){
                        // this can happen in UIs in the process of adding new members
                        continue;
                    }
                    String concat = TeamDefaultCacheStrategy.concatString(team, teamMembers, i);
                    shortCitation += concat + getPersonString(teamMember);
                }
                if (teamMembers.size() == 0){
                    shortCitation = TeamDefaultCacheStrategy.EMPTY_TEAM;
                } else if (team.isHasMoreMembers() || teamMembers.size() > etAlPosition){
                    shortCitation += TeamDefaultCacheStrategy.ET_AL_TEAM_CONCATINATION_FULL + "al.";
                }
            }
        }
        shortCitation = CdmUtils.concat(" ", shortCitation, getShortCitationDate(reference, withYearBrackets, citationDetail));

        return shortCitation;
    }

    /**
     * Adds the citationDetail to the titleCache string that is returned from a method as data is not
     * accurately parsed.
     * @return
     */
    private String handleCitationDetailInTitleCache(String titleCache, String citationDetail) {
        if (StringUtils.isBlank(citationDetail)){
            return titleCache;
        }else if (StringUtils.isBlank(titleCache)){
            return ": " + citationDetail;
        }else if (citationDetail.length() <= 3){
            if (titleCache.contains(": " + citationDetail)){
                return titleCache;
            }
        }else{
            if (titleCache.contains(citationDetail)){
                return titleCache;
            }
        }
        return titleCache + ": " + citationDetail;
    }

    private String getShortCitationDate(Reference reference, boolean withBrackets, String citationDetail) {
        String result = null;
        if (reference.getDatePublished() != null && !reference.getDatePublished().isEmpty()) {
            if (isNotBlank(reference.getDatePublished().getFreeText())){
                result = reference.getDatePublished().getFreeText();
            }else if (isNotBlank(reference.getYear()) ){
                result = reference.getYear();
            }
            if (StringUtils.isNotEmpty(citationDetail)){
                result = CdmUtils.Nz(result) + ": " + citationDetail;
            }
            if (StringUtils.isNotBlank(result) && withBrackets){
                result = "(" + result + ")";
            }
        }else if (reference.getInReference() != null){
            result = getShortCitationDate(reference.getInReference(), withBrackets, citationDetail);
        }
        return result;
    }

    private String getPersonString(Person person) {
        String shortCitation;
        shortCitation = person.getFamilyName();
        if (isBlank(shortCitation) ){
            shortCitation = person.getTitleCache();
        }
        return shortCitation;
    }

// ************************ TITLE CACHE SUBS ********************************************/

    private String titleCacheRealInRef(Reference reference, boolean isAbbrev) {
        ReferenceType type = reference.getType();
        Reference inRef = reference.getInReference();
        boolean hasInRef = (inRef != null);

        String result;
        //copy from InRefDefaultCacheStrategyBase
        if (inRef != null){
            result = CdmUtils.getPreferredNonEmptyString(inRef.getTitleCache(),
                    inRef.getAbbrevTitleCache(), isAbbrev, trim)  ;
        }else{
            result = String.format("- undefined %s -", getUndefinedLabel(type));
        }

        //in
        result = biblioInSeparator +  result;

        //section title
        String title = CdmUtils.getPreferredNonEmptyString(
                reference.getTitle(), reference.getAbbrevTitle(), isAbbrev, trim);
        if (title.matches(".*[.!\\?]")){
            title = title.substring(0, title.length() - 1);
        }
        if (title.length() > 0){
            result = title.trim() + "." + blank + result;
        }

        //section author
        TeamOrPersonBase<?> thisRefTeam = reference.getAuthorship();
        String thisRefAuthor = "";
        if (thisRefTeam != null){
            thisRefAuthor = CdmUtils.getPreferredNonEmptyString(thisRefTeam.getTitleCache(),
                    thisRefTeam.getNomenclaturalTitle(), isAbbrev, trim);
        }
        result = CdmUtils.concat(afterSectionAuthor, thisRefAuthor, result);

        //date
        if (reference.getDatePublished() != null && ! reference.getDatePublished().isEmpty()){
            String thisRefDate = reference.getDatePublished().toString();
            if (hasInRef && reference.getInBook().getDatePublished() != null){
                VerbatimTimePeriod inRefDate = reference.getInReference().getDatePublished();
                String inRefDateString = inRefDate.getYear();
                if (isNotBlank(inRefDateString)){
                    int pos = StringUtils.lastIndexOf(result, inRefDateString);
                    if (pos > -1 ){
                        result = result.substring(0, pos) + thisRefDate + result.substring(pos + inRefDateString.length());
                    }else{
                        logger.warn("InRefDateString (" + inRefDateString + ") could not be found in result (" + result +")");
                    }
                }else{
                    //avoid duplicate dots ('..')
                    String bYearSeparator = result.substring(result.length() -1).equals(beforeYear.substring(0, 1)) ? beforeYear.substring(1) : beforeYear;
                    result = result + bYearSeparator + thisRefDate + afterYear;
                }
            }else{
                result = result + beforeYear + thisRefDate + afterYear;
            }
        }
        return result;
    }

    private String titleCacheJournal(Reference reference, boolean isAbbrev) {
        String result;
        //copied from Journal

        //title
        result = CdmUtils.getPreferredNonEmptyString(reference.getTitle(),
                reference.getAbbrevTitle(), isAbbrev, trim);

//          //delete .
//          while (result.endsWith(".")){
//              result = result.substring(0, result.length()-1);
//          }
//          result = addYear(result, journal);

        TeamOrPersonBase<?> team = reference.getAuthorship();
        if (team != null){
            String author = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(),
                    team.getNomenclaturalTitle(), isAbbrev, trim);
            if (isNotBlank(author)){
                result = author + afterAuthor + result;
            }
        }
        return result;
    }

    private String titleCacheDefaultReference(Reference reference, boolean isAbbrev) {
        String result;
        //copied from ReferenceDefaultCacheStrategy
        result = "";
        String titel = CdmUtils.getPreferredNonEmptyString(reference.getTitle(),
                reference.getAbbrevTitle(), isAbbrev, trim);
        if (isNotBlank(titel)){
            result = titel + blank;
        }
        //delete .
        while (result.endsWith(".")){
            result = result.substring(0, result.length()-1);
        }

        result = addYearReferenceDefault(result, reference);
        TeamOrPersonBase<?> team = reference.getAuthorship();
        if (team != null){
            String author = CdmUtils.getPreferredNonEmptyString(team.getTitleCache(),
                    team.getNomenclaturalTitle(), isAbbrev, trim);
            if (isNotBlank(author)){
                result = author + afterAuthor + result;
            }
        }
        return result;
    }

// ******************************* HELPER *****************************************/

    public static String addYear(String yearStr, Reference nomRef, boolean useFullDatePublished){
        String result;
        if (yearStr == null){
            return null;
        }
        String year = useFullDatePublished ? nomRef.getDatePublishedString() : nomRef.getYear();
        if (isBlank(year)){
            result = yearStr + afterYear;
        }else{
            String concat = isBlank(yearStr)  ? "" : yearStr.endsWith(".")  ? " " : beforeYear;
            result = yearStr + concat + year + afterYear;
        }
        return result;
    }

    private String getTitleWithoutYearAndAuthor(Reference ref, boolean isAbbrev, boolean isNomRef){
        return TitleWithoutYearAndAuthorHelper.getTitleWithoutYearAndAuthor(ref, isAbbrev, isNomRef);
    }

    private Object getUndefinedLabel(ReferenceType type) {
        if (type == ReferenceType.BookSection){
            return "book";
        }else if (type == ReferenceType.Generic){
            return "generic reference";
        }else if (type == ReferenceType.Section){
            return "in reference";
        } else {
            return type.getLabel();
        }
    }

    /**
     * Returns <code>true</code> if the type of the reference originally corresponded to a cache strategy
     * which inherited from {@link InRefDefaultCacheStrategyBase} and in case of type {@link ReferenceType#Generic}
     * if it really has an inreference (reference.getInreference() != null).
     * @param reference
     */
    public static boolean isRealInRef(Reference reference) {
        ReferenceType type = (reference.getType());
        if (type == null){
            return false;
        }else if (type == ReferenceType.BookSection || type == ReferenceType.Section){
            return true;
        }else if (type == ReferenceType.Generic){
            return reference.getInReference() != null;
        }else{
            return false;
        }
    }

    /**
     * Returns <code>true</code> if the type of the reference originally corresponded to a cache strategy
     * which inherited from {@link NomRefDefaultCacheStrategyBase}.
     * @param type
     */
    public static boolean isNomRef(ReferenceType type){
        switch (type){
            case Article:
            case Book:
            case BookSection:
            case CdDvd:
            case Generic:
            case Section:
            case Thesis:
            case WebPage:
                return true;

            case Journal:
            default:
                return false;
        }
    }

    /**
     * Returns year information as originally computed by {@link ReferenceDefaultCacheStrategy}.
     */
    private String addYearReferenceDefault(String string, Reference ref){
        String result;
        if (string == null){
            return null;
        }
        String year = CdmUtils.Nz(ref.getYear());
        if ("".equals(year)){
            result = string + afterYear;
        }else{
            result = string.trim() + beforeYear + year + afterYear;
        }
        return result;
    }

// *************************** EXTERNAL USE *******************************************/

   public static String putAuthorToEndOfString(String referenceTitleCache, String authorTitleCache) {
       if(authorTitleCache != null){
           referenceTitleCache = referenceTitleCache.replace(authorTitleCache + ", ", "");
           referenceTitleCache += " - " + authorTitleCache;
       }
       return referenceTitleCache;
   }
}
