// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.UUID;

import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.SaltSource;
import org.springframework.security.providers.dao.UserCache;
import org.springframework.security.providers.dao.cache.NullUserCache;
import org.springframework.security.providers.dao.salt.ReflectionSaltSource;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IGrantedAuthorityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IGroupDao;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Service
@Transactional(readOnly=true)
public class UserService extends ServiceBase<User,IUserDao> implements IUserService {
	
	protected IGroupDao groupDao;
	
	protected IGrantedAuthorityDao grantedAuthorityDao;
	
	private SaltSource saltSource = new ReflectionSaltSource();
	
	private PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
	
	private AuthenticationManager authenticationManager;
	
	private UserCache userCache = new NullUserCache();
	
	@Autowired(required = false)
	public void setUserCache(UserCache userCache) {
		Assert.notNull(userCache, "userCache cannot be null");
		this.userCache = userCache;
	}
	
	@Autowired(required = false)
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		
		this.passwordEncoder = passwordEncoder;
	}

	@Autowired(required = false)
	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}
	
	@Autowired(required= false)
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
	@Override
	@Autowired
	protected void setDao(IUserDao dao) {
		this.dao = dao;
	}
	
	@Autowired
	public void setGroupDao(IGroupDao groupDao) {
		this.groupDao = groupDao;
	}
	
	@Autowired
	public void setGrantedAuthorityDao(IGrantedAuthorityDao grantedAuthorityDao) {
		this.grantedAuthorityDao = grantedAuthorityDao;
	}
	
	@Transactional(readOnly=false)
	protected Authentication createNewAuthentication(Authentication currentAuth, String newPassword) {
		UserDetails user = loadUserByUsername(currentAuth.getName());
			
		UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
		newAuthentication.setDetails(currentAuth.getDetails());
			
		return newAuthentication;
	}
	
	@Transactional(readOnly=false)
	public void changePassword(String oldPassword, String newPassword) {
		Assert.hasText(oldPassword);
		Assert.hasText(newPassword);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
			User user = (User)authentication.getPrincipal();
			
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), oldPassword));
			
			Object salt = this.saltSource.getSalt(user);
			
			String password = passwordEncoder.encodePassword(newPassword, salt);
			((User)user).setPassword(password);
			
			dao.update((User)user);
			SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(authentication, newPassword));
			userCache.removeUserFromCache(user.getUsername());
		} else {
			throw new AccessDeniedException("Can't change password as no Authentication object found in context for current user.");
		}		
	}
	
	@Transactional(readOnly=false)
	public void changePasswordForUser(String username, String newPassword) {
		Assert.hasText(username);
		Assert.hasText(newPassword);
		
		try {
		    User user = dao.findUserByUsername(username);
		    if(user == null) {
				throw new UsernameNotFoundException(username);
			}
		    
            Object salt = this.saltSource.getSalt(user);
			
			String password = passwordEncoder.encodePassword(newPassword, salt);
			((User)user).setPassword(password);
			
			dao.update((User)user);
			userCache.removeUserFromCache(user.getUsername());
		} catch(NonUniqueResultException nure) {
			throw new IncorrectResultSizeDataAccessException("More than one user found with name '" + username + "'", 1);
		}
	}

	@Transactional(readOnly=false)
	public void createUser(UserDetails user) {
		Assert.isInstanceOf(User.class, user);
		
		String rawPassword = user.getPassword();
		Object salt = this.saltSource.getSalt(user);
		
		String password = passwordEncoder.encodePassword(rawPassword, salt);
		((User)user).setPassword(password);
		
		dao.save((User)user);
	}

	@Transactional(readOnly=false)
	public void deleteUser(String username) {
		Assert.hasLength(username);
		
		User user = dao.findUserByUsername(username); 
        if(user != null) {		
		    dao.delete((User)user);
        }
        
        userCache.removeUserFromCache(username);
	}

	@Transactional(readOnly=false)
	public void updateUser(UserDetails user) {
		Assert.isInstanceOf(User.class, user);
		
		dao.update((User)user);
		userCache.removeUserFromCache(user.getUsername());
	}

	public boolean userExists(String username) {
		Assert.hasText(username);
		
		User user = dao.findUserByUsername(username);
		return user != null;
	}

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		Assert.hasText(username);
		try {
		    User user = dao.findUserByUsername(username);
		    if(user == null) {
				throw new UsernameNotFoundException(username);
			}
		    return user;
		} catch(NonUniqueResultException nure) {
			throw new IncorrectResultSizeDataAccessException("More than one user found with name '" + username + "'", 1);
		}
	}

	@Transactional(readOnly=false)
	public void addGroupAuthority(String groupName, GrantedAuthority authority) {
		Assert.hasText(groupName);
		Assert.notNull(authority);
		
		Group group = groupDao.findGroupByName(groupName);
		if(group.getGrantedAuthorities().add(authority)) {
			groupDao.update(group);
		}
	}

	@Transactional(readOnly=false)
	public void addUserToGroup(String username, String groupName) {
		Assert.hasText(username);
		Assert.hasText(groupName);
		
		Group group = groupDao.findGroupByName(groupName);
		User user = dao.findUserByUsername(username);
		
		if(group.addMember(user)) {
			groupDao.update(group);
			userCache.removeUserFromCache(user.getUsername());
		}		
	}

	@Transactional(readOnly=false)
	public void createGroup(String groupName, GrantedAuthority[] authorities) {
		Assert.hasText(groupName);
		Assert.notNull(authorities);
		
		Group group = new Group();
		group.setName(groupName);
		
		for(GrantedAuthority authority : authorities) {
			group.getGrantedAuthorities().add(authority);
		}
		
		groupDao.save(group);
	}

	@Transactional(readOnly=false)
	public void deleteGroup(String groupName) {
		Assert.hasText(groupName);
		
		Group group = groupDao.findGroupByName(groupName);
		groupDao.delete(group);
	}

	public String[] findAllGroups() {
		List<String> names = groupDao.listNames(null,null);
		return names.toArray(new String[names.size()]);
	}

	public GrantedAuthority[] findGroupAuthorities(String groupName) {
		Assert.hasText(groupName);
		Group group = groupDao.findGroupByName(groupName);
		
		return group.getGrantedAuthorities().toArray(new GrantedAuthority[group.getGrantedAuthorities().size()]);
	}

	public String[] findUsersInGroup(String groupName) {
		Assert.hasText(groupName);
		Group group = groupDao.findGroupByName(groupName);
		
		List<String> users = groupDao.listMembers(group, null, null);
		
		return users.toArray(new String[users.size()]);
	}

	@Transactional(readOnly=false)
	public void removeGroupAuthority(String groupName,	GrantedAuthority authority) {
		Assert.hasText(groupName);
		Assert.notNull(authority);
		
		Group group = groupDao.findGroupByName(groupName);
		
		if(group.getGrantedAuthorities().remove(authority)) {
			groupDao.update(group);
		}
	}

	@Transactional(readOnly=false)
	public void removeUserFromGroup(String username, String groupName) {
		Assert.hasText(username);
		Assert.hasText(groupName);
		
		Group group = groupDao.findGroupByName(groupName);
		User user = dao.findUserByUsername(username);
		
		if(group.removeMember(user)) {
			groupDao.update(group);
			userCache.removeUserFromCache(user.getUsername());
		}
	}

	@Transactional(readOnly=false)
	public void renameGroup(String oldName, String newName) {
		Assert.hasText(oldName);
		Assert.hasText(newName);
		
		Group group = groupDao.findGroupByName(oldName);
		
		group.setName(newName);
		groupDao.update(group);
	}
	
	@Transactional(readOnly=false)
	public UUID save(User user) {
		return dao.save(user);
	}

	@Transactional(readOnly=false)
	public UUID saveGrantedAuthority(GrantedAuthority grantedAuthority) {
		return grantedAuthorityDao.save((GrantedAuthorityImpl)grantedAuthority);
	}
	
	@Transactional(readOnly=false)
	public UUID saveGroup(Group group) {
		return groupDao.save(group);
	}

	public <TYPE extends User> Pager<TYPE> list(Class<TYPE> type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
		return null;
	}
} 
