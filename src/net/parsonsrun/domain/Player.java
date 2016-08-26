package net.parsonsrun.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Player extends DomainObject implements Comparable<Player>
{
	private static final long serialVersionUID = 1L;
	
	public static final String LastSlideshowIndex = "lastSlideshowIndex";
	public static final String SYSTEM_EMAIL = "ptwotennis@bellsouth.net";
	public static Player SYSTEM = new Player().set("P2", "System", SYSTEM_EMAIL, "");

	protected String first = "";
	protected String last = "";
	protected String email = "";
	protected String password = "";
	protected String phone = "";
	protected boolean isMale = true;
	protected boolean isAdmin = false;
	protected boolean isActive = true;
	protected boolean showElo = false;
	protected ArrayList<Team> teams = new ArrayList();
	
	public static class Comparators {

        public static Comparator<Player> NAME = new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                return o1.firstLastName().compareTo(o2.firstLastName());
            }
        };
        public static Comparator<Player> LAST = new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
            	return o1.lastFirstName().compareTo(o2.lastFirstName());
            }
        };
    }
	
	public Player set(String f, String l, String e, String p)
	{
		setFirst(f);
		setLast(l);
		setEmail(e);
		setPassword(p);
		return this;
	}
	
	public static String rot13(String input)
	{
	   StringBuilder sb = new StringBuilder();
	   for (int i = 0; i < input.length(); i++) 
	   {
	       char c = input.charAt(i);
	       if       (c >= 'a' && c <= 'm') c += 13;
	       else if  (c >= 'A' && c <= 'M') c += 13;
	       else if  (c >= 'n' && c <= 'z') c -= 13;
	       else if  (c >= 'N' && c <= 'Z') c -= 13;
	       sb.append(c);
		   }
		   return sb.toString();
	}
	
	public int getLastSlideshowIndex()
	{
		return getInt(LastSlideshowIndex);
	}
	
	public void setLastSlideshowIndex(int i)
	{
		put(LastSlideshowIndex, ""+i);
	}
	
	public String lastFirstName()
	{
		return getLast() +", " + getFirst();
	}
	
	public String getLastFirstName()
	{	
		String s = "";
		if (!isActive())
			s = " (inactive)";
		return lastFirstName() + s;
	}
	
	public void addTeam(Team t)
	{
		getTeams().add(t);
	}
	public boolean matches(String e, String p)
	{
		if (e.equalsIgnoreCase(getEmail()))
		{
			return (p == null) ? true : p.equals(getPassword());
		}
		return false;
	}
	
	public int compareTo(Player o) 
	{
		return Comparators.LAST.compare(this, o);
	}
	
	public boolean isInValid()
	{
		return getFirst().isEmpty() || getLast().isEmpty() || getEmail().isEmpty() || getPassword().isEmpty();
	}
	
	public void debugOut(StringBuilder sb)
	{
		sb.append(lastFirstName());
		newline(sb);
	}

	public String getFirst()
	{
		return first;
	}
	
	public String toString()
	{
		return firstLastName();
	}
	
	public String firstLastName()
	{
		return getFirst() + " " + getLast();
	}

	public void setFirst(String first)
	{
		this.first = first;
	}

	public String getLast()
	{
		return last;
	}
	
	public boolean allowedToEdit(Match m)
	{
		if (m.isBye())
			return false;
		if (isAdmin())
			return true;
		if (isOrganizer(m))
			return true;
		return (!m.isLocked()) && m.includes(this);
	}
	
	public boolean isOrganizer(Match m)
	{
		return m.getLeague().isOrganizer(this);
	}
	
	public boolean match(Player p)
	{
		return p.getEmail().equalsIgnoreCase(getEmail());
	}

	public void setLast(String last)
	{
		this.last = last;
	}

	public String getEmail()
	{
		return email;
	}
	
	public void setShowElo(boolean b)
	{
		showElo = b;
	}
	
	public boolean showElo()
	{
		return showElo;
	}
	
	public boolean canDelete()
	{
		return getTeams().isEmpty();
	}
	
	public boolean equals(Player p)
	{
		return getEmail().equalsIgnoreCase(p.getEmail());
	}
	
	public void setEmail(String email)
	{
		this.email = (email == null ? "" : email.trim());
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = (password == null ? "" : password.trim());
	}
	public ArrayList<Team> getTeams()
	{
		return teams;
	}
	public void setTeams(ArrayList<Team> teams)
	{
		this.teams = teams;
	}
	public boolean isMale()
	{
		return isMale;
	}
	public void setMale(boolean isMale)
	{
		this.isMale = isMale;
	}
	public boolean isAdmin()
	{
		return isAdmin;
	}
	public void setAdmin(boolean isAdmin)
	{
		this.isAdmin = isAdmin;
	}
	public boolean isActive()
	{
		return isActive;
	}
	public void setActive(boolean isActive)
	{
		this.isActive = isActive;
	}
	public String getPhone()
	{
		return phone;
	}
	public void setPhone(String p)
	{
		this.phone = p.replaceAll("[^0-9]", "");
	}
	
	public String getPhoneDisplay()
	{
		String p = getPhone();
		if (p.length() < 10)
			return p;
		else
			return "(" + p.substring(0, 3) + ") " + p.substring(3, 6) + "-" + p.substring(6, 10);
	}

}
