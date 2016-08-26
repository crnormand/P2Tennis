package net.parsonsrun;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ValoTheme;

public class LoginView extends BaseView
{
	public LoginView()
	{
		super();
		//setWidth("32em");
		//addBack();
		addTitle("Please log in using your P2 Tournament email address.");
		addComponent(new Label("(if this is your first time, your password is blank)"));
		addTitle("Once you have logged in, you will not need to log in again with this browser.");
		//addComponent(new Label("Log in once, and you won't have to log in again"));
		TextField tf1 = new TextField("Email:");
		tf1.setImmediate(true);
		tf1.setWidth("32em");
		addComponent(tf1);
		PasswordField tf2 = new PasswordField("Password:");
		tf2.setImmediate(true);
		addComponent(tf2);
		addComponent(createSpacer());
		Button b2 = new Button("Login");
		b2.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		addComponent(b2);
		addComponent(createSpacer());
		addComponent(new Label("<i>If you do not have an account, please email <a href=\"mailto:crnormand@bellsouth.net?Subject=New%20User%20Request\">Chris Normand</a></i>", ContentMode.HTML));
		
		b2.addClickListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		        String em = (String) tf1.getValue();
		        String ps = (String) tf2.getValue();
		        if (!em.isEmpty())
		        {
			        if (getP2tennisUI().login(em, ps))
			        {
			        	getP2tennisUI().navigateBack();
			        	if (ps.isEmpty())
			        		getP2tennisUI().updateProfile(false);
			        }
			        else
			        {
			        	tf2.setValue("");
			        	Notification.show("Unable to login:",
			                    "Email or passsword incorrect.",
			                    Notification.Type.WARNING_MESSAGE);
			        }
		        }
			}
		});
		b2.setClickShortcut(KeyCode.ENTER);
	}
	@Override
	public void enter(ViewChangeEvent event)
	{
		//Notification.show("In Login");
	}
}
