package eu.etaxonomy.cdm.remote.webapp.vaaditor.views;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.exception.ReferencedObjectUndeletableException;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.components.HorizontalToolbar;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.components.TaxonTableDTO;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.controller.AuthenticationController;
import eu.etaxonomy.cdm.remote.webapp.vaaditor.util.RedlistDTO;

/**
 * 
 * 
 * Main View Class of this prototype. Components can be autowired or created within this class.
 * This View will be auto-detected by the VaadinServlet. This will be enabled by the @VaadinView 
 * annotation. Further it is important to mark this View as a Component, so Spring is able to index 
 * it for Apsect Orientation stuff like Autowiring.
 *
 *<p>
 *
 * The @Scope annotation is important for session handling but there are still some issues that need 
 * clarification, for further information see 
 * {@link https://github.com/xpoft/spring-vaadin/issues/issuecomment-15107560 }
 * 
 * @author a.oppermann
 */

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(DashBoardView.NAME)
public class DashBoardView extends CustomComponent implements View{

	/**
	 * Automatically generated serial version ID
	 */
	private static final long serialVersionUID = 6724641666558728722L;

	public static final String NAME = "dash";
	Logger logger = Logger.getLogger(DashBoardView.class);
	@Autowired
	private AuthenticationController authenticationController;
	
	@Autowired
	private HorizontalToolbar toolbar;

	@Autowired
	private TaxonTableDTO taxonTable;
	
	@Autowired
	private ITaxonService taxonService;
	
	@Autowired
	private ITermService termService;
	
	@Autowired
	private IFeatureTreeService featureTreeService;
	
	@Autowired
	private IDescriptionService descriptionService;
	
	@Autowired
	private INameService nameService;
	
	private Collection<DescriptionElementBase>listDescriptions;
	
	private Table detailTable;
	
	private Taxon currentTaxon;

	private BeanItemContainer<RedlistDTO> taxonBaseContainer;

	private VerticalLayout layout;

	/*
	 * Method will be called initially, but executed after dependency injection
	 * further it constructs the whole UI based widgets.
	 * 
	 */
	@PostConstruct
	public void PostConstruct(){
		if(authenticationController.isAuthenticated()){

			layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setHeight("100%");
			
			final HorizontalSplitPanel horizontalSplit = new HorizontalSplitPanel();
			horizontalSplit.setStyleName(Runo.SPLITPANEL_SMALL);
			horizontalSplit.setHeight("100%");
			
			final VerticalLayout detailViewLayout = new VerticalLayout();
			
			final VerticalLayout descriptionViewLayout = new VerticalLayout();
			
			detailViewLayout.setSizeFull();
			detailViewLayout.setStyleName("taxonDetailView");
			
			horizontalSplit.setFirstComponent(detailViewLayout);
			horizontalSplit.setSecondComponent(descriptionViewLayout);
			
			descriptionViewLayout.setStyleName("descriptiveView");
			descriptionViewLayout.setSizeFull();

			VerticalSplitPanel vSplit = new VerticalSplitPanel();
			vSplit.setStyleName(Runo.SPLITPANEL_SMALL);
			vSplit.setSizeFull();
			vSplit.setFirstComponent(taxonTable);
			vSplit.setSecondComponent(horizontalSplit);
			
			AbsoluteLayout taxonLayout = new AbsoluteLayout();
			taxonLayout.setSizeFull();
			taxonLayout.setWidth("100%");
			int height = VaadinSession.getCurrent().getBrowser().getScreenHeight() - 175;
			taxonLayout.setHeight(height+"px");
//			taxonLayout.setSizeUndefined(); 
			taxonLayout.addComponent(vSplit);

		    layout.addComponent(toolbar);
			layout.addComponent(taxonLayout);
//			layout.addComponent(horizontalSplit);
			layout.setExpandRatio(taxonLayout, 1);
			
			createTaxonTableListener(detailViewLayout, descriptionViewLayout);
			createEditClickListener();
			setCompositionRoot(layout);
		}
	}

	 //---------------------------------------------------------------------------------------//
	//--------------------Begin of helper methods--------------------------------------------//
   //---------------------------------------------------------------------------------------//
	
	private void createTaxonTableListener(final VerticalLayout detailViewLayout, final VerticalLayout descriptionViewLayout) {
		
		taxonTable.addItemClickListener(new ItemClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void itemClick(ItemClickEvent event) {
				Object taxonbean = ((BeanItem<?>)event.getItem()).getBean();
				clickHandler(taxonbean, detailViewLayout,  descriptionViewLayout);
			}
		});
		
		taxonTable.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				Object taxonbean = event.getProperty().getValue();
				clickHandler(taxonbean, detailViewLayout,  descriptionViewLayout);
			}
		});
	}
	
	private void clickHandler(Object taxonbean, final VerticalLayout detailViewLayout, final VerticalLayout descriptionViewLayout){
		if(taxonbean instanceof RedlistDTO){
			detailViewLayout.removeAllComponents();
			descriptionViewLayout.removeAllComponents();
			RedlistDTO red = (RedlistDTO) taxonbean;
			currentTaxon = red.getTaxon();
			detailViewLayout.addComponent(constructFormLayout(red));
			descriptionViewLayout.addComponent(constructDetailPanel(red));
		}
	}
	
	private TabSheet constructFormLayout(RedlistDTO dto){
		TabSheet tabsheet = new TabSheet();
		VerticalLayout tab1 = new VerticalLayout();
		Taxon taxon = dto.getTaxon();
		listDescriptions = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
		
		tab1.addComponent(constructTaxonDetailForm(dto));
		tab1.setSizeFull();
		tabsheet.setStyleName(Runo.TABSHEET_SMALL);
		tabsheet.addTab(tab1, "Detail Data");

		return tabsheet;
	}

	
	private FormLayout constructForm(Taxon taxon, final Window window){
		RedlistDTO redlistDTO = new RedlistDTO(taxon, listDescriptions, null);
		final BeanFieldGroup<RedlistDTO> binder = new BeanFieldGroup<RedlistDTO>(RedlistDTO.class);
		binder.setItemDataSource(redlistDTO);
		binder.setBuffered(true);
		
		final FormLayout form = new FormLayout();
		form.setMargin(true);
		
		final Field<?> taxonField = binder.buildAndBind("Taxon Name: ", "fullTitleCache");
		taxonField.setSizeFull();
		
		form.addComponent(taxonField);
		form.addComponent(constructSaveButton(window, binder));
		form.setImmediate(true);
		form.setSizeFull();
		taxonField.commit();
		
		return form;
	}
	
	private FormLayout constructTaxonDetailForm(final RedlistDTO red){
		final BeanFieldGroup<RedlistDTO> binder = new BeanFieldGroup<RedlistDTO>(RedlistDTO.class);
		binder.setItemDataSource(red);
		binder.setBuffered(true);
		
		final ComboBox box = initComboBox(red);
		
		final FormLayout form = new FormLayout();
		form.setMargin(true);

		Field<?> nameCacheField = binder.buildAndBind("Taxon Name Cache: ", "taxonNameCache");
		Field<?> nomenCodeField = binder.buildAndBind("Nomenclatural Code: ", "nomenclaturalCode");
		Field<?> rankField = binder.buildAndBind("Rang: ", "rank");
		Field<?> secundumField = binder.buildAndBind("Secundum: ", "secundum");
		
		binder.bind(box, "distributionStatus");
		
		nameCacheField.setSizeFull();
		nameCacheField.commit();
		nomenCodeField.setSizeFull();
		rankField.setSizeFull();
		secundumField.setSizeFull();

		form.addComponents(nameCacheField, nomenCodeField, rankField, secundumField);
		form.addComponent(box);
		form.setImmediate(true);
		form.setSizeFull();
		
		return form;
	}

	@SuppressWarnings("rawtypes")
	private ComboBox initComboBox(final RedlistDTO red) {
		List<PresenceAbsenceTermBase> listTerm = termService.listByTermClass(PresenceAbsenceTermBase.class, null, null, null, DESCRIPTION_INIT_STRATEGY);
		BeanItemContainer<PresenceAbsenceTermBase> container = new BeanItemContainer<PresenceAbsenceTermBase>(PresenceAbsenceTermBase.class);
		container.addAll(listTerm);
		
		final ComboBox box = new ComboBox("Occurrence Status: ", container);
		box.setValue(red.getDistributionStatus());
		box.setImmediate(true);
		
		box.addValueChangeListener(new ValueChangeListener() {	
			private static final long serialVersionUID = 1L;
			@Override
			public void valueChange(ValueChangeEvent event) {
				logger.info(box.getValue());
				red.setDistributionStatus((PresenceAbsenceTermBase<?>) box.getValue());
				updateTables();
			}
		});
		return box;
	}
	
	private TabSheet constructDetailPanel(RedlistDTO dto){
		Taxon taxon = dto.getTaxon();
		TabSheet tabsheet = new TabSheet();
		
		VerticalLayout tab1 = new VerticalLayout();
		VerticalLayout tab2 = new VerticalLayout();
		VerticalLayout tab3 = new VerticalLayout();
		VerticalLayout tab4 = new VerticalLayout();
		
		tab1.addComponent(constructDescriptionTree(taxon));
		tab2.addComponent(initComboBox(dto));
		tab4.addComponent(constructGenerateButton());
		tab4.addComponent(constructDeleteButton());
		
		tabsheet.addTab(tab1, "Description Data");
		tabsheet.addTab(tab2, "Taxon Data");
		tabsheet.addTab(tab3, "Checklist Data");
		tabsheet.addTab(tab4, "Example Data");
		
		tabsheet.setStyleName(Runo.TABSHEET_SMALL);
		
		return tabsheet;
	}
	
	private Tree constructDescriptionTree(Taxon taxon){
		Tree tree = new Tree();
		tree.setSizeUndefined();
		String parent = "Descriptive Data";
		tree.setValue(parent);
		initDescriptionTree(tree, listDescriptions, parent);
		return tree;
	}
	
	private void initDescriptionTree(Tree tree, Collection<DescriptionElementBase>listDescriptions, Object parent) {
		//TODO: sorting List
		for (DescriptionElementBase deb : listDescriptions){
			tree.addItem(deb.getFeature());
			tree.setItemCaption(deb.getFeature(), deb.getFeature().getTitleCache());
			tree.setParent(deb.getFeature(), parent);
			tree.setChildrenAllowed(deb.getFeature(), true);
			
			if(deb instanceof CategoricalData){
				CategoricalData cd = (CategoricalData) deb;
				if(cd.getStatesOnly().size() <= 1){
					for(StateData st  : cd.getStateData()){
						tree.addItem(st);
						tree.setItemCaption(st, st.getState().getTitleCache());
						tree.setParent(st, deb.getFeature());
						tree.setChildrenAllowed(st, false);
					}
				}else{
					//TODO: implement recursion
				}
			}else if(deb instanceof TextData){
				TextData td = (TextData) deb;
				tree.addItem(td);
				tree.setItemCaption(td, td.getText(Language.GERMAN()));
				tree.setParent(td, deb.getFeature());
				tree.setChildrenAllowed(td, false);
			}else if(deb instanceof Distribution){
				Distribution db = (Distribution) deb;
				
				tree.addItem(db.toString());
				tree.setParent(db.toString(), deb.getFeature());
				tree.setChildrenAllowed(db.toString(), true);
				
				tree.addItem(db.getStatus().toString());
				tree.setParent(db.getStatus().toString(), db.toString());
				tree.setChildrenAllowed(db.getStatus().toString(), false);
			}
			tree.expandItemsRecursively(parent);
		}

	}
	
	private Button constructSaveButton(final Window window, final BeanFieldGroup<RedlistDTO> binder) {
		Button okButton = new Button("Save");
		okButton.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				try {
					binder.commit();
					BeanItem<RedlistDTO> beanItem = (BeanItem<RedlistDTO>) binder.getItemDataSource();
					binder.commit();
					RedlistDTO redlist = beanItem.getBean();
					logger.info("check das Taxon: "+ redlist.getTaxon());
//					Taxon tnb = redlist.getTaxon();
//					taxonService.saveOrUpdate(tnb);
					updateTables();
					window.close();
				} catch (CommitException e) {
					logger.info("Commit Exception: "+e);
				}
			}
		});
		return okButton;
	}

	private void updateTables() {
		taxonTable.markAsDirtyRecursive();
//		taxonTable.refreshRowCache();
	}
	
	private void openDetailWindow(Taxon taxon){
		Window window = new Window("Edit Taxon Information");
		window.setWidth("400px");
		window.setModal(true);
		window.setContent(constructForm(taxon, window));
		getUI().addWindow(window);
	}
	
	private void createEditClickListener(){
		toolbar.getEditButton().addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;
			@Override
			public void buttonClick(ClickEvent event) {
				if(currentTaxon != null){
					openDetailWindow(currentTaxon);
				}else{
					Notification.show("Please select a Taxon.", Notification.Type.HUMANIZED_MESSAGE);
				}
				
			}
		});
	}

	
	@Override
	public void enter(ViewChangeEvent event) {
	}
	
	  //---------------------------------------------------------------------------------------------------//
	 //------------------------------------Example Data Creation------------------------------------------//
	//---------------------------------------------------------------------------------------------------//
	public void createExampleData(){
		List<TaxonBase> listTaxa = taxonService.list(Taxon.class, null, null, null, NODE_INIT_STRATEGY);
		for(TaxonBase taxonBase : listTaxa){
			Taxon taxon = (Taxon) taxonBase;
			TaxonDescription td = getTaxonDescription(taxon, false, true);
			NamedArea na = NamedArea.NewInstance();//0a9727d2-8d1f-4a88-ad4c-d6ef4ebc112a
			na = (NamedArea) termService.load(UUID.fromString("cbe7ce69-2952-4309-85dd-0d7d4a4830a1"));
			PresenceAbsenceTermBase<?> absenceTermBase = (PresenceAbsenceTermBase<?>) termService.load(UUID.fromString("cef81d25-501c-48d8-bbea-542ec50de2c2"));
			Distribution db = Distribution.NewInstance(na, absenceTermBase);
			descriptionService.saveDescriptionElement(db);
			td.addElement(db);
			taxonService.saveOrUpdate(taxon);
		}
		
	}
	
	public void deleteExampleData(){
		List<TaxonBase> listTaxa = taxonService.list(Taxon.class, null, null, null, DESCRIPTION_INIT_STRATEGY);
		Iterator<TaxonBase> taxonIterator = listTaxa.iterator();
		while(taxonIterator.hasNext()){
			TaxonBase taxonBase = taxonIterator.next();
			Taxon taxon = (Taxon) taxonBase;
			TaxonDescription td = getTaxonDescription(taxon, false, false);
			Iterator<DescriptionElementBase> descriptionIterator = td.getElements().iterator();
			while(descriptionIterator.hasNext()){
				DescriptionElementBase descriptionElementBase = descriptionIterator.next();
				if(descriptionElementBase instanceof Distribution){
					logger.info("Will be deleted: " + descriptionIterator);
					td.removeElement(descriptionElementBase);
					taxonService.saveOrUpdate(taxon);
					break;
				}
			}
		}
	}

	private void refreshLayout(){
		//TODO: refresh of Taxon Table does not work properly
		layout.markAsDirtyRecursive();
		taxonTable.markAsDirtyRecursive();
		taxonTable.refreshRowCache();
	}
	
	
	private TaxonDescription getTaxonDescription(Taxon taxon, boolean isImageGallery, boolean createNewIfNotExists) {
		TaxonDescription result = null;
		Set<TaxonDescription> descriptions= taxon.getDescriptions();
		for (TaxonDescription description : descriptions){
			if (description.isImageGallery() == isImageGallery){
					result = description;
					break;
			}
		}
		if (result == null && createNewIfNotExists){
			result = TaxonDescription.NewInstance(taxon);
			result.setImageGallery(isImageGallery);
		}
		return result;
	}
	
	private Button constructDeleteButton() {
		Button deleteButton = new Button("Delete Data");
		deleteButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				deleteExampleData();
				refreshLayout();
			}
		});
		return deleteButton;
	}
	
	
	private Button constructGenerateButton() {
		Button generateButton = new Button("Generate Data");
		generateButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				createExampleData();
				updateTables();
			}
		});
		
		return generateButton;
	}

	
	  //---------------------------------------------------------------------------------------------------//
	 //------------------------------------Initialization Strategies--------------------------------------//
	//---------------------------------------------------------------------------------------------------//
	

    protected static final List<String> DESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "elements.*",
            "elements.sources.citation.authorTeam.$",
            "elements.sources.nameUsedInSource.originalNameString",
            "elements.area.level",
            "elements.modifyingText",
            "elements.states.*",
            "elements.media",
            "elements.multilanguageText",
            "elements.inDescription",
    		"descriptionElements",
    		"descriptionElements.$",
    		"descriptionElements.inDescription.$",
            "multilanguageText",
            "stateData.$"
    });
    
	private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
    		"classification",
    		"descriptions",
    		"descriptions.*",
    		"descriptionElements",
    		"descriptionElements.$",
    		"descriptionElements.inDescription.$",
    		"description.state",
    		"feature",
    		"feature.*",
    		"State",
    		"state",
    		"states",
    		"stateData.*",
    		"stateData.state",
    		"categoricalData",
    		"categoricalData.*",
    		"categoricalData.states.state",
    		"categoricalData.States.State",
    		"categoricalData.states.*",
    		"categoricalData.stateData.state",
    		"childNodes",
    		"childNodes.taxon",
    		"childNodes.taxon.name",
    		"taxonNodes",
    		"taxonNodes.*",
            "taxonNodes.taxon.*",
    		"taxon.*",
    		"taxon.descriptions",
    		"taxon.sec",
    		"taxon.name.*",
    		"taxon.synonymRelations",
    		"terms",
    		"$",
            "elements.$",
            "elements.states.*",
            "elements.inDescription",
            "elements.sources.citation.authorTeam",
            "elements.sources.nameUsedInSource.originalNameString",
            "elements.area.level",
            "elements.modifyingText",
            "elements.states.*",
            "elements.multilanguageText",
            "elements.media",
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "sources.$",
            "stateData.$"
    });
}
