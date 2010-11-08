/**
 * 
 */
package eu.etaxonomy.cdm.model.description;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * This class represents a statement or a question within a (polytomous) key.
 * Compare with SDD SimpleRepresentation.
 * 
 * @author a.mueller
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyStatement", propOrder = {
    "label"
//    ,"mediaObject"
})
@XmlRootElement(name = "KeyStatement")
@Entity
@Audited
public class KeyStatement extends VersionableEntity {
	private static final long serialVersionUID = 3771323100914695139L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(KeyStatement.class);
 
	
	@XmlElement(name = "MultiLanguageText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany (fetch= FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN })
//    @IndexedEmbedded
    private Map<Language, LanguageString> label = new HashMap<Language, LanguageString>();
	
	//private mediaObjects needs to be discussed (how to implement the role of the media)

	
	   
	public static KeyStatement NewInstance(){
		KeyStatement result = new KeyStatement();
		return result;
	}

	
	public static KeyStatement NewInstance(String defaultLabel){
		KeyStatement result = new KeyStatement();
		result.putLabel(defaultLabel, Language.DEFAULT());
		return result;
	}
	
	/**
	 * 
	 */
	public KeyStatement() {
	}

// ********************************* METHODS ***************************/
	
	/** 
	 * Returns the label with the content of <i>this</i> key statement. 
	 * The different {@link LanguageString language strings} (texts) contained in the
	 * label should all have the same meaning.
	 * 
	 * @see	#getText(Language)
	 */
    public Map<Language, LanguageString> getLabel() {
		return label;
	}
    
    /**
     * Returns the label with the content of <i>this</i> key statement for
     * a specific language.
     * 
     * @param language the language in which the label is formulated
     * @return
     */
    public LanguageString getLabel(Language language){
    	return label.get(language);
    }
    
    public void setLabel(Map<Language,LanguageString> label) {
    	this.label = label;
    }

	/** 
	 * Returns the text string in the given {@link Language language} with the content
	 * of <i>this</i> key statement.
	 * 
	 * @param language	the language in which the label is formulated
	 * @see				#getLabel(Language)
	 */ 
	public String getLabelText(Language language) {
		LanguageString languageString = label.get(language);
		if (languageString == null){
			return null;
		}else{
			return languageString.getText();
		}
	}
    
    /**
	 * Returns the LanguageString in the preferred language. Preferred languages
	 * are specified by the parameter languages, which receives a list of
	 * Language instances in the order of preference. If no representation in
	 * any preferred languages is found the method falls back to return the
	 * Representation in Language.DEFAULT() and if necessary further falls back
	 * to return the first element found if any.
	 * 
	 * TODO think about this fall-back strategy & 
	 * see also {@link TermBase#getPreferredRepresentation(List)}
	 * 
	 * @param languages
	 * @return
	 */
	public LanguageString getPreferredLanguageString(List<Language> languages) {
		return MultilanguageTextHelper.getPreferredLanguageString(label, languages);
	}
	
	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language}, returns it and adds it to the multilanguage 
	 * text representing the content of <i>this</i> text data.
	 * 
	 * @param text		the string representing the content of the text data
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @return			the language string
	 * @see    	   		#getMultilanguageText()
	 * @see    	   		#putText(LanguageString)
	 */
	public LanguageString putLabel(String label, Language language) {
		LanguageString result = this.label.put(language , LanguageString.NewInstance(label, language));
		return (result == null ? null : result);
	}
	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the label.
	 * The given language string will be returned. 
	 * 
	 * @param languageString	the language string representing the content of
	 * 							the text data in a particular language
	 * @return					the language string
	 * @see    	   				#getMultilanguageText()
	 * @see    	   				#putText(String, Language)
	 */
	public LanguageString putText(LanguageString languageString) {
		if (languageString == null){
			return null;
		}else{
			Language language = languageString.getLanguage();
			return this.label.put(language, languageString);
		}
	}
	/** 
	 * Removes from label the one {@link LanguageString language string}
	 * with the given {@link Language language}. Returns the removed
	 * language string.
	 *
	 * @param  language	the language in which the language string to be removed
	 * 					has been formulated
	 * @return			the language string associated with the given language
	 * @see     		#getLabelText()
	 */
	public LanguageString removeText(Language language) {
		return this.label.remove(language);
	}
	
	/** 
	 * Returns the number of {@link Language languages} in which the label
	 * of <i>this</i> key statement has been formulated.
	 * 
	 * @see	#getMultilanguageText()
	 */
	public int countLanguages(){
		return label.size();
	}


}
