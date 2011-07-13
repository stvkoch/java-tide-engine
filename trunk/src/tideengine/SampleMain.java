package tideengine;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import java.util.TimeZone;

import oracle.xml.parser.v2.XMLDocument;

public class SampleMain
{
  public static void main(String[] args) throws Exception
  {
    System.out.println(args.length + " Argument(s)...");
    
    BackEndTideComputer.setVerbose(false);
    
    if (false) // Regenerate XML files...
    {
      String harmonicName = "harmonics_06_14_2004.txt";
      long before = System.currentTimeMillis();
      try { BackEndTideComputer.generateXML(harmonicName); } catch (Exception ex) { ex.printStackTrace(); }
      long after = System.currentTimeMillis();
      System.out.println("It took " + Long.toString(after - before) + " ms to rebuild the XML files.");
    }
    
    // Some tests
    if (true)
    {
      BackEndTideComputer.TideStation ts = null;
      
      long before = System.currentTimeMillis();
      XMLDocument constituents = BackEndTideComputer.loadDOM(BackEndTideComputer.CONSTITUENT_FILE);
      long after = System.currentTimeMillis();
      System.out.println("DOM loading took " + Long.toString(after - before) + " ms");

      ArrayList<BackEndTideComputer.Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed(constituents);
      
      Calendar now = GregorianCalendar.getInstance();
      
      String location = "Port Townsend";
      ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
      if (ts != null)
      {
        double[] mm = BackEndTideComputer.getMinMaxWH(ts, constSpeed, now);
        System.out.println("At " + location + " in " + now.get(Calendar.YEAR) + ", min : " + BackEndTideComputer.DF22.format(mm[BackEndTideComputer.MIN_POS]) + " " + ts.getUnit() + ", max : " + BackEndTideComputer.DF22.format(mm[BackEndTideComputer.MAX_POS]) + " " + ts.getDisplayUnit());
        double wh = BackEndTideComputer.getWaterHeight(ts, constSpeed, now);
        System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + BackEndTideComputer.DF22.format(wh) + " " + ts.getDisplayUnit());
      }

      location = "Fare Ute";
      ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
      if (ts != null)
      {
        double wh = BackEndTideComputer.getWaterHeight(ts, constSpeed, now);
        System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + BackEndTideComputer.DF22.format(wh) + " " + ts.getDisplayUnit());
      }

      location = "Oyster Point Marina";
      ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
      if (ts != null)
      {
        double wh = BackEndTideComputer.getWaterHeight(ts, constSpeed, now);
        System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + BackEndTideComputer.DF22.format(wh) + " " + ts.getDisplayUnit());
      }

      location = "Alcatraz (North Point)";
      ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
      if (ts != null)
      {
        double wh = BackEndTideComputer.getWaterHeight(ts, constSpeed, now);
        System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + BackEndTideComputer.DF22.format(wh) + " " + ts.getDisplayUnit());
      }

      location = "Cape Cod Canal, Massachusetts Current";
      ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
      if (ts != null)
      {
        double wh = BackEndTideComputer.getWaterHeight(ts, constSpeed, now);
        System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + BackEndTideComputer.DF22.format(wh) + " " + ts.getDisplayUnit());
      }
      
      // Oyster Point for today (every 30 minutes)
      location = "Oyster Point Marina";
      ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
      if (ts != null)
      {
        TimeZone tz = TimeZone.getDefault();
        before = System.currentTimeMillis();
        for (int h=0; h<24; h++)
        {
          for (int m=0; m<60; m+=30)
          {
            Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
                                                 now.get(Calendar.MONTH),
                                                 now.get(Calendar.DAY_OF_MONTH),
                                                 h, m);
            
            double wh = BackEndTideComputer.getWaterHeight(ts, constSpeed, cal);
            TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(wh) + " " + ts.getDisplayUnit());
            TimeZone.setDefault(tz);
          }
        }
        after = System.currentTimeMillis();
        System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
      }
      
      // A test, CSV format, for a spreadsheet
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
   // location = "Oyster Point Marina";
   // ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
      if (ts != null)
      {
        TimeZone tz = TimeZone.getDefault();
        System.out.println("Date;Height (ft)");
        before = System.currentTimeMillis();
        for (int h=0; h<24; h++)
        {
          for (int m=0; m<60; m+=30)
          {
            Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
                                                 now.get(Calendar.MONTH),
                                                 now.get(Calendar.DAY_OF_MONTH),
                                                 h, m);
            
            double wh = BackEndTideComputer.getWaterHeight(ts, constSpeed, cal);
         // TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
            System.out.println(sdf.format(cal.getTime()) + ";" + BackEndTideComputer.DF22.format(wh));
         // TimeZone.setDefault(tz);
          }
        }
        after = System.currentTimeMillis();
        System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
      }

      // A test, High and Klow water at Oyster Point
   // location = "Oyster Point Marina";
   // ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
      if (ts != null)
      {
        final int RISING  =  1;
        final int FALLING = -1;
        
        double low1 = Double.NaN;
        double low2 = Double.NaN;
        double high1 = Double.NaN;
        double high2 = Double.NaN;
        Calendar low1Cal = null;
        Calendar low2Cal = null;
        Calendar high1Cal = null;
        Calendar high2Cal = null;
        int trend = 0;
        
        double previousWH = Double.NaN;
        before = System.currentTimeMillis();
        for (int h=0; h<24; h++)
        {
          for (int m=0; m<60; m++)
          {
            Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
                                                 now.get(Calendar.MONTH),
                                                 now.get(Calendar.DAY_OF_MONTH),
                                                 h, m);
            
            double wh = BackEndTideComputer.getWaterHeight(ts, constSpeed, cal);
            if (Double.isNaN(previousWH))
              previousWH = wh;
            else
            {
              if (trend == 0)
              {
                if (previousWH > wh)
                  trend = -1;
                else if (previousWH < wh)
                  trend = 1;
              }
              else
              {
                switch (trend)
                {
                  case RISING:
                    if (previousWH > wh) // Now going down
                    {
                      if (Double.isNaN(high1))
                      {
                        high1 = previousWH;
                        cal.add(Calendar.MINUTE, -1);
                        high1Cal = cal;
                      }
                      else
                      {
                        high2 = previousWH;
                        cal.add(Calendar.MINUTE, -1);
                        high2Cal = cal;
                      }
                      trend = FALLING; // Now falling
                    }
                    break;
                  case FALLING:
                    if (previousWH < wh) // Now going up
                    {
                      if (Double.isNaN(low1))
                      {
                        low1 = previousWH;
                        cal.add(Calendar.MINUTE, -1);
                        low1Cal = cal;
                      }
                      else
                      {
                        low2 = previousWH;
                        cal.add(Calendar.MINUTE, -1);
                        low2Cal = cal;
                      }
                      trend = RISING; // Now rising
                    }
                    break;
                }
              }
              previousWH = wh;
            }
          }
        }
        after = System.currentTimeMillis();
        System.out.println("High-Low water Calculation took " + Long.toString(after - before) + " ms");
        System.out.println("-- " + location + " --");
        if (low1Cal != null && high1Cal != null && low1Cal.compareTo(high1Cal) < 0)
        {
          if (low1Cal != null) System.out.println("LW " + low1Cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(low1) + " " + ts.getDisplayUnit());
          if (high1Cal != null) System.out.println("HW " + high1Cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(high1) + " " + ts.getDisplayUnit());
          if (low2Cal != null) System.out.println("LW " + low2Cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(low2) + " " + ts.getDisplayUnit());
          if (high2Cal != null) System.out.println("HW " + high2Cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(high2) + " " + ts.getDisplayUnit());
        }
        else
        {
          if (high1Cal != null) System.out.println("HW " + high1Cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(high1) + " " + ts.getDisplayUnit());
          if (low1Cal != null) System.out.println("LW " + low1Cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(low1) + " " + ts.getDisplayUnit());
          if (high2Cal != null) System.out.println("HW " + high2Cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(high2) + " " + ts.getDisplayUnit());
          if (low2Cal != null) System.out.println("LW " + low2Cal.getTime().toString() + " : " + BackEndTideComputer.DF22.format(low2) + " " + ts.getDisplayUnit());
        }
      }
    }
  }
}
