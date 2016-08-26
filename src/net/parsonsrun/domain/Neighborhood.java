package net.parsonsrun.domain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

import net.parsonsrun.P2tennisv2UI;

public class Neighborhood extends NamedDomainObject
{
	private static final long serialVersionUID = 1L;
	
	public static final String DIR = "C:\\utils\\";
	public static final String PICS = "C:\\utils\\p2pictures";
	public static final String FILE = DIR + "parsonsrun.ser";
	public static final String IMPORT = DIR + "parsonsrun.import";
	public static final String[] ADMIN_EMAILS = {
			"crnormand@bellsouth.net",
			"yoder.pd@gmail.com"
	};
	public static final String NEXT_PICS = "NextPics";

	public static Neighborhood singleton;
	
	protected ArrayList<Player> players = new ArrayList();
	protected ArrayList<Team> teams = new ArrayList();
	protected ArrayList<League> leagues = new ArrayList();
	
	protected HashMap<Match, Player> editing = new HashMap<Match, Player>();
	
	public static synchronized Neighborhood getSingleton()
	{
		if (singleton == null)
			singleton = loadData();
		return singleton;
	}
	
	protected static Neighborhood doImport()
	{
		return new Importer(IMPORT).run();
	}
	
	public static Neighborhood loadData()
	{

		File f = new File(FILE);
		if (f.exists())
		{
			System.out.println("Reading Neighborhood data from: " + f);
			try
			{
				InputStream file = new FileInputStream(f);
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);
				try
				{
					Neighborhood n = (Neighborhood)input.readObject();
					n.clearEditing();
					return n;
				}
				finally
				{
					input.close();
				}
			}
			catch (ClassNotFoundException ex)
			{
				System.out.println("Cannot perform input. Class not found." + ex);
			}
			catch (IOException ex)
			{
				System.out.println("Cannot perform input." + ex);
			}
		}
		System.out.println("Importing Neighborhood data from: " + IMPORT);
		Neighborhood h = doImport();
		//h.saveData();
		return h;
	}
	public static void setSingleton(Neighborhood singleton)
	{
		Neighborhood.singleton = singleton;
	}
	public ArrayList<Player> getPlayers()
	{
		return players;
	}
	public void setPlayers(ArrayList<Player> players)
	{
		this.players = players;
	}
	
	public Player getPlayer(String s)
	{
		for (Player p : getPlayers())
		{
			if (p.getEmail().equalsIgnoreCase(s))
				return p;
			if (p.firstLastName().equalsIgnoreCase(s))
				return p;
			if (p.getLast().equalsIgnoreCase(s))
				return p;
		}
		return null;
	}
	
	public Team getTeam(String s)
	{
		for (Team t : getTeams())
		{
			if (t.getName().equalsIgnoreCase(s))
				return t;
			if (t.getFullName().equalsIgnoreCase(s))
				return t;
		}
		return null;
	}
	
	public Team getTeam(Team t)
	{
		return getTeam(t.getName());
	}
	
	public League getLeagueNamed(String nm)
	{
		for (League lg : getLeagues())
			if (lg.getName().equals(nm))
				return lg;
		return null;
	}
	
	public ArrayList<League> getLeagues()
	{
		return leagues;
	}
	public void setLeagues(ArrayList<League> leagues)
	{
		this.leagues = leagues;
	}
	public Player addPlayer(Player p)
	{
		int i = getPlayers().indexOf(p);
		if (i < 0)
		{
			checkAdmin(p);
			getPlayers().add(p);
			return p;
		}
		else
			return getPlayers().get(i);
	}
	
	public void removePlayer(Player p)
	{
		getPlayers().remove(p);
	}
	
	protected void checkAdmin(Player p)
	{
		for (int i = 0; i < ADMIN_EMAILS.length; i++)
			if (ADMIN_EMAILS[i].equalsIgnoreCase(p.getEmail()))
				p.setAdmin(true);
	}
	
	public synchronized Player canEdit(Match m, Player p)
	{
		Player t = getEditing().get(m);
		if (t == null)
		{
			getEditing().put(m,  p);
			return null;
		}
		return t;
	}
	
	public synchronized void updateMatch(Match m, int[] scoresA, int[] scoresB, Date d, boolean fa, boolean fb, String comment, boolean locked, Player recorder, boolean show)
	{
		boolean old = m.hasBeenPlayed();
		if (scoresA != null)
		{
			for (int i = 0; i < 3; i++)
			{
				m.setScoreA(i, scoresA[i]);
				m.setScoreB(i, scoresB[i]);
			}
		}
		m.getSideA().setForfiet(fa);
		m.getSideB().setForfiet(fb);
		if (!m.hasBeenPlayed())
			m.setHasBeenPlayed(d);
		else
		{
			m.setPlayed(d);
		}
		m.setComment(comment);
		m.setLocked(locked);
		m.setRecordedBy(recorder);
		m.getLeague().recalculate();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd hh:mm a");
		System.out.println("Match: " + format.format(new Date()) + " " + recorder + " : " + m);
		sendMatchUpdateEmail(m, old);
		saveData(show);
	}
	
	protected void sendMatchUpdateEmail(Match m, boolean old)
	{
		String m1 = old ? "This message was automatically generated because details of the match have been changed (score, date, comment, etc.)"
				: "This message was automatically generated because the match score has been entered.";
		String m2 = "<br><br>The match was played on " + m.getPlayedDisplay();
		String m3 = "<br><br>" + m.getHtml(true, false) + "<br><br>" + m.getCommentHtml();
		String msg = m1 + m2 + m3;
		sendMatchEmail(m, "P2 update: " + m, msg);
	}
	
	protected void sendMatchEmail(Match m, String subj, String htmlMsg)
	{
		System.out.println("Sending email " + subj);
		ArrayList<String> to = new ArrayList<String>();
		ArrayList<String> cc = new ArrayList<String>();

		Team tm1 = m.getSideA().getTeam();
		Team tm2 = m.getSideB().getTeam();
		to.add(tm1.getPlayerA().getEmail());
		to.add(tm1.getPlayerB().getEmail());
		to.add(tm2.getPlayerA().getEmail());
		to.add(tm2.getPlayerB().getEmail());
		for (Player p : m.getLeague().getOrganizers())
			cc.add(p.getEmail());
		String h = htmlMsg 				
				+ "<br><br><a href=\"http://pdyoder.wixsite.com/p2-tournament/schedules-of-play-and-match-scores\">Desktop Parsons Run P2 website</a>"
				+ "<br><a href=\"http://normand.gotdns.com:8080/P2TennisV2\">Mobile Parsons Run P2 website</a>";
		P2tennisv2UI.sendEmail(to, cc, subj, h);
	}
	
	public synchronized void removeEditing(Match m)
	{
		getEditing().remove(m);
	}
	
	public boolean includes(Player np)
	{
		return find(np) != null;
	}
	
	public Player find(Player np)
	{
		for (Player p : getPlayers())
		{
			if (np.match(p))
				return p;
		}
		return null;
	}
	
	public Player login(String email, String password)
	{
		String e = (email == null ? "" : email.trim());
		String pw = (password == null ? null : password.trim());
		for (Player p : getPlayers())
		{
			if (p.matches(e, pw))
				return p;
		}
		return null;
	}
	
	public synchronized void saveData(boolean show)
	{
		try 
		{
			FileOutputStream file = new FileOutputStream(new File(FILE));
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try
			{
				output.writeObject(this);
			}
			finally
			{
				output.close();
				if (show) Notification.show("  The data has been saved...  "); 
			} 
		}
	    catch (IOException ex)
		{
	    	System.out.println("Cannot perform output." + ex);
	    }
	}
	
	public void saveData()
	{
		saveData(true);
	}
	
	public void removeLeague(League lg)
	{
		getLeagues().remove(lg);
	}
	

	
	public static Neighborhood createTestData()
	{
		Neighborhood hood = new Neighborhood();
		hood.setName("Parsons Run");
		League lg = new League();
		lg.setName("Mens Spring 2016 DEMO");
		hood.addLeague(lg);
		long week = 86400 * 7 * 1000;
		long time = new Date().getTime();
		time = time - (3 * week);
		Date start = new Date(time);
		int i = 0;
		String[] players = testPlayers();
		while (i < players.length)
		{
			Player p0 = new Player().set(players[i++], players[i++], players[i++], players[i++]);
			i++; // To skip over int
			hood.addPlayer(p0);
		}
		Iterator<Player> p = hood.getPlayers().iterator();
		while (p.hasNext())
		{
			Team t = new Team();
			t.setPlayerA(p.next());
			if (p.hasNext())
			{
				t.setPlayerB(p.next());
				hood.addTeam(t);
				lg.addTeam(t);
			}
		}
		lg.buildRounds(start);
		
		createTestScores(lg, start);
		
		lg = new League();
		lg.setName("Womens Spring 2016 DEMO");
		hood.addLeague(lg);
		i = 0;
		Team tm = null;
		players = testPlayersW();
		while (i < players.length)
		{
			Player t = new Player().set(players[i++], players[i++], players[i++], players[i++]);
			i++; // To skip over int
			t.setMale(false);
			hood.addPlayer(t);
			if (tm == null)
			{
				tm = new Team();
				tm.setPlayerA(t);
			}
			else
			{
				tm.setPlayerB(t);
				hood.addTeam(tm);
				lg.addTeam(tm);
				tm = null;
			}
		}
		lg.buildRounds(start);
		createTestScores(lg, start);
		
		lg = new League();
		lg.setName("Mens Fall 2016 DEMO");
		hood.addLeague(lg);
		
		ArrayList<Player> m = new ArrayList<Player>();
		for (Player p2 : hood.getPlayers())
		{
			if (p2.isMale())
				m.add(p2);
		}
		Random r = new Random(5001L);
		tm = null;
		while (!m.isEmpty())
		{
			int ind = r.nextInt(m.size());
			Player p1 = m.remove(ind);
			if (tm == null)
			{
				tm = new Team();
				tm.setPlayerA(p1);
			}
			else
			{
				tm.setPlayerB(p1);
				tm = hood.addTeam(tm);
				lg.addTeam(tm);
				tm = null;
			}
		}
		lg.buildRounds(new Date());
		
		lg = new League();
		lg.setName("Mens&Womens Full DEMO");
		hood.addLeague(lg);
		
		m = new ArrayList<Player>();
		for (Player p2 : hood.getPlayers())
		{
			m.add(p2);
		}
		tm = null;
		while (!m.isEmpty())
		{
			int ind = r.nextInt(m.size());
			Player p1 = m.remove(ind);
			if (tm == null)
			{
				tm = new Team();
				tm.setPlayerA(p1);
			}
			else
			{
				tm.setPlayerB(p1);
				tm = hood.addTeam(tm);
				lg.addTeam(tm);
				tm = null;
			}
		}
		lg.buildRounds(new Date());
		createTestScores(lg, start, true);
		lg.setClosed(true);
		Collections.reverse(hood.getLeagues());
		return hood;
	}
	
	public static void createTestScores(League lg, Date start)
	{
		createTestScores(lg, start, false);
	}
	public static void createTestScores(League lg, Date start, boolean full)
	{
		Random r = new Random(7001L);
		ArrayList<Round> rnds = lg.getRounds();
		int stop = rnds.size() / 2;
		if (full)
			stop = rnds.size();
		long time = start.getTime();
		long day = 86400 * 1000;
		long week = day * 7;
		int offset = 0;
		SimpleDateFormat format = new SimpleDateFormat("MM/dd");
	
		for (Round rnd : rnds)
		{
			long rndDate = time + (week * offset);
			for (Match m : rnd.getMatches())
			{
				if (! m.isBye())
				{
					boolean done = false;
					if (r.nextInt(100) < 15)
					{
						m.getSideA().setForfiet(true);
						done = true;
					}
					if (r.nextInt(100) < 10)
					{
						m.getSideB().setForfiet(true);
						done = true;
					}
					int w1 = 0;
					int w2 = 0;
					int i = 0;
					while (! done)
					{
						int s1 = r.nextInt(7);	
						int s2 = r.nextInt(7);
						if (r.nextBoolean())
						{
							if (++w1 == 2)
								done = true;
							s1 = 6;
							if (s2 >= 5)
								s1 = 7;
						}
						else
						{
							if (++w2 == 2)
								done = true;
							s2 = 6;
							if (s1 >= 5)
								s2 = 7;
						}
						m.setScoreA(i, s1);
						m.setScoreB(i, s2);
						i++;
					}
					long diff = 0; // (long)(day * ((r.nextInt(100) - 50) / 100.0f));
					Date d = new Date(rndDate + (r.nextInt(7) * day) + diff);
					m.determineWinner(d);
				}
				//System.out.println(m.getDescription());
			}
			if (stop-- <= 0)
				return;
			offset++;
		}
		
	}
	
	static String[] testPlayers()
	{
		return new String[] 
		{
			"Bob", "Creigh", "bob@test.com", "pass", "2000",
			"Chris", "Normand", "chris@test.com", "pass", "2100",
			"Doug", "Yoder", "doug@test.com", "pass", "2200",
			"Mike", "Harper", "mike@test.com", "pass", "2050",
			"Dan", "Lark", "dan@test.com", "pass", "1900",
			"Lee", "Ramby", "lee@test.com", "pass", "1700",
			"Mark", "Gillette", "mark@test.com", "pass", "1900",
			"Paul", "Giggi", "paul@test.com", "pass", "1850",
			"Bob", "Jones", "bobj@test.com", "pass", "2000",
			"Chris", "Jessel", "cj@test.com", "pass", "2100",
			"Jack", "Wilson", "jack@test.com", "pass", "2200",
			"Mike", "Ryan", "miker@test.com", "pass", "2050",
			"Dan", "Coady", "danc@test.com", "pass", "1900",
//			"Leroy", "Cheng", "leec@test.com", "pass", "1700",
			"Mark", "Paradiso", "A", "A", "1900",
			"Paul", "Handler", "a", "a", "1850"
		};
	}
	
	static String [] testPlayersW()
	{
		return new String[] 
		{
		"Theresa", "Creigh", "Theresa@test.com", "pass", "2000",
		"Janene", "Normand", "janene@test.com", "pass", "2100",
		"Viola", "Yoder", "viola@test.com", "pass", "2200",
		"Catherine", "Harper", "catherine@test.com", "pass", "2050",
		"Ellen", "Lark", "ellan@test.com", "pass", "1900",
		"Debbie", "Giggi", "debbie@test.com", "pass", "1850",
		"Maribeth", "Krawczyk", "maribeth@test.com", "pass", "2000",
		"Jennifer", "Jessel", "jennifer@test.com", "pass", "2100",
		"Carol", "Wilson", "carol@test.com", "pass", "2200",
		"Megan", "Ryan", "megan@test.com", "pass", "2050",
		"Michelle", "Coady", "michelle@test.com", "pass", "1900",
		"Shanta", "Dube", "shanta@test.com", "pass", "1900",
		"Alana", "Sulka", "alana@test.com", "pass", "1900",
		"Kelly", "Russel", "kelly@test.com", "pass", "1900"
		};
	}
	
	public boolean addLeague(League lg)
	{
		if (getLeagueNamed(lg.getName()) == null)
		{
			leagues.add(lg);
			lg.setHood(this);
			return true;
		}
		return false;
	}
	
	public void moveLeague(int delta, League lg)
	{
		int index = getLeagues().indexOf(lg);
		if (index == -1)
			return;
		Collections.swap(getLeagues(), index, index + delta);
	}
	
	public void sendUnplayedMatchReminders(String title)
	{
		for (League lg : getLeagues())
		{
			if (lg.isOpen())
				lg.sendUnplayedMatchReminders(title);
		}
	}
	
	public void closeOutWeek(Date d)
	{
		for (League lg : getLeagues())
		{
			if (lg.isOpen())
				lg.closeOutWeek(d);
		}
	}
				
	public ArrayList<Team> getTeams()
	{
		return teams;
	}
	
	public Team addTeam(Team t)
	{
		int i = getTeams().indexOf(t);
		if (i < 0)
		{
			getTeams().add(t);
			return t;
		}
		else
			return getTeams().get(i);
	}
	
	protected void clearEditing()
	{
		getEditing().clear();
	}
	
	public synchronized int getNextPic()
	{
		int i = getInt(NEXT_PICS);
		put(NEXT_PICS, "" + (i + 1));
		saveData(false);
		return i;
	} 
	public void setTeams(ArrayList<Team> teams)
	{
		this.teams = teams;
	}

	public HashMap<Match, Player> getEditing()
	{
		return editing;
	}

	public void setEditing(HashMap<Match, Player> editing)
	{
		this.editing = editing;
	}
}
