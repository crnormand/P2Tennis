package net.parsonsrun;

import java.util.ArrayList;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Standing;
import net.parsonsrun.domain.Team;

public abstract class BaseView extends VerticalLayout implements View
{	
	public BaseView()
	{
		setMargin(true);
		setSpacing(true);
//		setHeight(getBrowserHeight(), UNITS_PIXELS);
		setWidth(getBrowserWidth(), UNITS_PIXELS);
	}
	
	public void resized()
	{
		
	}
	
	public Player getLoginUser()
	{
		return getP2tennisUI().getLoginUser();
	}
	
	public int getBrowserHeight()
	{
		return getP2tennisUI().getBrowserHeight();
	}
	public int getBrowserWidth()
	{
		return getP2tennisUI().getBrowserWidth();
	}
	public Neighborhood getHood()
	{
		return getP2tennisUI().getHood();
	}
	
	public P2tennisv2UI getP2tennisUI()
	{
		return (P2tennisv2UI) UI.getCurrent();
	}
	
	public void navigateTo(String viewName)
	{
		getP2tennisUI().navigateTo(viewName);
	}
	
	public void addBack()
	{
		addComponent(getBack());
	}
	
	public Button getBack()
	{
		Button b = new Button("<= Back");
		b.addStyleName(ValoTheme.BUTTON_PRIMARY);
		//b.addStyleName(ValoTheme.BUTTON_TINY);
		b.addClickListener(e -> backClicked());
		return b;
	}

	
	public void backClicked()
	{
		getP2tennisUI().navigateBack();
	}
	
	public void animate()
	{
		//System.out.println("Animation tick");
	}
	
	public boolean showElo()
	{
		return getP2tennisUI().showElo();
	}
	
	public Label createSpacer()
	{
		Label sz = new Label("");
		sz.setWidth(null);
		sz.setHeight("30px");
		return sz;
	}
	
	public void addTitle(String title)
	{
		Label lbl = new Label(title);
		lbl.setStyleName("windowtitle");
		addComponent(lbl);
		setComponentAlignment(lbl, Alignment.MIDDLE_CENTER);
	}
	
	public void addBackAndTitle(String title)
	{
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		h.addComponent(getBack());
		Label lbl = new Label(title);
		lbl.setStyleName("windowtitle");
		h.addComponent(lbl);
		h.setComponentAlignment(lbl, Alignment.MIDDLE_CENTER);
		addComponent(h);
	}
	
	public void addTitle(String title, String small)
	{
		Label lbl1 = new Label(title);
		lbl1.setStyleName("windowtitle");
		Label lbl2 = new Label(small);
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		//h.setMargin(true);
		h.addComponent(lbl1);
		h.addComponent(lbl2);
		addComponent(h);
		h.setComponentAlignment(lbl1, Alignment.MIDDLE_CENTER);
		h.setComponentAlignment(lbl2, Alignment.MIDDLE_RIGHT);
	}
	
	public void addBackAndTitle(String title, String small)
	{
		Label lbl1 = new Label(title);
		lbl1.setStyleName("windowtitle");
		Label lbl2 = new Label(small);
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		//h.setMargin(true);
		h.addComponent(getBack());
		h.addComponent(lbl1);
		h.addComponent(lbl2);
		addComponent(h);
		h.setComponentAlignment(lbl1, Alignment.MIDDLE_CENTER);
		h.setComponentAlignment(lbl2, Alignment.MIDDLE_RIGHT);
	}
	
	public void addBigText(AbstractOrderedLayout l, String title)
	{
		Label lbl = new Label(title);
		lbl.setStyleName("windowtitle");
		l.addComponent(lbl);
		l.setComponentAlignment(lbl, Alignment.MIDDLE_CENTER);
	}

	public Player getCurrentUser()
	{
		return getP2tennisUI().getCurrentUser();
	}

	public void setCurrentUser(Player u)
	{
		getP2tennisUI().setCurrentUser(u);
	}
	
	public Team getCurrentTeam()
	{
		return getP2tennisUI().getCurrentTeam();
	}
	
	public ArrayList<Match> getCurrentMatches()
	{
		return getP2tennisUI().getCurrentMatches();
	}

	public void setCurrentTeam(Team t)
	{
		getP2tennisUI().setCurrentTeam(t);
	}
	
	public void setCurrentMatches(ArrayList<Match> l)
	{
		getP2tennisUI().setCurrentMatches(l);
	}

	
	public void setCurrentLeague(League lg)
	{
		getP2tennisUI().setCurrentLeague(lg);
	}
	
	public League getCurrentLeague()
	{
		return getP2tennisUI().getCurrentLeague();
	}
	
	public Match getCurrentMatch()
	{
		return getP2tennisUI().getCurrentMatch();
	}
	
	public void setCurrentMatch(Match m)
	{
		getP2tennisUI().setCurrentMatch(m);
	}
}
