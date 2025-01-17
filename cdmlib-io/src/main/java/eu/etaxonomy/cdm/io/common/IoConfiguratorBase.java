/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/
package eu.etaxonomy.cdm.io.common;

import org.apache.logging.log4j.LogManager;import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.config.Configuration;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.persistence.hibernate.HibernateConfiguration;

/**
 * Base class for all import/export configurators.
 * @author a.babadshanjan
 * @since 16.11.2008
 */
public abstract class IoConfiguratorBase extends ObservableBase implements IIoConfigurator{

    private static final long serialVersionUID = -2254648962451309933L;

    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(IoConfiguratorBase.class);

    public static final boolean NO_UNPUBLISHED = DaoBase.NO_UNPUBLISHED;
    public static final boolean INCLUDE_UNPUBLISHED = DaoBase.INCLUDE_UNPUBLISHED;

	//im-/export uses Classification for is_taxonomically_included_in relationships
	private boolean useClassification = true;

//	protected Class<ICdmIO>[] ioClassList;
	private DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;

	protected ICdmRepository cdmApp = null;

	//authentification token
	protected UsernamePasswordAuthenticationToken authenticationToken;

	protected HibernateConfiguration hibernateConfig = HibernateConfiguration.NewDefaultInstance();

    //etc
	private IProgressMonitor progressMonitor;
	private String userFriendlyIOName;

    @Override
    public String getUserFriendlyIOName() {
        return userFriendlyIOName;
    }
    public void setUserFriendlyIOName(String userFriendlyIOName) {
        this.userFriendlyIOName = userFriendlyIOName;
    }

	@Override
    public DbSchemaValidation getDbSchemaValidation() {
		return dbSchemaValidation;
	}

	@Override
    public void setDbSchemaValidation(DbSchemaValidation dbSchemaValidation) {
		this.dbSchemaValidation = dbSchemaValidation;
	}

	@Override
    public ICdmRepository getCdmAppController(){
		return this.cdmApp;
	}
	@Override
    public void setCdmAppController(ICdmRepository cdmApp) {
		this.cdmApp = cdmApp;
	}

	public boolean isUseClassification() {
		return useClassification;
	}
	public void setUseClassification(boolean useClassification) {
		this.useClassification = useClassification;
	}

	@Override
	public void setProgressMonitor(IProgressMonitor monitor) {
		this.progressMonitor = monitor;
	}

	@Override
    public IProgressMonitor getProgressMonitor(){
		return progressMonitor != null ? progressMonitor : new NullProgressMonitor();
	}

	@Override
	public UsernamePasswordAuthenticationToken getAuthenticationToken() {
		return this.authenticationToken;
	}
	@Override
	public void setAuthenticationToken(UsernamePasswordAuthenticationToken authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	@Override
	public void setAuthentication(String login, String password) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login, password);
		this.authenticationToken = token;
	}

	@Override
	public void authenticateAsDefaultAdmin() {
		setAuthenticationToken(new UsernamePasswordAuthenticationToken(Configuration.adminLogin, Configuration.adminPassword));
	}

    @Override
    public HibernateConfiguration getHibernateConfig() {
        return hibernateConfig;
    }
}
