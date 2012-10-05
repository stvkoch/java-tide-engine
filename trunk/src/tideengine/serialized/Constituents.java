package tideengine.serialized;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

public class Constituents extends TideDataObject implements Serializable
{
  @SuppressWarnings("compatibility:2145474960542046771")
  private final static long serialVersionUID = 1L;
  private Map<String, ConstSpeed> constSpeedMap = new HashMap<String, ConstSpeed>();
  
  public Map<String, ConstSpeed> getConstSpeedMap()
  {
    return constSpeedMap;
  }
    
  public static class ConstSpeed implements Serializable
  {
    @SuppressWarnings("compatibility:7961834886107806402")
    private final static long serialVersionUID = 1L;

    private int idx = 0;
    private String coeffName = "";
    private double coeffValue = 0d;
    private Map<Integer, Float> equilibrium = new HashMap<Integer, Float>();
    private Map<Integer, Float> factors     = new HashMap<Integer, Float>();
    
    public ConstSpeed(int idx, String name, double val)
    {
      this.idx = idx;
      this.coeffName = name;
      this.coeffValue = val;
    }
    
    public void putEquilibrium(int year, float val)
    {
      equilibrium.put(new Integer(year), new Float(val));
    }
    public void putFactor(int year, float val)
    {
      factors.put(new Integer(year), new Float(val));
    }

    public String getCoeffName()
    {
      return coeffName;
    }

    public double getCoeffValue()
    {
      return coeffValue;
    }

    public Map<Integer, Float> getEquilibrium()
    {
      return equilibrium;
    }

    public Map<Integer, Float> getFactors()
    {
      return factors;
    }
  }
}
