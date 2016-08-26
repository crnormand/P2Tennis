package net.parsonsrun.domain;

import java.io.Serializable;

public abstract class NamedDomainObject extends DomainObject
{
	private static final long serialVersionUID = 1L;

	protected String name;
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}
