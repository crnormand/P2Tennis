package net.parsonsrun;

import java.util.ArrayList;
import java.util.Collections;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Player;

public class AdminView extends BaseView
{
	BeanItemContainer<Player> players;
	Grid playersGrid;
	BeanItemContainer<League> leagues;
	Grid leaguesGrid;

	public AdminView()
	{
		super();
		addBack();
		addTitle("Administration functions:");
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		VerticalLayout left = new VerticalLayout();
		VerticalLayout right = new VerticalLayout();
		h.addComponent(left);
		h.addComponent(right);
		addComponent(h);
		setMargin(true);
		Button b = new Button("Create New User");
		b.addStyleName(ValoTheme.BUTTON_DANGER);
		b.addClickListener(e -> createProfile());
		left.addComponent(b);
		
		b = new Button("Manage Leagues");
		b.addStyleName(ValoTheme.BUTTON_DANGER);
		b.addClickListener(e -> assignLos());
		right.addComponent(b);
		right.addComponent(new Label("Create/Delete Leagues, Assign Coordinators"));
		right.addComponent(createSpacer());
		
		leagues = new BeanItemContainer<League>(League.class);
		leagues.addAll(getHood().getLeagues());
		leaguesGrid = new Grid("Select League to Open Organizer:", leagues);
		right.addComponent(leaguesGrid);
		leaguesGrid.removeAllColumns();
		leaguesGrid.setHeaderVisible(false);
		leaguesGrid.setHeight("12em");
		leaguesGrid.addColumn("name");
		leaguesGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		leaguesGrid.setImmediate(true);
		leaguesGrid.addSelectionListener(e -> leagueSelected());
		leaguesGrid.setWidth("100%");	
		
		players = new BeanItemContainer<Player>(Player.class);
		ArrayList<Player> list = (ArrayList<Player>)getHood().getPlayers().clone();
		Collections.sort(list);
		players.addAll(list);
		playersGrid = new Grid("Or Select User to Edit Profile", players);
		playersGrid.addSelectionListener(e -> playerSelected());
		playersGrid.removeAllColumns();
		//playersGrid.setHeaderVisible(false);
		playersGrid.setHeight("24em");
		playersGrid.addColumn("lastFirstName");
		playersGrid.addColumn("active");
		playersGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		playersGrid.setImmediate(true);
		left.addComponent(playersGrid);
	}
	
	protected void createProfile()
	{
		setCurrentUser(null);
		navigateTo(P2tennisv2UI.REGISTER);
	}
		
	protected void leagueSelected()
	{
		League lg = (League)leaguesGrid.getSelectedRow();
		if (lg != null)
		{
			setCurrentLeague(lg);
			navigateTo(P2tennisv2UI.ORGANIZER);
		}
	}
	
	protected void playerSelected()
	{
		Player p = (Player)playersGrid.getSelectedRow();
		if (p != null)
		{
			setCurrentUser(p);
			navigateTo(P2tennisv2UI.REGISTER);
		}
	}

	protected void assignLos()
	{
		navigateTo(P2tennisv2UI.ASSIGNLO);		
	}
	
	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
