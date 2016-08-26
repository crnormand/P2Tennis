package net.parsonsrun.domain;

import java.io.*;
import java.util.*;

public class Importer
{
	protected String file;
	protected Neighborhood hood;
	protected BufferedReader reader;
	String line;
	String [] splits;
	
	public Importer(String f)
	{
		file = f;
		hood = new Neighborhood();
	}
	
	public Neighborhood run()
	{
		try
		{
			open();
			System.out.println("*** Reading Players ***");
			readUsers();
			System.out.println("*** Reading Teams ***");
			readTeams();
			System.out.println("*** Reading Leagues ***");
			readLeagues();
			System.out.println("*** Done!");
			close();
		}
		catch (Exception e)
		{
			System.out.println("Unable to import " + e);
			e.printStackTrace();
		}
		return hood;
	}
	
	protected boolean next() throws IOException
	{
		line = reader.readLine();
		if (line == null)
			return false;
		line = line.trim();
		return ! line.isEmpty();
	}
	
	protected void split()
	{
		splits = line.split(" ");
	}
	
	protected void splitt()
	{
		splits = line.split("/");
	}

	protected void open() throws IOException
	{
		reader = new BufferedReader(new FileReader(file));
	}
	
	protected void close() throws IOException
	{
		reader.close();
	}
	
	protected void readUsers() throws IOException
	{
		while (next())
		{
			split();
			Player p = new Player();
			p.setFirst(splits[0]);
			p.setLast(splits[1]);
			p.setEmail(splits[2]);
			if (splits.length > 3)
				p.setPassword(splits[3]);
			hood.addPlayer(p);
			System.out.println(p);
		}
	}
	
	protected void readTeams() throws IOException
	{
		for (Team t : readTeamList())
			hood.addTeam(t.updatePlayers());
	}
	protected ArrayList<Team> readTeamList() throws IOException
	{
		ArrayList<Team> tms = new ArrayList<Team>();
		while (next())
		{
			splitt();
			Player p1 = hood.getPlayer(splits[0]);
			Player p2 = hood.getPlayer(splits[1]);
			Team t = new Team();
			t.setPlayers(p1, p2);
			tms.add(t);
			System.out.println(t);
		}
		return tms;
	}
	
	protected void readLeagues() throws IOException
	{
		Player c = hood.getPlayer("Chris Normand");
		Player d = hood.getPlayer("Doug Yoder");
		while (next())
		{
			League lg = new League();
			hood.addLeague(lg);
			lg.setName(line);
			//lg.addOrganizer(c);
			lg.addOrganizer(d);
			System.out.println(line);
			for (Team t : readTeamList())
			{
				Team lgt = hood.getTeam(t);
				lg.addTeam(lgt);
			}
			while (next())
			{
				Round rnd = new Round();
				rnd.setName(line);
				lg.addRound(rnd);
				while (next())
				{
					splitt();
					Team t1 = lg.getTeam(splits[0]);
					Team t2 = lg.getTeam(splits[1]);
					Match m = new Match();
					m.setTeamA(t1);
					m.setTeamB(t2);
					rnd.addMatch(m);
					System.out.println(m);
				}
			}
		}
	}

}
