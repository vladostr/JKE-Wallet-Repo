package org.vaadin.presentation.views;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.vaadin.backend.AccountService;
import org.vaadin.backend.TransactionService;
import org.vaadin.backend.domain.Account;
import org.vaadin.backend.domain.Payment;
import org.vaadin.cdiviewmenu.ViewMenuItem;
import org.vaadin.presentation.AppUI;
import org.vaadin.presentation.ScreenSize;
import org.vaadin.presentation.views.AccountEvent.Type;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.fields.MValueChangeEvent;
import org.vaadin.viritin.fields.MValueChangeListener;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.cdi.CDIView;
import com.vaadin.event.FieldEvents;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;
import java.security.Principal;
import javax.security.auth.login.CredentialExpiredException;
import com.ibm.websphere.security.auth.WSSubject;

/**
 * A view that lists Accounts in a Table and lets user to choose one for
 * editing. There is also RIA features like on the fly filtering.
 */
@CDIView("accountlist")
@ViewMenuItem(icon = FontAwesome.LIST)
public class MyAccountsView extends MVerticalLayout implements View {

    @Inject
    private AccountService service;

	@Inject
	private TransactionService tservice;

    @Inject
    AccountForm accountEditor;

	String authname = "unknown";

    // Introduce and configure some UI components used on this view
    MTable<Account> accountTable = new MTable(Account.class).withFullWidth().
            withFullHeight();

    MHorizontalLayout mainContent = new MHorizontalLayout(accountTable).
            withFullWidth().withMargin(false);

    //TextField filter = new TextField();

    Header header = new Header("Accounts").setHeaderLevel(2);

    Button addButton = new MButton(FontAwesome.EDIT,
            new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    addAccount();
                }
            });

    @PostConstruct
    public void init() {

        /*
         * Add value change listener to table that opens the selected Account into
         * an editor.
         */
        accountTable.addMValueChangeListener(new MValueChangeListener<Account>() {

            @Override
            public void valueChange(MValueChangeEvent<Account> event) {
                editAccount(event.getValue());
            }
        });

        /*
         * Configure the filter input and hook to text change events to
         * repopulate the table based on given filter. Text change
         * events are sent to the server when e.g. user holds a tiny pause
         * while typing or hits enter.
         * */
       // filter.setInputPrompt("Filter Accounts...");
       // filter.addTextChangeListener(new FieldEvents.TextChangeListener() {
       //     @Override
       //     public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
       //         listAccounts(textChangeEvent.getText());
       //     }
       // });


        /* "Responsive Web Design" can be done with plain Java as well. Here we
         * e.g. do selective layouting and configure visible columns in
         * table based on available width */
        layout();
        adjustTableColumns();
        /* If you wish the UI to adapt on window resize/page orientation
         * change, hook to BrowserWindowResizeEvent */
        UI.getCurrent().setResizeLazy(true);
        Page.getCurrent().addBrowserWindowResizeListener(
                new Page.BrowserWindowResizeListener() {
                    @Override
                    public void browserWindowResized(
                            Page.BrowserWindowResizeEvent browserWindowResizeEvent) {
                                adjustTableColumns();
                                layout();
                            }
                });
        listAccounts();
    }

    /**
     * Do the application layout that is optimized for the screen size.
     * <p>
     * Like in Java world in general, Vaadin developers can modularize their
     * helpers easily and re-use existing code. E.g. In this method we are using
     * extended versions of Vaadins basic layout that has "fluent API" and this
     * way we get bit more readable code. Check out vaadin.com/directory for a
     * huge amount of helper libraries and custom components. They might be
     * valuable for your productivity.
     * </p>
     */
    private void layout() {
        removeAllComponents();
        if (ScreenSize.getScreenSize() == ScreenSize.LARGE) {
            addComponents(
                    new MHorizontalLayout(header, addButton)
                    .expand(header)
                    .alignAll(Alignment.MIDDLE_LEFT),
                    mainContent
            );

            //filter.setSizeUndefined();
        } else {
            addComponents(
                    header,
                    new MHorizontalLayout(addButton)
                    //.expand(filter)
                    .alignAll(Alignment.MIDDLE_LEFT),
                    mainContent
            );
        }
        setMargin(new MarginInfo(false, true, true, true));
        expand(mainContent);
    }

    /**
     * Similarly to layouts, we can adapt component configurations based on the
     * client details. Here we configure visible columns so that a sane amount
     * of data is displayed for various devices.
     */
    private void adjustTableColumns() {
        accountTable.setVisibleColumns("accountName", "accountType", "balance");
        accountTable.setColumnHeaders("Name", "Account", "Balance");
    }

    /* ******* */
    // Controller methods.
    //
    // In a big project, consider using separate controller/presenter
    // for improved testability. MVP is a popular pattern for large
    // Vaadin applications.
    private void listAccounts() {
        // Here we just fetch data straight from the EJB.
        //
        // If you expect a huge amount of data, do proper paging,
        // or use lazy loading Vaadin Container like LazyQueryContainer
        // See: https://vaadin.com/directory#addon/lazy-query-container:vaadin
        
        try {
		        Subject s = WSSubject.getCallerSubject();
				    		
				if (s != null) {
		        Set<Principal> principals = s.getPrincipals();
		        if (principals != null && principals.size() > 0) {
		            authname = principals.iterator().next().getName();
		        }
		    }
    		
	    } catch (Exception exc) {
	        authname = "bad stuff";
	    }
       	//accountTable.setBeans(new ArrayList<>(service.findByName(authname)));
       	//service.updateAccounts(authname);
       	tservice.updateTransactions(authname);
       	accountTable.setBeans(new ArrayList<>(service.findByName(authname)));
    }

    private void listAccounts(String filterString) {
        //service.updateAccounts(authname);
        accountTable.setBeans(new ArrayList<>(service.findByName(filterString)));
    }

    void editAccount(Account trans) {
        if (trans != null) {
            openEditor(trans);
        } else {
            closeEditor();
        }
    }

    void addAccount() {
        openEditor(new Account());
    }

    private void openEditor(Account trans) {
        accountEditor.setEntity(trans);
        // display next to table on desktop class screens
        if (ScreenSize.getScreenSize() == ScreenSize.LARGE) {
            mainContent.addComponent(accountEditor);
            accountEditor.focusFirst();
        } else {
            // Replace this view with the editor in smaller devices
            AppUI.get().getContentLayout().
                    replaceComponent(this, accountEditor);
        }
    }

    private void closeEditor() {
        // As we display the editor differently in different devices,
        // close properly in each modes
        if (accountEditor.getParent() == mainContent) {
            mainContent.removeComponent(accountEditor);
        } else {
            AppUI.get().getContentLayout().
                    replaceComponent(accountEditor, this);
        }
    }

    /* These methods gets called by the CDI event system, which is also
     * available for Vaadin UIs when using Vaadin CDI add-on. In this
     * example events are arised from AccountForm. The CDI event system
     * is a great mechanism to decouple components.
     */
    void saveAccount(@Observes @AccountEvent(Type.SAVE) Account trans) {
        listAccounts();
        closeEditor();
    }

    void resetAccount(@Observes @AccountEvent(Type.REFRESH) Account trans) {
        listAccounts();
        closeEditor();
    }

    void deleteAccount(@Observes @AccountEvent(Type.DELETE) Account trans) {
        closeEditor();
        listAccounts();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }

}
