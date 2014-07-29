package eu.etaxonomy.cdm.api.service.dto;

import java.util.List;

import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;

public class DerivateHierarchyDTO {

	private DerivedUnit typeUnit;
	private List<DnaSample> dnaSamples;
	private List<DerivedUnit> preservedSpecimensWithSpecimenScan;
	private int numberOfDerivates;
	private List<String> herbaria;
	private FieldUnit fieldUnit;

	//Filter Flags
	private boolean hasDna;
	private boolean hasDetailImage;

	//Row Attributes
	private String country;
	private String collection;
	private String date;
	private String herbarium;
	private boolean hasType;
	private boolean hasSpecimenScan;

	//Detail pop-down
	private String taxonName;
	private String protologue;
	private String citation;
	private List<String> types;
	private List<String> specimenScans;
	private List<String> molecularData;
	private List<String> detailImages;
    /**
     * @return the typeUnit
     */
    public DerivedUnit getTypeUnit() {
        return typeUnit;
    }
    /**
     * @param typeUnit the typeUnit to set
     */
    public void setTypeUnit(DerivedUnit typeUnit) {
        this.typeUnit = typeUnit;
    }
    /**
     * @return the dnaSamples
     */
    public List<DnaSample> getDnaSamples() {
        return dnaSamples;
    }
    /**
     * @param dnaSamples the dnaSamples to set
     */
    public void setDnaSamples(List<DnaSample> dnaSamples) {
        this.dnaSamples = dnaSamples;
    }
    /**
     * @return the preservedSpecimensWithSpecimenScan
     */
    public List<DerivedUnit> getPreservedSpecimensWithSpecimenScan() {
        return preservedSpecimensWithSpecimenScan;
    }
    /**
     * @param preservedSpecimensWithSpecimenScan the preservedSpecimensWithSpecimenScan to set
     */
    public void setPreservedSpecimensWithSpecimenScan(List<DerivedUnit> preservedSpecimensWithSpecimenScan) {
        this.preservedSpecimensWithSpecimenScan = preservedSpecimensWithSpecimenScan;
    }
    /**
     * @return the numberOfDerivates
     */
    public int getNumberOfDerivates() {
        return numberOfDerivates;
    }
    /**
     * @param numberOfDerivates the numberOfDerivates to set
     */
    public void setNumberOfDerivates(int numberOfDerivates) {
        this.numberOfDerivates = numberOfDerivates;
    }
    /**
     * @return the herbaria
     */
    public List<String> getHerbaria() {
        return herbaria;
    }
    /**
     * @param herbaria the herbaria to set
     */
    public void setHerbaria(List<String> herbaria) {
        this.herbaria = herbaria;
    }
    /**
     * @return the fieldUnit
     */
    public FieldUnit getFieldUnit() {
        return fieldUnit;
    }
    /**
     * @param fieldUnit the fieldUnit to set
     */
    public void setFieldUnit(FieldUnit fieldUnit) {
        this.fieldUnit = fieldUnit;
    }
    /**
     * @return the hasDna
     */
    public boolean isHasDna() {
        return hasDna;
    }
    /**
     * @param hasDna the hasDna to set
     */
    public void setHasDna(boolean hasDna) {
        this.hasDna = hasDna;
    }
    /**
     * @return the hasDetailImage
     */
    public boolean isHasDetailImage() {
        return hasDetailImage;
    }
    /**
     * @param hasDetailImage the hasDetailImage to set
     */
    public void setHasDetailImage(boolean hasDetailImage) {
        this.hasDetailImage = hasDetailImage;
    }
    /**
     * @return the country
     */
    public String getCountry() {
        return country;
    }
    /**
     * @param country the country to set
     */
    public void setCountry(String country) {
        this.country = country;
    }
    /**
     * @return the collection
     */
    public String getCollection() {
        return collection;
    }
    /**
     * @param collection the collection to set
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }
    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }
    /**
     * @return the herbarium
     */
    public String getHerbarium() {
        return herbarium;
    }
    /**
     * @param herbarium the herbarium to set
     */
    public void setHerbarium(String herbarium) {
        this.herbarium = herbarium;
    }
    /**
     * @return the hasType
     */
    public boolean isHasType() {
        return hasType;
    }
    /**
     * @param hasType the hasType to set
     */
    public void setHasType(boolean hasType) {
        this.hasType = hasType;
    }
    /**
     * @return the hasSpecimenScan
     */
    public boolean isHasSpecimenScan() {
        return hasSpecimenScan;
    }
    /**
     * @param hasSpecimenScan the hasSpecimenScan to set
     */
    public void setHasSpecimenScan(boolean hasSpecimenScan) {
        this.hasSpecimenScan = hasSpecimenScan;
    }
    /**
     * @return the taxonName
     */
    public String getTaxonName() {
        return taxonName;
    }
    /**
     * @param taxonName the taxonName to set
     */
    public void setTaxonName(String taxonName) {
        this.taxonName = taxonName;
    }
    /**
     * @return the protologue
     */
    public String getProtologue() {
        return protologue;
    }
    /**
     * @param protologue the protologue to set
     */
    public void setProtologue(String protologue) {
        this.protologue = protologue;
    }
    /**
     * @return the citation
     */
    public String getCitation() {
        return citation;
    }
    /**
     * @param citation the citation to set
     */
    public void setCitation(String citation) {
        this.citation = citation;
    }
    /**
     * @return the types
     */
    public List<String> getTypes() {
        return types;
    }
    /**
     * @param types the types to set
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }
    /**
     * @return the specimenScans
     */
    public List<String> getSpecimenScans() {
        return specimenScans;
    }
    /**
     * @param specimenScans the specimenScans to set
     */
    public void setSpecimenScans(List<String> specimenScans) {
        this.specimenScans = specimenScans;
    }
    /**
     * @return the molecularData
     */
    public List<String> getMolecularData() {
        return molecularData;
    }
    /**
     * @param molecularData the molecularData to set
     */
    public void setMolecularData(List<String> molecularData) {
        this.molecularData = molecularData;
    }
    /**
     * @return the detailImages
     */
    public List<String> getDetailImages() {
        return detailImages;
    }
    /**
     * @param detailImages the detailImages to set
     */
    public void setDetailImages(List<String> detailImages) {
        this.detailImages = detailImages;
    }


}
