package net.parsonsrun;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class OkView extends BaseView
{

	public OkView()
	{
		addComponent(new Label("You have been logged out."));
		Button b = new Button("OK");
		b.addStyleName(ValoTheme.BUTTON_DANGER);
		addComponent(b);
		b.addClickListener(e -> ok());
	}
	
	protected void ok()
	{
		backClicked();
	}
	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
