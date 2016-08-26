package net.parsonsrun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.vaadin.addon.charts.*;
import com.vaadin.addon.charts.model.*;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.TextArea;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Round;
import net.parsonsrun.domain.Team;

public class EloChartView extends BaseView
{
	TextArea comment;

	public EloChartView()
	{
		if (getCurrentLeague() == null || getCurrentLeague().getRounds() == null)
		{
			League l = new League();
			l.setName("Unknown");
			setCurrentLeague(l);
		}
		addBack();
		Chart chart = new Chart();
		addComponent(chart);
		chart.setHeight("450px");
	    chart.setWidth("100%");

	    Configuration configuration = chart.getConfiguration();
	    configuration.getChart().setType(ChartType.LINE);
	    configuration.getChart().setMarginRight(130);
	    configuration.getChart().setMarginBottom(25);

	    configuration.getTitle().setText("ELO scores for " + getCurrentLeague().getName());
	    //configuration.getSubTitle().setText("Source: WorldClimate.com");

	    int sz = getCurrentLeague().getRounds().size();
	    int min = 2000;
	    int max = 2000;
	    for (Round r : getCurrentLeague().getRounds())
	    {
	    	for (Match m : r.getMatches())
	    	{
	    		if (m.hasBeenPlayed())
	    		{
		    		int k = m.getSideA().getResultRank();
		    		if (k < min) min = k;
		    		if (k > max) max = k;
		    		k = m.getSideB().getResultRank();
		    		if (k < min) min = k;
		    		if (k > max) max = k;
	    		}
	    	}
	    }
	    max = ((max + 99) / 100) * 100;
	    min = (min / 100) * 100;
	    String cats[] = new String[sz + 1];
	    cats[0] = "Start";
	    for (int i = 1; i <= sz; i++)
	    {
	    	cats[i] = "Match #" + i;
	    }
	    configuration.getxAxis().setCategories(cats);

	    YAxis yAxis = configuration.getyAxis();
	    //System.out.println("Min:" + min + " max:" + max);
	    //yAxis.setMin(min);
	    //yAxis.setMax(max);
	    yAxis.setTitle(new AxisTitle("ELO Score"));
	    yAxis.getTitle().setAlign(VerticalAlign.MIDDLE);
        configuration.getTooltip().setFormatter("'<b>'+ this.series.name +'</b><br/>'+this.x +': '+ this.y");

	    PlotOptionsLine plotOptions = new PlotOptionsLine();
	    plotOptions.getDataLabels().setEnabled(true);
	    configuration.setPlotOptions(plotOptions);
        Legend legend = configuration.getLegend();
        legend.setLayout(LayoutDirection.VERTICAL);
	    legend.setAlign(HorizontalAlign.RIGHT);
	    legend.setVerticalAlign(VerticalAlign.TOP);
	    //legend.setX(-10d);
	    //legend.setY(100d);
	    legend.setBorderWidth(0);
	    addData(configuration);
	    chart.drawChart(configuration);
	    comment = new TextArea();
	    comment.setWidth("100%");
	    comment.setHeight("11em");
	    comment.setValue(comment());
	    comment.setReadOnly(true);
	    addComponent(comment);
	}
	
	public void resized()
	{
		getP2tennisUI().redrawView();
	}
	
	protected String comment()
	{
		return "ELO scores are calculated from the difference in each set score, and are scaled by the relative differences in ELO scores between the teams."
				+ "  If the teams have similar ELO scores, each set is worth approximately 10 points.   However, winning a set 6-0 is worth more"
				+ " (approximately 11 points) than winning a set 6-4 (approximately 9 points).   Winning a set 7-5 is the same as winning 6-4."
				+ "  Winning a two set match 6-4/6-4 is worth more (approximately 20 points) than winning a three set match 6-0/0-6/6-0 (approximate 10 points)."
				+ "  If the teams have different ELO scores, winning a set is worth more to the lower team, and worth less to the higher team (it isn't easy beating"
				+ " a \"better\" team, so the reward is greater).    The numbers mentioned are approximations of the underlying mathmatical formula,"
				+ " and are rounded to the nearest whole integer.";
	}
	
	protected void addData(Configuration conf)
	{
		HashMap<Team, ListSeries> map = new HashMap<Team, ListSeries>();
		for (Team t : getCurrentLeague().getTeams())
		{
			ListSeries ls = new ListSeries();
	        ls.setName(t.getName());
	        ls.addData(Team.STARTING_RANK);
	        map.put(t, ls);
	        conf.addSeries(ls);
		}
		ArrayList<Match> rev = new ArrayList<Match>();
		rev.addAll(getCurrentLeague().getPlayedMatches());
		Collections.reverse(rev);
		for (Match m : rev)
		{
			Team a = m.getSideA().getTeam();
			ListSeries ls = map.get(a);
			ls.addData(m.getSideA().getResultRank(), false, false);
			Team b = m.getSideB().getTeam();
			ls = map.get(b);
			ls.addData(m.getSideB().getResultRank(), false, false);
		}
	}
	
	@Override
	public void enter(ViewChangeEvent event)
	{
		// TODO Auto-generated method stub

	}

}
