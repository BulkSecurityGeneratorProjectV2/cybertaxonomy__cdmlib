package eu.etaxonomy.cdm.model.common;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.wsdl.Definition;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.ibm.lsid.MalformedLSIDException;

@Entity
@TypeDefs(@TypeDef(name="wsdlDefinitionUserType", typeClass=WSDLDefinitionUserType.class))
public class LSIDAuthority extends CdmBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9168994979216936689L;

	public static final String AUTHORITY_ID_PREFIX = "lsidauth:";
	
	private static final String AUTHORITY_PROTOCOL="http";
    private static final String AUTHORITY_PATH="/authority/";

    @NaturalId
	private String authority;
	
	// the resolved components of the lsid
	private String server;
	private int port = -1;
	private String url;
	
	// the wsdl describing how to invoke operations on the authority
	@Type(type = "wsdlDefinitionUserType")
	private Definition authorityWSDL;
	
	@CollectionOfElements(fetch = FetchType.LAZY)
	private Map<String,Class<? extends IIdentifiableEntity>> namespaces = new HashMap<String,Class<? extends IIdentifiableEntity>>();
	
	/**
	 * Hibernate requires a no-arguement constructor (this could be private, I suppose)
	 */
	private LSIDAuthority() { }
	
	/**
	 * Construct an LSID authority object.
	 * @param String LSID Authority must be valid authority string, or an ID of the form: lsidauth:<validauthoritystring>"
	 */
	public LSIDAuthority(String authstr) throws MalformedLSIDException {
		try {
			authority = authstr.toLowerCase();
			if (authority.startsWith(AUTHORITY_ID_PREFIX))
				authority = authority.substring(AUTHORITY_ID_PREFIX.length());
		} catch (Exception e) {
			throw new MalformedLSIDException(e, "LSID Authority must be valid authority string, or of form: lsidauth:<validauthoritystring>");
		}								
	}
	
	/**
	 * Convenience constructor, construct an LSID authority object
	 * @param LSID use this LSID's authority to construct this object
	 */
	public LSIDAuthority(LSID lsid) throws MalformedLSIDException {
		authority = lsid.getAuthority();						
	}		
	
	/**
	 * Returns the authority String
	 * @return String the authority String
	 */
	public String getAuthority() {
		return authority;	
	}
	
	/**
	 * Returns the authority ID representation of this authority, lsidauth:authstr
	 * @return String the ID representation
	 */
	public String getAuthorityID() {
		return AUTHORITY_ID_PREFIX + authority;
	}
	
	/**
	 * Returns the authority String
	 * @return String the authority String
	 */
	public String toString() {
		return authority;	
	}
	
	/**
	 * Tests equality on the authority string
	 * @return
	 */
	public boolean equals(Object o) {
	    if (!(o instanceof LSIDAuthority))
	        return false;
	    LSIDAuthority auth = (LSIDAuthority)o;
	    return o.toString().equals(toString());
	}
	
	/**
	 * Returns the port of the resolved Authority, invalid until resolved.
	 * @return int the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns the server of the resolved Authority, invalid until resolved.
	 * @return String the hostname of the server
	 */
	public String getServer() {
		return server;
	}
	
	/**
	 * Returns the url of the resolved Authority, invalid until resolved.  This overrides the 
	 * server and port properties, and might contain a full path or even a different protocol.
	 * @return String
	 */
	public String getUrl() {
		if (url == null) {
			if (server != null && port != -1)
				url = "http://" + getServer() + ":" + getPort() + AUTHORITY_PATH;
			else
				return null;
		}
		return url;
	}

	/**
	 * Sets the port.
	 * @param port The port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Sets the server.
	 * @param server The server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}
	
	/**
	 * Sets the URL to use.  This overrides the server and port properties, and might contain a full path or even a 
	 * different protocol.
	 * @param server The server to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * Set the wsdl describing the ports available
	 * @param LSIDWSDLWrapper the wsdl to set
	 */
	public void setWSDL(Definition wsdl) {
		this.authorityWSDL = wsdl;
	}
	
	/**
	 * get the wsdl describing the ports available
	 * @return LSIDWSDLWRapper the wsdl
	 */
	public Definition getWSDL() {
		return this.authorityWSDL;
	}
	
	/**
	 * @return boolean, whether or not the authority has been resolved.
	 */
	public boolean isResolved() {
		return (getUrl() != null);
	}
	
	/**
	 * get the url of an authority with the given server and port
	 */
	public String getAuthorityEnpointURL(String server, int port) {
		return AUTHORITY_PROTOCOL + "://" + server + ":" + port + AUTHORITY_PATH;	
	}

	public Map<String,Class<? extends IIdentifiableEntity>> getNamespaces() {
		return namespaces;
	}
	
	public void addNamespace(String namespace, Class<? extends IIdentifiableEntity> identifiableClass) {
		this.namespaces.put(namespace, identifiableClass);
	}
	
	public void removeNamespace(String namespace) {
		this.namespaces.remove(namespace);
	}
}
