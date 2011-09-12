package tideengine;

import java.util.ArrayList;

public class TideStation
{
  public final static String METERS        = "meters";
  public final static String FEET          = "feet";
  public final static String KNOTS         = "knots";
  public final static String SQUARE_KNOTS  = "knots^2";
  
  private String fullName = "";
  private ArrayList<String> nameParts = new ArrayList<String>();
  private double latitude = 0D;
  private double longitude = 0D;
  private double baseHeight = 0D;
  private String unit = "";
  private String timeZone = "";
  private String timeOffset = "";
  private ArrayList<Harmonic> harmonics = new ArrayList<Harmonic>();

  public void setFullName(String fullName)
  {
    this.fullName = fullName;
  }

  public String getFullName()
  {
    return fullName;
  }

  public ArrayList<String> getNameParts()
  {
    return nameParts;
  }

  public void setLatitude(double latitude)
  {
    this.latitude = latitude;
  }

  public double getLatitude()
  {
    return latitude;
  }

  public void setLongitude(double longitude)
  {
    this.longitude = longitude;
  }

  public double getLongitude()
  {
    return longitude;
  }

  public void setBaseHeight(double baseHeight)
  {
    this.baseHeight = baseHeight;
  }

  public double getBaseHeight()
  {
    return baseHeight;
  }

  public void setUnit(String unit)
  {
    this.unit = unit;
  }

  public String getUnit()
  {
    return unit;
  }

  public ArrayList<Harmonic> getHarmonics()
  {
    return harmonics;
  }

  public void setTimeZone(String timeZone)
  {
    this.timeZone = timeZone;
  }

  public String getTimeZone()
  {
    return timeZone;
  }

  public void setTimeOffset(String timeOffset)
  {
    this.timeOffset = timeOffset;
  }

  public String getTimeOffset()
  {
    return timeOffset;
  }

  public boolean isCurrentStation()
  {
    return unit.startsWith(KNOTS);
  }

  public boolean isTideStation()
  {
    return !unit.startsWith(KNOTS);
  }

  public String getDisplayUnit()
  {
    if (unit.equals(SQUARE_KNOTS))
      return KNOTS;
    else
      return unit;
  }
}
