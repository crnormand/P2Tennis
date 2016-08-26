package net.parsonsrun.domain;

import java.util.ArrayList;
import java.util.Iterator;

public class Side extends DomainObject
{
	private static final long serialVersionUID = 1L;

	public static final int BEST_OF = 3;
	public static final int NEEDED = (BEST_OF / 2) + 1;
	public static int [] EloDifferences = { 0, 20, 40, 60, 80, 100, 120, 140, 160, 180, 200, 300, 400 };
	public static double [] ExpectedScores = { 0.5, 0.53, 0.58, 0.62, 0.66, 0.69, 0.73, 0.76, 0.79, 0.82, 0.84, 0.93, 0.97 };
	
	protected Team team;
	protected boolean forfiet;
	protected boolean winner;
	protected int set1;
	protected int set2;
	protected int set3;
	protected int set4;
	protected int set5;
	protected int numberOfSets;
	
	protected int previousRank;
	protected int resultRank;
	
	
	public void resetScore()
	{
		set1 = set2 = set3 = set4 = set5 = 0;
		forfiet = false;
		winner = false;
		previousRank = 0;
		resultRank = 0;
	}
	@Override
	public String toString()
	{
		return getTeam().toString();
	}
	
	public String getName()
	{
		return getTeam().getName();
	}
	
	public String getFullName()
	{
		return getTeam().getFullName();
	}

	
	public String getSideName(boolean showElo)
	{
		if (getPreviousRank() > 0 && showElo)
			return getTeam().getFullName() + " (" + getPreviousRank() + ")";
		else
			return getTeam().getFullName();
	}
	public String getRankChange()
	{
		//return "(" + getPreviousRank() + "->" + getResultRank() + ")";
		return getPreviousRank() + "->" + getResultRank();
	}
	
	public String getRankDiff()
	{
		int i = getResultRank() - getPreviousRank();
		return (i < 0) ? "" + i : "+" + i;
	}
	
	public boolean isBye()
	{
		return getTeam().isBye();
	}
	
	public int getGamesWonAgainst(Side other)
	{
		int total = 0;
		if (isForfeit())
			return 0;
		if (other.isForfeit())
			return 12;  // magic 6-0/6-0 win
		for (int i = 0; i < numberOfSets; i++)
			total += getSetScore(i);
		return total;
	}
	
	public int getSetsWonAgainst(Side other)
	{
		int total = 0;
		if (isForfeit())
			return 0;
		if (other.isForfeit())
			return 2;  // magic 6-0/6-0 score
		for (int i = 0; i < numberOfSets; i++)
			if (getSetScore(i) > other.getSetScore(i))
				total += 1;
		return total;
	
	}
	
//  http://gobase.org/studying/articles/elo/
	public int determineWinner(Side other)
	{
		int w1 = 0;
		int w2 = 0;
		if (isForfeit() || other.isForfeit())
		{
			setPreviousRank(getTeam().getRank());
			setResultRank(getTeam().getRank());
			other.setPreviousRank(other.getTeam().getRank());
			other.setResultRank(other.getTeam().getRank());
			setWinner(other.isForfeit() && !isForfeit());
			other.setWinner(!other.isForfeit() && isForfeit());
		}
		else
		{
			int i = 0;
			int r1 = getTeam().getRank();
			setPreviousRank(r1);
			int r2 = other.getTeam().getRank();
			other.setPreviousRank(r2);
			boolean done = false;
			while (! done)
			{
				double score = expectedScore(Math.abs(r1 - r2));
				double diff1 = score;
				double diff2 = score;
				if (r1 > r2)
					diff1 = 1.0 - score;
				else
					diff2 = 1.0 - score;
				int s1 = getSetScore(i);
				int s2 = other.getSetScore(i);
				//double k = 15.0 + (Math.abs(s1 - s2) / 2.0);   //  6 - 2
				//double k = 12.0 + Math.abs(s1 - s2);
				//double k = 24.0 + Math.abs(s1 - s2);
				double k = 20.0 + (Math.abs(s1 - s2) / 2.0);
				//double k = 20.0 + Math.abs(s1 - s2);
				if (s1 > s2)
				{
					int d = (int)(k * diff1);
					r1 = r1 + d;
					r2 = r2 - d;
					if (++w1 >= NEEDED)
						done = true;
				}
				else
				{
					int d = (int)(k * diff2);
					r1 = r1 - d;
					r2 = r2 + d;
					if (++w2 >= NEEDED)
						done = true;
				}
				i++;
				//System.out.println(getTeam().getName() + " s1:" + s1 + " s2:" + s2 + " diff1:" + diff1 + " diff2:" + diff2 + " r1:" + r1 + " r2:" + r2 + " " + other.getTeam().getName());
			}
			setResultRank(r1);
			setWinner(w1 >= NEEDED);
			other.setResultRank(r2);
			other.setWinner(w2 >= NEEDED);
		}
		numberOfSets = w1 + w2;
		other.numberOfSets = w1 + w2;
		return numberOfSets;
	}
	
	protected double expectedScore(int diff)
	{
		int len = EloDifferences.length;
		double score = 0.50;
		for (int i = 0; i < len; i++)
		{
			if (diff < EloDifferences[i])
				return score;
			score = ExpectedScores[i];
		}
		return score;
	}

	
	public String getScoresString(Side other, boolean addSpaces)
	{
		StringBuilder sb = new StringBuilder();
		if (isForfeit())
		{
			if (other.isForfeit())
				return "Default/Default";
			return "Default/Win";
		}
		if (other.isForfeit())
			return "Win/Default";

		String split = "";
		int i = 0;
		int w1 = 0;
		int w2 = 0;
		boolean done = false;
		while (! done)
		{
			int s1 = getSetScore(i);
			int s2 = other.getSetScore(i);
			if (s1 > s2)
			{
				if (++w1 == NEEDED)
					done = true;
			}
			else
				if (++w2 == NEEDED)
					done = true;

			sb.append(split);
			sb.append(s1);
			sb.append('-');
			sb.append(s2);
			if (addSpaces)
				split = " / ";
			else
				split = "/";
			i++;
		}
		return sb.toString();
	}
	
	public String getScoresString(Side other)
	{
		return getScoresString(other, false);
	}
	
	protected boolean isWinner(Side other)
	{
		int needed = (BEST_OF / 2) + 1;
		for (int i = 0; i < BEST_OF; i++)
		{
			if (getSetScore(i) > other.getSetScore(i))
				needed--;
		}
		return needed <= 0;
	}
	
	protected int parseSet(String s)
	{
		if (s == null || s.isEmpty())
			return 0;
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
		}
		return 0;
	}
	public boolean includes(Player p)
	{
		return getTeam().includes(p);
	}
	
	public boolean includes(Team t)
	{
		return getTeam().equals(t);
	}
	
	public boolean isWinner(Team t)
	{
		return isWinner() && getTeam().equals(t);
	}
	
	public boolean isForfeit(Team t)
	{
		return isForfeit() && getTeam().equals(t);
	}
	public int getSetScore(int i)
	{
		switch (i)
		{
			case 0: return getSet1(); 
			case 1: return getSet2(); 
			case 2: return getSet3(); 
			case 3: return getSet4(); 
			case 4: return getSet5(); 
		}
		return 0;
	}
	public Team getTeam()
	{
		return team;
	}

	public void setTeam(Team team)
	{
		this.team = team;
	}

	public boolean isForfeit()
	{
		return forfiet;
	}

	public void setForfiet(boolean forfiet)
	{
		this.forfiet = forfiet;
	}

	public int getPreviousRank()
	{
		return previousRank;
	}

	public void setPreviousRank(int previousRank)
	{
		this.previousRank = previousRank;
	}

	public int getResultRank()
	{
		return resultRank;
	}
	
	public String getRankDisplay()
	{
		return getTeam().getRankDisplaySmall();
	}

	public void setResultRank(int resultRank)
	{
		this.resultRank = resultRank;
		getTeam().setRank(resultRank);
	}

	public int getSet1()
	{
		return set1;
	}

	public void setSet1(int set1)
	{
		this.set1 = set1;
	}

	public int getSet2()
	{
		return set2;
	}

	public void setSet2(int set2)
	{
		this.set2 = set2;
	}

	public int getSet3()
	{
		return set3;
	}

	public void setSet3(int set3)
	{
		this.set3 = set3;
	}

	public int getSet4()
	{
		return set4;
	}

	public void setSet4(int set4)
	{
		this.set4 = set4;
	}

	public int getSet5()
	{
		return set5;
	}

	public void setSet5(int set5)
	{
		this.set5 = set5;
	}

	public boolean isWinner()
	{
		return winner;
	}

	public void setWinner(boolean winner)
	{
		this.winner = winner;
	}
}
