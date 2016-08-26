package net.parsonsrun.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.vaadin.ui.UI;

import net.parsonsrun.P2tennisv2UI;

public class Match extends DomainObject 
{
	private static final long serialVersionUID = 1L;

	protected Side sideA;
	protected Side sideB;
	protected Date played;
	protected Round round;
	protected int setsPlayed;
	protected String comment = "";
	protected Player recordedBy;
	protected boolean locked = false;
	
	public Round getRound()
	{
		return round;
	}
	
	public boolean hasComment()
	{
		return comment != null && !comment.isEmpty();
	}
	
	public boolean isLocked()
	{
		return locked;
	}
	
	public void setLocked(boolean b)
	{
		locked = b;
	}
	
	public void resetScore()
	{
		setPlayed(null);
		getSideA().resetScore();
		getSideB().resetScore();
		setComment("");
		setRecordedBy(null);
	}

	public boolean isClosed()
	{
		return getRound().isClosed();
	}

	public void setRound(Round round)
	{
		this.round = round;
		getSideA().getTeam().addMatch(round.getLeague(), this);
		getSideB().getTeam().addMatch(round.getLeague(), this);
	}

	@Override
	public String toString()
	{
		return getDescription(false);
	}
	
	public boolean isForfiet()
	{
		return getSideA().isForfeit() || getSideB().isForfeit();
	}
	
	public boolean isForfiet(Team t)
	{
		return getSideA().isForfeit(t) || getSideB().isForfeit(t);
	}
	
	public boolean isBye()
	{
		return getSideA().isBye() || getSideB().isBye();
	}
	public String getHtml()
	{
		return getHtml(false);
	}
	
	public String getHtml(boolean fullname)
	{
		return getHtml(fullname, showElo());
	}
	
	
	
	public String getCommentHtml(int width)
	{
		String d = "<div>";
		if (width > 0)
			d = "<div style=\"width:" + width + "px;word-wrap:normal;\">";
		if (!getComment().isEmpty())
			return d + getComment().replaceAll("\n", "<br>") + "<br>-------------------<br><div style=\"font-size: xx-small;\">Recorded by " + getRecordedByFirstLastName() + "</div></div>";
		else
			return "";
	}
	
	public String getCommentHtml()
	{
		return getCommentHtml(0);
	}
	
	public String getHtml2()
	{
		return getHtml();
	}
	
	public String getOldHtml(boolean full, boolean showElo)
	{
		Side sideA = getSideA();
		Side sideB = getSideB();
		if (hasBeenPlayed() && getSideB().isWinner())
		{
			sideA = getSideB();
			sideB = getSideA();
		}
		String w1s = "";
		String w2s = "";
		String we = "";
		String br = "<br>";
		String vs = "vs";
		String r1 = sideA.getRankDisplay();
		String r2 = sideB.getRankDisplay();
		if (isBye())
		{
			r1 = "-";
			r2 = "-";
		}
		StringBuilder sb = new StringBuilder();
		if (getP2tennisUI() != null && includes(getP2tennisUI().getLoginUser()))
			sb.append("<div style='background-color:#ccffff'>"); 
		else
			sb.append("<div>");
		if (hasBeenPlayed())
		{
			we = "</div></b>";
			vs = "<span style='font-weight:normal;color:#000000'>" + sideA.getScoresString(sideB, true) + "</span>";
			r1 = "<small>(" + sideA.getRankChange() + ") [" + sideA.getRankDiff() + "]</small>";
			r2 = "<small>(" + sideB.getRankChange() + ") [" + sideB.getRankDiff() + "]</small>";
			if (sideA.isWinner())
			{
				w1s = "<b><div style='color:#006600'>W: ";
			}
			else
			{
				if (sideA.isForfeit())
					w1s = "<b><div style='color:#660000'>F: ";
				else
					w1s = "<b><div style='color:#660000'>L: ";
			}
			if (sideB.isWinner())
			{
				w2s = "<b><div style='color:#006600'>W: ";
			}
			else
			{
				if (sideB.isForfeit())
					w2s = "<b><div style='color:#660000'>F: ";
				else
					w2s = "<b><div style='color:#660000'>L: ";
			}
		}
		sb.append(w1s);
		sb.append(full ? sideA.getTeam().getFullName() : sideA.getName());
		if (showElo)
		{
			sb.append(br);
			sb.append(r1);
		}
		sb.append(we);
		sb.append("<div>");
		sb.append(vs);
		sb.append("</div>");
		sb.append(w2s);
		sb.append(full ? sideB.getTeam().getFullName() : sideB.getName());
		if (showElo)
		{
			sb.append(br);
			sb.append(r2);
		}
		sb.append(we);
		sb.append("</div>");
		return sb.toString();
	}
	
	public String getHtml(boolean full, boolean showElo)
	{
		Side top = getSideA();
		Side bot = getSideB();
		if (hasBeenPlayed() && bot.isWinner())
		{
			top = getSideB();
			bot = getSideA();
		}
		String w1s = "";
		String w2s = "";
		String we = "";
		String br = "<br>";
		String vs = "vs";
		String r1 = top.getRankDisplay();
		String r2 = bot.getRankDisplay();
		if (isBye())
		{
			r1 = "-";
			r2 = "-";
		}
		StringBuilder sb = new StringBuilder();
		if (getP2tennisUI() != null && includes(getP2tennisUI().getLoginUser()))
			sb.append("<div style='background-color:#ccffff'>"); 
		else
			sb.append("<div>");
		if (hasBeenPlayed())
		{
			we = "</div></b>";
			vs = "<span style='font-weight:normal;color:#000000'>" + top.getScoresString(bot, true) + "</span>";
			r1 = "<small>(" + top.getRankChange() + ") [" +top.getRankDiff() + "]</small>";
			r2 = "<small>(" + bot.getRankChange() + ") [" +bot.getRankDiff() + "]</small>";
			if (top.isWinner())
			{
				w1s = "<b><div style='color:#006600'>W: ";
			}
			else
			{
				if (top.isForfeit())
					w1s = "<b><div style='color:#660000'>F: ";
				else
					w1s = "<b><div style='color:#660000'>L: ";
			}
			if (bot.isWinner())
			{
				w2s = "<b><div style='color:#006600'>W: ";
			}
			else
			{
				if (bot.isForfeit())
					w2s = "<b><div style='color:#660000'>F: ";
				else
					w2s = "<b><div style='color:#660000'>L: ";
			}
		}
		sb.append(w1s);
		sb.append(full ? top.getTeam().getFullName() : top.getName());
		if (showElo)
		{
			sb.append(br);
			sb.append(r1);
		}
		sb.append(we);
		sb.append("<div>");
		sb.append(vs);
		sb.append("</div>");
		sb.append(w2s);
		sb.append(full ? bot.getTeam().getFullName() : bot.getName());
		if (showElo)
		{
			sb.append(br);
			sb.append(r2);
		}
		sb.append(we);
		sb.append("</div>");
		return sb.toString();
	}
	
	public String getPlayedDisplay()
	{
		return (getPlayed() == null) ? "" : new SimpleDateFormat("M/dd").format(getPlayed());
	}
	
	public String getRecentHtml()
	{
		StringBuilder s = new StringBuilder();
		s.append("<table style='width: 100%' border=1>");
		s.append("<tr>");
		s.append("<td style='width:100px'>");
		s.append(getRound().getHtml());
		s.append("</td>");
		s.append("<td style='width:100px'>");
		s.append("Played on<br>");
		s.append(getPlayedDisplay());
//		s.append("<br>at<br>");
//		s.append(new SimpleDateFormat("h:mm a").format(getPlayed()));
		s.append("</td>");
		s.append("<td align='center'>");
		s.append(getHtml(true));
		s.append("</td>");
		s.append("</tr>");
		s.append("</table>");
		return s.toString();
	}
		
	
	public Side getSide(Team t)
	{
		if (getSideA().getTeam().equals(t))
			return getSideA();
		if (getSideB().getTeam().equals(t))
			return getSideB();
		return null;
	}
	
	public int getSideASet1()
	{
		return getSideA().getSet1();
	}
	public int getSideASet2()
	{
		return getSideA().getSet2();
	}
	public int getSideASet3()
	{
		return getSideA().getSet3();
	}
	public int getSideBSet1()
	{
		return getSideB().getSet1();
	}
	public int getSideBSet2()
	{
		return getSideB().getSet2();
	}
	public int getSideBSet3()
	{
		return getSideB().getSet3();
	}

	public void setSideASet1(int i)
	{
		getSideA().setSet1(i);
	}
	public void setSideASet2(int i)
	{
		getSideA().setSet2(i);
	}
	public void setSideASet3(int i)
	{
		getSideA().setSet3(i);
	}
	public void setSideBSet1(int i)
	{
		getSideB().setSet1(i);
	}
	public void setSideBSet2(int i)
	{
		getSideB().setSet2(i);
	}
	
	public void setScoreA(int i, int sc)
	{
		switch (i)
		{
			case 0: setSideASet1(sc); break ;
			case 1: setSideASet2(sc); break ;
			case 2: setSideASet3(sc); break ;
		}
	}
	
	public void setScoreB(int i, int sc)
	{
		switch (i)
		{
			case 0: setSideBSet1(sc); break ;
			case 1: setSideBSet2(sc); break ;
			case 2: setSideBSet3(sc); break ;
		}
	}
	public void setSideBSet3(int i)
	{
		getSideB().setSet3(i);
	}

	public void setTeamB(Team t)
	{
		setSideB(new Side());
		getSideB().setTeam(t);
	}
	
	public void setTeamA(Team t)
	{
		setSideA(new Side());
		getSideA().setTeam(t);
	}

	public boolean includes(Player p)
	{
		if (p == null)
			return false;
		return getSideA().includes(p) || getSideB().includes(p);
	}
	
	public boolean includes(Team t)
	{
		if (t == null)
			return false;
		return getSideA().includes(t) || getSideB().includes(t);
	}
	
	public int getSetsWon(Team t)
	{
		if (getSideA().includes(t))
			return getSideA().getSetsWonAgainst(getSideB());
		if (getSideB().includes(t))
			return getSideB().getSetsWonAgainst(getSideA());
		return 0;
	}
	
	public int getGamesWon(Team t)
	{
		if (getSideA().includes(t))
			return getSideA().getGamesWonAgainst(getSideB());
		if (getSideB().includes(t))
			return getSideB().getGamesWonAgainst(getSideA());
		return 0;
	}
	
	public boolean hasBeenPlayedLastWeek(Date d)
	{
		if (hasBeenPlayed())
		{
			long now = d.getTime();
			long game = getPlayed().getTime();
			return TimeUnit.MILLISECONDS.toDays(now - game) < 7;
		}
		return false;
	}
		
	public boolean isWinner(Team t)
	{
		return getSideA().isWinner(t) || getSideB().isWinner(t);
	}
	
	public boolean hasBeenPlayed()
	{
		return getPlayed() != null;
	}
	
	public void setHasBeenPlayed(Date d)
	{
		setPlayed(d);
		getLeague().addMatch(this);
	}
	
	public League getLeague()
	{
		return getRound().getLeague();
	}
	public String getDescription(boolean showElo)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getSideA().getSideName(showElo));
		sb.append(' ');
		if (hasBeenPlayed())
		{
			sb.append(getSideA().getScoresString(getSideB()));
		}
		else
			sb.append("vs.");
		sb.append(' ');
		sb.append(getSideB().getSideName(showElo));
		if (getPlayed() != null)
		{
			sb.append(" [");
			sb.append(getPlayedDisplay());
			sb.append("]");
		}
		return sb.toString();
	}
	
	public String getDescription()
	{	
		return getDescription(showElo());
	}
	
	protected String getTeamTitle(Side side)
	{
		String n = side.getTeam().getName();
		if (hasBeenPlayed())
			n = n + " " + side.getRankChange();
		else
			n = n + " (" + side.getTeam().getRank() + ")";
		return n;
	}
	
	public void determineWinner(Date d)
	{
		setHasBeenPlayed(d);
		determineWinner();
	}
	
	public void determineWinner()
	{
		setsPlayed = getSideA().determineWinner(getSideB());
	}
	

	public Date getPlayed()
	{
		return played;
	}

	public void setPlayed(Date played)
	{
		this.played = played;
	}

	public Side getSideA()
	{
		return sideA;
	}

	public void setSideA(Side teamA)
	{
		this.sideA = teamA;
	}
	

	public Side getSideB()
	{
		return sideB;
	}

	public void setSideB(Side teamB)
	{
		this.sideB = teamB;
	}

	public int getSetsPlayed()
	{
		return setsPlayed;
	}

	public String getComment()
	{
		return comment.trim();
	}
	
	public String getFullHtml()
	{
		return getRound().toString() + "<br>Match played on: " + getPlayedDisplay() + "<br>" + getHtml(true, false) + "<br>" + getCommentHtml();
	}
	
	public String getRecordedByFirstLastName()
	{
		return (getRecordedBy() == null) ? "" : getRecordedBy().firstLastName();
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public Player getRecordedBy()
	{
		return recordedBy;
	}

	public void setRecordedBy(Player recordedBy)
	{
		this.recordedBy = recordedBy;
	}

}
