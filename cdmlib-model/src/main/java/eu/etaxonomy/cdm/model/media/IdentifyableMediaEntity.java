package eu.etaxonomy.cdm.model.media;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

@MappedSuperclass
public abstract class IdentifyableMediaEntity extends IdentifiableEntity implements IMediaDocumented, IMediaEntity{
	static Logger logger = Logger.getLogger(IdentifyableMediaEntity.class);

	private Set<Media> media = getNewMediaSet();
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.media.IMediaEntity#getMedia()
	 */
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Media> getMedia() {
		return media;
	}
	protected void setMedia(Set<Media> media) {
		this.media = media;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.media.IMediaEntity#addMedia(eu.etaxonomy.cdm.model.media.Media)
	 */
	public void addMedia(Media media) {
		this.media.add(media);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.media.IMediaEntity#removeMedia(eu.etaxonomy.cdm.model.media.Media)
	 */
	public void removeMedia(Media media) {
		this.media.remove(media);
	}
	
//******************** CLONE **********************************************/
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#clone()
	 */
	public Object clone() throws CloneNotSupportedException{
		IdentifyableMediaEntity result = (IdentifyableMediaEntity)super.clone();
		//Media
		Set<Media> media = getNewMediaSet();
		media.addAll(this.media);
		result.setMedia(media);
		//no changes to: -
		return result;
	}
	
	@Transient
	private Set<Media> getNewMediaSet(){
		return new HashSet<Media>();
	}

}
