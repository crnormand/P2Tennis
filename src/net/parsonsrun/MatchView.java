package net.parsonsrun;

import java.util.ArrayList;
import java.util.Date;

import com.vaadin.data.Property;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Side;

public class MatchView extends BaseView
{
	static final String SETA = "-- SET ";
	static final String SETB = "--";
	static final String ZERO = "0";
	String[] setIds = { "7", "6", "5", "4", "3", "2", "1", ZERO };
	ArrayList<ArrayList<CheckBox>> aSets = new ArrayList<ArrayList<CheckBox>>();
	ArrayList<ArrayList<CheckBox>> bSets = new ArrayList<ArrayList<CheckBox>>();
	CheckBox isFA;
	CheckBox isFB;
	TextField scores;
	int [] scoresA = new int[3];
	int [] scoresB = new int[3];
	Label warning;
	Button ok;
	DateField date;
	TextArea comment;
	CheckBox locked;

	public MatchView()
	{
		super();
		addBackAndTitle("Update Match: " + getCurrentMatch().getRound().getName());
		addComponent(new Label("NOTE:   You only have 5 minutes to enter the score."));
		HorizontalLayout top = new HorizontalLayout();
		top.setSpacing(true);
		addComponent(top);
		VerticalLayout left = new VerticalLayout();
		VerticalLayout right = new VerticalLayout();
		top.addComponent(left);
		top.addComponent(right);
		addBigText(left, getSideA().getName());
		addBigText(right, getSideB().getName());
		//left.addComponent(new Label(getSideA().getName()));
		//right.addComponent(new Label(getSideB().getName()));
		aSets = buildSets(left, true);
		bSets = buildSets(right, false);
		left.addComponent(createSpacer());
		right.addComponent(createSpacer());
		isFA = new CheckBox("Default?");
		isFA.setImmediate(true);
		isFA.addValueChangeListener(e -> forfietA());
		left.addComponent(isFA);
		isFB = new CheckBox("Default?");
		isFB.setImmediate(true);
		isFB.addValueChangeListener(e -> forfietB());
		right.addComponent(isFB);
		//addComponent(createSpacer());
		HorizontalLayout h2 = new HorizontalLayout();
		h2.setSpacing(true);
		addComponent(h2);
		Label lbl = new Label("Played on: ");
		h2.addComponent(lbl);
		date = new DateField();
		date.setValue(new Date());
		date.setDateFormat("MM-dd-yyyy");
		h2.addComponent(date);
		h2.setComponentAlignment(lbl, Alignment.MIDDLE_LEFT);
		
		HorizontalLayout h1 = new HorizontalLayout();
		scores = new TextField("Scores:");
		scores.setStyleName("inline-label");
		h1.addComponent(scores);
		h1.setComponentAlignment(scores, Alignment.TOP_CENTER);
		warning = new Label();
		warning.addStyleName("bigredtext");
		h1.addComponent(warning);
		h1.setComponentAlignment(warning, Alignment.BOTTOM_RIGHT);
		addComponent(h1);
		
		comment = new TextArea("Comment on match (feel free to trash talk):");
		comment.setWidth("30em");
		addComponent(comment);

		if (getCurrentMatch().getRecordedBy() != null)
		{
			lbl = new Label("Match last updated by " + getCurrentMatch().getRecordedBy().firstLastName());
			lbl.setStyleName("tiny");
			addComponent(lbl);
		}
		
		locked = new CheckBox("Lock match so players cannot edit", getCurrentMatch().isLocked());
		locked.setStyleName("redtext");
		addComponent(locked);
		locked.setVisible(getLoginUser().isAdmin() || getLoginUser().isOrganizer(getCurrentMatch()));
		
		ok = new Button("Update Match");
		ok.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		ok.addClickListener(e -> update());
		addComponent(ok);

		loadCurrentMatch();
	}
	
	public void backClicked()
	{
		getHood().removeEditing(getCurrentMatch());
		getP2tennisUI().clearEditor();
		super.backClicked();
	}
	
	protected void update()
	{
		getHood().updateMatch(getCurrentMatch(), scoresA, scoresB, date.getValue(), isFA.getValue(), isFB.getValue(), comment.getValue(), locked.getValue(), getP2tennisUI().getLoginUser(), true); 
		backClicked();
	}
	
	
	protected void loadCurrentMatch()
	{
		if (getCurrentMatch() == null)
			return;
		Side a = getCurrentMatch().getSideA();
		Side b = getCurrentMatch().getSideB();
		isFA.setValue(a.isForfeit());
		isFB.setValue(b.isForfeit());
		setCheckBoxes(a, aSets);
		setCheckBoxes(b, bSets);
		if (getCurrentMatch().getPlayed() != null)
			date.setValue(getCurrentMatch().getPlayed());
		comment.setValue(getCurrentMatch().getComment());
	}
	
	protected void setCheckBoxes(Side s, ArrayList<ArrayList<CheckBox>> boxes)
	{
		setScoreBoxes(s.getSet1(), boxes.get(0));
		setScoreBoxes(s.getSet2(), boxes.get(1));
		setScoreBoxes(s.getSet3(), boxes.get(2));
	}
	
	protected void setScoreBoxes(int score, ArrayList<CheckBox> boxes)
	{
		int index = 7 - score;
		boxes.get(index).setValue(true);
	}
	
	protected void forfietA()
	{
		forfiet(!isFA.getValue(), !isFB.getValue(), aSets, bSets);
	}
	
	protected void forfietB()
	{
		forfiet(!isFB.getValue(), !isFA.getValue(), bSets, aSets);		
	}
	
	protected void forfiet(boolean f1, boolean f2, ArrayList<ArrayList<CheckBox>> boxes, ArrayList<ArrayList<CheckBox>> otherBoxes)
	{
		enableCheckBoxes(f1 && f2);
		int sa = f1 ? 6 : 0;
		int sb = f2 ? 6 : 0;
		setScoreBoxes(sa, boxes.get(0));
		setScoreBoxes(sa, boxes.get(1));
		setScoreBoxes(0, boxes.get(2));
		setScoreBoxes(sb, otherBoxes.get(0));
		setScoreBoxes(sb, otherBoxes.get(1));
		setScoreBoxes(0, otherBoxes.get(2));
		showScores();
	}
	
	protected void enableCheckBoxes(boolean e)
	{
		for (ArrayList<CheckBox> bs : aSets)
		{
			for (CheckBox b : bs)
			{
				b.setEnabled(e);
			}
		}
		for (ArrayList<CheckBox> bs : bSets)
		{
			for (CheckBox b : bs)
			{
				b.setEnabled(e);
			}
		}
	}

	protected ArrayList<ArrayList<CheckBox>> buildSets(VerticalLayout side, boolean needCaption)
	{
		ArrayList<ArrayList<CheckBox>> sets = new ArrayList<ArrayList<CheckBox>>();
		ArrayList<CheckBox> set1 = new ArrayList<CheckBox>();
		ArrayList<CheckBox> set2 = new ArrayList<CheckBox>();
		ArrayList<CheckBox> set3 = new ArrayList<CheckBox>();
		sets.add(set1);
		sets.add(set2);
		sets.add(set3);
		HorizontalLayout h = new HorizontalLayout();
		side.addComponent(h);
		for (String s : setIds)
		{
			VerticalLayout v = new VerticalLayout();
			v.addComponent(new Label(s));
			cb(needCaption && s.equals(ZERO), 1, v, set1);
			cb(needCaption && s.equals(ZERO), 2, v, set2);
			cb(needCaption && s.equals(ZERO), 3, v, set3);			
			h.addComponent(v);
		}
		return sets;
	}
	
	protected void cb(boolean caption, int set, VerticalLayout v, ArrayList<CheckBox> boxes)
	{
		CheckBox c = new CheckBox();
		c.setImmediate(true);
		if (caption)
			c.setCaption(SETA + set + SETB);
		c.addValueChangeListener(e -> boxChecked(c));
		v.addComponent(c);
		boxes.add(c);
	}
	
	protected void boxChecked(CheckBox b)
	{
		for (int i = 0; i < 3; i++)
		{
			scoresA[i] = boxChecked(b, aSets.get(i), scoresA[i]);
			scoresB[i] = boxChecked(b, bSets.get(i), scoresB[i]);
		}
		showScores();
	}
	
	protected void showScores()
	{
		if (isFA.getValue() || isFB.getValue())
		{
			String a = "Win";
			String b = "Win";
			if (isFA.getValue())
				a = "Default";
			if (isFB.getValue())
				b = "Default";
			scores.setValue(a + " - " + b);
			warning.setCaption("");
			ok.setEnabled(true);
			return;
		}
		StringBuilder wn = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		String sep = "";
		int winsa = 0;
		int winsb = 0;
		boolean valid = true;
		for (int i = 0; i < 3; i++)
		{
			String w = " Set" + (i+1) + " not valid.";
			int a = scoresA[i];
			int b = scoresB[i];
			if (a >=0 && b >= 0 && (a+b) > 0)
			{
				sb.append(sep + a + "-" + b + " ");
				sep = " / ";
				boolean good = false;
				if ((a == 7 && (b == 5 || b == 6)) || (a == 6 && b < 5))
				{
					good = true;
					winsa++;
				}
				if ((b == 7 && (a == 5 || a == 6)) || (b == 6 && a < 5))
				{
					good = true;
					winsb++;
				}
				if (!good)
				{
					valid = false;
					wn.append(w);
				}
			}
		}
		boolean enable = false;
		if (valid)
		{
			if (winsa >= 2)
			{
				wn.append(getSideA().getName() + " wins");
				enable = true;
			}
			else if (winsb >= 2)
			{
				wn.append(getSideB().getName() + " wins");
				enable = true;
			}
			else
				wn.append(" Not enough sets.");
		}
		ok.setEnabled(enable);
		scores.setValue(sb.toString());
		warning.setCaption(wn.toString());
	}
	
	protected int boxChecked(CheckBox cb, ArrayList<CheckBox> set, int score)
	{
		int index = set.indexOf(cb);
		int s = score;
		if (index >= 0)
		{
			s = 0;
			if (cb.getValue())
			{
				s = 7 - index;
				for (CheckBox b : set)
				{
					if (!cb.equals(b))
					{
						b.setValue(false);
					}
				}
			}
		}
		return s;
	}
	
	protected Side getSideA()
	{
		return getCurrentMatch().getSideA();
	}
	
	protected Side getSideB()
	{
		return getCurrentMatch().getSideB();
	}
	@Override
	public void enter(ViewChangeEvent event)
	{
		getP2tennisUI().setEditor(this);
	}

}
