/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.database.PermissionDeniedException;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;


/**
 * @author a.kohlbecker
 * @date Feb 4, 2014
 *
 */
@DataSet(value="SecurityTest.xml")
public class UserAndGroupServiceImplTest extends AbstractSecurityTestBase {

    protected static final Logger logger = Logger.getLogger(UserAndGroupServiceImplTest.class);

    @SpringBeanByType
    private AuthenticationManager authenticationManager;

    @SpringBeanByType
    private IUserService userService;

    @SpringBeanByType
    private IGroupService groupService;

    @SpringBeanByType
    private ITaxonService taxonService;


    private Authentication authentication;


    @Test
    public void testCreateUser() {


        authentication = authenticationManager.authenticate(tokenForAdmin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);


        try{
            userService.createUser(User.NewInstance("new user 1", "00000"));
        }catch(Exception e){
            Assert.fail();
        }

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        try{
            userService.createUser(User.NewInstance("new user 2", "00000"));
            Assert.fail();
        }catch(Exception e){
            Assert.assertEquals("Access is denied", e.getMessage());
        }
    }


    @Test
    public void testUpdateUser(){

        // TaxonEditor should be able to change its own email address
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        User user= userService.find(TAXON_EDITOR_UUID);
        user.setEmailAddress("test@bgbm.org");

        /* FIXME
        try{
            userService.updateUser(user);
        }catch (Exception e){
            Assert.fail("the user TaxonEditor should be able to change its own email address");
        }
        */

        authentication = authenticationManager.authenticate(tokenForUserManager);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        user.setEmailAddress("user@bgbm.org");

        try{
            userService.updateUser(user);
        }catch (Exception e){
            Assert.fail("the user UserManager should be able to change others email addresses");
        }

        authentication = authenticationManager.authenticate(tokenForPartEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        try{
            userService.updateUser(user);
            Assert.fail("the user PartEditor should NOT be able to change others email addresses");
        }catch (Exception e){
            Assert.assertEquals("Access is denied", e.getMessage());
        }

    }

    @Test
    public void testChangePassword(){

        // the user TaxonEditor should be able to change its own password
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        userService.changePasswordForUser(tokenForTaxonEditor.getName(), "newPassword");

        Exception exception = null;
        // the user TaxonEditor should NOT be able to change others passwords
        try{
            userService.changePasswordForUser(tokenForAdmin.getName(), "newPassword");
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.debug("Expected failure of evaluation.", e);
            exception  = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
            logger.debug("Expected failure of evaluation.", exception);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNotNull("must fail here!", exception);

        // the user User manager should be able to change others passwords
        authentication = authenticationManager.authenticate(tokenForUserManager);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        userService.changePasswordForUser(tokenForAdmin.getName(), "newPassword");
    }


    @Test
    public void testCreateGroup(){

        authentication = authenticationManager.authenticate(tokenForUserManager);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);


        List<GrantedAuthority> authorityList = new ArrayList<GrantedAuthority>();
        GrantedAuthorityImpl rolePublishAthotrity = GrantedAuthorityImpl.NewInstance();
        rolePublishAthotrity.setAuthority(Role.ROLE_PUBLISH.toString()); // testing if creating a Role from string is working
        authorityList.add(rolePublishAthotrity);

        String publishersGroupName = "publishers";

        groupService.createGroup(publishersGroupName, authorityList);

        commitAndStartNewTransaction(null);

        List<GrantedAuthority> groupAuthorities = groupService.findGroupAuthorities(publishersGroupName);

        Assert.assertEquals(Role.ROLE_PUBLISH.toString(), groupAuthorities.get(0).getAuthority());

    }




}
