package tideengine.serialized;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

public class Stations extends TideDataObject implements Serializable
{
  @SuppressWarnings("compatibility:-4066682052950242165")
  private final static long serialVersionUID = 1L;
  
  private Map<String, Station> stations = new HashMap<String, Station>();

  public Map<String, Station> getStations()
  {
    return stations;
  }


  public static class Station implements Serializable
  {
    @SuppressWarnings("compatibility:7385357192904270225")
    private final static long serialVersionUID = 1L;

    private String fullName = "";
    private Vector<String> nameParts = new Vector<String>();
    private StationPosition position = null;
    private TimeZone tz = null; 
    private BaseHeight baseHeight = null;
    private Map<String, HarmonicCoeff> harmonicCoeffList = new HashMap<String, HarmonicCoeff>();
  
    public Station(String fn, Vector<String> names, StationPosition pos, TimeZone tz, BaseHeight bh)
    {
      this.fullName = fn;
      this.nameParts = names;
      this.position = pos;
      this.tz = tz;
      this.baseHeight = bh;
    }

    public String getFullName()
    {
      return fullName;
    }

    public Vector<String> getNameParts()
    {
      return nameParts;
    }

    public Stations.StationPosition getPosition()
    {
      return position;
    }

    public TimeZone getTz()
    {
      return tz;
    }

    public Stations.BaseHeight getBaseHeight()
    {
      return baseHeight;
    }

    public Map<String, Stations.HarmonicCoeff> getHarmonicCoeffList()
    {
      return harmonicCoeffList;
    }
  }
  
  public static class StationPosition implements Serializable
  {
    @SuppressWarnings("compatibility:7401952547164309223")
    private final static long serialVersionUID = 1L;

    private double latitude = 0d;
    private double longitude = 0d;
    
    public StationPosition(double l, double g)
    {
      this.latitude = l;
      this.longitude = g;
    }

    public double getLatitude()
    {
      return latitude;
    }

    public double getLongitude()
    {
      return longitude;
    }
  }
  
  public static class BaseHeight implements Serializable
  {
    @SuppressWarnings("compatibility:4451565490372475818")
    private final static long serialVersionUID = 1L;

    private double height = 0d;
    private String unit = "";
    
    public BaseHeight(double d, String s)
    {
      this.height = d;
      this.unit = s;
    }

    public double getHeight()
    {
      return height;
    }

    public String getUnit()
    {
      return unit;
    }
  }
  
  public static class HarmonicCoeff implements Serializable
  {
    @SuppressWarnings("compatibility:4986896959431299749")
    private final static long serialVersionUID = 1L;

    private int rnk = 0;
    private String name = "";
    private float amplitude = 0f;
    private float epoch = 0f;
    
    public HarmonicCoeff(int i, String s, float a, float e)
    {
      this.rnk = i;
      this.name = s;
      this.amplitude = a;
      this.epoch = e;
    }

    public int getRnk()
    {
      return rnk;
    }

    public String getName()
    {
      return name;
    }

    public float getAmplitude()
    {
      return amplitude;
    }

    public float getEpoch()
    {
      return epoch;
    }
  }  
}
