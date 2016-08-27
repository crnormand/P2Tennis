package net.parsonsrun.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Round extends NamedDomainObject
{
	private static final long serialVersionUID = 1L;

	protected ArrayList<Match> matches = new ArrayList();
	protected League league;

	public League getLeague()
	{
		return league;
	}
	
	public String getHtml()
	{
		return getToString("<br>");
	}
	
	public String getToString(String br)
	{
		int i = getLeague().getRounds().indexOf(this);
		return "Round #" + (i + 1) + br + getName();
	}
	
	public String toString()
	{
		return getToString(" ");
	}

	public void setLeague(League league)
	{
		this.league = league;
	}
	
	public boolean isClosed()
	{
		return getLeague().isClosed();
	}
	
	public boolean isRoundFor(Date d)
	{
		SimpleDateFormat fm = new SimpleDateFormat("MM");
		SimpleDateFormat fd = new SimpleDateFormat("dd");
		int mon = Integer.parseInt(fm.format(d));
		int day = Integer.parseInt(fd.format(d));
		String[] rn = getName().split("-");
		String[] start = rn[0].split("/");
		String[] end = rn[1].split("/");
		int startm = Integer.parseInt(start[0]);
		int startd = Integer.parseInt(start[1]);
		int endm = Integer.parseInt(end[0]);
		int endd = Integer.parseInt(end[1]);
		if (startm == mon && endm == mon)
			return (startd <= day && endd >= day);
		else
			return (startm == mon && startd <= day) || (endm == mon && endd >= day);
	}

	public ArrayList<Match> getMatches()
	{
		return matches;
	}
	
	public Match getMatch(int i)
	{
		return getMatches().get(i);
	}

	public void setMatches(ArrayList<Match> matches)
	{
		this.matches = matches;
	}
	
	public Neighborhood getHood()
	{
		return getLeague().getHood();
	}
	
	public void addMatch(Match m)
	{
		getMatches().add(m);
		m.setRound(this);
	}
	
	public void sendUnplayedMatchReminders(String title)
	{
		for (Match m : getMatches())
		{
			if (!m.hasBeenPlayed() && !m.isBye() && !m.isLocked())
			{
				String htmlMsg = "<b>" + title + "</b><br><br>The following match has not been played:<br><br> <div style=\"margin-left:30px;\">" + getHtml() + "<br><br>" + m.getHtml(true, false) + "</div><br>You have until Sunday evening at midnight to enter your scores.";
				getHood().sendMatchEmail(m, "P2 Match Reminder: " + m, htmlMsg);
			}
		}
	}
	
	public void closeOutWeek(Date d)
	{
		for (Match m : getMatches())
		{
			if (!m.isBye())
			{
				if (!m.hasBeenPlayed())
				{
					getHood().updateMatch(m, null, null, d, true, true, "Automatic default", true, Player.SYSTEM, false);
				}
				else
					m.setLocked(true);
			}
		}
		
	}
}
