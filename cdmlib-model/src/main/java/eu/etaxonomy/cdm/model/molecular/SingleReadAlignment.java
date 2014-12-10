/**
 * 
 */
package eu.etaxonomy.cdm.model.molecular;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * @author a.mueller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SingleReadAlignment", propOrder = {
	"consensusAlignment",
	"singleRead",
	"shifts",
	"editedSequence",
	"reverseComplement"
})
@XmlRootElement(name = "SingleReadAlignment")
@Entity
@Audited
public class SingleReadAlignment extends VersionableEntity {
	private static final long serialVersionUID = 6141518347067279304L;

	/** @see #getDnaMarker() */
	@XmlElement(name = "ConsensusAlignment")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	//for now we do not cascade but expect the user to save the sequence manually
	private Sequence consensusAlignment;
	
	/** @see #getDnaMarker() */
	@XmlElement(name = "SingleRead")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private SingleRead singleRead;
	
	//TODO XML mapping / user type
	@Type(type="shiftUserType")
    private Shift[] shifts;
	
	@XmlElement(name = "EditedSequence")
    @Lob
    private String editedSequence;
	
	@XmlElement(name = "ReverseComplement")
    private boolean reverseComplement;
	
	
	public static class Shift{
		public int position;
		public int shift;
		
		public Shift(){};
		public Shift(int position, int steps) {
			this.position = position; 
			this.shift = steps;
		}
		
		@Override
		public String toString(){
			return String.valueOf(position) + "," + String.valueOf(shift);
		}
	}
	
//****************** FACTORY *******************/
	
	public static SingleReadAlignment NewInstance(Sequence consensusSequence, SingleRead singleRead){
		return new SingleReadAlignment(consensusSequence, singleRead, null, null);
	}
	
	public static SingleReadAlignment NewInstance(Sequence consensusSequence, SingleRead singleRead,
			Shift[] shifts, String editedSequence){
		return new SingleReadAlignment(consensusSequence, singleRead, shifts, editedSequence);
	}

// ***************** CONSTRUCTOR *************************/	
	
	protected SingleReadAlignment(){};
	
	private SingleReadAlignment(Sequence consensusAlignment, SingleRead singleRead,
			Shift[] shifts, String editedSequence){
		setConsensusAlignment(consensusAlignment);
		setSingleRead(singleRead);
		this.shifts = shifts;
		this.editedSequence = editedSequence;
	}
	

// ****************** GETTER / SETTER ***********************/	
	
	//consensus sequence
	public Sequence getConsensusSequence() {
		return consensusAlignment;
	}
	public void setConsensusAlignment(Sequence consensusAlignment) {
		if (this.consensusAlignment != null && this.consensusAlignment.getSingleReadAlignments().contains(this)){
			this.consensusAlignment.removeSingleReadAlignment(this);
		}
		this.consensusAlignment = consensusAlignment;
		if (consensusAlignment != null && ! consensusAlignment.getSingleReadAlignments().contains(this)){
			consensusAlignment.addSingleReadAlignment(this);
		}	
	}

	public SingleRead getSingleRead() {
		return singleRead;
	}
	public void setSingleRead(SingleRead singleRead) {
//		if (this.singleRead != null xxx){
//			this.singleRead.removeSingleReadAlignment(this);
//		}
		this.singleRead = singleRead;
//		if (singleRead != null && singleRead.getSingleReadAlignments().contains(this)){
//			singleRead.addSingleReadAlignment(this);
//		}	
	}
	
	//shifts
	public Shift[] getShifts() {
		return shifts;
	}
	public void setShifts(Shift[] shifts) {
		this.shifts = shifts;
	}

	//edited sequence
	public String getEditedSequence() {
		return editedSequence;
	}

	public void setEditedSequence(String editedSequence) {
		this.editedSequence = editedSequence;
	}

// ******************* CLONE *********************/



	@Override
	public Object clone() throws CloneNotSupportedException {
		//all objects can be reused
		return super.clone();
	}
}
