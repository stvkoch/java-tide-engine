package tideengine;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;

import java.net.URL;

import java.text.DecimalFormat;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BackEndTideComputer
{
  private final static double COEFF_FOR_EPOCH = 0.017453292519943289D;
  
  public final static String CONSTITUENT_FILE = "xml.data" + File.separator + "constituents.xml";
  public final static String STATION_FILE     = "xml.data" + File.separator + "stations.xml";
  
  public final static DecimalFormat DF22 = new DecimalFormat("#0.00");
  
  private final static String METERS        = "meters";
  private final static String FEET          = "feet";
  private final static String KNOTS         = "knots";
  private final static String SQUARE_KNOTS  = "knots^2";
  
  private static DOMParser parser = new DOMParser();
  private static boolean verbose = false;
  
  static 
  {
    DF22.setPositivePrefix("+");  
  }
  
  public static void generateXML(String harmonicName) throws Exception
  {
    final String LONGITUDE = "!longitude:";
    final String LATITUDE  = "!latitude:";
    
    BackEndTideComputer tc = new BackEndTideComputer();
    URL harmonic = tc.getClass().getResource(harmonicName);
    if (verbose) System.out.println("URL:" + harmonic.toString());

    BufferedReader br = new BufferedReader(new FileReader(harmonic.getFile()));
    BufferedWriter bw = null;
    
//  String idxLine = Integer.toString(0) + "\t" + harmonicName + "";
//  bw.write(idxLine + "\n");
    
    String line = "";
    long nbStations = 0;
    
    XMLDocument doc = new XMLDocument();
    XMLElement root = (XMLElement)doc.createElement("constituents");
    doc.appendChild(root);
    XMLElement speedConst = (XMLElement)doc.createElement("speed-constituents");
    root.appendChild(speedConst);
    // Step one: constituents
    while (line != null)
    {
      line = br.readLine();
      if (line != null && line.indexOf("# Number of constituents") > -1) // Aha!
      {
        line = br.readLine();
        /* int nbElements = */ Integer.parseInt(line);  // Not used for now... we just keep looping
        boolean skipComments = true;
        while (skipComments)
        {
          line = br.readLine();
          if (line == null || !line.startsWith("#"))
            skipComments = false;
        }
        boolean loopOnCoeff = true;
        int coeffIndex = 1;
        while (loopOnCoeff)
        {
          String[] constSpeed = line.split(" ");
          String coeffName = constSpeed[0];
          String coeffValue = "";
          for (int i=1; i<constSpeed.length; i++)
          {
            if (constSpeed[i].trim().length() > 0)
            {
              coeffValue = constSpeed[i];
              break;
            }
          }
          XMLElement coeff = (XMLElement)doc.createElement("const-speed"); // Speed in degree per solar hour
          speedConst.appendChild(coeff);
          coeff.setAttribute("idx", Integer.toString(coeffIndex++));
          XMLElement name = (XMLElement)doc.createElement("coeff-name");
          XMLElement value = (XMLElement)doc.createElement("coeff-value");
          coeff.appendChild(name);
          coeff.appendChild(value);
          Text nameValue = doc.createTextNode("#text");
          nameValue.setNodeValue(coeffName);
          name.appendChild(nameValue);
          Text valueValue = doc.createTextNode("#text");
          valueValue.setNodeValue(coeffValue);
          value.appendChild(valueValue);
          
          line = br.readLine();
          if (line == null || line.startsWith("#"))
            loopOnCoeff = false;
        }
        // Look for the origin of the years
        boolean keepLooping = true;
        while (keepLooping)
        {
          if (!line.startsWith("#"))
            keepLooping = false;
          else
            line = br.readLine();
        }
        int yearOrigin = Integer.parseInt(line);
        // Look for the number of years in the file for equilibrium
        line = br.readLine();        
        keepLooping = true;
        while (keepLooping)
        {
          if (!line.startsWith("#"))
            keepLooping = false;
          else
            line = br.readLine();
        }
        int nbYears = Integer.parseInt(line);
        // Now, loop on the coeffs
        line = br.readLine();
        keepLooping = true;
        while (keepLooping)
        {
          if (line.startsWith("*END*"))
            keepLooping = false;
          else
          {
            String coeffName = line.trim();
            String xPath = "/constituents/speed-constituents/const-speed[./coeff-name = '" + coeffName + "']";
            XMLElement coeffElement = (XMLElement)doc.selectNodes(xPath).item(0);
            XMLElement yearsHolder = (XMLElement)doc.createElement("equilibrium-arguments");
            coeffElement.appendChild(yearsHolder);
            int y = yearOrigin;
            loopOnCoeff = true;
            while (loopOnCoeff)
            {
              line = br.readLine();
              String[] coeff = line.split(" ");
              for (int i=0; i<coeff.length; i++)
              {
                try
                {
                  String s = coeff[i].trim();
                  if (s.length() > 0)
                  {
                    /* double d = */ Double.parseDouble(s);
                    XMLElement equ = (XMLElement)doc.createElement("equilibrium");
                    equ.setAttribute("year", Integer.toString(y++));
                    yearsHolder.appendChild(equ);
                    Text value = doc.createTextNode("#text");
                    value.setNodeValue(s);
                    equ.appendChild(value);
                  }
                }
                catch (NumberFormatException nfe)
                {
                  loopOnCoeff = false;
                  break;
                }
              }
            }
          }
        }
        // Look for the number of years in the file for node factors
        line = br.readLine();        
        keepLooping = true;
        while (keepLooping)
        {
          if (!line.startsWith("#"))
            keepLooping = false;
          else
            line = br.readLine();
        }
        nbYears = Integer.parseInt(line);
        // Now, loop on the coeffs
        line = br.readLine();
        keepLooping = true;
        while (keepLooping)
        {
          if (line.startsWith("*END*"))
            keepLooping = false;
          else
          {
            String coeffName = line.trim();
            String xPath = "/constituents/speed-constituents/const-speed[./coeff-name = '" + coeffName + "']";
            XMLElement coeffElement = (XMLElement)doc.selectNodes(xPath).item(0);
            XMLElement yearsHolder = (XMLElement)doc.createElement("node-factors");
            coeffElement.appendChild(yearsHolder);
            int y = yearOrigin;
            loopOnCoeff = true;
            while (loopOnCoeff)
            {
              line = br.readLine();
              String[] coeff = line.split(" ");
              for (int i=0; i<coeff.length; i++)
              {
                try
                {
                  String s = coeff[i].trim();
                  if (s.length() > 0)
                  {
                    /* double d = */ Double.parseDouble(s);
                    XMLElement fact = (XMLElement)doc.createElement("factor");
                    fact.setAttribute("year", Integer.toString(y++));
                    yearsHolder.appendChild(fact);
                    Text value = doc.createTextNode("#text");
                    value.setNodeValue(s);
                    fact.appendChild(value);
                  }
                }
                catch (NumberFormatException nfe)
                {
                  loopOnCoeff = false;
                  break;
                }
              }
            }
          }
        }
      }
    }
    br.close();
    // Spit out result here
    bw = new BufferedWriter(new FileWriter(CONSTITUENT_FILE));
    doc.print(bw);
    bw.close();
    
    // Stgep two: Stations
    doc = new XMLDocument();
    root = (XMLElement)doc.createElement("stations");
    doc.appendChild(root);

    br = new BufferedReader(new FileReader(harmonic.getFile()));
    line = "";
    while (line != null)
    {
      line = br.readLine();
      if (line != null)
      {
//      System.out.println("-> [" + line + "]");
        if (line.indexOf(LONGITUDE) > -1)
        {
          nbStations++;
          double lng = Double.parseDouble(line.substring(line.indexOf(LONGITUDE) + LONGITUDE.length()));
//        System.out.println("Longitude:" + lng);
          // Now, latitude
          line = br.readLine();
          if (line.indexOf(LATITUDE) > -1) // As expected
          {
            XMLElement station = (XMLElement)doc.createElement("station");
            root.appendChild(station);
            
            XMLElement position = (XMLElement)doc.createElement("position");
            
            double lat = Double.parseDouble(line.substring(line.indexOf(LATITUDE) + LATITUDE.length()));
//          System.out.println("Latitude:" + lat);
            position.setAttribute("latitude", Double.toString(lat));
            position.setAttribute("longitude", Double.toString(lng));
            // Station name
            line = br.readLine();
            station.setAttribute("name", line.trim());
            String[] stationElement = line.split(",");      
            XMLElement names = (XMLElement)doc.createElement("name-collection");
            
            for (int i=stationElement.length-1; i>=0; i--)
            {
//            System.out.println("-> " + stationElement[i].trim());
              XMLElement namePart = (XMLElement)doc.createElement("name-part");
              namePart.setAttribute("rnk", Integer.toString(stationElement.length - i));
              namePart.setAttribute("name", stationElement[i].trim());
              names.appendChild(namePart);
            }
            // Station Zone and time offset
            line = br.readLine();
            String[] zoneElement = line.split(" ");
//          System.out.println("Time offset: " + zoneElement[0]);
//          System.out.println("Zone :" + zoneElement[1].substring(1)); // To skip the ":" at the beginning
            XMLElement tz = (XMLElement)doc.createElement("time-zone");
            tz.setAttribute("offset", zoneElement[0]);
            tz.setAttribute("name", zoneElement[1].substring(1));
            // Base Height
            line = br.readLine();
//          System.out.println("Base Height: " + line);
            XMLElement baseHeight = (XMLElement)doc.createElement("base-height");
            String[] bh = line.split(" ");
            String val = bh[0];
            String unit = bh[1];
            baseHeight.setAttribute("value", val);
            baseHeight.setAttribute("unit", unit);
            
            station.appendChild(names);
            station.appendChild(position);
            station.appendChild(tz);
            station.appendChild(baseHeight);
            
            XMLElement data = (XMLElement)doc.createElement("station-data");
            station.appendChild(data);
            
            // Now, coefficients
            boolean keepLooping = true;
            int rnk = 1;
            while (keepLooping)
            {
              line = br.readLine();
              if (line == null || line.startsWith("#"))
                keepLooping = false;
              else
              {
                XMLElement harm = (XMLElement)doc.createElement("harmonic-coeff");
                data.appendChild(harm);
                harm.setAttribute("rnk", Integer.toString(rnk++));                
                String[] coeffElement = line.split(" ");
                harm.setAttribute("name", coeffElement[0]);
//              System.out.print(" -> Coeff: ");
                int coeffFound = 0;
                for (int i=1; i<coeffElement.length; i++)
                {
                  if (coeffElement[i].trim().length() > 0)
                  {
                    coeffFound++;
                    if (coeffFound == 1)
                      harm.setAttribute("amplitude", coeffElement[i].trim());
                    else if (coeffFound == 2)
                      harm.setAttribute("epoch", coeffElement[i].trim());
//                  System.out.print(coeffElement[i] + " ");
                  }
                }
              }
            }
          }
          else
            System.out.println("Warning! No " + LATITUDE + " found after " + LONGITUDE);
        }
        // else keep looping          
      }
    }
    br.close();
    // Spit out result here
    bw = new BufferedWriter(new FileWriter(STATION_FILE));
    doc.print(bw);
    bw.close();
    
    if (verbose) System.out.println("Done, " + nbStations + " Station(s)");
  }
    
  public static XMLDocument loadDOM(String resource) throws Exception 
  {
    XMLDocument doc = null;
    try
    {
      synchronized (parser)
      {
        parser.parse(new FileReader(resource));
        doc = parser.getDocument();
      }
    }
    catch (Exception ex)
    {
      throw ex;
    }
    return doc;
  }
  
  public static ArrayList<Coefficient> buildSiteConstSpeed(XMLDocument doc) throws Exception
  {
    ArrayList<Coefficient> csal = new ArrayList<Coefficient>();
    String xPath = "//const-speed";
    NodeList nl = doc.selectNodes(xPath);
    for (int i=0; i<nl.getLength(); i++)
    {
      String name = ((XMLElement)nl.item(i)).selectNodes("./coeff-name").item(0).getFirstChild().getNodeValue();
      double value = Double.parseDouble(((XMLElement)nl.item(i)).selectNodes("./coeff-value").item(0).getFirstChild().getNodeValue());
      Coefficient coef = new Coefficient(name, value * COEFF_FOR_EPOCH);
      csal.add(coef);
    }
    return csal;
  }
  
  public static double getAmplitudeFix(XMLDocument doc, int year, String name) throws Exception
  {
    double d = 0;
    try
    {
      String xPath = "//const-speed[./coeff-name = '" + name + "']/node-factors/factor[./@year='" + Integer.toString(year) + "']";
      NodeList nl = doc.selectNodes(xPath);
      d = Double.parseDouble (((XMLElement)nl.item(0)).getFirstChild().getNodeValue());    
    }
    catch (Exception ex)
    {
      System.err.println("Error for [" + name + "] in [" + year + "]");
      throw ex;
    }
    return d;
  }
  
  public static double getEpochFix(XMLDocument doc, int year, String name) throws Exception
  {
    double d = 0;
    try
    {
      String xPath = "//const-speed[./coeff-name = '" + name + "']/equilibrium-arguments/equilibrium[./@year='" + Integer.toString(year) + "']";
      NodeList nl = doc.selectNodes(xPath);
      d = Double.parseDouble (((XMLElement)nl.item(0)).getFirstChild().getNodeValue()) * COEFF_FOR_EPOCH;    
    }
    catch (Exception ex)
    {
      System.err.println("Error for [" + name + "] in [" + year + "]");
      throw ex;
    }
    return d;
  }
  
  public static double getWaterHeight(Date d, Date jan1st, TideStation ts, ArrayList<Coefficient> constSpeed)
  {
    double value = 0d;
    
    double stationBaseHeight = ts.getBaseHeight();
    long nbSecSinceJan1st = (d.getTime() - jan1st.getTime() ) / 1000L;
    double timeOffset = nbSecSinceJan1st * 0.00027777777777777778D;
    value = stationBaseHeight;
    for (int i=0; i<constSpeed.size(); i++)
      value += (ts.getHarmonics().get(i).getAmplitude() * Math.cos(constSpeed.get(i).getValue() * timeOffset - ts.getHarmonics().get(i).getEpoch()));
       
    if (ts.getUnit().indexOf("^2") > -1)
      value = (value >= 0.0D ? Math.sqrt(value): -Math.sqrt(-value));
    
    return value;
  }

  public static TideStation findTideStation(String stationName, int year, XMLDocument constituents) throws Exception
  {
    TideStation ts = null;
    long before = System.currentTimeMillis();
    StationFinder sf = new StationFinder();
    try
    {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      
      sf.setStationName(stationName);
      InputSource is = new InputSource(new FileInputStream(new File(STATION_FILE)));
      is.setEncoding("ISO-8859-1");
      saxParser.parse(is, sf);       
    }
    catch (DoneWithSiteException dwse)
    {
      ts = sf.getTideStation();
//    System.out.println("All right.");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
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
  
  public static double getWaterHeight(TideStation ts, ArrayList<Coefficient> constSpeed, Calendar when) throws Exception
  {
    double wh = 0d;
    if (ts != null)
    {      
      // Calculate min/max, for the graph
      int year = when.get(Calendar.YEAR); 
      // Calc Jan 1st of the current year
      Date jan1st = new GregorianCalendar(year, 0, 1).getTime();
      wh = getWaterHeight(when.getTime(), jan1st, ts, constSpeed);
//    System.out.println("Water Height in " + ts.getFullName() + " on " + now.toString() + " is " + DF22.format(wh) + " " + ts.getUnit());
    }
    else
      System.out.println("Ooch!");    
    return wh;
  }
  
  public final static int MIN_POS = 0;
  public final static int MAX_POS = 1;
  
  public static double[] getMinMaxWH(TideStation ts, ArrayList<Coefficient> constSpeed, Calendar when) throws Exception
  {
    double[] minMax = { 0d, 0d };
    if (ts != null)
    {      
      // Calculate min/max, for the graph
      int year = when.get(Calendar.YEAR); 
      // Calc Jan 1st of the current year
      Date jan1st = new GregorianCalendar(year, 0, 1).getTime();
      Date dec31st = new GregorianCalendar(year, 11, 31, 12, 0).getTime(); // 31 Dec, At noon
      double max = -Double.MAX_VALUE;
      double min =  Double.MAX_VALUE;
      Date date = jan1st;
      while (date.before(dec31st))
      {
        double d = getWaterHeight(date, jan1st, ts, constSpeed);
        max = Math.max(max, d);
        min = Math.min(min, d);

        date = new Date(date.getTime() + (7200 * 1000)); // Plus 2 hours
      }
  //  System.out.println("In " + year + ", Min:" + min + ", Max:" + max);
      minMax[MIN_POS] = min;
      minMax[MAX_POS] = max;
    }
    return minMax;
  }

  public static void setVerbose(boolean verbose)
  {
    BackEndTideComputer.verbose = verbose;
  }

  public static class StationFinder extends DefaultHandler
  {
    private String stationName = "";
    private TideStation ts = null;
    
    public void setStationName(String sn)
    {
      this.stationName = sn;
    }
    
    public TideStation getTideStation()
    {
      return ts;
    }

    private boolean foundStation        = false;
    private boolean foundNameCollection = false;
    private boolean foundStationData    = false;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
//    super.startElement(uri, localName, qName, attributes);
      if (!foundStation && "station".equals(qName))
      {
        String name = attributes.getValue("name");
        if (name.contains(this.stationName))
        {
          foundStation = true;
          ts = new TideStation();
          ts.setFullName(name);
        }
      }
      else if (foundStation)
      {
        if ("name-collection".equals(qName))
        {
          foundNameCollection = true;
        }
        else if ("name-part".equals(qName) && foundNameCollection)
        {
          ts.getNameParts().add(attributes.getValue("name"));
        }
        else if ("position".equals(qName))
        {
          ts.setLatitude(Double.parseDouble(attributes.getValue("latitude")));
          ts.setLongitude(Double.parseDouble(attributes.getValue("longitude")));
        }
        else if ("time-zone".equals(qName))
        {
          ts.setTimeZone(attributes.getValue("name"));
          ts.setTimeOffset(attributes.getValue("offset"));
        }
        else if ("base-height".equals(qName))
        {
          ts.setBaseHeight(Double.parseDouble(attributes.getValue("value")));
          ts.setUnit(attributes.getValue("unit"));
        }
        else if ("station-data".equals(qName))
        {
          foundStationData = true;
        }
        else if (foundStationData && "harmonic-coeff".equals(qName))
        {
          String name = attributes.getValue("name");
          double amplitude = Double.parseDouble(attributes.getValue("amplitude"));
          double epoch     = Double.parseDouble(attributes.getValue("epoch")) * COEFF_FOR_EPOCH;
          Harmonic h = new Harmonic(name, amplitude, epoch);
          ts.getHarmonics().add(h);
        }
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
      throws SAXException
    {
      super.endElement(uri, localName, qName);
      if (foundStation && "station".equals(qName))
      {
        foundStation = false;
        throw new DoneWithSiteException("Done with it.");
      }
      else if (foundNameCollection && "name-collection".equals(qName))
      {
        foundNameCollection = false;
      }
      else if (foundStationData && "station-data".equals(qName))
      {
        foundStationData = false;
      }
    }
  }
  
  public static class DoneWithSiteException extends SAXException
  {
    public DoneWithSiteException(String s)
    {
      super(s);
    }
  }
  
  public static class TideStation 
  {
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

    public ArrayList<BackEndTideComputer.Harmonic> getHarmonics()
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
  
  public static class Harmonic
  {
    private String name = "";
    private double amplitude = 0D;
    private double epoch = 0D;

    public Harmonic(String name, double ampl, double e)
    {
      this.name = name;
      this.amplitude = ampl;
      this.epoch = e;
    }
    
    public String getName()
    {
      return name;
    }

    public double getAmplitude()
    {
      return amplitude;
    }

    public double getEpoch()
    {
      return epoch;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public void setAmplitude(double amplitude)
    {
      this.amplitude = amplitude;
    }

    public void setEpoch(double epoch)
    {
      this.epoch = epoch;
    }
  }
  
  public static class Coefficient
  {
    private String name = "";
    private double value = 0D;

    public Coefficient(String name, double d)
    {
      this.name = name;
      this.value = d;
    }
    
    public String getName()
    {
      return name;
    }

    public double getValue()
    {
      return value;
    }
  }
}
