package net.parsonsrun.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VsMatch extends Match
{
	protected Match match;
	protected Side me;
	public Match getMatch()
	{
		return match;
	}
	
	public String getComment()
	{
		return match.getComment();
	}
	
	public String getRecordedByFirstLastName()
	{
		return match.getRecordedByFirstLastName();
	}
	public void setMatch(Match match)
	{
		this.match = match;
	}
	public Side getMe()
	{
		return me;
	}
	public void setPlayed(Date d)
	{
		getMatch().setPlayed(d);
	}
	public void setMe(Side me)
	{
		this.me = me;
	}
	
	public boolean hasBeenPlayed()
	{
		return getMatch().hasBeenPlayed();
	}
	public Round getRound()
	{
		return getMatch().getRound();
	}
	public String getDescription()
	{
		StringBuilder sb = new StringBuilder();
		if (getMatch().hasBeenPlayed())
		{
			if (getMe().isWinner())
				sb.append("W:");
			else
				sb.append("L:");
			sb.append('(');
			sb.append(getMe().getRankChange());
			sb.append(')');
			sb.append(" ");

			sb.append(getMe().getScoresString(getOtherSide()));
		}
		else
			sb.append("vs.");
		sb.append(' ');
		sb.append(getOtherSide().getSideName(showElo()));
		return sb.toString();
	}
	
	public String getHtml()
	{
		String br = "<br>";
		StringBuilder sb = new StringBuilder();
		String w = getMatch().getRound().getName();
		if (getMatch().hasBeenPlayed())
		{
			SimpleDateFormat f = new SimpleDateFormat("MM/dd");
			w = w + " (Played on " + f.format(getMatch().getPlayed()) + ")";
		}
		sb.append("<div");
		if (getMatch().hasBeenPlayed())
		{
			if (getMe().isWinner())
			{
				sb.append(" style='color:#006600;width: 100%'><b>");
				sb.append(w);
				sb.append(br);
				sb.append("Win!");
			}
			else
			{
				sb.append(" style='color:#660000;width: 100%'><b>");
				sb.append(w);
				sb.append(br);
				if (getMatch().isForfiet())
					sb.append("Default");
				else
					sb.append("Loss");
			}
			sb.append(br);
			sb.append("vs");
			sb.append(br);
			sb.append(getOtherSide().getSideName(showElo()));
			sb.append(br);
			sb.append(getMe().getScoresString(getOtherSide(), true));
			sb.append(br);
			if (!getMatch().isForfiet())
			{
				sb.append("New Ranking: ");
				sb.append(getMe().getRankChange());
				sb.append(" [");
				sb.append(getMe().getRankDiff());
				sb.append("]");
			}
			sb.append("</b>");
		}
		else
		{
			sb.append(">");
			sb.append(w);
			sb.append(br);
			if (!getMatch().isBye())
			{
				sb.append("Pending");
				sb.append(br);
				sb.append("vs.");
			}
			else
				sb.append(br);
			sb.append(br);
			sb.append(getOtherSide().getSideName(showElo()));
			sb.append(br);
			sb.append(getOtherSide().getRankDisplay());
		}
		sb.append("</div>");
		
		return sb.toString();
	}
	
	public String getHtml2()
	{
		StringBuilder s = new StringBuilder();
		s.append("<table style='width: 100%' border=1>");
		s.append("<tr>");
		s.append("<td style='width:110px'>");
		s.append(getRound().getHtml());
		s.append("</td>");
		s.append("<td align='center'>");
		s.append(getMatch().getHtml(true));
		s.append("</td>");
		s.append("</tr>");
		s.append("</table>");
		return s.toString();
	}

	public Side getOtherSide()
	{
		if (getMatch().getSideA().equals(getMe()))
			return getMatch().getSideB();
		else
			return getMatch().getSideA();
	}
}
