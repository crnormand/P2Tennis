package net.parsonsrun.domain;

public class ByeTeam extends Team
{
	private static final long serialVersionUID = 1L;

	public String toString()
	{
		return "BYE";
	}
	
	public String getName()
	{
		return "BYE";
	}
	
	public String getFullName()
	{
		return getName();
	}
	
	public String firstLastName()
	{
		return getName();
	}
	public boolean isBye()
	{
		return true;
	}
	
	public String getRankDisplay()
	{
		return "";
	}
	public boolean includes(Player p)
	{
		return false; 
	}
	
	public boolean equals(Team t)
	{
		return false;
	}
}
