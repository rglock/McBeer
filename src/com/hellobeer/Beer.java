package com.hellobeer;

public class Beer implements Comparable<Beer>
{
	String name;
	boolean found = false;
	
	public Beer(String name)
	{
		this.name = name;
	}

	@Override
	public int compareTo(Beer other)
	{
		return name.compareTo(other.name);
	}
}
