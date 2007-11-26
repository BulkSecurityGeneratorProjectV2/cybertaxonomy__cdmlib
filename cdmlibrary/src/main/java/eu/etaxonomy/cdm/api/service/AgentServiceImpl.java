/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.ITaxonNameDao;


/**
 * @author a.mueller
 *
 */
@Service
public class AgentServiceImpl extends ServiceBase<Agent> implements IAgentService {
    private static final Logger logger = Logger.getLogger(AgentServiceImpl.class);

	public List<Agent> findAgentsByTitle(String title) {
		return super.findCdmObjectsByTitle(title);
	}

	public Agent getAgentByUuid(String uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	public String saveAgent(Agent agent) {
		return super.saveCdmObject(agent);
	}

}
