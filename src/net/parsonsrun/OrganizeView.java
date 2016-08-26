package net.parsonsrun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Team;

public class OrganizeView extends BaseView
{
	TextField lgName;
	BeanItemContainer<Player> players;
	Grid playersGrid;
	BeanItemContainer<Team> teams;
	Grid teamsGrid;
	Player current;
	TextField currentTeam;
	Button clear;
	TextField numRnds;
	

	public OrganizeView()
	{

		super();
		addBack();
		setSizeFull();
		lgName = new TextField("Manage league:");
		lgName.setWidth("30em");
		lgName.setValue(getCurrentLeague().getName());
		addComponent(lgName);
		HorizontalLayout h = new HorizontalLayout();
		h.setSizeFull();
		//addComponent(new Label("Select Player to add to team"));
		addComponent(h);
		h.setSpacing(true);
		VerticalLayout left = new VerticalLayout();
		left.setSizeFull();		
		VerticalLayout middle = new VerticalLayout();
		//middle.setSizeFull();
		VerticalLayout right = new VerticalLayout();
		right.setSizeFull();
		h.addComponent(left);
		h.addComponent(middle);
		h.addComponent(right);
		h.setExpandRatio(left, 0.4f);
		h.setExpandRatio(middle, 0.2f);
		h.setExpandRatio(right, 0.4f);
		buildLeft(left);
		buildMiddle(middle);
		buildRight(right);
		numRnds = new TextField("Number of rounds:");
		addComponent(numRnds);
		setExpandRatio(h, 1.0f);
	}
	
	protected void buildMiddle(VerticalLayout mid)
	{
		mid.addComponent(new Label("Make a Team with"));
		currentTeam = new TextField();
		currentTeam.setReadOnly(true);
		mid.addComponent(currentTeam);
		clear = new Button("Clear");
		clear.setEnabled(false);
		clear.addStyleName(ValoTheme.BUTTON_TINY);
		clear.addClickListener(e -> clear());
		mid.addComponent(clear);
	}
	
	protected void insert(Player np)
	{
		boolean inserted = false;
		int index = 0;
		for (Player p : players.getItemIds())
		{
			if (np.compareTo(p) < 1)
			{
				players.addItemAt(index, np);
				inserted = true;
				break;
			}
			index++;
		}
		if (!inserted)
			players.addItem(np);
		playersGrid.select(null);
		playersGrid.markAsDirty();

	}
	
	protected void clear()
	{
		if (current != null)
		{
			insert(current);
		}
		current = null;
		updateTeam();
	}
	
	protected void updateTeam()
	{
		currentTeam.setReadOnly(false);
		currentTeam.setValue(current == null ? "" : current.firstLastName());
		currentTeam.setReadOnly(true);
		clear.setEnabled(current != null);
	}
	
	protected void buildRight(VerticalLayout r)
	{
		teams = new BeanItemContainer<Team>(Team.class);
		teams.addAll(getCurrentLeague().getTeams());
		teamsGrid = new Grid("Select Team to remove it", teams);
		teamsGrid.setHeaderVisible(false);
		teamsGrid.addSelectionListener(e -> teamSelected());
		teamsGrid.removeAllColumns();
		teamsGrid.setHeight("24em");
		teamsGrid.setWidth("100%");
		teamsGrid.addColumn("fullName");
		teamsGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		r.addComponent(teamsGrid);
	}
	
	protected void buildLeft(VerticalLayout v)
	{
		players = new BeanItemContainer<Player>(Player.class);
		List<Player> list = getHood().getPlayers().stream().filter(p -> p.isActive()).collect(Collectors.toList());
		Collections.sort(list);
		players.addAll(list);
		playersGrid = new Grid("Select Player to add to team", players);
		playersGrid.setHeaderVisible(false);
		playersGrid.addSelectionListener(e -> playerSelected());
		playersGrid.removeAllColumns();
		playersGrid.setWidth("100%");
		playersGrid.setHeight("24em");
		playersGrid.addColumn("lastFirstName");
		playersGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		v.addComponent(playersGrid);
	}
	
	protected void playerSelected()
	{
		Player p = (Player)playersGrid.getSelectedRow();
		if (p != null)
		{
			players.removeItem(p);
			playersGrid.markAsDirty();
			if (current != null)
			{
				makeNewTeam(p);
				current = null;
			}
			else
				current = p;
		}
		updateTeam();
	}
	
	protected void teamSelected()
	{
		Team t = (Team)teamsGrid.getSelectedRow();
		if (t != null)
		{
			if (t.canDelete())
			{
				teams.removeItem(t);
				teamsGrid.markAsDirty();
				insert(t.getPlayerA());
				insert(t.getPlayerB());
			}
			else
				Notification.show("Unable to remove a team that already has played matches", Type.WARNING_MESSAGE);
		}
	}
	
	protected void makeNewTeam(Player p)
	{
		Team t = new Team();
		t.setPlayers(current, p);
		teams.addBean(t);
		teamsGrid.markAsDirty();
		calcRndSz();
	}
	
	protected void calcRndSz()
	{
		int sz = teams.size();
		if (sz % 2 == 0)
			sz++;
		numRnds.setValue(""+sz);
	}
	
	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
