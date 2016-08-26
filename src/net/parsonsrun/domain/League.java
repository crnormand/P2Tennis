package net.parsonsrun.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import net.parsonsrun.P2tennisv2UI;

public class League extends NamedDomainObject
{
	private static final long serialVersionUID = 1L;

	protected Neighborhood hood;
	protected ArrayList<Team> teams = new ArrayList();
	protected ArrayList<Round> rounds = new ArrayList();
	protected ArrayList<Match> playedMatches = new ArrayList();
	protected HashMap<Team, Integer> startingRanks = new HashMap<Team, Integer>();
	protected ArrayList<Player> organizers = new ArrayList<Player>();
	protected boolean closed = false;

	public ArrayList<Team> getTeams()
	{
		return teams;
	}
	
	public boolean canDelete()
	{
		return playedMatches.isEmpty();
	}
	
	public void closeOutWeek(Date d)
	{
		Round current = null;
		for (Round r : getRounds())
		{
			if (r.isRoundFor(d))
			{
				r.closeOutWeek(d);
				current = r;
			}
		}
		boolean first = true;		
		StringBuilder sb = new StringBuilder();
		sb.append("Current standings:");
		sb.append("<br>");
		sb.append("<table style='width: 100%' border=1>");
		sb.append("<tr><td align='center'>Position</td><td>Team Name</td><td align='center'>W-L [SW:GW]</td><td align='center'>Win %</td><td align='center'>Played</td></tr>");
		int position = 0;
		for (Standing s : getStandings())
		{
			sb.append(s.getHtml(++position));
		}
		sb.append("</table><br>W-L [SW:GW] = Matches Won-Lost [Sets Won:Games Won]<br><br>");
		sb.append("<b>Recently scored matches:</b><hr>");
		for (Match m : getPlayedMatches())
		{
			if (m.hasBeenPlayedLastWeek(d))
			{
				if (!first)
					sb.append("<hr>");
				first = false;
				sb.append(m.getFullHtml());
			}
		}
		for (Team t : getTeams())
		{
			ArrayList<Match> unplayed = t.getUnplayedVsMatches(this);
			
			ArrayList<String> to = new ArrayList<String>();
			to.add(t.getPlayerA().getEmail());
			to.add(t.getPlayerB().getEmail());
			P2tennisv2UI.sendEmail(to, "Weekly update for " + getName() + " " + current, sb.toString());
		}
	}
	
	public void resetScores()
	{
		synchronized (getHood())
		{
			for (Match m : getPlayedMatches())
			{
				m.resetScore();
			}
			playedMatches.clear();
			Iterator<Team> i = getStartingRanks().keySet().iterator();
			while (i.hasNext())
			{
				Team t = i.next();
				t.setRank(getStartingRank(t));
			}
		}
	}
	
	public ArrayList<Standing> getStandings()
	{
		ArrayList<Standing> standings = new ArrayList<Standing>();
		for (Team t : getTeams())
		{
			standings.add(t.getStandingFor(this));
		}
		Collections.sort(standings);
		return standings;
	}
	public void setTeams(ArrayList<Team> teams)
	{
		this.teams = teams;
	}
	public ArrayList<Round> getRounds()
	{
		return rounds;
	}
	
	public Round getRound(int i)
	{
		return getRounds().get(i);
	}
	
	public int getStartingRank(Team t)
	{
		Integer i = getStartingRanks().get(t);
		return (i == null) ? 0 : i.intValue();			
	}
	
	public String toString()
	{
		return getName();
	}
	
	public Team getTeam(Player p)
	{
		for (Team t : getTeams())
		{
			if (t.includes(p))
				return t;
		}
		return null;
	}
	
	public Team getTeam(String s)
	{
		for (Team t : getTeams())
		{
			if (t.getName().equalsIgnoreCase(s))
				return t;
		}
		int i = 0;
		try
		{
			i = Integer.parseInt(s);
		} catch (NumberFormatException e) {}
		
		if (i > 0)
		{
			return getTeams().get(i - 1);
		}
		ByeTeam b = new ByeTeam();
		if (b.getName().equalsIgnoreCase(s))
			return b;
		return null;
	}
	
	public void addTeam(Team t)
	{
		getTeams().add(t);
		getStartingRanks().put(t, t.getRank());
	}
	public void setRounds(ArrayList<Round> rounds)
	{
		this.rounds = rounds;
	}
	public ArrayList<Match> getPlayedMatches()
	{
		return playedMatches;
	}
	
	public void addMatch(Match m)
	{
		getPlayedMatches().add(0, m);
	}
	public void setPlayedMatches(ArrayList<Match> playedMatches)
	{
		this.playedMatches = playedMatches;
	}
	public void buildRounds(Date startDate)
	{
		ArrayList<Team> tms = (ArrayList<Team>)getTeams().clone();
		if (tms.size() % 2 != 0)
		{
			tms.add(new ByeTeam());
		}
		int round = 1;
		while (round < tms.size())
		{
			createRound(round++, tms, startDate);
			shuffleTeams(tms);
		};
	}
	
	/*
	 * Following algorithm from:  https://en.wikipedia.org/wiki/Round-robin_tournament
	 * Leave team @ 0 in place, and move all other teams around.
	 */
	protected void shuffleTeams(ArrayList<Team> tms)
	{
		int lastIndex = tms.size() - 1;
		Team last = tms.get(lastIndex);
		for (int i = lastIndex; i > 1; i--)
		{
			tms.set(i, tms.get(i - 1));
		}
		tms.set(1, last);
	}
	protected void createRound(int r, ArrayList<Team> tms, Date startDate)
	{
		long time = startDate.getTime();
		long week = 86400 * 7 * 1000;
		int offset =  r - 1;
		SimpleDateFormat format = new SimpleDateFormat("M/dd");
		Date d = new Date(time + (week * offset));
		if (tms.size() % 2 != 0)
			throw new RuntimeException("Must have an even number of teams... Include a ByeTeam if necessary");
		
		Round round = new Round();
		round.setName("Week of " + format.format(d));
		addRound(round);
		int count = tms.size() / 2;
		int t1 = 0;
		int t2 = tms.size() - 1;
		while (count-- > 0)
		{
			Team teamA = tms.get(t1++);
			Team teamB = tms.get(t2--);
			Match m = new Match();
			m.setTeamA(teamA);
			m.setTeamB(teamB);
			round.addMatch(m);
		}
	}
	
	public void addRound(Round r)
	{
		getRounds().add(r);
		r.setLeague(this);
	}
	public Neighborhood getHood()
	{
		return hood;
	}
	
	public void sendUnplayedMatchReminders(String title)
	{
		Date d = new Date();
		for (Round r : getRounds())
		{
			if (r.isRoundFor(d))
				r.sendUnplayedMatchReminders(title);
		}
	}
	
	public boolean isOpen()
	{
		return !isClosed();
	}
	
	public void setOpen(boolean b)
	{
		setClosed(!b);
	}
	
	public void recalculate()
	{
		Iterator<Team> i = getStartingRanks().keySet().iterator();
		while (i.hasNext())
		{
			Team t = i.next();
			t.setRank(getStartingRank(t));
		}	
		ArrayList<Match> rev = new ArrayList<Match>();
		rev.addAll(getPlayedMatches());
		Collections.reverse(rev);
		for (Match m :rev)
		{
			m.determineWinner();
		}
	}
	public void setHood(Neighborhood hood)
	{
		this.hood = hood;
	}

	public HashMap<Team, Integer> getStartingRanks()
	{
		return startingRanks;
	}

	public void setStartingRanks(HashMap<Team, Integer> startingRanks)
	{
		this.startingRanks = startingRanks;
	}

	public ArrayList<Player> getOrganizers()
	{
		return organizers;
	}
	
	public String getOrganizerNames()
	{
		StringBuilder sb = new StringBuilder();
		boolean q = false;
		for (Player p : getOrganizers())
		{
			q = true;
			sb.append(p.firstLastName());
			sb.append(", ");
		}
		if (q)
			sb.setLength(sb.length() - 2);
		else
			sb.append("n/a");
		return sb.toString();
	}

	
	
	
	public boolean isOrganizer(Player p)
	{
		return getOrganizers().contains(p);
	}

	public void setOrganizers(ArrayList<Player> organizers)
	{
		this.organizers = organizers;
	}
	
	public void addOrganizer(Player p)
	{
		organizers.add(p);
	}

	public boolean isClosed()
	{
		return closed;
	}

	public void setClosed(boolean closed)
	{
		this.closed = closed;
	}
}
