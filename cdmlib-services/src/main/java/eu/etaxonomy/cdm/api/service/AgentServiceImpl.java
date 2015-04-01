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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;



/**
 * @author a.mueller
 *
 */
@Service
@Transactional(readOnly = true)
public class AgentServiceImpl extends IdentifiableServiceBase<AgentBase,IAgentDao> implements IAgentService {
    private static final Logger logger = Logger.getLogger(AgentServiceImpl.class);
	
    @Autowired
    ICdmGenericDao genericDao;

	@Autowired
	protected void setDao(IAgentDao dao) {
		assert dao != null;
		this.dao = dao;
	}
    
 	/**
	 * Constructor
	 */
	public AgentServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load AgentService Bean"); }
	}
	

	@Override
	@Transactional(readOnly = false)
    public void updateTitleCache(Class<? extends AgentBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<AgentBase> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = AgentBase.class;
		}
		super.updateTitleCacheImpl(clazz, stepSize, cacheStrategy, monitor);
	}
	
	@Override
	public List<Institution> searchInstitutionByCode(String code) {
		return dao.getInstitutionByCode(code);
	}

	@Override
	public Pager<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countInstitutionalMemberships(person);
		
		List<InstitutionalMembership> results = new ArrayList<InstitutionalMembership>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getInstitutionalMemberships(person, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<InstitutionalMembership>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public Pager<Person> getMembers(Team team, Integer pageSize, Integer pageNumber) {
		Integer numberOfResults = dao.countMembers(team);
			
		List<Person> results = new ArrayList<Person>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMembers(team, pageSize, pageNumber); 
		}
			
		return new DefaultPagerImpl<Person>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public Pager<Address> getAddresses(AgentBase agent, Integer pageSize, Integer pageNumber) {
		Integer numberOfResults = dao.countAddresses(agent);
		
		List<Address> results = new ArrayList<Address>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getAddresses(agent, pageSize, pageNumber); 
		}
			
		return new DefaultPagerImpl<Address>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public List<UuidAndTitleCache<Team>> getTeamUuidAndNomenclaturalTitle() {
		return dao.getTeamUuidAndNomenclaturalTitle();
	}

	@Override
	public List<UuidAndTitleCache<Person>> getPersonUuidAndTitleCache() {
		return dao.getPersonUuidAndTitleCache();
	}

	@Override
	public List<UuidAndTitleCache<Team>> getTeamUuidAndTitleCache() {
		return dao.getTeamUuidAndTitleCache();
	}

	@Override
	public List<UuidAndTitleCache<Institution>> getInstitutionUuidAndTitleCache() {
		return dao.getInstitutionUuidAndTitleCache();
	}
	
	@Override
    public DeleteResult delete(AgentBase base){
    	
		DeleteResult result = this.isDeletable(base, null);
    	
    	if (result.isOk()){
			if (base instanceof Team){
				Team baseTeam = (Team) base;
				List<Person> members = baseTeam.getTeamMembers();
				List<Person> temp = new ArrayList<Person>();
				for (Person member:members){
					temp.add(member);
				}
				for (Person member: temp){
					members.remove(member);
				}
			}
			saveOrUpdate(base);
			
			dao.delete(base);
			
		}
		
		return result;		
    }

	@Override
	public Person convertTeam2Person(Team team) throws MergeException {
		Person result = null;
		team = CdmBase.deproxy(team, Team.class);
		if (team.getTeamMembers().size() > 1){
			throw new IllegalArgumentException("Team must not have more than 1 member to be convertable into a person");
		}else if (team.getTeamMembers().size() == 1){
			result = team.getTeamMembers().get(0);
			IMergeStrategy strategy = DefaultMergeStrategy.NewInstance(TeamOrPersonBase.class);
			strategy.setDefaultCollectionMergeMode(MergeMode.FIRST);
			genericDao.merge(result, team, strategy);
		}else if (team.getTeamMembers().isEmpty()){
			result = Person.NewInstance();
			genericDao.save(result);
			IMergeStrategy strategy = DefaultMergeStrategy.NewInstance(TeamOrPersonBase.class);
			strategy.setDefaultMergeMode(MergeMode.SECOND);
			strategy.setDefaultCollectionMergeMode(MergeMode.SECOND);
			genericDao.merge(result, team, strategy);
		}else{
			throw new IllegalStateException("Unhandled state of team members collection");
		}
		
		return result;
	}

	@Override
	public Team convertPerson2Team(Person person) throws MergeException, IllegalArgumentException {
		Team team = Team.NewInstance();
		IMergeStrategy strategy = DefaultMergeStrategy.NewInstance(TeamOrPersonBase.class);
		strategy.setDefaultMergeMode(MergeMode.SECOND);
		strategy.setDefaultCollectionMergeMode(MergeMode.SECOND);
		
		if (! genericDao.isMergeable(team, person, strategy)){
			throw new MergeException("Person can not be transformed into team.");
		}
		try {
			this.save(team);
			team.setProtectedNomenclaturalTitleCache(true);
			genericDao.merge(team, person, strategy);
//			team.setNomenclaturalTitle(person.getNomenclaturalTitle(), true);
		} catch (Exception e) {
			throw new MergeException("Unhandled merge exception", e);
		}
		return team;
	}
	
	
}
