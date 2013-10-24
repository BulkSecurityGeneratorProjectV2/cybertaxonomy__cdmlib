package eu.etaxonomy.cdm.remote.webapp.view.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.hql.internal.ast.util.SessionFactoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.haijian.ExcelExporter;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import ru.xpoft.vaadin.VaadinView;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Runo;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.webapp.view.ui.wizard.FourthStep;
import eu.etaxonomy.cdm.remote.webapp.view.ui.wizard.IntroStep;
import eu.etaxonomy.cdm.remote.webapp.view.ui.wizard.SetupStep;
import eu.etaxonomy.cdm.remote.webapp.view.ui.wizard.ThirdStep;
import eu.etaxonomy.cdm.strategy.parser.INonViralNameParser;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * Main UI class
 */
@Component
@Scope("prototype")
@Theme("mytheme")
@SuppressWarnings("serial")
@VaadinView(HelloWorld.NAME)
@PreserveOnRefresh
public class HelloWorld extends UI implements View, WizardProgressListener{
	
	private static final Logger logger = Logger.getLogger(HelloWorld.class);
	public static final String NAME = "main";
	@Autowired
	private IClassificationService classificationService;
	@Autowired
    private ITaxonService taxonService;
	@Autowired
	private ITermService termService; 
	@Autowired
    private ITaxonNodeService taxonNodeService;
	@Autowired
	private IDescriptionService descriptionService;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private transient ApplicationContext applicationContext;

	private SecurityContext context;
	private Authentication authentication;
	
	private Collection<Classification> classifications;
	private Tree classificationTree;
	private Table classificationOverviewTable;
	private Table taxonDetailTable;

	private AbsoluteLayout loginLayout;
	private AbsoluteLayout rightLayout;
	private AbsoluteLayout editTaxonTabLayout;
	private AbsoluteLayout editWindowLayout;
	private AbsoluteLayout editSynonymTabLayout;
	
	private HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();;
	private TabSheet editTabSheet;

	private final Button sendButton = new Button();;
	private final Button logoutButton= new Button("Logout");;
	private final Button editButton = new Button("Edit");
	
	private Button saveSynonymButton;
	private Button cancelSynonymButton;
	private NativeButton cancelTaxonButton;
	private NativeButton saveTaxonButton;

	private PasswordField passwordField;
	private TextField userName;
	private TextField editTaxonTextField;
	private TextField editSynonymTextField;

	private Label loginName;
	@SuppressWarnings("rawtypes")
	private TaxonBase selectedTaxonBase; 
	private boolean isEditable = false;
	private boolean isSidebar = true;
	
	private ExcelExporter exporter = new ExcelExporter();
	private Wizard wizard;
	private boolean isWizard = true;
		
	@Override 	
	protected void init(VaadinRequest request) {
		context = SecurityContextHolder.getContext();
		
		if(context.getAuthentication() == null){
			setContent(loginLayout());
			handleButtonLogic();

		}else{
			mainLayout();
		}
	}

	protected void mainLayout() {
		
		
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSizeFull();
		if(isWizard ){
			initWizard();
			verticalLayout.addComponent(wizard);
			verticalLayout.setComponentAlignment(wizard, Alignment.TOP_CENTER);
			setContent(verticalLayout);
		}else{
		
		verticalLayout.addComponent(initToolbar());
		horizontalSplitPanel.addStyleName(Runo.SPLITPANEL_SMALL);
		verticalLayout.addComponent(horizontalSplitPanel);
		verticalLayout.setExpandRatio(horizontalSplitPanel, 1);

		
		horizontalSplitPanel.setSplitPosition((float) 20.0);
		
		initTaxonTree();
		initTaxonDetailView();
		
//		horizontalSplitPanel.setFirstComponent(classificationTree);
		horizontalSplitPanel.setFirstComponent(classificationTree);
		horizontalSplitPanel.setSecondComponent(rightLayout);
		
		
//		classificationTree.setSizeFull();
		classificationOverviewTable.setSizeFull();


		classificationTree.setImmediate(true);
		classificationTree.setSelectable(true);
		//set content to screen
		setContent(verticalLayout);
		}
	}
	
	private void initWizard(){

        // create the Wizard component and add the steps
        wizard = new Wizard();
        wizard.setUriFragmentEnabled(true);
        wizard.addListener(this);
        wizard.addStep(new IntroStep(), "intro");
        wizard.addStep(new SetupStep(), "vaadin");
        wizard.addStep(new ThirdStep(), "oSStack");
        wizard.addStep(new FourthStep(), "advantages");
//        wizard.addStep(new ListenStep(), "listen");
//        wizard.addStep(new LastStep(wizard), "last");
        wizard.setHeight("600px");
        wizard.setWidth("800px");
	}
	
	private VerticalLayout initSideBar(){
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponentAsFirst(classificationTree);
		classificationTree.setSizeFull();

//		verticalLayout.addComponent(hideSideBarButton);
		
		return verticalLayout;
	}
	
    private HorizontalLayout initToolbar() {
        HorizontalLayout leftLayout = new HorizontalLayout();
        leftLayout.addComponent(editButton);
        leftLayout.addComponent(exporter);
    	
        exporter.setCaption("Export");
    	exporter.setIcon(new ThemeResource("icons/32/document-xsl.png"));

    	editButton.setIcon(new ThemeResource("icons/32/document-edit.png"));
        logoutButton.setIcon(new ThemeResource("icons/32/cancel.png"));
        leftLayout.setStyleName("toolbar");
        //lo.addComponent(logoutButton);
        //lo.addComponent(loginName);
    	loginName = new Label(context.getAuthentication().getName());
        loginName.setIcon(new ThemeResource("icons/32/user.png"));
        HorizontalLayout rightLayout = new HorizontalLayout(); 
        Image image = new Image(null, new ThemeResource("icons/32/vseparator1.png"));
        rightLayout.addComponent(logoutButton);
        rightLayout.addComponent(image);
        rightLayout.addComponent(loginName);
        
        leftLayout.addComponent(rightLayout);
        
        leftLayout.setComponentAlignment(rightLayout, Alignment.MIDDLE_RIGHT);
        leftLayout.setExpandRatio(rightLayout, 1);

        
//        lo.addComponent(em);
//        lo.setComponentAlignment(em, Alignment.MIDDLE_RIGHT);
//        lo.setExpandRatio(em, 1);
//        
        leftLayout.setMargin(true);
        leftLayout.setSpacing(true);
        leftLayout.setStyleName("toolbar");
        leftLayout.setWidth("100%"); 
       // lo.setExpandRatio(logoutButton, 1);
        return leftLayout;
    }
	
	private void initTaxonDetailView() {
		rightLayout = new AbsoluteLayout();
		rightLayout.setImmediate(false);
		rightLayout.setWidth("100%");
		rightLayout.setHeight("100%");
	
		classificationOverviewTable = new Table();
		classificationOverviewTable.addContainerProperty("Wissenschaftlicher Name", TaxonNameBase.class, null);
		classificationOverviewTable.addContainerProperty("Synonym", String.class, null);
		
		
//        TODO: create Field Property
//		classificationOverviewTable.setTableFieldFactory(new TableFieldFactory() {
//			
//			@Override
//			public Field<?> createField(Container container, Object itemId,
//					Object propertyId, com.vaadin.ui.Component uiContext) {
//				
//				
//				return null;
//			}
//		});
//		
		
		classificationOverviewTable.addContainerProperty("Etablierungsstatus", String.class, null);
//		classificationOverviewTable.addContainerProperty("Etablierungsstatus", NativeSelect.class, null);
//		classificationOverviewTable.setEditable(true);
		
		
		
		taxonDetailTable = new Table();
		taxonDetailTable.addContainerProperty("Synonym", Synonym.class, null);
		taxonDetailTable.addContainerProperty("Etablierungsstatus", State.class, null);
		taxonDetailTable.setSizeFull();
		
		rightLayout.addComponent(taxonDetailTable);
		rightLayout.addComponent(classificationOverviewTable);
		
		classificationOverviewTable.setVisible(false);
		classificationOverviewTable.setNullSelectionAllowed(false);
//		classificationOverviewTable.setStyleName("small striped");
		
		taxonDetailTable.setVisible(false);
		taxonDetailTable.setNullSelectionAllowed(false);
		taxonDetailTable.setImmediate(true);
		taxonDetailTable.setSortEnabled(true);
//		taxonDetailTable.setStyleName("big striped");
		
	}

	private void initTaxonTree() {
		classifications = classificationService.listClassifications(null, null, null, VOC_CLASSIFICATION_INIT_STRATEGY);
		classificationTree = new Tree("Classifications:");
		classificationTree.addContainerProperty("Classification", Classification.class, null);
		classificationTree.addContainerProperty("Taxon", Taxon.class, null);
		classificationTree.setNullSelectionAllowed(false);
		boolean setFirst = true;
		for(Classification c : classifications) {
			classificationTree.addItem(c);
			if(setFirst){
				classificationTree.setValue(c);
				setFirst=false;
			}
			if(c.hasChildNodes()){
				List<TaxonNode> setTaxonNodes = c.getChildNodes();
				initTaxonTree(setTaxonNodes, c);
			}
		}
		//TODO: Extract Listener in separate method and find NPE
		classificationTree.addValueChangeListener(new ValueChangeListener()  {
			@Override
			public void valueChange(ValueChangeEvent event) {
				try{

					//applicationContext.get
					
					if(classificationTree.getValue() instanceof Classification){
						classificationOverviewTable.setVisible(true);
						taxonDetailTable.setVisible(false);
						if(classificationOverviewTable.removeAllItems()){
							Classification selectedValue = (Classification) classificationTree.getValue();
							List<TaxonNode> allNodes =  classificationService.getAllNodes();
//							sortTaxonNodeList(allNodes);
							Taxon taxon = null;
							Synonym synonym = null;
							for (TaxonNode node : allNodes){
								TaxonNode taxonNode = taxonNodeService.load(node.getUuid(), NODE_INIT_STRATEGY);
								boolean isPrintedFirst = false;
								if(taxonNode.getClassification()!= null){
									if(taxonNode.getClassification().equals(selectedValue)){
										taxon = taxonNode.getTaxon();
										String taxonName = taxon.getName().toString();
										//get taxon description
										State population = null;
										//get aktuelle Bestandssituation
										taxon = (Taxon) taxonService.load(taxon.getUuid());
										//									List<DescriptionElementBase> listDescriptionElements = descriptionService.getDescriptionElementsForTaxon(taxon, null, null, null, null, NODE_INIT_STRATEGY);
										List<DescriptionElementBase> listDescriptionElements = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
										for(DescriptionElementBase deb:listDescriptionElements){
											if(deb instanceof CategoricalData){
												Feature feature = deb.getFeature();
												if(feature.toString().equalsIgnoreCase("aktuelle Bestandsstituation")){
													List<State> stateList = ((CategoricalData) deb).getStatesOnly();//((CategoricalData) deb).getStates();
													for(State state :stateList){
														population = state;
													}
												}
											}
										}
										String popString;
										if(population != null)
											popString = population.toString();
										else{
											popString = StringUtils.EMPTY;
										}
										if(taxon.hasSynonyms()){
											for(Synonym s : taxon.getSynonyms()){
												synonym = s;
												if(!isPrintedFirst){
													classificationOverviewTable.addItem(new Object[]{taxonNode.getTaxon().getName(), s.toString(), popString}, synonym.getId());
													isPrintedFirst = true;
												}
												classificationOverviewTable.addItem(new Object[]{null, s.toString(), popString}, synonym.getId());
											}
										}else{
											classificationOverviewTable.addItem(new Object[]{taxonNode.getTaxon().getName(), StringUtils.EMPTY, popString}, taxon.getId());
										}
									}

								}
							}
							exporter.setTableToBeExported(classificationOverviewTable);
							exporter.setDownloadFileName(classificationTree.getValue().toString());
						}
					}else if(classificationTree.getValue() instanceof Taxon){
						classificationOverviewTable.setVisible(false);
						taxonDetailTable.setVisible(true);
						if(taxonDetailTable.removeAllItems()){
							State population = null;
							Taxon selectedTaxon = (Taxon) classificationTree.getValue();
							Taxon taxon = (Taxon) taxonService.load(selectedTaxon.getUuid(), NODE_INIT_STRATEGY);
							List<DescriptionElementBase> listDescriptionElements = descriptionService.listDescriptionElementsForTaxon(taxon, null, null, null, null, DESCRIPTION_INIT_STRATEGY);
							for(DescriptionElementBase deb:listDescriptionElements){
								if(deb instanceof CategoricalData){
									Feature feature = deb.getFeature();
									if(feature.toString().equalsIgnoreCase("aktuelle Bestandsstituation")){
										List<State> stateList = ((CategoricalData) deb).getStatesOnly();//((CategoricalData) deb).getStates();
										for(State state :stateList){
											population = state;
										}
									}
								}
							}
							for(Synonym s : taxon.getSynonyms()){
								taxonDetailTable.addItem(new Object[]{s, population}, s.getId());
							}
						}
						taxonDetailTable.setSelectable(true);
						exporter.setTableToBeExported(taxonDetailTable);
					}
				}catch(Table.CacheUpdateException tc){
					logger.info(tc.getCause().toString());
				}catch(Exception e){
					logger.info(e);
				}
			}
		});
	}
	/**
	 * 
	 * @param setTaxonNodes
	 * @param parent
	 */
	private void initTaxonTree(List<TaxonNode> setTaxonNodes, Object parent) {
		List<TaxonNode> listTaxonNodes = new ArrayList<TaxonNode>();
		listTaxonNodes.addAll(setTaxonNodes);
		//sorting List
		sortTaxonNodeList(listTaxonNodes);
		for (TaxonNode tn : listTaxonNodes){
			TaxonNode taxonNode = taxonNodeService.load(tn.getUuid(), NODE_INIT_STRATEGY);
			classificationTree.addItem(taxonNode.getTaxon());
			classificationTree.setItemCaption(taxonNode.getTaxon(), taxonNode.getTaxon().getName().toString());
			classificationTree.setParent(taxonNode.getTaxon(), parent);
			classificationTree.setChildrenAllowed(taxonNode.getTaxon(), true);
			
			if(taxonNode.hasChildNodes()){
				initTaxonTree(taxonNode.getChildNodes(), taxonNode.getTaxon());
			}
			if(!taxonNode.hasChildNodes()){
				classificationTree.setChildrenAllowed(taxonNode.getTaxon(), false);
				classificationTree.setItemCaption(taxonNode.getTaxon(), taxonNode.getTaxon().getName().toString());
			}
			classificationTree.expandItemsRecursively(parent);
		}

	}
	@Transactional
	private void sortTaxonNodeList(List<TaxonNode> listTaxonNodes) {
		Collections.sort(listTaxonNodes, new Comparator<TaxonNode>() {

			@Override
			public int compare(TaxonNode tn1, TaxonNode tn2) {
				Taxon taxon1 = tn1.getTaxon();
				Taxon taxon2 = tn2.getTaxon();
				if(taxon1 != null && taxon2 != null){
					return taxon1.getTitleCache().compareTo(taxon2.getTitleCache());
				}
				else{
					return 0;
				}
			}
		});
	}
	
	private void initEditWindow(@SuppressWarnings("rawtypes") TaxonBase taxonBase) {
		final Window window = new Window();
		window.setStyleName(Runo.WINDOW_DIALOG);
		window.setCaption("Edit Window");
		window.setContent(editWindow());
		window.setEnabled(true);
		window.setWidth("640");
		window.setHeight("480");
		window.setPositionX((int)getWidth());
		window.setPositionY((int)getHeight()/2);
		editSynonymTextField.setValue(taxonBase.getTitleCache());
		addWindow(window);
		//TODO extract listener into separate method
		saveSynonymButton.addClickListener(new ClickListener() {	
//		if(feature.toString().equalsIgnoreCase("aktuelle Bestandssituation")){
//		for(State state :((CategoricalData) deb).getStatesOnly()){
//			population = state;
//		}
//	}
			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				String saveStringAsSynonym = editSynonymTextField.getValue();
				INonViralNameParser<?> parser = NonViralNameParserImpl.NewInstance();
				@SuppressWarnings("rawtypes")
				TaxonNameBase tnb = parser.parseFullName(saveStringAsSynonym);
				Synonym synonym = new Synonym(tnb, null);
				if(selectedTaxonBase != null){
					Taxon taxon = (Taxon) selectedTaxonBase;
					taxon = (Taxon) taxonService.load(selectedTaxonBase.getUuid());
					if(taxon.hasSynonyms()){
						//TODO add logic here
						//iterate over all synonyms
						//check if synonym already exists
						//if not save else notification
						Set<Synonym> synonyms = taxon.getSynonyms();
						for(Synonym s : synonyms){
							if(s.getTitleCache().equals(synonym.getTitleCache())){
								//Ask User if he wants overwrite existing synonym
								Notification.show("Synonym already exists, please choose different Name", Notification.Type.WARNING_MESSAGE);
							}else{
								taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
								taxonService.saveOrUpdate(taxon);
								Notification.show("Synonym saved", Notification.Type.HUMANIZED_MESSAGE);
//								taxonDetailTable.setContainerDataSource(taxonDetailTable.getContainerDataSource());
								taxonDetailTable.addItem(new Object[]{synonym}, synonym.getId());
								removeWindow(window);
							}
						}
					}else{
						taxon.addSynonym(synonym, SynonymRelationshipType.SYNONYM_OF());
						taxonService.saveOrUpdate(taxon);
						Notification.show("Synonym saved", Notification.Type.HUMANIZED_MESSAGE);
						taxonDetailTable.addItem(new Object[]{synonym}, synonym.getId());
						removeWindow(window);
					}
				}
			}
		});
		cancelSynonymButton.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				removeWindow(window);
			}
		});
	}
	
	
	
	
	private AbsoluteLayout loginLayout() {
		// common part: create layout
		loginLayout = new AbsoluteLayout();
		loginLayout.setImmediate(false);
		loginLayout.setWidth("100%");
		loginLayout.setHeight("100%");
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// userName
		userName = new TextField();
		userName.setCaption("Username");
		userName.setImmediate(false);
		userName.setWidth("-1px");
		userName.setHeight("-1px");
		loginLayout.addComponent(userName, "top:176.0px;left:160.0px;");
		userName.focus();
		
		// passwordField
		passwordField = new PasswordField();
		passwordField.setCaption("Password");
		passwordField.setImmediate(false);
		passwordField.setWidth("-1px");
		passwordField.setHeight("-1px");
		loginLayout.addComponent(passwordField, "top:220.0px;left:160.0px;");
		
		// sendButton
		sendButton.setCaption("Send");
		sendButton.setImmediate(true);
		sendButton.setWidth("-1px");
		sendButton.setHeight("-1px");
		loginLayout.addComponent(sendButton, "top:260.0px;left:160.0px;");
		
		return loginLayout;
	}
	
	
	/*
	 * Helper Methods
	 */
	@Transactional
	private void handleButtonLogic() {
		sendButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String username = userName.getValue();
				String password = passwordField.getValue();
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
				try{
					authentication = authenticationManager.authenticate(token);
					context = SecurityContextHolder.getContext();
					context.setAuthentication(authentication);		
					mainLayout();
					return;

				}catch(BadCredentialsException e){
					Notification.show("Bad credentials", Notification.Type.ERROR_MESSAGE);
					logger.info("FAILED");
				}
			}
		});
		
		editButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if(classificationTree.getValue() instanceof Taxon){
					Taxon taxon = (Taxon) classificationTree.getValue();
					selectedTaxonBase = taxonService.load(taxon.getUuid(), TAXON_NODE_INIT_STRATEGY);
					initEditWindow(selectedTaxonBase);
				
				}else if(classificationTree.getValue() instanceof Classification){
					if(isEditable == false){
						isEditable = true;
						classificationOverviewTable.setTableFieldFactory(new TableFieldFactory() {
							
							@Override
							public Field<?> createField(Container container, Object itemId, Object propertyId,
									com.vaadin.ui.Component uiContext) {
			                    if (propertyId.toString().equals("Etablierungsstatus")) {
			                        return new TextField();
			                    }
			   
			                return null;
							}
				        });
						classificationOverviewTable.setEditable(isEditable);
						//set 
						
						
						
					}else if(isEditable == true){
						isEditable = false;
						classificationOverviewTable.setEditable(isEditable);
					}
				}else{
					Notification.show("Please choose a Taxon to edit", Notification.Type.HUMANIZED_MESSAGE);
				}
			}
		});


		logoutButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				SecurityContext context = SecurityContextHolder.getContext();
				context.setAuthentication(null);
				setContent(loginLayout);
			}
		});
		
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	private AbsoluteLayout editWindow(){
		// common part: create layout
		editWindowLayout = new AbsoluteLayout();
		editWindowLayout.setImmediate(false);
		editWindowLayout.setWidth("100%");
		editWindowLayout.setHeight("100%");
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// editTabSheet
		editTabSheet = buildEditTabSheet();
		editWindowLayout.addComponent(editTabSheet,
				"top:0.0px;right:7.0px;bottom:8.0px;left:0.0px;");
		
		return editWindowLayout;
	}
	
	private AbsoluteLayout buildEditTaxonTabLayout() {
		// common part: create layout
		editTaxonTabLayout = new AbsoluteLayout();
		editTaxonTabLayout.setCaption("Edit Taxon");
		editTaxonTabLayout.setImmediate(false);
		editTaxonTabLayout.setWidth("100.0%");
		editTaxonTabLayout.setHeight("100.0%");
		
		// editTaxonTextField
		editTaxonTextField = new TextField();
		editTaxonTextField.setCaption("Enter taxon name:");
		editTaxonTextField.setImmediate(false);
		editTaxonTextField.setWidth("100.0%");
		editTaxonTextField.setHeight("-1px");
		editTaxonTabLayout.addComponent(editTaxonTextField,
				"top:48.0px;right:21.0px;left:19.0px;");
		
		// saveTaxonButton
		saveTaxonButton = new NativeButton();
		saveTaxonButton.setCaption("Save");
		saveTaxonButton.setImmediate(true);
		saveTaxonButton.setWidth("-1px");
		saveTaxonButton.setHeight("-1px");
		editTaxonTabLayout.addComponent(saveTaxonButton,
				"top:88.0px;left:19.0px;");
		
		// cancelTaxonButton
		cancelTaxonButton = new NativeButton();
		cancelTaxonButton.setCaption("Cancel");
		cancelTaxonButton.setImmediate(true);
		cancelTaxonButton.setWidth("-1px");
		cancelTaxonButton.setHeight("-1px");
		editTaxonTabLayout.addComponent(cancelTaxonButton,
				"top:88.0px;left:79.0px;");
		
		return editTaxonTabLayout;
	}

	private AbsoluteLayout buildEditSynonymTabLayout() {
		// common part: create layout
		editSynonymTabLayout = new AbsoluteLayout();
		editSynonymTabLayout.setCaption("Edit Synonym");
		editSynonymTabLayout.setImmediate(false);
		editSynonymTabLayout.setWidth("100.0%");
		editSynonymTabLayout.setHeight("100.0%");
		
		// editSynonymTextField
		editSynonymTextField = new TextField();
		editSynonymTextField.setCaption("Enter synonym name:");
		editSynonymTextField.setImmediate(false);
		editSynonymTextField.setWidth("100.0%");
		editSynonymTextField.setHeight("-1px");
		editSynonymTabLayout.addComponent(editSynonymTextField,
				"top:48.0px;right:21.0px;left:19.0px;");
		
		// saveSynonymButton
		saveSynonymButton = new Button();
		saveSynonymButton.setCaption("Save");
		saveSynonymButton.setImmediate(true);
		saveSynonymButton.setWidth("-1px");
		saveSynonymButton.setHeight("-1px");
		editSynonymTabLayout.addComponent(saveSynonymButton,
				"top:88.0px;left:19.0px;");
		
		// cancelSynonymButton
		cancelSynonymButton = new Button();
		cancelSynonymButton.setCaption("Cancel");
		cancelSynonymButton.setImmediate(true);
		cancelSynonymButton.setWidth("-1px");
		cancelSynonymButton.setHeight("-1px");
		editSynonymTabLayout.addComponent(cancelSynonymButton,
				"top:88.0px;left:79.0px;");
		
		return editSynonymTabLayout;
	}
	
	private TabSheet buildEditTabSheet() {
		// common part: create layout
		editTabSheet = new TabSheet();
		editTabSheet.setStyleName(Runo.TABSHEET_SMALL);
		editTabSheet.setImmediate(true);
		editTabSheet.setWidth("100.0%");
		editTabSheet.setHeight("100.0%");
		
		// editTaxonTabLayout
		editTaxonTabLayout = buildEditTaxonTabLayout();
		editTabSheet.addTab(editTaxonTabLayout, "Edit Taxon", null);
		
		// editSynonymTabLayout
		editSynonymTabLayout = buildEditSynonymTabLayout();
		editTabSheet.addTab(editSynonymTabLayout, "Edit Synonym", null);
		
		return editTabSheet;
	}
	
    /** Hibernate classification vocabulary initialisation strategy */
    private static final List<String> VOC_CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[] {
            "classification",
    		"classification.rootNodes",
    		"childNodes",
    		"childNodes.taxon",
            "childNodes.taxon.name",
            "taxonNodes",
            "taxonNodes.taxon",
    		"taxon.*",
            "taxon.sec",
            "taxon.name.*",
            "taxon.synonymRelations",
    		"description",
    		"descriptions.*",
    		"terms"
    });

    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
    		"classification",
    		"descriptions",
    		"descriptions.*",
    		"description.state",
    		"feature",
    		"feature.*",
    		"State",
    		"state",
    		"states",
    		"stateData",
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
    		"taxon.descriptions",
    		"taxon.sec",
    		"taxon.name.*",
    		"taxon.synonymRelations",
    		"terms",
    		"$",
            "elements.$",
            "elements.states.$",
            "elements.sources.citation.authorTeam",
            "elements.sources.nameUsedInSource.originalNameString",
            "elements.multilanguageText",
            "elements.media",
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "sources.$"
    		
    });
    
    protected static final List<String> DESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
    		 "$",
    		 "states.$",
    		 "sources.citation.authorTeam",
    		 "sources.nameUsedInSource.originalNameString",
    		 "multilanguageText",
    		 "media",
    });
    
    private static final  List<String> TAXON_NODE_INIT_STRATEGY = Arrays.asList(new String[] {"synonymRelations","descriptions, features, categoricalData"});

	@Override
	public void activeStepChanged(WizardStepActivationEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stepSetChanged(WizardStepSetChangedEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void wizardCompleted(WizardCompletedEvent event) {
		// TODO Auto-generated method stub
		isWizard = false;
		mainLayout();
	}

	@Override
	public void wizardCancelled(WizardCancelledEvent event) {
		// TODO Auto-generated method stub
		isWizard = false;
		mainLayout();
	}
    private void endWizard(String message) {
        wizard.setVisible(false);
        Notification.show(message);
        Page.getCurrent().setTitle(message);
        Button startOverButton = new Button("Run the demo again",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        // Close the session and reload the page.
                        VaadinSession.getCurrent().close();
                        Page.getCurrent().setLocation("");
                    }
                });
//        mainLayout.addComponent(startOverButton);
//        mainLayout.setComponentAlignment(startOverButton, Alignment.MIDDLE_CENTER);
        isWizard = false;
        mainLayout();
    }

}