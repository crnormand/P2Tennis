package net.parsonsrun;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.renderers.HtmlRenderer;

import net.parsonsrun.ScheduleView.Row;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Standing;
import net.parsonsrun.domain.VsMatch;

public class MatchesView2 extends MatchesView
{
	
	public MatchesView2()
	{
		super();
	}
	

	protected String prefix()
	{
		return "All";
	}

	
	public void addBack()
	{
		
	}
}
