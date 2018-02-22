/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.registration.messages.redmine;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.CustomFieldFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.UserFactory;
import com.taskadapter.redmineapi.bean.WatcherFactory;
import com.taskadapter.redmineapi.internal.ResultsWrapper;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.config.ApplicationConfiguration;
import eu.etaxonomy.cdm.api.config.ApplicationConfigurationFile;
import eu.etaxonomy.cdm.api.config.CdmConfigurationFileNames;
import eu.etaxonomy.cdm.ext.common.ExternalServiceException;
import eu.etaxonomy.cdm.ext.registration.messages.IRegistrationMessageService;
import eu.etaxonomy.cdm.ext.registration.messages.Message;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;

/**
 * @author a.kohlbecker
 * @since Feb 15, 2018
 *
 */
@Service
public class RedmineRegistrationMessageService implements IRegistrationMessageService {

    /**
     *
     */
    protected static final UUID EXTTYPE_REGMESG_REDMINEUID_UUID = UUID.fromString("d5319a4f-b6fe-4d65-a14e-4b6faa707fcf");

    private static final Logger logger = Logger.getLogger(RedmineRegistrationMessageService.class);

    @Autowired
    ApplicationConfiguration appConfig;

    @Autowired
    @Qualifier("cdmRepository")
    ICdmRepository api;

    static final String PROPERTIES_FILE_NAME = CdmConfigurationFileNames.SERVICES_EXTERNAL;

    static final String RedminePreferenceKeyPrefix = "registrationMessageService.redmine.";

    //@formatter:off
    private static final String APP_FILE_CONTENT=
            "########################################################\n"+
            "#                                                       \n"+
            "# Configurations for external services                  \n"+
            "#                                                       \n"+
            "########################################################\n"+
            "                                                        \n"+
            "### Redmine configuration\n"+
            "#" + RedminePreferenceKey.REDMINE_URL + ":\n"+
            "#" + RedminePreferenceKey.ADMIN_USER_API_KEY + ":\n"+
            "#" + RedminePreferenceKey.PROJECT_ID + ":\n";
    //@formatter:on

    private ApplicationConfigurationFile appConfigFile = new ApplicationConfigurationFile(
            CdmConfigurationFileNames.SERVICES_EXTERNAL,
            APP_FILE_CONTENT);

    private EnumSet activeStatus = EnumSet.of(RegistrationStatus.PREPARATION, RegistrationStatus.CURATION);

    private RedmineManager _redmineManager;

    private static Field mapField;

    static {
        try {
            mapField = com.taskadapter.redmineapi.bean.PropertyStorage.class.getDeclaredField("map");
            mapField.setAccessible(true);
        } catch ( SecurityException | IllegalArgumentException | NoSuchFieldException  e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("rawtypes")
    private Map storageMap(com.taskadapter.redmineapi.bean.PropertyStorage storage){
        try {
            return (Map) mapField.get(storage);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private String getPreference(RedminePreferenceKey key){
        // FIXME check for complete configuration the first time this is called, take all RedminePreferenceKeys into account
        return appConfig.getProperty(appConfigFile, RedminePreferenceKeyPrefix + key.toString());
    }

    private int getPreferenceAsInt(RedminePreferenceKey key) throws ExternalServiceException{
        String value = appConfig.getProperty(appConfigFile, RedminePreferenceKeyPrefix + key.toString());
        if(value != null) {
            return Integer.valueOf(value);
        } else {
            throw new ExternalServiceException("Configuration problem: ",  RedminePreferenceKeyPrefix + key.toString() + " is undefined.");
        }
    }


    protected RedmineManager redmineManager(){
        if(_redmineManager == null){
            _redmineManager = RedmineManagerFactory.createWithApiKey(
                    getPreference(RedminePreferenceKey.REDMINE_URL),
                    getPreference(RedminePreferenceKey.ADMIN_USER_API_KEY)
                    );
        }
        return _redmineManager;
    }


    public void postMessage(Registration registration, String message, User fromUser, User toUser)  throws ExternalServiceException {

        Issue issue = findIssue(registration);
        if(issue == null){
            issue = createIssue(registration);
        } else {
            issue.setStatusId(statusIdFrom(registration.getStatus()));
            issue.setPriorityId(getPreferenceAsInt(RedminePreferenceKey.ISSUE_PRIORITIY_ACTIVE_ID));
        }

        activateMessagesFor(registration, toUser);

        com.taskadapter.redmineapi.bean.User redmineFromUser = findUser(fromUser);
        if(redmineFromUser == null){
            redmineFromUser = createUser(fromUser);
        }
        issue.addWatchers(Arrays.asList(WatcherFactory.create(redmineFromUser.getId())));

        issue.setNotes(message);

        try {
            redmineManager().setOnBehalfOfUser(redmineFromUser.getLogin());
            redmineManager().getIssueManager().update(issue);
        } catch (RedmineException e) {
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), e);
        } finally {
            redmineManager().setOnBehalfOfUser(null);
        }


    }

    @Transactional(readOnly=true) // allow the users being loaded from the session
    public List<Message> listMessages(Registration registration)  throws ExternalServiceException {

        return listMessages(registration, null);
    }

    /**
     *
     * @param registration
     * @param toUser
     * @return
     * @throws ExternalServiceException
     */
    protected List<Message> listMessages(Registration registration, User toUser) throws ExternalServiceException {
        ArrayList<Message> messages = new ArrayList<>();

        Issue issue = findIssue(registration);
        Map<Integer, User> userMap = new HashMap<>();

        if(issue == null){
            return messages;
        }

        boolean applyFilter = toUser != null;
        boolean isActive = issue.getPriorityId() == getPreferenceAsInt(RedminePreferenceKey.ISSUE_PRIORITIY_ACTIVE_ID);
        boolean assigeeIsToUser = false;
        if(issue.getAssigneeId() != null){
            User assignee = userByRedmineUserId(userMap, issue.getAssigneeId());
            assigeeIsToUser = assignee.equals(toUser);
        }
        if(applyFilter && !(isActive && assigeeIsToUser )) {
            // return empty list
            return messages;
        }


        for(Journal journal : issue.getJournals()){
            if(StringUtils.isNoneEmpty(journal.getNotes())){

                Integer redmineFromUserID = journal.getUser().getId();
                User fromUser = userByRedmineUserId(userMap, redmineFromUserID);

                boolean noteMatches = false;
                if(toUser == null){
                    noteMatches = true;
                } else {
                    noteMatches = !fromUser.equals(toUser);

                }
                if(noteMatches){
                    messages.add(new Message(journal.getId(), journal.getNotes(), fromUser, journal.getCreatedOn()));
                }
            }
        }
        messages.sort(new Comparator<Message>() {

            @Override
            public int compare(Message o1, Message o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        return messages;
    }

    /**
     * @param userMap
     * @param redmineFromUserID
     * @return
     * @throws ExternalServiceException
     *
     * TODO merge with findUser ?
     */
    protected User userByRedmineUserId(Map<Integer, User> userMap, Integer redmineUserID) throws ExternalServiceException {
        if(!userMap.containsKey(redmineUserID)){
            com.taskadapter.redmineapi.bean.User redmineUser;
            try {
                redmineUser = redmineManager().getUserManager().getUserById(redmineUserID);
            } catch (RedmineException e) {
                throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL),  e);
            }
            String login = redmineUser.getLogin();
            // primarily users are synchronized via the redmine user id which is stored in the
            // user.person extensions, but login names are also kept in sync so we can use
            // the login to load the users, this way it is more direct and thus faster:
            UserDetails userDetails = api.getUserService().loadUserByUsername(login);
            if(userDetails != null){
                userMap.put(redmineUserID, (User)userDetails);
            }
        }
        User user = userMap.get(redmineUserID);
        return user;
    }

    /**
     *
     * @param registration
     * @param user
     * @return
     * @throws ExternalServiceException
     */
    public List<Message> listActiveMessagesFor(Registration registration, User user) throws ExternalServiceException {
        return listMessages(registration, user);
    }

    /**
     *
     * @param registration
     * @param user
     * @return
     * @throws ExternalServiceException
     */
    public int countActiveMessagesFor(Registration registration, User user) throws ExternalServiceException {

        return listMessages(registration, user).size();
    }

    /**
     *
     * @param user
     * @return
     * @throws ExternalServiceException
     */
    protected com.taskadapter.redmineapi.bean.User createUser(User user) throws ExternalServiceException{

        com.taskadapter.redmineapi.bean.User redmineUser = UserFactory.create();
        redmineUser.setLogin(user.getUsername());
        redmineUser.setMail(user.getEmailAddress());
        redmineUser.setPassword(user.getPassword());

        // LastName and FirstName are required in redmine!
        redmineUser.setFirstName(user.getUsername());
        redmineUser.setLastName(user.getUsername());

        Person person = HibernateProxyHelper.deproxyOrNull(user.getPerson());
        if(person != null){
            if(StringUtils.isNoneEmpty(user.getPerson().getFirstname())){
                redmineUser.setFirstName(user.getPerson().getFirstname());
            }
            if(StringUtils.isNoneEmpty(user.getPerson().getLastname())){
                redmineUser.setLastName(user.getPerson().getLastname());
            }
            redmineUser.setFullName(StringUtils.trimToNull(user.getPerson().getFullTitle()));
        }

        Role roleContributor  = roleContributor();

        try {
            redmineUser = redmineManager().getUserManager().createUser(redmineUser);
            redmineManager().getMembershipManager().createMembershipForUser(getPreferenceAsInt(RedminePreferenceKey.PROJECT_ID), redmineUser.getId(), Arrays.asList(roleContributor));
            if(person == null){
                person = Person.NewInstance();
                user.setPerson(person);
            }

            Extension.NewInstance(person, Integer.toString(redmineUser.getId()), registrationMessageRedmineUserIDExtendsionType());
            api.getUserService().saveOrUpdate(user);
        } catch (RedmineException e) {
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), e);
        }
        return redmineUser;
    }

    /**
     * @return
     * @throws RedmineException
     * @throws ExternalServiceException
     */
    protected Role roleContributor() throws ExternalServiceException {
                try {
                    Role roleContributor = redmineManager().getUserManager().getRoleById(getPreferenceAsInt(RedminePreferenceKey.ROLE_ID_CONTRIBUTOR));
                    Collection<String> permissions = roleContributor.getPermissions();
                    List<String> requiredPermissions = Arrays.asList(
                            "view_issues",
                            "add_issue_notes",
                            "add_issue_watchers"
                            );
                    if(!permissions.containsAll(requiredPermissions
                          )){
                        throw new ExternalServiceException(
                                getPreference(RedminePreferenceKey.REDMINE_URL),
                                "Insuficcient rights: The role 'contributor' must at least include the premissions " + requiredPermissions.toString());
                    }
                    return roleContributor;
                } catch (RedmineException e) {
                    throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL) , e);
                }
    }

    protected com.taskadapter.redmineapi.bean.User findUser(User user) throws ExternalServiceException {

        User submitterReloaded = api.getUserService().load(user.getUuid(), Arrays.asList("person.extensions.$"));
        Person person = HibernateProxyHelper.deproxyOrNull(submitterReloaded.getPerson());

        if(person == null){
            // the redmine user id is stored in a extension to the person
            // no person, no redmine user!
            return null;
        }
        ExtensionType registrationMessageRedmineUserIDExtendsionType = registrationMessageRedmineUserIDExtendsionType();
        Set<String> exts = person.getExtensions(registrationMessageRedmineUserIDExtendsionType);
        if(exts.size() == 0){
            // user not yet registered in redmine
            return null;
        }
        if(exts.size() > 1){
            throw new RuntimeException("Corrupt user data: A user mous only have one " + registrationMessageRedmineUserIDExtendsionType.getLabel() + " extension");
        }

        try {
            return redmineManager().getUserManager().getUserById(Integer.valueOf(exts.iterator().next()));
        } catch (RedmineException e) {
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), e);
        }
    }

    public void updateIssueStatus(Registration registration) throws ExternalServiceException {

        Issue issue = findIssue(registration);
        issue.setStatusId(statusIdFrom(registration.getStatus()));

        try {
            redmineManager().getIssueManager().update(issue);
        } catch (RedmineException e) {
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), e);
        }
    }

    public void inactivateMessages(Registration registration) throws ExternalServiceException {

        Issue issue = findIssue(registration);
        issue.setPriorityId(getPreferenceAsInt(RedminePreferenceKey.ISSUE_PRIORITIY_INACTIVE_ID));
        // issue.setAssigneeId(null); // the redmine api will not reset the assignee id when the json value null is submitted
        // it must be set to the empty string though: "", see https://github.com/taskadapter/redmine-java-api/issues/306
        // see RedmineTransportAccessor
        try {
        RedmineTransportAccessor transport = new RedmineTransportAccessor(redmineManager());
        URI uri = new URI(getPreference(RedminePreferenceKey.REDMINE_URL)
                + "/issues/" + issue.getId()
                + ".json?key=" + getPreference(RedminePreferenceKey.ADMIN_USER_API_KEY));
        transport.httpPut(uri, "{\"issue\":{\"assigned_to_id\":\"\"}}");
        } catch ( URISyntaxException e) {
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), e);
        }

    }

    public void activateMessagesFor(Registration registration, User user) throws ExternalServiceException {

        com.taskadapter.redmineapi.bean.User redmineUser = findUser(user);
        if(redmineUser == null){
            redmineUser = createUser(user);
        }
        Issue issue = findIssue(registration);
        issue.setPriorityId(getPreferenceAsInt(RedminePreferenceKey.ISSUE_PRIORITIY_ACTIVE_ID));
        issue.setAssigneeId(redmineUser.getId());
        issue.addWatchers(Arrays.asList(WatcherFactory.create(redmineUser.getId())));

        try {
            redmineManager().getIssueManager().update(issue);
        } catch (RedmineException e) {
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), e);
        }

    }

    protected Issue createIssue(Registration registration) throws ExternalServiceException {

        com.taskadapter.redmineapi.bean.User redmineSubmitter = findUser(registration.getSubmitter());
        if(redmineSubmitter == null){
            redmineSubmitter = createUser(registration.getSubmitter());
        }

        Issue issue = IssueFactory.create(
                getPreferenceAsInt(RedminePreferenceKey.PROJECT_ID),
                "Registration " + registration.getIdentifier()
                );

        issue.setDescription("["+registration.getIdentifier()+"]("+registration.getIdentifier()+")"); // clickable link

        issue.setPriorityId(getPreferenceAsInt(RedminePreferenceKey.ISSUE_PRIORITIY_INACTIVE_ID));

        issue.setStatusId(statusIdFrom(registration.getStatus()));

        issue.setAssigneeId(redmineSubmitter.getId());

        CustomField customFieldIdentifier = CustomFieldFactory.create(getPreferenceAsInt(RedminePreferenceKey.CUSTOM_FIELD_IDENTIFIER_ID));
        customFieldIdentifier.setValue(registration.getIdentifier());
        issue.addCustomField(customFieldIdentifier);

        CustomField customFieldSubmitter = CustomFieldFactory.create(getPreferenceAsInt(RedminePreferenceKey.CUSTOM_FIELD_SUBMITTER_ID));
        customFieldSubmitter.setValue(Integer.toString(redmineSubmitter.getId()));
        issue.addCustomField(customFieldSubmitter);

        try {
            return redmineManager().getIssueManager().createIssue(issue);
        } catch (RedmineException e) {
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), e);
        }
    }

    /**
     * @param status
     * @return
     */
    private Integer statusIdFrom(RegistrationStatus status) throws ExternalServiceException {
        switch(status){
            case CURATION:
                return getPreferenceAsInt(RedminePreferenceKey.ISSUE_STATUS_CURATION_ID);
            case PREPARATION:
                return getPreferenceAsInt(RedminePreferenceKey.ISSUE_STATUS_PREPARATION_ID);
            case PUBLISHED:
                return getPreferenceAsInt(RedminePreferenceKey.ISSUE_STATUS_PUBLISHED_ID);
            case READY:
                return getPreferenceAsInt(RedminePreferenceKey.ISSUE_STATUS_READY_ID);
            case REJECTED:
                return getPreferenceAsInt(RedminePreferenceKey.ISSUE_STATUS_REJECTED_ID);
            default:
                return getPreferenceAsInt(RedminePreferenceKey.ISSUE_STATUS_PREPARATION_ID);
        }
    }

    protected Issue findIssue(Registration registration) throws ExternalServiceException {

        Map<String, String>params = new HashMap<>();
        params.put("status_id", "*"); // * to get open and closed issues
        params.put("cf_"+ getPreference(RedminePreferenceKey.CUSTOM_FIELD_IDENTIFIER_ID), registration.getIdentifier());
        try {
            ResultsWrapper<Issue> issues = redmineManager().getIssueManager().getIssues(params);
            if(issues.getResults().size() == 0){
                return null;
            }
            if(issues.getResults().size() == 1){
                return redmineManager().getIssueManager().getIssueById(issues.getResults().get(0).getId(), Include.journals);
            }
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), "Multiple Issues found for " + registration.getIdentifier() );
        } catch (RedmineException e) {
            throw new ExternalServiceException(getPreference(RedminePreferenceKey.REDMINE_URL), e);
        }
    }

    private ExtensionType registrationMessageRedmineUserIDExtendsionType(){

        ExtensionType extType = (ExtensionType) api.getTermService().load(EXTTYPE_REGMESG_REDMINEUID_UUID);
        if(extType == null){
            extType = ExtensionType.NewInstance("RegistrationMessageService RedmineUserID", "RegistrationMessageService RedmineUserID", "RegMesg_RedmineUID");
            extType.setUuid(EXTTYPE_REGMESG_REDMINEUID_UUID);
            api.getTermService().save(extType);
        }
        return extType;
    }


}