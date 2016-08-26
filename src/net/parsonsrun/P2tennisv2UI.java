package net.parsonsrun;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mpilone.vaadin.uitask.UIAccessor;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Standing;
import net.parsonsrun.domain.Team;

import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Theme("p2tennisv2")
public class P2tennisv2UI extends UI
{
	public static final String USER_COOKIE = "p2user";
	public static final String LEAGUE_COOKIE = "p2league";
	public static final String LOGIN = "Login";
	public static final String MAIN = "";					// Empty string "" is default window
	public static final String STANDINGS = "standings";
	public static final String ROUNDS = "rounds";
	public static final String RECORD = "record";
	public static final String RECENT = "recent";
	public static final String MATCH = "match";
	public static final String MATCHES = "matches";
	public static final String REGISTER = "register";
	public static final String INITIALREGISTER = "initialregister";
	public static final String OK = "ok";
	public static final String ORGANIZER = "ORGANIZER";
	public static final String ASSIGNLO = "assignlo";
	public static final String ADMIN = "admin";
	public static final String CHART = "chart";
	
	public static final int ANNIMATION_DELAY = 8000;
	public static final int COOKIE_AGE = Integer.MAX_VALUE;
	public static long DELAY = 500;
	public static int MAX_EDIT = (5 * 60) * 1000;
	public static boolean ENABLE_EMAIL = true;

	protected Player loginUser;
	protected Window dialog;
	protected Stack<String> previousPages = new Stack<String>();
	protected Player currentUser;
	protected League currentLeague;
	protected ArrayList<Match> currentMatches;
	protected Match currentMatch;
	protected Team currentTeam;
	protected Thread currentThread;
	protected MatchView editor;
	protected boolean showElo;
	protected int browserHeight;
	protected int browserWidth;
	protected boolean running = true;
	protected boolean wix = false;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = P2tennisv2UI.class, widgetset = "net.parsonsrun.widgetset.P2tennisv2Widgetset")
	public static class Servlet extends VaadinServlet {
	}
	
	public static void comment()
	{
/*
	TODO:
	
	SuperUser/Admin functions
	Organizer functions
	email
	picture per match?
	mobile enter score
	mobile schedule

*/
	}
	
	public void navigateBack()
	{
		if (previousPages.isEmpty())
			getNavigator().navigateTo(MAIN);
		else
			getNavigator().navigateTo(previousPages.pop());
	}
	
	public void setShowElo(boolean b)
	{
		showElo = b;
		if (getLoginUser() != null)
			getLoginUser().setShowElo(b);
	}
	
	public boolean showElo()
	{
		return showElo;
	}
	
	public void navigateTo(String viewName)
	{
		previousPages.push(getNavigator().getState());
		getNavigator().navigateTo(viewName);
	}
	
	public Neighborhood getHood()
	{
		return Neighborhood.getSingleton();
	}
	
	public static void sendEmail(ArrayList<String> to, ArrayList<String> cc, String s, String m)
	{
		try
		{
			HtmlEmail email = newEmail(s, m);
			for (String t : to)
//			String t= "crnormand@bellsouth.net";
				email.addTo(t);
			if (cc != null)
				for (String c : cc)
					email.addCc(c);
			if (ENABLE_EMAIL)
				email.send();
		} catch (EmailException e) 
		{
			System.out.println("Unable to send email. " + e);
		}
	}
	
	public static void sendEmail(ArrayList<String> to, String s, String m)
	{
		sendEmail(to, null, s, m);
	}

	protected static HtmlEmail newEmail(String s, String m) throws EmailException
	{
		System.out.println("Email sent: " + s);
		HtmlEmail email = new HtmlEmail();
		email.setHostName("outbound.att.net");
		email.setSmtpPort(465);
		email.setSSLOnConnect(true);
		email.setAuthentication(Player.SYSTEM_EMAIL, "11055lbl415NCD");
		email.setFrom(Player.SYSTEM_EMAIL);
		email.setSubject(s);
		email.setHtmlMsg(m);
		return email;
	}
	
	public BaseView getBaseView() 
	{
		return (BaseView)getNavigator().getCurrentView();
	}
	
	public void viewResized()
	{
		BaseView b = getBaseView();
		if (b != null)
			b.resized();
	}
	
	public void redrawView()
	{
		getNavigator().navigateTo(getNavigator().getState());
	}

	protected void init(VaadinRequest request) 
	{
		wix = "main".equalsIgnoreCase(request.getParameter("page"));
		getPage().setTitle("Parsons Run Tennis");
		browserHeight = getPage().getBrowserWindowHeight();
		browserWidth = getPage().getBrowserWindowWidth();
		//System.out.println("Size: " + browserWidth + "," +  browserHeight);
		getPage().addBrowserWindowResizeListener(new BrowserWindowResizeListener() {
			
			@Override
			public void browserWindowResized(BrowserWindowResizeEvent event)
			{
				browserHeight = event.getHeight();
				browserWidth = event.getWidth();
				viewResized();
				//System.out.println("Resize: " + browserWidth + "," +  browserHeight);
			}
		});
		
		setNavigator(new Navigator(this, this));
		getNavigator().addView(LOGIN, LoginView.class);
		getNavigator().addView(MAIN, MainView.class);
		getNavigator().addView(REGISTER, RegisterView.class);
		getNavigator().addView(STANDINGS, StandingsView.class);
		getNavigator().addView(ROUNDS, ScheduleView.class);
		getNavigator().addView(MATCH, MatchView.class);
		getNavigator().addView(OK, OkView.class);
		getNavigator().addView(MATCHES, MatchesView.class);
		getNavigator().addView(ORGANIZER, OrganizeView.class);
		getNavigator().addView(ASSIGNLO, LeagueManageView.class);
		getNavigator().addView(ADMIN, AdminView.class);
		getNavigator().addView(INITIALREGISTER, InitialRegisterView.class);
		getNavigator().addView(CHART,  EloChartView.class);

		checkUser();
		UI.getCurrent().setPollInterval(ANNIMATION_DELAY / 2);
		startAnnimationThread();
	}
	
	protected void startAnnimationThread()
	{
		new Thread(new Runnable() {
			public void run()
			{
				while (running)
				{
					try
					{
						Thread.sleep(ANNIMATION_DELAY);
						if (getNavigator().getCurrentView() != null)
							((BaseView)getNavigator().getCurrentView()).animate();
					} 
					catch (InterruptedException e) {}
					catch (UIDetachedException q) {
						running = false;
					}
				}
			}
		}).start();
	}
	
	public void detach()
	{
		running = false;
        super.detach();
    }
	
	protected void checkUser()
	{
		String l = getCookieValue(LEAGUE_COOKIE);
		League lg = getHood().getLeagueNamed(l);
		if (lg != null)
			setCurrentLeague(lg);
		String u = getCookieValue(USER_COOKIE);
		if (login(u, null))
			navigateTo(MAIN);
		
	}
	
	public void clearUserCookie()
	{
		addCookie(USER_COOKIE, "ZZZZZZZZZZZ", COOKIE_AGE);
		addCookie(LEAGUE_COOKIE, "ZZZZZZZZZZZ", COOKIE_AGE);
		setLoginUser(null);
	}
	
	protected boolean login(String email, String password)
	{
		String e = (email == null ? "" : email.toLowerCase());
		setLoginUser(getHood().login(e, password));
		if (getLoginUser() != null)
		{
			SimpleDateFormat format = new SimpleDateFormat("MM/dd hh:mm a");
			System.out.println("Login: " + format.format(new Date()) + " " + getLoginUser());
			addCookie(USER_COOKIE, e, COOKIE_AGE);   //Integer.MAX_VALUE
			return true;
		}
		return false;
	}
	
	public Player getCurrentUser()
	{
		return currentUser;
	}
	
	public void closeDialog()
	{
		if (dialog != null)
		{
			dialog.close();
			removeWindow(dialog);
			dialog = null;
		}
	}
	
	public void updateProfile(boolean addBack)
	{
		setCurrentUser(getLoginUser());
		if (addBack)
			navigateTo(P2tennisv2UI.REGISTER);
		else
			navigateTo(P2tennisv2UI.INITIALREGISTER);

	}
	
	public Cookie getCookieByName(String name) 
	{ 
		// Fetch all cookies from the request 
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		if (cookies == null)
			return null;
		// Iterate to find cookie by its name 
		for (Cookie cookie : cookies) 
		{ 
			if (name.equals(cookie.getName()))
			{ 
				return cookie; 
			} 
		}
		return null; 
	}
	
	public String getCookieValue(String name) 
	{
		Cookie c = getCookieByName(name);
		return (c == null) ? "" : c.getValue();
	}
	
	public void addCookie(String cookieName, String cookieValue, int maxAge)
	{
		if (VaadinService.getCurrentRequest() != null)
		{
			Cookie c = new Cookie(cookieName, cookieValue);
			c.setMaxAge(maxAge);
			c.setPath(VaadinService.getCurrentRequest().getContextPath());
			addCookie(c);
		}
	}
	
	protected void addCookie(Cookie c)
	{
		VaadinService.getCurrentResponse().addCookie(c);
	}

	public Window getDialog()
	{
		return dialog;
	}

	public void openDialog(Window dialog)
	{
		this.dialog = dialog;
		if (dialog != null)
		{
			//dialog.setModal(true);
			dialog.center();
			addWindow(dialog);
		}
	}
	
	public void updateCurrentMatch()
	{
		if (getCurrentMatch() == null)
			Notification.show("No Match to update.", Type.WARNING_MESSAGE);
		else if (getCurrentMatch().isBye())
			Notification.show("Cannot update a 'BYE' match.", Type.WARNING_MESSAGE);
		else if (getCurrentMatch().isClosed())
			Notification.show("This league is closed.", Type.WARNING_MESSAGE);
		else
		{
			if (getLoginUser() == null)
			{
				navigateTo(LOGIN);
			}
			else
			{
				if (getLoginUser().allowedToEdit(getCurrentMatch()))
				{
					Player p = getHood().canEdit(getCurrentMatch(), getLoginUser());
					if (p == null)
						openMatchEditor();
					else
						Notification.show(p.firstLastName() + " is currently updating that match, please wait a few minutes", Type.WARNING_MESSAGE);
				}
				else
					if (getCurrentMatch().isLocked())
						Notification.show("The match has been locked by the organizer", Type.WARNING_MESSAGE);
					
					else
						Notification.show("You don't have permission to update this match", Type.WARNING_MESSAGE);
			}
		}
	}
	
	protected void openMatchEditor()
	{
		final P2tennisv2UI t = this;
		currentThread = new Thread(new Runnable()
			{
				public void run()
				{
					try {
						Thread.sleep(MAX_EDIT);
					} catch (InterruptedException e) {}
					
					if (currentThread == Thread.currentThread())
					{
						if (getEditor() != null)
						{
							SimpleDateFormat format = new SimpleDateFormat("MM/dd hh:mm a");
							System.out.println("TmOut: " + format.format(new Date()) + " " + getLoginUser() + " : " + getCurrentMatch());
							t.access(new Runnable() {
								public void run() 
								{
									getEditor().backClicked();
									//Notification.show("Match edit time EXPIRED", Type.WARNING_MESSAGE);
									ConfirmDialog.show(t, 
										"Warning:  Match Not Updated", "You did not complete your edits within the 2 minute time limit.\n\nThe match was not updated.",
								        "I understand", "I'm sorry.   I will be faster next time", new ConfirmDialog.Listener() {
								            public void onClose(ConfirmDialog dialog) {
								                if (dialog.isConfirmed())
								                	Notification.show("Thank you", Type.WARNING_MESSAGE);
								                else
								                	Notification.show("Please do. I can't wait around all day for you", Type.WARNING_MESSAGE);
								            }
								        });
								}
							});
						}
					}
				}
			});
		currentThread.start();
		navigateTo(MATCH);
	}
	
	public void clearEditor()
	{
		setEditor(null);
	}

	public void setCurrentUser(Player u)
	{
		currentUser = u;
	}

	public Player getLoginUser()
	{
		return loginUser;
	}

	public void setLoginUser(Player loginUser)
	{
		this.loginUser = loginUser;
		if (loginUser != null)
			showElo = loginUser.showElo();
	}

	public League getCurrentLeague()
	{
		if (currentLeague == null)
		{
			currentLeague = new League();
			currentLeague.setName("Unknown");
			currentLeague.setHood(getHood());
		}
		return currentLeague;
	}

	public void setCurrentLeague(League currentLeague)
	{
		this.currentLeague = currentLeague;
		String s = "zzzzz";
		if (currentLeague != null)
			s = currentLeague.getName();
		addCookie(LEAGUE_COOKIE, s, COOKIE_AGE);   
	}

	public ArrayList<Match> getCurrentMatches()
	{
		return currentMatches;
	}

	public void setCurrentMatches(ArrayList<Match> currentMatches)
	{
		this.currentMatches = currentMatches;
	}

	public Team getCurrentTeam()
	{
		return currentTeam;
	}

	public void setCurrentTeam(Team currentTeam)
	{
		this.currentTeam = currentTeam;
	}

	public Match getCurrentMatch()
	{
		return currentMatch;
	}

	public void setCurrentMatch(Match currentMatch)
	{
		this.currentMatch = currentMatch;
	}

	public MatchView getEditor()
	{
		return editor;
	}
	
	public boolean getWix()
	{
		return wix;
	}

	public void setEditor(MatchView editor)
	{
		this.editor = editor;
	}

	public int getBrowserHeight()
	{
		return browserHeight;
	}

	public void setBrowserHeight(int browserHeight)
	{
		this.browserHeight = browserHeight;
	}

	public int getBrowserWidth()
	{
		return browserWidth;
	}

	public void setBrowserWidth(int browserWidth)
	{
		this.browserWidth = browserWidth;
	}
}