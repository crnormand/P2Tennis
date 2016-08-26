package net.parsonsrun.domain;

import java.util.ArrayList;

public class Standing implements Comparable<Standing>
{
	protected League league;
	protected Team team;
	protected int played;
	protected int wins;
	protected int loses;
	protected int total;
	protected int winnerSets;
	protected int winnerGames;
	protected int forfeits;
	
	public void set(League lg, Team t)
	{
		setLeague(lg);
		setTeam(t);
		calculate();
	}
	
	// position, Name, wins loses winSetsGames, win %
	public String getHtml(int position)
	{
		StringBuilder s = new StringBuilder();
		s.append("<tr>");
		s.append("<td align='center'>");
		s.append(position);
		s.append("</td>");		
		s.append("<td>");
		s.append(getTeam().getFullName());
		s.append("</td>");
		s.append("<td align='center'>");
		s.append(getWinLose());
		s.append("</td>");
		s.append("<td align='center'>");
		s.append(getWinPerc());
		s.append("</td>");
		s.append("<td align='center'>");
		s.append(getPlayed());
		s.append("</td>");
		s.append("</tr>");
		return s.toString();
	}
	
	public int compareTo(Standing other)
	{
		if (getWins() == other.getWins())
		{
			if (getWinPercentage() == other.getWinPercentage())
			{
				if (!wonHeadToHeadAgainst(other))
				{
					if (getWinnerSets() == other.getWinnerSets())
					{
						if (getWinnerGames() == other.getWinnerGames())
						{
							return getTeam().getName().compareTo(other.getTeam().getName());
						}
						else return getWinnerGames() > other.getWinnerGames() ? -1 : 1;
					}
					else
						return getWinnerSets() > other.getWinnerSets() ? -1 : 1;
				}
				else 
					return -1;	// Won head to head matchup
			}
			else return getWinPercentage() > other.getWinPercentage() ? -1 : 1;
		}
		else
			return getWins() > other.getWins() ? -1 : 1;
	}
	
	protected boolean wonHeadToHeadAgainst(Standing other)
	{
		for (Match m : getMatches())
		{
			if (m.hasBeenPlayed() && m.includes(other.getTeam()) && m.isWinner(getTeam()))
				return true;
		}
		return false;
	}
	
	protected double f(int n, int t)
	{
		return ((double)n) / ((double)t);
	}
	
	public String getElo()
	{
		return "" + getTeam().getRank();
	}
	
	protected int getWinnerSets()
	{
		return winnerSets;
	}
	
	
	
	protected void calculate()
	{
		for (Match m : getMatches())
		{
			total++;
			if (m.hasBeenPlayed())
			{
				played++;
				if (m.isWinner(getTeam()))
				{
					wins++;
				}
				else
				{
					loses++;
					if (m.isForfiet(getTeam()))
						forfeits++;
				}
				winnerSets += m.getSetsWon(getTeam());
				winnerGames += m.getGamesWon(getTeam());
			}
		}
	}
	
	public ArrayList<Match> getMatches()
	{
		return getTeam().getMatches(getLeague());
	}
	
	public ArrayList<Match> getVsMatches()
	{
		return getTeam().getVsMatches(getLeague());
	}
	
	public String getName()
	{
		return getTeam().getFullName();
	}
	
	public int getRank()
	{
		return getTeam().getRank();
	}
	
	public int getStartingRank()
	{
		return getLeague().getStartingRank(getTeam());
	}

	public League getLeague()
	{
		return league;
	}

	public void setLeague(League league)
	{
		this.league = league;
	}

	public Team getTeam()
	{
		return team;
	}

	public void setTeam(Team team)
	{
		this.team = team;
	}

	public int getWins()
	{
		return wins;
	}

	public void setWins(int wins)
	{
		this.wins = wins;
	}

	public int getLoses()
	{
		return loses;
	}
	
	public String getWinLose()
	{
		return getWins() + "-" + getLoses() + " [" + getWinnerSets() + ":" + getWinnerGames() + "]";
	}
	
	public String getWinPerc()
	{
		if (getPlayed() == 0)
			return "n/a";
		return String.format("%.1f", getWinPercentage());
	}
	
	public double getWinPercentage()
	{
		if (getPlayed() == 0)
			return 0.0;
		return ((double)getWins()) / ((double)getPlayed()) * 100.0;
	}
	
	public String getRankChange()
	{
		return getStartingRank() + " -> " + getRank();
	}

	public void setLoses(int loses)
	{
		this.loses = loses;
	}
	
	public String getStanding()
	{
		return getTeam().getName() + " W:" + wins + " L:" + loses + " R:" + getTeam().getRank();
	}

	public int getPlayed()
	{
		return played;
	}

	public void setPlayed(int played)
	{
		this.played = played;
	}

	public int getTotal()
	{
		return total;
	}

	public void setTotal(int total)
	{
		this.total = total;
	}

	public int getWinnerGames()
	{
		return winnerGames;
	}

	public void setWinnerGames(int wg)
	{
		this.winnerGames = wg;
	}

	public void setWinnerSets(int winnerSets)
	{
		this.winnerSets = winnerSets;
	}

}
