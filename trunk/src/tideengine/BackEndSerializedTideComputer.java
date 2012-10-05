package tideengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xml.sax.InputSource;

import tideengine.serialized.Constituents;
import tideengine.serialized.Stations;
import tideengine.serialized.TideDataObject;


public class BackEndSerializedTideComputer
{
  public final static String ARCHIVE_STREAM      = "ser/ser.zip"; 
  public final static String CONSTITUENTS_ENTRY  = "constituents.ser";
  public final static String STATIONS_ENTRY      = "stations.ser";

  private static boolean verbose  = false;
    
  public static TideDataObject loadObject(String zipStream, String entryName) throws Exception
  {    
    return loadObject(getZipInputStream(zipStream, entryName));
  }
    
  public static TideDataObject loadObject(String resource) throws Exception
  {
    InputStream is = new FileInputStream(new File(resource));
    return loadObject(is);
  }

  public static TideDataObject loadObject(InputStream resource) throws Exception 
  {
    TideDataObject  tideObject = null;
    try
    {
      ObjectInputStream ois = new ObjectInputStream(resource);
      tideObject = (TideDataObject)ois.readObject();
      ois.close();
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return tideObject;
  }
  
  public static List<Coefficient> buildSiteConstSpeed(Constituents doc) throws Exception
  {
    List<Coefficient> csal = new ArrayList<Coefficient>();
    Map<String, Constituents.ConstSpeed> csm = doc.getConstSpeedMap();
    Set<String> keys = csm.keySet();    
    for (String k : keys)
    {
      Constituents.ConstSpeed cs = csm.get(k);
      Coefficient coef = new Coefficient(cs.getCoeffName(), cs.getCoeffValue() * TideUtilities.COEFF_FOR_EPOCH);
      csal.add(coef);
    }
    return csal;
  }
  
  public static double getAmplitudeFix(Constituents doc, int year, String name) throws Exception
  {
    double d = 0;
    try
    {
      Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
      float f = cs.getFactors().get(year);
      d = f;    
    }
    catch (Exception ex)
    {
      System.err.println("Error for [" + name + "] in [" + year + "]");
      throw ex;
    }
    return d;
  }
  
  public static double getEpochFix(Constituents doc, int year, String name) throws Exception
  {
    double d = 0;
    try
    {
      Constituents.ConstSpeed cs = doc.getConstSpeedMap().get(name);
      float f = cs.getEquilibrium().get(year);
      d = f * TideUtilities.COEFF_FOR_EPOCH;    
    }
    catch (Exception ex)
    {
      System.err.println("Error for [" + name + "] in [" + year + "]");
      throw ex;
    }
    return d;
  }
  
  public static InputStream getZipInputStream(String zipStream, String entryName) throws Exception
  {
    ZipInputStream zip = new ZipInputStream(BackEndSerializedTideComputer.class.getResourceAsStream(zipStream));
    InputStream is = null;
    boolean go = true;
    while (go)
    {
      ZipEntry ze = zip.getNextEntry();
      if (ze == null)
        go = false;
      else
      {
        if (ze.getName().equals(entryName))
        {
          is = zip;
          go = false;
        }
      }
    }
    if (is == null)
    {
      throw new RuntimeException("Entry " + entryName + " not found in " + zipStream.toString());
    }
    return is;    
  }
  
  public static InputSource getZipInputSource(String zipStream, String entryName) throws Exception
  {
    ZipInputStream zip = new ZipInputStream(BackEndSerializedTideComputer.class.getResourceAsStream(zipStream));
    InputSource is = null;
    boolean go = true;
    while (go)
    {
      ZipEntry ze = zip.getNextEntry();
      if (ze == null)
        go = false;
      else
      {
        if (ze.getName().equals(entryName))
        {
          is = new InputSource(zip);
          is.setEncoding("ISO-8859-1");
          go = false;
        }
      }
    }
    if (is == null)
    {
      throw new RuntimeException("Entry " + entryName + " not found in " + zipStream.toString());
    }
    return is;    
  }
  
  public static TideStation findTideStation(String stationName, int year, Constituents constituents, Stations stations) throws Exception
  {
    long before = System.currentTimeMillis();
    TideStation ts = null;
    Stations.Station station = stations.getStations().get(stationName);
    ts = new TideStation();
    ts.setFullName(station.getFullName());
    ts.setBaseHeight(station.getBaseHeight().getHeight());
    ts.setUnit(station.getBaseHeight().getUnit());
    ts.setLatitude(station.getPosition().getLatitude());
    ts.setLongitude(station.getPosition().getLongitude());
    ts.setTimeZone(station.getTz().getID());
    for (String n : station.getNameParts())
      ts.getNameParts().add(n);
    Set<String> keys = station.getHarmonicCoeffList().keySet();
    for (String k : keys)
    {
      Stations.HarmonicCoeff hc = station.getHarmonicCoeffList().get(k);
      ts.getHarmonics().add(new Harmonic(hc.getName(), hc.getAmplitude(), hc.getEpoch() * TideUtilities.COEFF_FOR_EPOCH));
    }    
    long after = System.currentTimeMillis();
    if (verbose) System.out.println("Finding the node took " + Long.toString(after - before) + " ms");
    
    // Fix for the given year
//  System.out.println("We are in " + year);
    // Correction to the Harmonics
    for (Harmonic harm : ts.getHarmonics())
    {
      String name = harm.getName();
      if (!"x".equals(name))
      {
        double amplitudeFix = getAmplitudeFix(constituents, year, name);
        double epochFix     = getEpochFix(constituents, year, name);
        
        harm.setAmplitude(harm.getAmplitude() * amplitudeFix);
        harm.setEpoch(harm.getEpoch() - epochFix);
      }
    }
    if (verbose) System.out.println("Sites coefficients of [" + ts.getFullName() + "] fixed for " + year);
    
    return ts;
  }
  
  public static List<TideStation> getStationData(Stations stations) throws Exception
  {
    long before = System.currentTimeMillis();
    List<TideStation> stationData = new ArrayList<TideStation>();
    Set<String> keys = stations.getStations().keySet();
    for (String k : keys)
    {
      try
      {
        Stations.Station station = stations.getStations().get(k);
        TideStation ts = null;
        ts = new TideStation();
        ts.setFullName(station.getFullName());
        ts.setBaseHeight(station.getBaseHeight().getHeight());
        ts.setUnit(station.getBaseHeight().getUnit());
        ts.setLatitude(station.getPosition().getLatitude());
        ts.setLongitude(station.getPosition().getLongitude());
        ts.setTimeZone(station.getTz().getID());
        for (String n : station.getNameParts())
          ts.getNameParts().add(n);
        Set<String> ckeys = station.getHarmonicCoeffList().keySet();
        for (String ck : ckeys)
        {
          Stations.HarmonicCoeff hc = station.getHarmonicCoeffList().get(ck);
          ts.getHarmonics().add(new Harmonic(hc.getName(), hc.getAmplitude(), hc.getEpoch() * TideUtilities.COEFF_FOR_EPOCH));
        }    
        stationData.add(ts);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    long after = System.currentTimeMillis();
    if (verbose) System.out.println("Finding all the stations took " + Long.toString(after - before) + " ms");
    
    return stationData;
  }
  
  public static void setVerbose(boolean verbose)
  {
    BackEndSerializedTideComputer.verbose = verbose;
  }
}
