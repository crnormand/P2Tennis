package net.parsonsrun;

import org.openqa.selenium.remote.server.handler.GetCurrentWindowHandle;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.renderers.HtmlRenderer;

import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Standing;
import net.parsonsrun.domain.VsMatch;

public class RecentMatchesView extends BaseView
{
	Grid grid;
	long lastClick;
	
	public RecentMatchesView()
	{
		super();
		int height = showElo() ? 9 : 6;
		setMargin(false);
		setWidth("100%");
		//addTitle("Recent Matches for " + getCurrentLeague().getName());
		Panel main = new Panel();
		main.setWidth("37em");
		main.setHeight("32em");
		main.setCaption("Recent Matches for \"" + getCurrentLeague().getName() + "\"" + (getCurrentLeague().isClosed() ? " (CLOSED)" : ""));
		addComponent(main);
		BeanItemContainer con = new BeanItemContainer<Match>(Match.class);
		con.addAll(getCurrentLeague().getPlayedMatches());
		grid = new Grid("Recent Matches for \"" + getCurrentLeague().getName() + "\"" + (getCurrentLeague().isClosed() ? " (CLOSED)" : ""), con);
		main.setContent(grid);
		grid.setWidth("35em");
		int hgt = height * con.size();
		grid.setHeight((hgt + 2) + "em");
		grid.setStyleName("tallgrid" + height);
		grid.removeAllColumns();
		grid.setHeaderVisible(false);
		grid.setSelectionMode(SelectionMode.NONE);
		grid.addColumn("recentHtml");
		Column col = grid.getColumn("recentHtml");
		col.setRenderer(new HtmlRenderer());
		grid.getColumns().stream().forEach(c -> c.setSortable(false));
		grid.setImmediate(true);
		grid.addItemClickListener(e -> matchSelected(e));
		grid.setCellDescriptionGenerator(new Grid.CellDescriptionGenerator() {
			public String getDescription(Grid.CellReference cellReference) {
				String tooltip = "";
				Match m = (Match)cellReference.getItemId();

				if (m != null)
				{
					tooltip = m.getCommentHtml();
				} 
				return tooltip;
			}
			});


	}
	
	protected void matchSelected(ItemClickEvent event)
	{
		Match m = (Match)event.getItemId();
		setCurrentMatch(m);
		long t = System.currentTimeMillis();
		if (t - lastClick < P2tennisv2UI.DELAY)
			getP2tennisUI().updateCurrentMatch();
		lastClick = t;
	}

	
	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
