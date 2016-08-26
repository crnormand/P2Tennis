package net.parsonsrun.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Team extends DomainObject 
{
	private static final long serialVersionUID = 1L;
	public static int STARTING_RANK = 2000;

	protected Player playerA;
	protected Player playerB;
	protected int rank = STARTING_RANK;
	protected HashMap<League, ArrayList<Match>> matches = new HashMap<League, ArrayList<Match>>();

	public String toString()
	{
		return getName() + "(" + getRank() + ")";
	}
	
	public String getName()
	{
		return getPlayerA().getLast() + "/" + getPlayerB().getLast();
	}
	
	public boolean canDelete()
	{
		Iterator<League> lgs = getMatches().keySet().iterator();
		while (lgs.hasNext())
		{
			League lg = lgs.next();
			if (!getMatches(lg).isEmpty())
				return false;
		}
		return true;
	}
	
	public boolean equals(Team t)
	{
		if (t.isBye())
			return false;
		if (this == t)
			return true;
		return getPlayerA().equals(t.getPlayerA()) && getPlayerB().equals(t.getPlayerB());
	}
	
	public String getFullName()
	{
		return getPlayerA().firstLastName() + "/" + getPlayerB().firstLastName();
	}

	public Player getPlayerA()
	{
		return playerA;
	}
	
	public Standing getStandingFor(League lg)
	{
		Standing s = new Standing();
		s.set(lg, this);
		return s;
	}
	public boolean isBye()
	{
		return false;
	}

	public String getRankDisplaySmall()
	{
		return "<small>" + getRankDisplay() + "</small>";
	}
	
	public String getRankDisplay()
	{
		return "(" + getRank() + ")";
	}
	public boolean includes(Player p)
	{
		return getPlayerA().equals(p) || getPlayerB().equals(p);
	}
	public void setPlayerA(Player playerA)
	{
		this.playerA = playerA;
		playerA.addTeam(this);
		checkOrder();
	}
	
	public void setPlayers(Player a, Player b)
	{
		playerA = a;
		playerB = b;
		checkOrder();
	}

	public Player getPlayerB()
	{
		return playerB;
	}

	public void setPlayerB(Player playerB)
	{
		this.playerB = playerB;
		playerB.addTeam(this);
		checkOrder();
	}
	
	public Team updatePlayers()
	{
		playerA.addTeam(this);
		playerB.addTeam(this);
		return this;
	}
	
	protected void checkOrder()
	{
		if (getPlayerA() != null && getPlayerB() != null)
		{
			String a = getPlayerA().lastFirstName();
			String b = getPlayerB().lastFirstName();
			if (a.compareToIgnoreCase(b) > 0)
			{
				Player t = getPlayerA();
				playerA = playerB;
				playerB = t;
			}
		}
	}

	public HashMap<League, ArrayList<Match>> getMatches()
	{
		return matches;
	}
	
	public synchronized ArrayList<Match> getMatches(League lg)
	{
		ArrayList<Match> m = matches.get(lg);
		if (m == null)
		{
			m = new ArrayList<Match>();
			matches.put(lg, m);
		}
		return m;
	}
	
	public ArrayList<Match> getVsMatches(League lg)
	{
		ArrayList<Match> vl = new ArrayList<Match>();
		for (Match m : getMatches(lg))
		{
			VsMatch v = new VsMatch();
			v.setMatch(m);
			v.setMe(m.getSide(this));
			vl.add(v);
		}
		return vl;
	}
	
	public ArrayList<Match> getUnplayedVsMatches(League lg)
	{
		ArrayList<Match> vl = new ArrayList<Match>();
		for (Match m : getMatches(lg))
		{
			if (!m.isBye() && !m.hasBeenPlayed())
			{
				VsMatch v = new VsMatch();
				v.setMatch(m);
				v.setMe(m.getSide(this));
				vl.add(v);
			}
		}
		return vl;
	}
	
	public void addMatch(League lg, Match m)
	{
		getMatches(lg).add(m);
	}

	public void setMatches(HashMap<League, ArrayList<Match>> matches)
	{
		this.matches = matches;
	}

	public int getRank()
	{
		return rank;
	}

	public void setRank(int rank)
	{
		this.rank = rank;
	}
}
