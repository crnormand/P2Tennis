package net.parsonsrun;

import java.util.ArrayList;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.ScheduleView.Row;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Standing;
import net.parsonsrun.domain.Team;
import net.parsonsrun.domain.VsMatch;

public class MatchesView extends BaseView
{
	Grid grid;
	Button update;
	Button invite;
	long lastClick;
	Window dialog;
	TextArea prefix;
	TextArea email;
	
	public MatchesView()
	{
		super();
		int height = showElo() ? 10 : 6;
		addBack();
		addComponent(new Label(gridTitle()));
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		//h.setMargin(true);
		invite = new Button("Send Invite email");
		invite.addClickListener(e -> inviteToMatch());
		invite.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		update = new Button(updateLabel());
		update.addClickListener(e -> updateMatch());
		update.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		h.addComponent(invite);
		h.addComponent(update);
		invite.setEnabled(false);
		update.setEnabled(false);
		addComponent(h);
		BeanItemContainer con = new BeanItemContainer<Match>(Match.class);
		con.addAll(getCurrentMatches());
		grid = new Grid(con);
		addComponent(grid);
		grid.setWidth("30em");
		int hgt = height * con.size();
		grid.setHeight((hgt + 2) + "em");
		grid.setStyleName("tallgrid" + height); 
		grid.removeAllColumns();
		grid.setHeaderVisible(false);
		grid.setSelectionMode(SelectionMode.NONE);
		grid.addColumn("html2");
		Column col = grid.getColumn("html2");
		col.setRenderer(new HtmlRenderer());
		grid.getColumns().stream().forEach(c -> c.setSortable(false));
		grid.setCellStyleGenerator(cell -> "smallcell" );

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
		
	protected void sendEmail()
	{
		getP2tennisUI().closeDialog();
		Match m = getCurrentMatch();
		if (m == null)
			return;
		ArrayList<String> to = new ArrayList<String>();
		Team tm1 = m.getSideA().getTeam();
		Team tm2 = m.getSideB().getTeam();
		to.add(tm1.getPlayerA().getEmail());
		to.add(tm1.getPlayerB().getEmail());
		to.add(tm2.getPlayerA().getEmail());
		to.add(tm2.getPlayerB().getEmail());
		StringBuffer sb = new StringBuffer();
		sb.append("You have been invited to play: <br><br>");
		sb.append(m.getRound().getHtml());
		sb.append("<br>");
		sb.append(m.getDescription(false));
		sb.append("<hr><br>");
		sb.append(email.getValue().replaceAll("\n", "<br>"));
		sb.append("<br><br><div style=\"font-size: xx-small;\">Sent by " + getLoginUser().firstLastName() + "</div>");
		P2tennisv2UI.sendEmail(to, "P2 Invite for " + m.getDescription(false), sb.toString());
		email.setValue("");
	}
	
	protected String updateLabel() 
	{
		return "Enter Match score";
	}
	
	protected String gridTitle()
	{
		String s1 = "";
		if (getCurrentLeague().isClosed())
			s1 = " Note: This league is closed.";

		return prefix() + " Matches for " + getCurrentTeam().getName() + ": " + s1;
	}
	
	protected String prefix()
	{
		return "Unplayed";
	}
	protected void matchSelected(ItemClickEvent event)
	{
		Match m = null;
		VsMatch r = (VsMatch)event.getItemId();
		if (r != null)
			m = r.getMatch();
		setCurrentMatch(m);
		long t = System.currentTimeMillis();
		if (t - lastClick < P2tennisv2UI.DELAY)
			updateMatch();
		lastClick = t;
	}
	
	protected void updateMatch()
	{
		getP2tennisUI().updateCurrentMatch();
	}
	
	protected void inviteToMatch()
	{
		Match m = getCurrentMatch();
		if (m == null)
			return;
		dialog = new Window();
		dialog.setModal(true);
		VerticalLayout v = new VerticalLayout();
		dialog.setContent(v);
		v.setWidth(600, Unit.PIXELS);
		prefix = new TextArea();
		prefix.setWidth("100%");
		prefix.setHeight("12em");
		StringBuffer sb = new StringBuffer();
		sb.append("Send an invite message to:\n");
		Team tm1 = m.getSideA().getTeam();
		Team tm2 = m.getSideB().getTeam();
		sb.append("\n  ");
		sb.append(tm1.getPlayerA().firstLastName());
		sb.append(" (");
		sb.append(tm1.getPlayerA().getEmail());
		sb.append(")\n  ");
		sb.append(tm1.getPlayerB().firstLastName());
		sb.append(" (");
		sb.append(tm1.getPlayerB().getEmail());
		sb.append(")\n  ");
		sb.append(tm2.getPlayerA().firstLastName());
		sb.append(" (");
		sb.append(tm2.getPlayerA().getEmail());
		sb.append(")\n  ");
		sb.append(tm2.getPlayerB().firstLastName());
		sb.append(" (");
		sb.append(tm2.getPlayerB().getEmail());
		sb.append(")\n\nWhat would you like to say:");
		prefix.setValue(sb.toString());
		prefix.setReadOnly(true);
		v.addComponent(prefix);
		email = new TextArea();
		email.setSizeFull();
		v.addComponent(email);
		HorizontalLayout h = new HorizontalLayout();
		h.setHeight("30px");
		Button c = new Button("Cancel");
		c.addClickListener(e -> getP2tennisUI().closeDialog());
		h.addComponent(c);
		Button send = new Button("Send email");
		send.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		//send.addStyleName(ValoTheme.BUTTON_HUGE);
		send.addClickListener(e -> sendEmail());
		h.addComponent(send);
		h.setComponentAlignment(c, Alignment.TOP_LEFT);
		h.setComponentAlignment(send, Alignment.TOP_RIGHT);
		v.addComponent(h);
		v.setComponentAlignment(h, Alignment.TOP_RIGHT);
		getP2tennisUI().openDialog(dialog);
	}
	
	public void setCurrentMatch(Match m)
	{
		super.setCurrentMatch(m);
		if (m == null)
			return;
		update.setEnabled(getLoginUser().allowedToEdit(m));
		if (m.includes(getLoginUser()) && !m.isBye())
		{
			invite.setEnabled(!m.hasBeenPlayed());
		}
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
