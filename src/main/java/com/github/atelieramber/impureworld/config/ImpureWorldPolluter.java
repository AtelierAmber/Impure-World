package com.github.atelieramber.impureworld.config;

import java.util.List;

public class ImpureWorldPolluter {

	public String registryName;
	public String direction;
	public Emissions emissions;
	public float frequency;
	public String type;
	public List<PolluterExtras> extras;
	public List<PolluterProperties> properties;

	public class Emissions
	{
	  public float carbon;
	  public float sulfur;
	  public float particulate;
	}

	public class PolluterExtras
	{
	  public String name;
	  public String target;
	  Emissions emissions;
	}

	public class PolluterProperties
	{
	  public String name;
	  public String value;
	  public String handling;
	}	
	
}
