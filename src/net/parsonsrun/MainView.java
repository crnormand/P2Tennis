package net.parsonsrun;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.vaadin.peter.imagestrip.ImageStrip;
import org.vaadin.tepi.imageviewer.ImageViewer;
import org.vaadin.tepi.imageviewer.ImageViewer.ImageSelectedEvent;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Neighborhood;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Round;
import net.parsonsrun.domain.Team;
import net.parsonsrun.domain.VsMatch;

public class MainView extends BaseView
{
	protected Grid leaguesGrid;
	protected BeanItemContainer<League> leagues;
	protected Button showStandings;
	protected Button showRounds;
	protected VerticalLayout right;
	protected Button record;
	protected Label record1;
	protected Label record2;
	protected Button leagueOrganizer;
	protected CheckBox elo;
	protected Button showChart;
	protected ImageStrip strip;
	protected List<FileResource> images;
	protected List<String> imageNames;
	protected int selectedThumb;
	
	protected static String record1label = "Click to record a score for";	
	protected static String record2label = "one of your unplayed matches.";
	
	public MainView()
	{
		super();
		setWidth("100%");
		String n = "to Parson Run P2 Tennis";
		if (getLoginUser() != null)
			n = getLoginUser().firstLastName();
		//addTitle("Welcome " + n);
		
		HorizontalLayout t = new HorizontalLayout();
		//t.setMargin(true);
		Label lbl = new Label("Welcome " + n);
		lbl.setStyleName("windowtitle");
		t.addComponent(lbl);
		t.setComponentAlignment(lbl, Alignment.BOTTOM_CENTER);
		addComponent(t);
		setComponentAlignment(t, Alignment.TOP_LEFT);
		//addComponent(createPopup());
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		VerticalLayout left = new VerticalLayout();
		//left.setWidth("40%");
		left.setWidth("24em");
		right = new VerticalLayout();
		right.setWidth("60%");
		h.addComponent(left);
		h.addComponent(right);
		if (getLoginUser() != null)
		{
			Button b = new Button("(Click here if you are not " + getLoginUser().firstLastName() + ")");
			b.addStyleName(BaseTheme.BUTTON_LINK);
			b.addStyleName(ValoTheme.BUTTON_TINY);
			b.addClickListener(e -> clear());
			t.addComponent(b);
			t.setComponentAlignment(b, Alignment.BOTTOM_RIGHT);
			
			leagueOrganizer = new Button("Organize League");
			leagueOrganizer.addStyleName(ValoTheme.BUTTON_DANGER);
			leagueOrganizer.addStyleName(ValoTheme.BUTTON_TINY);
			leagueOrganizer.addClickListener(e -> organizeLeague());
			leagueOrganizer.setVisible(false);
			t.addComponent(leagueOrganizer);
			t.setComponentAlignment(leagueOrganizer, Alignment.BOTTOM_RIGHT);
			
			if (getLoginUser().isAdmin())
			{
				b = new Button("< Admin Functions >");
				b.addStyleName(ValoTheme.BUTTON_DANGER);
				b.addStyleName(ValoTheme.BUTTON_TINY);
				b.addClickListener(e -> admin());
				t.addComponent(b);
				t.setComponentAlignment(b, Alignment.BOTTOM_RIGHT);
			}
		}
		addComponent(h);
		leagues = new BeanItemContainer<League>(League.class);
		leagues.addAll(getHood().getLeagues());
		leaguesGrid = new Grid("Select a League to see Schedule and Standings:", leagues);
		left.addComponent(leaguesGrid);
		leaguesGrid.removeAllColumns();
		leaguesGrid.setHeaderVisible(false);
		leaguesGrid.setHeight("8em");
		leaguesGrid.addColumn("name");
		leaguesGrid.getColumns().stream().forEach(c -> c.setSortable(false));
		leaguesGrid.setImmediate(true);
		SingleSelectionModel sm = new SingleSelectionModel();
		sm.setDeselectAllowed(false);
		leaguesGrid.setSelectionModel(sm);
		leaguesGrid.addSelectionListener(e -> leagueSelected());
		leaguesGrid.setWidth("100%");
		left.addComponent(createSpacer());
		showRounds = new Button("Schedule");
		showRounds.setDescription("Show the full Schedule for the currently selected League");
		showRounds.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		showRounds.addStyleName(ValoTheme.BUTTON_HUGE);
		showRounds.setEnabled(false);
		showRounds.addClickListener(e -> showRounds());
		left.addComponent(showRounds);
		left.addComponent(createSpacer());
		showStandings = new Button("Standings");
		showStandings.setDescription("Show the Team Standings for the currently selected League");
		showStandings.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		showStandings.addStyleName(ValoTheme.BUTTON_HUGE);
		showStandings.setEnabled(false);
		showStandings.addClickListener(e -> showStandings());
		left.addComponent(showStandings);
		if (getLoginUser() == null)
		{
			left.addComponent(createSpacer());
			Button b = new Button("LOGIN");
			//record.addStyleName(ValoTheme.BUTTON_FRIENDLY);
			b.setEnabled(true);
			b.addClickListener(e -> login());
			left.addComponent(b);
			left.addComponent(new Label("(necessary to record Match score)"));
		}
		else
		{
			left.addComponent(createSpacer());
			record = new Button("Send Invite/Enter Scores");
			record.setDescription("Send an email invite or enter scores for a pending match");
			record.addStyleName(ValoTheme.BUTTON_FRIENDLY);
			record.setEnabled(false);
			record.addClickListener(e -> recordScore());
			left.addComponent(record);
			record1 = new Label(record1label);
			record2 = new Label(record2label);
			//left.addComponent(record1);
			//left.addComponent(record2);
			left.addComponent(new Label(""));
			Button b = new Button("Update User Profile");
			b.setDescription("Edit your user profile (name, phone, password, etc.)");
			b.addClickListener(e -> updateProfile());
			left.addComponent(b);
			//left.addComponent(createSpacer());
		}
		//left.addComponent(createSpacer());
		HorizontalLayout he = new HorizontalLayout();
		he.setWidth("20em");
		//he.setHeight("1em");
		elo = new CheckBox("Show ELO scores");
		elo.setImmediate(true);
		elo.setValue(showElo());
		elo.setDescription("Display the ELO score for each team (and the changes in ELO scores for each match).  NOTE: These scores are for entertainment purposes only.   They are NOT used to determine standings.");
		elo.addValueChangeListener(e -> changeElo());
		he.addComponent(elo);
		he.setComponentAlignment(elo, Alignment.BOTTOM_LEFT);
		showChart = new Button("ELO Chart");
		showChart.addClickListener(e -> navigateTo(P2tennisv2UI.CHART));
		showChart.setEnabled(showElo() && getCurrentLeague() != null);
		showChart.setDescription("Open a chart view to see the progression of each team's ELO score");
		he.addComponent(showChart);
		he.setComponentAlignment(showChart, Alignment.TOP_RIGHT);
		left.addComponent(he);
		if (!getP2tennisUI().getWix())
		{
			addImageStrip();
			addRules();
		}
		initialSelect();
	}
	
	protected void addRules()
	{
		RichTextArea a = new RichTextArea();
		StringBuilder s = new StringBuilder();
		s.append("<font size='5'><b>Format and Rules:</b></font><br><br>");
		s.append("This year's P2 Tournament will be held in two brackets of Round Robin play.  Each team in bracket B plays 6 matches.");
		s.append("  Due to an odd number of teams in bracket A, each team in bracket A plays 5 matches, with the exception of Perry/Smith (they play 6 matches).");
		s.append("  Rankings within both brackets are based on number of wins.  In the case of Perry/Smith, their number of wins will");
		s.append(" be scaled at the end of the season by 5/6. Ties will be broken based on the results of head-to-head competition.");
		s.append("  Any remaining ties will be broken based on total number of sets won, then total number of games won,");
		s.append(" with Perry/Smith statistics scaled by 5/6.<br><br>");
		s.append("To accommodate players' travel schedules, 1) all matches in the first 2 Rounds can be played at any time on or before");
		s.append(")August 7th, and 2) all matches after the first 2 Rounds can be pre-played an arbitrary number of days in advance.");
		s.append("  To ensure fairness to all competitors, the use of substitutes is not permissible, and unplayed matches are defaulted");
		s.append(" by the team which offers the least availability in a given Round.  Beginning with Round 2, results of all matches");
		s.append(" must be reported by midnight of the last day of each Round.  No exceptions.<br><br>");  
		s.append("At the end of the season, the top 4 teams in each bracket will play in a single-elimination tournament for the Championship.<br><br>");
		s.append("<font color='#ff0ff'><b>The Championship matches for both brackets will be held at 5PM on Sunday, September 4th.  Mark your calendars!</b></font>");
		s.append("<br><br>Exception to the \"no substitutes\" rule:<br><br>");
		s.append("Should one of the players whose team has qualified for the Championships be unavailable for competition on the day of the");
		s.append(" Championships due to a travel-related conflict, that player's team must negotiate a substitute (from among the list");
		s.append(" of P2 participants) with all other Championship teams in the same bracket, and secure their unanimous approval.");
		s.append(" This rule applies only to travel- and injury-related conflicts. In the unlikely event that all 4 teams in the affected");
		s.append(" bracket cannot agree on a mutually acceptable substitute, the Organizing Committee will appoint a substitute. A team will");
		s.append(" be considered ineligible for participation in the Championships if one of its members is unavailable for non-travel-related");
		s.append(" reasons, or if both of its members are unavailable for any reason.");
		a.setValue(s.toString());
		a.setReadOnly(true);
		a.setWidth("100%");
		addComponent(a);
	}
	
	protected void addImageStrip()
	{
		strip = new ImageStrip();
		strip.addValueChangeListener(e -> imageSelected(e));
		strip.setDescription("Click on picture to expand");
	    strip.setSelectable(true);
	    strip.setMaxAllowed(10);
	    for (FileResource fr : createImageList())
	    	strip.addImage(fr);
	    addComponent(strip);	
	    HorizontalLayout h = new HorizontalLayout();
	    h.setSizeFull();
	    Label l = new Label("Click a thumbnail to view picture");
	    h.addComponent(upload());
	    h.addComponent(l);
	    addComponent(h);
	}
	
	protected Upload upload()
	{
		class ImageUploader implements Receiver, SucceededListener {
			public String mainfn;
			public String tmp;
			public String fname;
	
		    public OutputStream receiveUpload(String filename, String mimeType) {
		        // Create upload stream
		        FileOutputStream fos = null; // Stream to write to
		        try {
		        	new Notification("Image uploading",
                            "Your picture '" + mainfn + "' is uploading...",
                            Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
		        	String n = String.format("%03d", getHood().getNextPic());
		        	mainfn = n + "-" + getLoginUser().getLast() + "-" +  filename;
		        	tmp = Neighborhood.PICS + "\\" + "tmp-" + mainfn;
		        	fname = Neighborhood.PICS + "\\" + mainfn;
		            fos = new FileOutputStream(new File(tmp));
		        } catch (final java.io.FileNotFoundException e) {
		            new Notification("Could not open file<br/>",
		                             e.getMessage(),
		                             Notification.Type.ERROR_MESSAGE)
		                .show(Page.getCurrent());
		            return null;
		        }
		        return fos; // Return the output stream to write to
		    }

		    public void uploadSucceeded(SucceededEvent event) {
		    	try 
		    	{
		    		File t = new File(tmp);
			    	BufferedImage originalImage = ImageIO.read(t);
			    	BufferedImage scaledImage = Scalr.resize(originalImage, 800);
			    	File f = new File(fname);
			    	ImageIO.write(scaledImage, "jpg", f);
			    	t.delete();
			    	FileResource fr = new FileResource(f);
			    	images.add(fr);
			        strip.addImage(fr);
			        new Notification("Image uploaded!",
                            "Your picture '" + mainfn + "' was successfully uploaded",
                            Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
		    	} catch (IOException ioe)
		    	{
		    		System.out.println("Error resizing " + fname + " " + ioe);
		    		new Notification("Could not resize file<br/>",
                            ioe.getMessage(),
                            Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
		    	}
		    }
		};
		ImageUploader receiver = new ImageUploader();
		Upload upload = new Upload("Add your own slideshow picture!", receiver);
		upload.setButtonCaption("Upload picture");
		upload.addSucceededListener(receiver);
		return upload;
	}
	
	public void animate()
	{
		UI.getCurrent().access(new Runnable() {
			public void run()
			{
				if (strip != null)
				{
					try
					{
						strip.scrollToLeft();
					}
					catch (Exception e)
					{
							System.out.println("Animate error: " + e);
					}
				}
			}
		});
	}
	
	protected void imageSelected(ValueChangeEvent e)
	{
		int i = ((ImageStrip.Image)e.getProperty().getValue()).getImageIndex();
		selectedThumb = i;
		Window w = new Window();
		VerticalLayout v = new VerticalLayout();
		v.setSizeFull();
		v.addComponent(new Label("Click anywhere to exit"));
		final HorizontalLayout h = new HorizontalLayout();
		v.addComponent(h);
		v.setExpandRatio(h, 1);
		h.setSizeFull();
		Button b1 = new Button("<");
		b1.setStyleName(ValoTheme.BUTTON_PRIMARY);
		b1.addClickListener(e1 -> shift(h, -1));
		h.addComponent(b1);
		Image img = new Image(null, images.get(i));
		h.addComponent(img);
		Button b2 = new Button(">");
		b2.setStyleName(ValoTheme.BUTTON_PRIMARY);
		b2.addClickListener(e2 -> shift(h, 1));
		h.addComponent(b2);
		shift(h, 0);
		w.setContent(v);
		w.setSizeFull();
		w.addClickListener(q -> getP2tennisUI().closeDialog());
		getP2tennisUI().openDialog(w);
	}
	
	protected void shift(final HorizontalLayout h, int ind)
	{
		int i = selectedThumb +  ind;
		int sz = images.size() - 1;
		if (i < 0)
			i = sz;
		if (i > sz)
			i = 0;
		selectedThumb = i;
		Image img = new Image(null, images.get(i));
		String lbl = imageNames.get(i);
		Component cl = h.getComponent(0);
		Component cr = h.getComponent(2);
		h.removeAllComponents();
		VerticalLayout v = new VerticalLayout();
		v.setSizeUndefined();
		v.addComponent(img);
		v.addComponent(new Label(lbl));
		h.addComponent(cl);
		h.addComponent(v);
		h.addComponent(cr);
		h.setComponentAlignment(cl, Alignment.MIDDLE_LEFT);
		h.setExpandRatio(v, 1);
		h.setComponentAlignment(v, Alignment.MIDDLE_CENTER);
		h.setComponentAlignment(cr, Alignment.MIDDLE_RIGHT);
	}
	
	protected List<FileResource> createImageList() 
	{
		Random r = new Random();
        images = new ArrayList<FileResource>();
        imageNames = new ArrayList<String>();
        File d = new File(Neighborhood.PICS);
        for (String f : d.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name)
			{
				return !name.startsWith("tmp-");
			}
        }))
        {
            images.add(new FileResource(new File(d, f)));
            imageNames.add(f);
        }
        if (images.size() > 6)
        {
	        int mv = r.nextInt(images.size() - 2);
	        for (int i = 0; i < mv ; i++)
	        {
	        	FileResource f = images.remove(0);
	        	images.add(f);
	        	String n = imageNames.remove(0);
	        	imageNames.add(n);
	        }
        }
        return images;
	}
	
	protected void initialSelect()
	{
		if (getCurrentLeague() != null)
		{
			League l = getHood().getLeagueNamed(getCurrentLeague().getName());
			leaguesGrid.select(l);
			if (getLoginUser() != null && l != null && leagueOrganizer != null)
			{
				leagueOrganizer.setVisible(l.isOrganizer(getLoginUser()));
			}
		}
	}
	
	protected void organizeLeague()
	{
		navigateTo(P2tennisv2UI.ORGANIZER);
	}
	
	protected void changeElo()
	{
		getP2tennisUI().setShowElo(elo.getValue());
		getHood().saveData(false);
		leagueSelected();
	}
	
	protected void admin()
	{
		navigateTo(P2tennisv2UI.ADMIN);
	}
	
	protected void recordScore()
	{
		Team t = getCurrentLeague().getTeam(getLoginUser());
		setCurrentTeam(t);
		setCurrentMatches(t.getUnplayedVsMatches(getCurrentLeague()));
		navigateTo(P2tennisv2UI.MATCHES);
	}
	
	protected void updateProfile()
	{
		getP2tennisUI().updateProfile(true);
	}
	

	protected void clear()
	{
		getP2tennisUI().clearUserCookie();
		navigateTo(P2tennisv2UI.OK);
	}
	
	protected void showStandings()
	{
		navigateTo(P2tennisv2UI.STANDINGS);
	}
	
	protected void showRounds()
	{
		navigateTo(P2tennisv2UI.ROUNDS);
	}

	protected void login()
	{
		navigateTo(P2tennisv2UI.LOGIN);
	}

	
	protected void leagueSelected()
	{
		League lg = (League)leaguesGrid.getSelectedRow();
		setCurrentLeague(lg);
		if (lg == null)
		{
			showStandings.setCaption("Standings");
			showStandings.setEnabled(false);
			showRounds.setCaption("Schedule");
			showRounds.setEnabled(false);
			showChart.setEnabled(false);
			leagueOrganizer.setVisible(false);
			record.setEnabled(false);
		}
		else
		{
			showChart.setEnabled(showElo());
			showStandings.setEnabled(true);
			showStandings.setCaption("Show Standings for " + lg.getName());
			showRounds.setEnabled(true);
			showRounds.setCaption("Show Schedule for " + lg.getName());
			right.removeAllComponents();
			RecentMatchesView r = new RecentMatchesView();
			right.addComponent(r);
			if (record != null)
			{
				Team t = getCurrentLeague().getTeam(getLoginUser());
				boolean e = t != null && t.getUnplayedVsMatches(getCurrentLeague()).size() > 0;
				record.setEnabled(e);
				if (e)
				{
					record1.setValue(record1label);
					record2.setValue(record2label);
				}
				else
				{
					record1.setValue("You do not have any unplayed");
					record2.setValue("matches in this league.");
				}
			}
			if (leagueOrganizer != null)
				leagueOrganizer.setVisible(lg.isOrganizer(getLoginUser()));
		}
	}

	@Override
	public void enter(ViewChangeEvent event)
	{
		String s = "";
		try
		{
			s = VaadinService.getCurrentRequest().getParameter("start");
		}
		catch (NullPointerException e) {}
		
		if (getLoginUser() == null)
			login();

	}


}
