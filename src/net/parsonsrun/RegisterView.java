package net.parsonsrun;

import java.util.Iterator;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.themes.ValoTheme;

import net.parsonsrun.domain.League;
import net.parsonsrun.domain.Match;
import net.parsonsrun.domain.Player;
import net.parsonsrun.domain.Team;

public class RegisterView extends BaseView
{
	TextField first;
	TextField last;
	TextField email;
	PasswordField pass;
	PasswordField pass2;
	TextField phone;
	CheckBox isMale;
	CheckBox isFemale;
	CheckBox isActive;
	CheckBox isAdmin;

	Button delete;

	Player player;
	
	public RegisterView()
	{
		super();
		player = getCurrentUser();
		addBack();
		String til = " user information:";
		addTitle((player == null) ? "Enter" + til : "Update" + til);
		HorizontalLayout top = new HorizontalLayout();
		top.setSpacing(true);
		VerticalLayout left = new VerticalLayout();
		left.setWidth("24em");
		top.addComponent(left);
		addComponent(top);
		first = new TextField("First Name:");
		first.setImmediate(true);
		first.setRequired(true);
		first.setWidth("100%");
		left.addComponent(first);
		last = new TextField("Last Name:");
		last.setImmediate(true);
		last.setRequired(true);
		last.setWidth("100%");
		left.addComponent(last);
		email = new TextField("Email:");
		email.setImmediate(true);
		email.setRequired(true);
		email.setWidth("100%");
		left.addComponent(email);
		phone = new TextField("Phone:");
		phone.setWidth("100%");
		//phone.setImmediate(true);
		phone.addBlurListener(e -> phoneChanged());
		left.addComponent(phone);
		if (player != null && player.getPassword().isEmpty())
		{
			left.addComponent(createSpacer());
			Label l = new Label("Your password is blank.   You must enter a password.");
			l.addStyleName("biggreentext");
			left.addComponent(l);
			left.addComponent(createSpacer());
		}
		pass = new PasswordField("Password:");
		pass.setImmediate(true);
		pass.setRequired(true);
		pass.setWidth("100%");
		left.addComponent(pass);
		pass2 = new PasswordField("Password (Confirm):");
		pass2.setImmediate(true);
		pass2.setRequired(true);
		pass2.setWidth("100%");
		left.addComponent(pass2);
		HorizontalLayout h = new HorizontalLayout();
		h.setSpacing(true);
		left.addComponent(h);
		isMale = new CheckBox("Male");
		isMale.setValue(true);
		isMale.addValueChangeListener(event -> isFemale.setValue(! isMale.getValue()));
		h.addComponent(isMale);
		isFemale = new CheckBox("Female");
		isFemale.addValueChangeListener(event -> isMale.setValue(! isFemale.getValue()));
		h.addComponent(isFemale);
		isActive = new CheckBox("Is Active?");
		isActive.setValue(true);
		isActive.setVisible(false);
		left.addComponent(isActive);
		Button b = new Button(player == null ? "Create New User" : "Update User Profile");
		b.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		b.addClickListener(e -> create());
		left.addComponent(b);
		delete = new Button("Delete User");
		delete.addStyleName(ValoTheme.BUTTON_DANGER);
		delete.addClickListener(e -> delete());
		delete.setVisible(false);
		left.addComponent(delete);
		Tree t = buildTree();
		if (t != null)
			top.addComponent(t);
	}
	
	protected Tree buildTree()
	{
		if (player == null)
			return null;
		Tree tree = new Tree("Teams/Leagues/Matches");
		for (Team tm: player.getTeams())
		{
			String t = tm.getFullName();
			tree.addItem(t);
			boolean lgleaf = true;
			Iterator<League> lgs = tm.getMatches().keySet().iterator();
			while (lgs.hasNext())
			{
				lgleaf = false;
				League lg = lgs.next();
				String l = lg.getName();
				tree.addItem(l);
				tree.setParent(l, t);
				boolean mleaf = true;
				for (Match mtch : tm.getMatches(lg))
				{
					mleaf = false;
					String  m = mtch.getDescription(false) + " (" + mtch.getRound().getName() + ")";
					tree.addItem(m);
					tree.setParent(m, l);
					tree.setChildrenAllowed(m, false);
				}
				if (mleaf)
					tree.setChildrenAllowed(l, false);
				//tree.expandItemsRecursively(l);
			}
			if (lgleaf)
				tree.setChildrenAllowed(t, false);

		}	
        for (Object itemId: tree.getItemIds())
            tree.expandItem(itemId);
		return tree;
	}
	
	protected void phoneChanged()
	{
		Player p = new Player();
		p.setPhone(phone.getValue());
		phone.setValue(p.getPhoneDisplay());
	}
	
	protected void create()
	{
		if (pass.getValue().isEmpty() && !getLoginUser().isAdmin())
		{
			Notification.show("You MUST enter a Password.", Type.WARNING_MESSAGE);
			return;
		}

		if (!pass.getValue().equals(pass2.getValue()))
		{
			Notification.show("Password fields DO NOT MATCH.", Type.WARNING_MESSAGE);
			return;
		}
		Player p = player;
		if (p == null)
			p = new Player();
		p.setFirst(first.getValue());
		p.setLast(last.getValue());
		p.setEmail(email.getValue());
		p.setPassword(pass.getValue());
		p.setMale(isMale.getValue());
		p.setActive(isActive.getValue());
		p.setPhone(phone.getValue());
		if (p.isInValid())
		{
			Notification.show("Unable to update.  Some required fields are empty.", Type.WARNING_MESSAGE);
			return;
		}
		Player existing = getHood().find(p);
		if (existing != null)
		{
			if (player == null)
			{
				Notification.show("Unable to create new user. '" + p.getEmail() + "' already exists.", Type.WARNING_MESSAGE);
				return;
			}
			else
			{
				if (existing != p)
				{
					Notification.show("Unable to update user. '" + p.getEmail() + "' is already used by " + existing.firstLastName(), Type.WARNING_MESSAGE);
					return;
				}
			}
		}
		getHood().addPlayer(p);
		getHood().saveData();
		setCurrentUser(p);
		backClicked();
	}
	
	protected void delete()
	{

		ConfirmDialog.show(getP2tennisUI(), 
				"Please Confirm:", "Are you really sure you want to delete \"" + player.firstLastName() + "\"?",
		        "I am", "Not quite", new ConfirmDialog.Listener() {

		            public void onClose(ConfirmDialog dialog) {
		                if (dialog.isConfirmed()) {
		            		getHood().removePlayer(player);
		            		getHood().saveData();
		            		setCurrentUser(null);
		            		backClicked();
		                }
		            }
		        });
	
	}
	
	@Override
	public void enter(ViewChangeEvent event)
	{
		if (player != null)
		{
			first.setValue(player.getFirst());
			last.setValue(player.getLast());
			email.setValue(player.getEmail());
			pass.setValue(player.getPassword());
			pass2.setValue(player.getPassword());
			isMale.setValue(player.isMale());
			isActive.setValue(player.isActive());
			phone.setValue(player.getPhoneDisplay());
			delete.setVisible(getLoginUser().isAdmin());
			delete.setEnabled(player.canDelete());
		}
		isActive.setVisible(getLoginUser().isAdmin());
	}

}
