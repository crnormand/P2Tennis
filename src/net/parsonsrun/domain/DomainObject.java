package net.parsonsrun.domain;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Properties;

import com.vaadin.ui.UI;

import net.parsonsrun.P2tennisv2UI;

public abstract class DomainObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	protected Properties properties;

	public P2tennisv2UI getP2tennisUI()
	{
		return (P2tennisv2UI) UI.getCurrent();
	}
	
	protected Properties getProperties()
	{
		if (properties == null)
			properties = new Properties();
		return properties;
	}
	
	public String get(String key)
	{
		return getProperties().getProperty(key);
	}
	public boolean getBoolean(String key)
	{
		return "true".equalsIgnoreCase(get(key));
	}
	public int getInt(String key)
	{
		try
		{
			return Integer.parseInt(get(key));
		} 
		catch (NumberFormatException e) 
		{
			return 0;
		}
	}
	
	public boolean showElo()
	{
		return getP2tennisUI().showElo();
	}
	
	public static void newline(StringBuilder sb)
	{
		sb.append('\n');
	}
	
    public void setVar(String fieldName, String value) 
    {
    	try
    	{
	        Field field = this.getClass().getDeclaredField(fieldName);
	        if (field.getType() == Character.TYPE) {field.set(getClass(), value.charAt(0)); return;}
	        if (field.getType() == Short.TYPE) {field.set(getClass(), Short.parseShort(value)); return;}
	        if (field.getType() == Integer.TYPE) {field.set(getClass(), Integer.parseInt(value)); return;}
	        if (field.getType() == Long.TYPE) {field.set(getClass(), Long.parseLong(value)); return;}
	        if (field.getType() == Float.TYPE) {field.set(getClass(), Float.parseFloat(value)); return;}
	        if (field.getType() == Double.TYPE) {field.set(getClass(), Double.parseDouble(value)); return;}
	        if (field.getType() == Byte.TYPE) {field.set(getClass(), Byte.parseByte(value)); return;}
	        if (field.getType() == Boolean.TYPE) {field.set(getClass(), Boolean.parseBoolean(value)); return;}
	        field.set(getClass(), value);
    	}
    	catch (Exception e)
    	{	
    		System.out.println("Exception " + e + "\nUnable to set field '" + fieldName + "' to " + value);
    	}
    }
    
    public void put(String k, String v)
    {
    	getProperties().setProperty(k, v);
    }
}
