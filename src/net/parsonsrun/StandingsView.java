package net.parsonsrun;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.CellReference;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.Standing;

public class StandingsView extends BaseView
{
	Grid st;
	VerticalLayout side;
	
	public StandingsView()
	{
		super();
		String w = "31em"; 
		if (showElo()) w = "37em";
		//addBack();
		addBackAndTitle("Standings for \"" + getCurrentLeague().getName() + "\"");
		//addComponent(new Label("Standings for \"" + getCurrentLeague().getName() + "\""));
		addComponent(new Label("Coordinator(s): " + getCurrentLeague().getOrganizerNames()));
		HorizontalLayout h = new HorizontalLayout();
		addComponent(h);
		h.setWidth("950px");
		//setExpandRatio(h, 1.0f);
		VerticalLayout v = new VerticalLayout();
		BeanItemContainer<Standing> con = new BeanItemContainer<Standing>(Standing.class);
		con.addAll(getCurrentLeague().getStandings());
		st = new Grid("Select team to see individual matches:", con);
		v.addComponent(st);
		st.setImmediate(true);
		st.removeAllColumns();
		st.addColumn("name").setExpandRatio(1).setHeaderCaption("Team");
//		st.addColumn("wins").setHeaderCaption("W");
//		st.addColumn("loses").setHeaderCaption("L");
		st.addColumn("winPerc").setHeaderCaption("W%");
		st.addColumn("winLose").setHeaderCaption("W-L [SW:GW]");
//		st.addColumn("startingRank").setHeaderCaption("Start");
//		st.addColumn("rank").setHeaderCaption("Rank");
//		st.addColumn("rank").setHeaderCaption("Now");
		st.addColumn("played").setHeaderCaption("#");
		if (showElo())
			st.addColumn("elo").setHeaderCaption("ELO");
		st.getColumns().stream().forEach(c -> c.setSortable(false));
		st.addSelectionListener(e -> standingSelected());
		st.setCellStyleGenerator(cell -> "smallcell" );
		st.setWidth(w);
		TextArea t = new TextArea();
		t.setWidth(w);
		t.setHeight("13em");
		StringBuffer sb = new StringBuffer();
		sb.append("  W%\t\t\t= Win Percentage (Wins/Played)\n");
		sb.append("  W-L [SW:GW]\t= Matches Won-Lost [Sets Won:Games Won]\n");
		sb.append("  #\t\t\t\t= Number of Matches played\n");
		if (showElo())
			sb.append("  ELO\t\t\t= ELO Score\n");
		sb.append("\nThe Standings are based on Most Matches Won, ");
		sb.append("(with ties going to the winner of a Head-to-Head match) ");
		sb.append("then Most Sets Won, then Most Games Won, ");
		sb.append("and finally Alphabetically by First name ;-)");
		t.setValue(sb.toString());
		t.setReadOnly(true);
		v.addComponent(t);
		side = new VerticalLayout();
		side.setVisible(false);
		h.addComponent(v);
		//h.setExpandRatio(v, 0.5f);
		h.addComponent(side);
		h.setExpandRatio(side, 1.0f);
		v.setWidth(w);
	}
	
	protected void standingSelected()
	{
		Standing s = (Standing)st.getSelectedRow();
		if (s != null)
		{
			side.removeAllComponents();
			side.setVisible(true);
			setCurrentTeam(s.getTeam());
			setCurrentMatches(s.getVsMatches());
			MatchesView2 m = new MatchesView2();
			m.setMargin(false);
			m.setSpacing(false);
			side.addComponent(m);
		}
	}
	
	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
