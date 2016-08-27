package net.parsonsrun;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.dev.javac.testing.GeneratorContextBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.vaadin.addon.contextmenu.ContextMenu;
import com.vaadin.addon.contextmenu.GridContextMenu;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.GridContextClickEvent;
import com.vaadin.ui.Grid.HeaderRow;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Round;

public class ScheduleView extends BaseView
{
	protected Grid grid;
	protected BeanItemContainer<Row> rows;
	protected Button update;
	protected Button comment;
	protected RichTextArea commentLabel;
	protected PopupView popup;
	protected long lastClick;
	
	public class Row
	{
		League league;
		int index;
		public Row(League lg, int rowNumber)
		{
			league = lg;
			index = rowNumber;
		}
		
		public String get(int i)
		{
			return getMatch(i).getHtml();
		}
		
		public Match getMatch(String prop)
		{
			String col = prop.substring(5);
			int i = Integer.parseInt(col);
			return getMatch(i);
		}
		
		public Match getMatch(int i)
		{
			Round r = league.getRound(i - 1);
			return r.getMatch(index);
		}
		
		public String getRound0()
		{
			return get(0);
		}

		public String getRound1()
		{
			return get(1);
		}
		
		public String getRound2()
		{
			return get(2);
		}

		public String getRound3()
		{
			return get(3);
		}

		public String getRound4()
		{
			return get(4);
		}

		public String getRound5()
		{
			return get(5);
		}

		public String getRound6()
		{
			return get(6);
		}

		public String getRound7()
		{
			return get(7);
		}

		public String getRound8()
		{
			return get(8);
		}

		public String getRound9()
		{
			return get(9);
		}

		public String getRound10()
		{
			return get(10);
		}

		public String getRound11()
		{
			return get(11);
		}

		public String getRound12()
		{
			return get(12);
		}

		public String getRound13()
		{
			return get(13);
		}

		public String getRound14()
		{
			return get(14);
		}

		public String getRound15()
		{
			return get(15);
		}

		public String getRound16()
		{
			return get(16);
		}

		public String getRound17()
		{
			return get(17);
		}

		public String getRound18()
		{
			return get(18);
		}

		public String getRound19()
		{
			return get(19);
		}

		public String getRound20()
		{
			return get(20);
		}

	}
	
	public void resized()
	{
		getP2tennisUI().redrawView();
	}
	
	protected ArrayList<Row> getRowData()
	{
		ArrayList<Row> r = new ArrayList<Row>();
		League lg = getCurrentLeague();
		if (lg != null && lg.getRounds().size() > 0)
		{
			Round first = lg.getRound(0);
			int sz = first.getMatches().size();
			for (int i = 0; i < sz; i++)
			{
				r.add(new Row(lg, i));
			}
		}
		return r;
	}
	
	public ScheduleView()
	{
		super();
		if (getCurrentLeague() == null || getCurrentLeague().getRounds() == null)
		{
			League l = new League();
			l.setName("Unknown");
			setCurrentLeague(l);
		}
		int height = showElo() ? 9 : 6;
		rows = new BeanItemContainer<Row>(Row.class);
		ArrayList<Row> rs = getRowData();
		rows.addAll(rs);
		String s1 = "(double click match or select match and press button to edit match score)";
		if (getCurrentLeague().isClosed())
			s1 = "This league is closed.";
		addBackAndTitle("Schedule for \"" + getCurrentLeague().getName() + "\"", s1);
		Panel p = new Panel();
		grid = new Grid("Coordinator(s): " + getCurrentLeague().getOrganizerNames(), rows);
		p.setContent(grid);
		//addComponent(grid);
		addComponent(p);
		grid.setWidth(getBrowserWidth() - 50, UNITS_PIXELS);
		int h = height * rs.size();
		grid.setHeight((h + 1) + "em");
		grid.setSelectionMode(SelectionMode.NONE);
		grid.setStyleName("tallgrid" + (height - 1));
		grid.removeAllColumns();
		grid.getDefaultHeaderRow().setStyleName("centered");
		int i = 0;
		for (Round rnd : getCurrentLeague().getRounds())
		{
			String s = "round" + (++i);
			grid.addColumn(s);
			Column col = grid.getColumn(s);
			col.setRenderer(new HtmlRenderer());
			col.setHeaderCaption(rnd.getName());
			col.setWidth(160);
		}
		grid.getColumns().stream().forEach(c -> c.setSortable(false));
		grid.setImmediate(false);
		grid.addItemClickListener(e -> matchSelected(e));
		grid.setCellDescriptionGenerator(new Grid.CellDescriptionGenerator() {
			public String getDescription(Grid.CellReference cellReference) {
				String tooltip = "";
				Row r = (Row)cellReference.getItemId();
				String p = (String)cellReference.getPropertyId();
				Match m = r.getMatch(p);

				if (m != null)
				{
					tooltip = m.getCommentHtml();
				} 
				return tooltip;
			}
			});
		update = new Button("Update");
		update.setEnabled(false);
		update.addClickListener(e -> updateMatch());
		update.setDescription("Enter the scores for the selected match");
		comment = new Button("Show comment");
		comment.setEnabled(false);
		comment.addClickListener(e -> showComment());
		comment.setDescription("View the comment from a match");
		HorizontalLayout h1 = new HorizontalLayout();
		h1.addComponent(update);
		h1.addComponent(comment);
		addComponent(h1);
		ContextMenu contextMenu = new ContextMenu(update, true);
		contextMenu.addContextMenuOpenListener(e -> { openContextOnUpdate();});
		VerticalLayout v = new VerticalLayout();
		commentLabel = new RichTextArea();
		commentLabel.setStyleName("bkcoloryellow"); 
		commentLabel.setWidth(getBrowserWidth() - 50, Unit.PIXELS);
		v.addComponent(commentLabel);
		popup = new PopupView(null, v);
		popup.setPopupVisible(false);
		addComponent(popup);
	}
	
	protected void openContextOnUpdate()
	{
		 getCurrentLeague().recalculate(); 
		 Notification.show("League scores recalculated");
//		 getCurrentLeague().closeOutWeek(new Date(116,7,28));
//		 for (Player p : getHood().getPlayers())
//			 System.out.println(p.firstLastName() + " = " + p.showElo() + " pw:" + p.getPassword());
	}
	
	protected void showComment()
	{
		//ConfirmDialog.show(getP2tennisUI(), getCurrentMatch().getCommentHtml(), null);
		//new Notification(getCurrentMatch().getCommentHtml(), null, Notification.TYPE_WARNING_MESSAGE, true).show(Page.getCurrent());
		commentLabel.setReadOnly(false);
		commentLabel.setValue(getCurrentMatch().getCommentHtml(getBrowserWidth() - 70));
		commentLabel.setReadOnly(true);
		popup.setPopupVisible(true);
	}
	
	protected void addGridMenu() {
		
		ContextMenu contextMenu = new ContextMenu(grid, true);
		contextMenu.addContextMenuOpenListener(e -> {
            GridContextClickEvent gridE = (GridContextClickEvent) e.getContextClickEvent();

    		Row r = (Row)gridE.getItemId();
    		String p = (String)gridE.getPropertyId();
    		Match m = r.getMatch(p);
    		matchSelected(m);
    		//grid.select(m);
    		contextMenu.removeItems();
    		contextMenu.addItem("Update match", k -> { updateMatch(); });
    		contextMenu.open(gridE.getClientX(), gridE.getClientY()); 
    		
    		System.out.println("Open menu: " + gridE.getClientX() + "," +  gridE.getClientY());
		});
	}
	
	protected void matchSelected(ItemClickEvent event)
	{
		Row r = (Row)event.getItemId();
		String p = (String)event.getPropertyId();
		Match m = r.getMatch(p);
		matchSelected(m);
		long t = System.currentTimeMillis();
		if (t - lastClick < P2tennisv2UI.DELAY)
			updateMatch();
		lastClick = t;
	}
	
	protected void matchSelected(Match m)
	{
		setCurrentMatch(m);
		update.setEnabled(m != null && !m.isBye());
		String s = (m == null || m.isBye() || m.hasBeenPlayed()) ? "Update match score" : "Enter match score";
		update.setCaption(s);
		comment.setEnabled(m != null && m.hasComment());
	}

	
	protected void updateMatch()
	{
		getP2tennisUI().updateCurrentMatch();
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
