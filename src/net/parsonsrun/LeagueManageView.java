package net.parsonsrun;

import java.util.*;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.MultiSelectionModel;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Player;

public class LeagueManageView extends BaseView
{
	Panel panel;
	BeanItemContainer<League> leagues;
	Grid leaguesGrid;
	BeanItemContainer<Player> players;
	Grid playersGrid;
	TextArea orgs;
	TextField newLeague;
	String orgsLabel = "Coordinators: ";
	Button delete;
	Button reset;
	Button up;
	Button down;
	
	public LeagueManageView()
	{
		super();
		addBack();
		setWidth("100%");
		addTitle("League Management");
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		VerticalLayout left = new VerticalLayout();
		left.setWidth("40%");
		left.setSpacing(true);
		VerticalLayout right = new VerticalLayout();
		right.setWidth("60%");
		h.addComponent(left);
		h.addComponent(right);
		HorizontalLayout h2 = new HorizontalLayout();
		newLeague = new TextField();
		newLeague.setWidth("20em");
		newLeague.setImmediate(true);
		h2.addComponent(newLeague);
		Button b = new Button("Create new League");
		b.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		b.addClickListener(e -> createNew());
		h2.addComponent(b);
		addComponent(h2);
		panel = new Panel();
		left.addComponent(panel);
		panel.setContent(buildLeagues());
		left.addComponent(new Label("(Double click to edit name/set closed)"));
		
		h2 = new HorizontalLayout();
		left.addComponent(h2);
		h2.setSpacing(true);
		up = new Button("^ Move up ^");
		up.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		up.addStyleName(ValoTheme.BUTTON_TINY);
		up.addClickListener(e -> up());
		up.setEnabled(false);
		h2.addComponent(up);
		down = new Button("v Move down v");
		down.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		down.addStyleName(ValoTheme.BUTTON_TINY);
		down.addClickListener(e -> down());
		down.setEnabled(false);
		h2.addComponent(down);

		
		reset = new Button("Remove all Match Scores (RESET)");
		reset.addStyleName(ValoTheme.BUTTON_DANGER);
		reset.setWidth("100%");	
		reset.addClickListener(e -> resetLeague());
		reset.setEnabled(false);
		left.addComponent(reset);
		
		delete = new Button("Delete Selected League");
		delete.addStyleName(ValoTheme.BUTTON_DANGER);
		//delete.addStyleName(ValoTheme.BUTTON_TINY);
		delete.setWidth("100%");	
		delete.addClickListener(e -> delete());
		delete.setEnabled(false);
		left.addComponent(delete);
		
		players = new BeanItemContainer<Player>(Player.class);
		ArrayList<Player> list = (ArrayList<Player>)getHood().getPlayers().clone();
		Collections.sort(list);
		players.addAll(list);
		playersGrid = new Grid("Coordinators for selected League", players);
		playersGrid.setSelectionMode(SelectionMode.MULTI);
		playersGrid.addSelectionListener(e -> playerSelected());
		playersGrid.removeAllColumns();
		playersGrid.setHeaderVisible(false);
		playersGrid.setHeight("24em");
		playersGrid.addColumn("lastFirstName");
		playersGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		playersGrid.setImmediate(true);
		playersGrid.setEnabled(false);
		right.addComponent(playersGrid);
		orgs = new TextArea();
		orgs.setWidth("100%");
		orgs.setReadOnly(true);
		addComponent(h);
		right.addComponent(orgs);
	}
	
	protected Component buildLeagues()
	{
		leagues = new BeanItemContainer<League>(League.class);
		leagues.addAll(getHood().getLeagues());
		leaguesGrid = new Grid("Select a League:", leagues);
		leaguesGrid.setEditorEnabled(true);
		//leaguesGrid.setEditorBuffered(false);
		leaguesGrid.removeAllColumns();
		//leaguesGrid.setHeaderVisible(false);
		leaguesGrid.setHeight("24em");
		leaguesGrid.addColumn("name");
		leaguesGrid.addColumn("open");
		leaguesGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		leaguesGrid.setImmediate(true);
		leaguesGrid.addSelectionListener(e -> leagueSelected());
		leaguesGrid.setWidth("100%");	
		return leaguesGrid;
	}
	

	
	protected void resetLeague()
	{
		ConfirmDialog.show(getP2tennisUI(), 
				"Please Confirm:", "Are you really sure you want to remove ALL SCORES?",
		        "I am", "Not quite", new ConfirmDialog.Listener() {

		            public void onClose(ConfirmDialog dialog) {
		                if (dialog.isConfirmed()) {
		                	getCurrentLeague().resetScores();
		                	Notification.show("All scores removed from \"" + getCurrentLeague().getName() + "\"");
		                }
		            }
		        });
		
	}
	
	public void backClicked()
	{
		getHood().saveData();
		super.backClicked();
	}
	
	protected void delete()
	{
		League lg = (League)leaguesGrid.getSelectedRow();
		if (lg != null)
		{
			ConfirmDialog.show(getP2tennisUI(), 
					"Please Confirm:", "Are you really sure you want to delete \"" + lg.getName() + "\"?",
			        "I am", "Not quite", new ConfirmDialog.Listener() {

			            public void onClose(ConfirmDialog dialog) {
			                if (dialog.isConfirmed()) {
			        			getHood().removeLeague(lg);
			        			update(null);
			                }
			            }
			        });
		}
	}
	
	protected void up()
	{
		League lg = (League)leaguesGrid.getSelectedRow();
		if (lg == null)
			return;
		getHood().moveLeague(-1, lg);
		update(lg);
	}
	
	protected void update(League lg)
	{
		panel.setContent(buildLeagues());
		if (lg != null)
			leaguesGrid.select(lg);
		leagueSelected();	
		setCurrentLeague(lg);
	}
	
	protected void down()
	{
		League lg = (League)leaguesGrid.getSelectedRow();
		if (lg == null)
			return;
		getHood().moveLeague(1, lg);
		update(lg);		
	}
	
	protected void enableMove(League lg)
	{
		if (lg == null)
		{
			up.setEnabled(false);
			down.setEnabled(false);
		}
		else
		{
			int index = getHood().getLeagues().indexOf(lg) + 1;
			up.setEnabled(index > 1);
			down.setEnabled(index < getHood().getLeagues().size());
		}
	}
	
	public void createNew()
	{
		String s = newLeague.getValue();
		if (s.isEmpty())
		{
			Notification.show("Unable to create league with no name ;-)", Type.WARNING_MESSAGE);
		}
		else
		{
			League lg = new League();
			lg.setName(s);
			newLeague.setValue("");
			if (getHood().addLeague(lg))
			{
				getHood().saveData();
				panel.setContent(buildLeagues());
				leaguesGrid.select(lg);
				leagueSelected();
				setCurrentLeague(lg);
			}
			else
				Notification.show("Unable to create league (\"" + s + "\" already exists)", Type.WARNING_MESSAGE);
		}
	}
	
	
	protected void leagueSelected()
	{
		League lg = (League)leaguesGrid.getSelectedRow();
		if (lg != null)
		{
			ArrayList<Player> os = lg.getOrganizers();
			MultiSelectionModel sel = (MultiSelectionModel) playersGrid.getSelectionModel();
			sel.setSelected(os);
			delete.setEnabled(lg.canDelete());
			playersGrid.setEnabled(true);
			orgs.setEnabled(true);
			reset.setEnabled(true);
		}
		else
		{
			delete.setEnabled(false);
			playersGrid.setEnabled(false);
			orgs.setEnabled(false);
			reset.setEnabled(false);
		}
		enableMove(lg);
		updateOrgs(lg);
	}
	
	protected void playerSelected()
	{
		League lg = (League)leaguesGrid.getSelectedRow();
		if (lg == null)
			return;
		Iterator itr = playersGrid.getSelectedRows().iterator();
		ArrayList<Player> pls = new ArrayList<Player>();
		while (itr.hasNext())
		{
			Player p = (Player)itr.next();
			pls.add(p);
		}
		lg.setOrganizers(pls);
		updateOrgs(lg);
	}
	
	protected void updateOrgs(League lg)
	{
		orgs.setReadOnly(false);
		orgs.setValue(orgsLabel + (lg == null ? "" : lg.getOrganizerNames()));
		orgs.setReadOnly(true);
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
