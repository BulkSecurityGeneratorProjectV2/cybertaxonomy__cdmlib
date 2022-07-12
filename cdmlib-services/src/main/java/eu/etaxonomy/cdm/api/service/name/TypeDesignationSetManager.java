/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.name;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.exception.RegistrationValidationException;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationSet.TypeDesignationSetType;
import eu.etaxonomy.cdm.compare.name.NullTypeDesignationStatus;
import eu.etaxonomy.cdm.compare.name.TypeDesignationStatusComparator;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextBuilder;

/**
 * Manages a collection of {@link TypeDesignationBase type designations} for the same typified name.
 *
 * Type designations are ordered by the base type which is a {@link TaxonName} for {@link NameTypeDesignation NameTypeDesignations} or
 * in case of {@link SpecimenTypeDesignation SpecimenTypeDesignations} the  associate {@link FieldUnit} or the {@link DerivedUnit}
 * if the former is missing. The type designations per base type are furthermore ordered by the {@link TypeDesignationStatusBase}.
 *
 * The TypeDesignationSetManager also provides string representations of the whole ordered set of all
 * {@link TypeDesignationBase TypeDesignations} and of the TypeDesignationSets:
 * <ul>
 *  <li>{@link #print()}
 *  <li>{@link #getOrderedTypeDesignationSets()} ... {@link TypeDesignationSet#getLabel()}
 * </ul>
 * Prior using the representations you need to trigger their generation by calling {@link #buildString()}
 *
 * @author a.kohlbecker
 * @since Mar 10, 2017
 */
public class TypeDesignationSetManager {

    //currently not really in use
    enum NameTypeBaseEntityType{
        NAME_TYPE_DESIGNATION,
        TYPE_NAME;
    }

    private NameTypeBaseEntityType nameTypeBaseEntityType = NameTypeBaseEntityType.NAME_TYPE_DESIGNATION;

    private Map<UUID,TypeDesignationBase<?>> typeDesignations = new HashMap<>();

    private TaxonName typifiedName;

    private Class tdType1;

    /**
     * Sorts the base entities (TypedEntityReference) in the following order:
     *
     * 1. FieldUnits
     * 2. DerivedUnit (in case of missing FieldUnit we expect the base type to be DerivedUnit)
     * 3. NameType
     *
     * {@inheritDoc}
     */
    private Comparator<Entry<VersionableEntity,TypeDesignationSet>> entryComparator = (o1,o2)->{

         TypeDesignationSet ws1 = o1.getValue();
         TypeDesignationSet ws2 = o2.getValue();

         if (ws1.getWorkingsetType() != ws2.getWorkingsetType()){
             //first specimen types, then name types (very rare case anyway)
             return ws1.getWorkingsetType() == TypeDesignationSetType.NAME_TYPE_DESIGNATION_SET? 1:-1;
         }

         boolean hasStatus1 = !ws1.keySet().contains(null) && !ws1.keySet().contains(NullTypeDesignationStatus.SINGLETON());
         boolean hasStatus2 = !ws2.keySet().contains(null) && !ws2.keySet().contains(NullTypeDesignationStatus.SINGLETON());
         if (hasStatus1 != hasStatus2){
             //first without status as it is difficult to distinguish a non status from a "same" status record if the first record has a status and second has no status
             return hasStatus1? 1:-1;
         }

         //boolean hasStatus1 = ws1.getTypeDesignations(); //.stream().filter(td -> td.getSt);

         Class<?> type1 = o1.getKey().getClass();
         Class<?> type2 = o2.getKey().getClass();

         if(!type1.equals(type2)) {
             if(type1.equals(FieldUnit.class) || type2.equals(FieldUnit.class)){
                 // FieldUnits first
                 return type1.equals(FieldUnit.class) ? -1 : 1;
             } else {
                 // name types last (in case of missing FieldUnit we expect the base type to be DerivedUnit which comes into the middle)
                 return type2.equals(TaxonName.class) || type2.equals(NameTypeDesignation.class) ? -1 : 1;
             }
         } else {
//             tdType1 = ws1.getTypeDesignations().stream().map(td->td.get).sorted(null).findFirst().orElseGet(()->{return null;});
             String label1 = TypeDesignationSetFormatter.entityLabel(o1.getKey());
             String label2 = TypeDesignationSetFormatter.entityLabel(o2.getKey());
             return label1.compareTo(label2);
         }
     };

    /**
     * Groups the EntityReferences for each of the TypeDesignations by the according TypeDesignationStatus.
     * The TypeDesignationStatusBase keys are already ordered by the term order defined in the vocabulary.
     */
    private LinkedHashMap<VersionableEntity,TypeDesignationSet> orderedByTypesByBaseEntity;

    private List<String> problems = new ArrayList<>();

// **************************** CONSTRUCTOR ***********************************/

    public TypeDesignationSetManager(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations)
            throws RegistrationValidationException{
    	this(typeDesignations, null);
    }

    public TypeDesignationSetManager(@SuppressWarnings("rawtypes") Collection<TypeDesignationBase> typeDesignations,
            TaxonName typifiedName)
            throws RegistrationValidationException  {
        for (TypeDesignationBase<?> typeDes:typeDesignations){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        try {
        	findTypifiedName();
        }catch (RegistrationValidationException e) {
        	if (typifiedName == null) {
        		throw e;
        	}
        	this.typifiedName = typifiedName;
        }

        mapAndSort();
    }

    public TypeDesignationSetManager(HomotypicalGroup group) {
        for (TypeDesignationBase<?> typeDes: group.getTypeDesignations()){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        //findTypifiedName();
        mapAndSort();
    }

    public TypeDesignationSetManager(TaxonName typifiedName) {
        this.typifiedName = typifiedName;
    }

// **************************************************************************/

    /**
     * Add one or more TypeDesignations to the manager. This causes re-grouping and re-ordering
     * of all managed TypeDesignations.
     *
     * @param containgEntity
     * @param typeDesignations
     */
    public void addTypeDesigations(TypeDesignationBase<?> ... typeDesignations){
        for (TypeDesignationBase<?> typeDes: typeDesignations){
            this.typeDesignations.put(typeDes.getUuid(), typeDes);
        }
        mapAndSort();
    }

    public TaxonName getTypifiedName() {
        return typifiedName;
    }

    public void setNameTypeBaseEntityType(NameTypeBaseEntityType nameTypeBaseEntityType){
        this.nameTypeBaseEntityType = nameTypeBaseEntityType;
    }

    public NameTypeBaseEntityType getNameTypeBaseEntityType(){
        return nameTypeBaseEntityType;
    }

// ******************************** METHODS *********************************/

    /**
     * Groups and orders all managed TypeDesignations.
     */
    protected void mapAndSort() {

        Map<VersionableEntity,TypeDesignationSet> byBaseEntityByTypeStatus = new HashMap<>();
        this.typeDesignations.values().forEach(td -> mapTypeDesignation(byBaseEntityByTypeStatus, td));
        orderedByTypesByBaseEntity = orderByTypeByBaseEntity(byBaseEntityByTypeStatus);
    }

    private void mapTypeDesignation(Map<VersionableEntity,TypeDesignationSet> byBaseEntityByTypeStatus,
            TypeDesignationBase<?> td){

        td = HibernateProxyHelper.deproxy(td);
        TypeDesignationStatusBase<?> status = td.getTypeStatus();

        try {
            final VersionableEntity baseEntity = baseEntity(td);
//            final TypedEntityReference<? extends VersionableEntity> baseEntityReference = makeEntityReference(baseEntity);

            TaggedTextBuilder workingsetBuilder = new TaggedTextBuilder();
            boolean withCitation = true;
            TypeDesignationSetFormatter.buildTaggedTextForSingleType(td, withCitation, workingsetBuilder, 0);

            @SuppressWarnings({ "unchecked", "rawtypes" })
            TypeDesignationDTO<?> typeDesignationDTO
                = new TypeDesignationDTO(
                    td.getClass(),
                    td.getUuid(),
                    workingsetBuilder.getTaggedText(),
                    getTypeUuid(td));

            if(!byBaseEntityByTypeStatus.containsKey(baseEntity)){
                byBaseEntityByTypeStatus.put(baseEntity, new TypeDesignationSet(baseEntity));
            }
            byBaseEntityByTypeStatus.get(baseEntity).insert(status, typeDesignationDTO);

        } catch (DataIntegrityException e){
            problems.add(e.getMessage());
        }
    }


    /**
     * Returns the uuid of the type designated by this {@link TypeDesignationDTO#}.
     * This is either a TaxonName or a {@link SpecimenOrObservationBase}.
     */
    private UUID getTypeUuid(TypeDesignationBase<?> td) {
        IdentifiableEntity<?> type;
        if (td instanceof SpecimenTypeDesignation){
            type = ((SpecimenTypeDesignation) td).getTypeSpecimen();
        }else if (td instanceof NameTypeDesignation){
            type = ((NameTypeDesignation) td).getTypeName();
        }else{
            type = null;
        }
        return type == null? null : type.getUuid();
    }

    protected VersionableEntity baseEntity(TypeDesignationBase<?> td) throws DataIntegrityException {

        VersionableEntity baseEntity = null;
        if(td instanceof SpecimenTypeDesignation){
            SpecimenTypeDesignation std = (SpecimenTypeDesignation) td;
            FieldUnit fu = findFieldUnit(std.getTypeSpecimen());
            if(fu != null){
                baseEntity = fu;
            } else if(((SpecimenTypeDesignation) td).getTypeSpecimen() != null){
                baseEntity = ((SpecimenTypeDesignation) td).getTypeSpecimen();
            }
        } else if(td instanceof NameTypeDesignation){
            if(nameTypeBaseEntityType == NameTypeBaseEntityType.NAME_TYPE_DESIGNATION){
                baseEntity = td;
            } else {
                // only other option is TaxonName
                baseEntity = ((NameTypeDesignation)td).getTypeName();
            }
        }
        if(baseEntity == null) {
            throw new DataIntegrityException("Incomplete TypeDesignation, no type missin in " + td.toString());
        }
        return baseEntity;
    }

    //TODO maybe not needed anymore
    protected static TypedEntityReference<? extends VersionableEntity> makeEntityReference(VersionableEntity baseEntity) {

        baseEntity = CdmBase.deproxy(baseEntity);
        String label = TypeDesignationSetFormatter.entityLabel(baseEntity);

        TypedEntityReference<? extends VersionableEntity> baseEntityReference =
                new TypedEntityReference<>(baseEntity.getClass(), baseEntity.getUuid(), label);

        return baseEntityReference;
    }

    private LinkedHashMap<VersionableEntity,TypeDesignationSet> orderByTypeByBaseEntity(
            Map<VersionableEntity,TypeDesignationSet> stringsByTypeByBaseEntity){

       // order the FieldUnit TypeName keys
       Set<Entry<VersionableEntity,TypeDesignationSet>> entrySet
               = stringsByTypeByBaseEntity.entrySet();
       LinkedList<Entry<VersionableEntity,TypeDesignationSet>> baseEntityKeyList
               = new LinkedList<>(entrySet);
       Collections.sort(baseEntityKeyList, entryComparator);

       // new LinkedHashMap for the ordered FieldUnitOrTypeName keys
       LinkedHashMap<VersionableEntity,TypeDesignationSet> stringsOrderedbyBaseEntityOrderdByType
           = new LinkedHashMap<>(stringsByTypeByBaseEntity.size());

       for(Entry<VersionableEntity,TypeDesignationSet> entry : baseEntityKeyList){
           VersionableEntity baseEntity = entry.getKey();
           TypeDesignationSet typeDesignationSet = stringsByTypeByBaseEntity.get(baseEntity);
           // order the TypeDesignationStatusBase keys
            List<TypeDesignationStatusBase<?>> keyList = new LinkedList<>(typeDesignationSet.keySet());
            Collections.sort(keyList, new TypeDesignationStatusComparator());
            // new LinkedHashMap for the ordered TypeDesignationStatusBase keys
            TypeDesignationSet orderedStringsByOrderedTypes = new TypeDesignationSet(
                    typeDesignationSet.getBaseEntity());
            keyList.forEach(key -> orderedStringsByOrderedTypes.put(key, typeDesignationSet.get(key)));
            stringsOrderedbyBaseEntityOrderdByType.put(baseEntity, orderedStringsByOrderedTypes);
        }

        return stringsOrderedbyBaseEntityOrderdByType;
    }

    /**
     * FIXME use the validation framework validators to store the validation problems!!!
     *
     * @return
     * @throws RegistrationValidationException
     */
    private void findTypifiedName() throws RegistrationValidationException {

        List<String> problems = new ArrayList<>();

        TaxonName typifiedName = null;

        for(TypeDesignationBase<?> typeDesignation : typeDesignations.values()){
            typeDesignation.getTypifiedNames();
            if(typeDesignation.getTypifiedNames().isEmpty()){

                //TODO instead throw RegistrationValidationException()
                problems.add("Missing typifiedName in " + typeDesignation.toString());
                continue;
            }
            if(typeDesignation.getTypifiedNames().size() > 1){
                //TODO instead throw RegistrationValidationException()
                problems.add("Multiple typifiedName in " + typeDesignation.toString());
                continue;
            }
            if(typifiedName == null){
                // remember
                typifiedName = typeDesignation.getTypifiedNames().iterator().next();
            } else {
                // compare
                TaxonName otherTypifiedName = typeDesignation.getTypifiedNames().iterator().next();
                if(!typifiedName.getUuid().equals(otherTypifiedName.getUuid())){
                    //TODO instead throw RegistrationValidationException()
                    problems.add("Multiple typifiedName in " + typeDesignation.toString());
                }
            }
        }
        if(!problems.isEmpty()){
            // FIXME use the validation framework
            throw new RegistrationValidationException("Inconsistent type designations", problems);
        }

        if(typifiedName != null){
            // ON SUCCESS -------------------
            this.typifiedName = typifiedName;
        }
    }

    /**
     * @return the title cache of the typifying name or <code>null</code>
     */
    public String getTypifiedNameCache() {
        if(typifiedName != null){
            return typifiedName.getTitleCache();
        }
        return null;
    }

    /**
     * @return the title cache of the typifying name or <code>null</code>
     */
    public EntityReference getTypifiedNameAsEntityRef() {
       return new EntityReference(typifiedName.getUuid(), typifiedName.getTitleCache());
    }

    public Collection<TypeDesignationBase<?>> getTypeDesignations() {
        return typeDesignations.values();
    }

    public TypeDesignationBase<?> findTypeDesignation(UUID uuid) {
        return this.typeDesignations.get(uuid);
    }

    public Map<VersionableEntity,TypeDesignationSet> getOrderedTypeDesignationSets() {
        return orderedByTypesByBaseEntity;
    }

    private FieldUnit findFieldUnit(DerivedUnit du) {

        if(du == null || du.getOriginals() == null || du.getOriginals().isEmpty()){
            return null;
        }
        @SuppressWarnings("rawtypes")
        Set<SpecimenOrObservationBase> originals = du.getOriginals();
        @SuppressWarnings("rawtypes")
        Optional<SpecimenOrObservationBase> fieldUnit = originals.stream()
                .filter(original -> original instanceof FieldUnit).findFirst();
        if (fieldUnit.isPresent()) {
            return (FieldUnit) fieldUnit.get();
        } else {
            for (@SuppressWarnings("rawtypes") SpecimenOrObservationBase sob : originals) {
                if (sob instanceof DerivedUnit) {
                    FieldUnit fu = findFieldUnit((DerivedUnit) sob);
                    if (fu != null) {
                        return fu;
                    }
                }
            }
        }

        return null;
    }

    public String print(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable) {
        return new TypeDesignationSetFormatter(withCitation, withStartingTypeLabel, withNameIfAvailable).format(this);
    }

    public String print(boolean withCitation, boolean withStartingTypeLabel, boolean withNameIfAvailable, HTMLTagRules htmlRules) {
        return new TypeDesignationSetFormatter(withCitation, withStartingTypeLabel, withNameIfAvailable).format(this, htmlRules);
    }


    class DataIntegrityException extends Exception {

        private static final long serialVersionUID = 1464726696296824905L;

        public DataIntegrityException(String string) {
            super(string);
        }
    }
}
