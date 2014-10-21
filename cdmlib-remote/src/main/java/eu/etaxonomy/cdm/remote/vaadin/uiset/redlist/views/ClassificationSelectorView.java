package eu.etaxonomy.cdm.remote.vaadin.uiset.redlist.views;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.Theme;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import eu.etaxonomy.cdm.remote.vaadin.components.ClassificationSelectionForm;
import eu.etaxonomy.cdm.remote.vaadin.service.VaadinAuthenticationService;

@Component
@Scope("prototype")
@Theme("mytheme")
@VaadinView(ClassificationSelectorView.NAME)
public class ClassificationSelectorView extends CustomComponent implements View {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "ClassificationSelector";
	@Autowired
	private VaadinAuthenticationService authenticationService;
	@Autowired
	private ClassificationSelectionForm classificationSelectionForm;
	
	@PostConstruct
	public void PostConstruct(){
		if(authenticationService.isAuthenticated()){
			VerticalLayout layout = new VerticalLayout();
			layout.setWidth("100%");
			layout.setHeight("100%");

			HorizontalLayout hLayout = new HorizontalLayout();
			//FIXME: Quick'n'dirty hack
			int hh = Page.getCurrent().getBrowserWindowHeight()-300;
			setHeight(hh +"px");
			
			
			Panel panel = new Panel();
			panel.setSizeUndefined();
			panel.setContent(classificationSelectionForm);
			panel.setStyleName("login");
			
			layout.addComponent(hLayout);
			layout.addComponent(panel);

			layout.setSizeFull();
			layout.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);

			setCompositionRoot(layout);
		}
	}
		

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		Boolean isAuthenticated = (Boolean)UI.getCurrent().getSession().getAttribute("isAuthenticated");
		if(isAuthenticated == null || !isAuthenticated){
			Page.getCurrent().setLocation("/edit/");
		}
	}
}
