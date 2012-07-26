package tideengine;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;


public class SampleMain
{
  private final static SimpleDateFormat SDF = new SimpleDateFormat("yyy-MMM-dd HH:mm z (Z)");
  
  public static void main(String[] args) throws Exception
  {
    System.out.println(args.length + " Argument(s)...");
    boolean xmlTest = true;
    boolean dbTest  = false;
    
    if (xmlTest)
    {
      System.out.println("XML Tests");
      BackEndTideComputer.connect(BackEndTideComputer.XML_OPTION);
      BackEndTideComputer.setVerbose(false);
      
      if (false) // Regenerate XML files...
      {
  //    String harmonicName = "harmonics_06_14_2004.txt";
        String harmonicName = "harmonics_06_14_2004_fr.txt";
  //    String harmonicName1 = "harmonics-dwf-20110410-free.txt";
  //    String harmonicName2 = "harmonics-dwf-20110410-nonfree.txt";
        BackEndXMLTideComputer.setVerbose(true);
        long before = System.currentTimeMillis();
        try {
          BackEndXMLTideComputer.generateXML(harmonicName); } catch (Exception ex) { ex.printStackTrace(); }
  //    try { BackEndTideComputer.generateXML(new String[] { harmonicName1, 
  //                                                         harmonicName2 }); } catch (Exception ex) { ex.printStackTrace(); }
        long after = System.currentTimeMillis();
        System.out.println("It took " + Long.toString(after - before) + " ms to rebuild the XML files.");
      }
      
      // Some tests
      if (true)
      {
        TideStation ts = null;
        
        long before = 0;
        long after = 0;
  
        List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();
        
        Calendar now = GregorianCalendar.getInstance();
        String location = "Port Townsend";
        if (false)
        {
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            if (true)
            {
              double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
              System.out.println("At " + location + " in " + now.get(Calendar.YEAR) + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
            }
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
            if (false && ts.isTideStation())
              System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(TideUtilities.getWaterHeightIn(wh, ts, TideStation.METERS)) + " " + TideStation.METERS);
          }
        }
        if (false)
        {
          location = "Fare Ute";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
    
          location = "Oyster Point Marina";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
    
          location = "Shediac Bay, New Brunswick";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
          else
            System.out.println("-- " + location + " not found...");
    
          location = "Alcatraz (North Point)";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
    
          location = "Cape Cod Canal, Massachusetts Current";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
          
          // Oyster Point for today (every 30 minutes)
          location = "Oyster Point Marina";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
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
                
                double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
                TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
                System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + cal.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
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
         // TimeZone tz = TimeZone.getDefault();
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
                
                double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
             // TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
                System.out.println(sdf.format(cal.getTime()) + ";" + TideUtilities.DF22PLUS.format(wh));
             // TimeZone.setDefault(tz);
              }
            }
            after = System.currentTimeMillis();
            System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
          }
    
          // A test, High and Low water at Oyster Point
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
                
                double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
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
    
            List<TimedValue> timeAL = new ArrayList<TimedValue>(4);
            if (low1Cal != null)
              timeAL.add(new TimedValue("LW", low1Cal, low1));
            if (low2Cal != null)
              timeAL.add(new TimedValue("LW", low2Cal, low2));
            if (high1Cal != null)
              timeAL.add(new TimedValue("HW", high1Cal, high1));
            if (high2Cal != null)
              timeAL.add(new TimedValue("HW", high2Cal, high2));
            
            Collections.sort(timeAL);
            
            for (TimedValue tv : timeAL)
              System.out.println(tv.getType() + " " + tv.getCalendar().getTime().toString() + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit());
          }
        }
        // Kodiak for today (every 30 minutes)
        location = "Kodiak, Women's Bay, Alaska";
        ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));

        if (true)
        {
          double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
          System.out.println("At " + location + " in " + now.get(Calendar.YEAR) + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
        }

        now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
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
              cal.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
              double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
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
        
          List<TimedValue> timeAL = new ArrayList<TimedValue>(4);
          if (low1Cal != null)
            timeAL.add(new TimedValue("LW", low1Cal, low1));
          if (low2Cal != null)
            timeAL.add(new TimedValue("LW", low2Cal, low2));
          if (high1Cal != null)
            timeAL.add(new TimedValue("HW", high1Cal, high1));
          if (high2Cal != null)
            timeAL.add(new TimedValue("HW", high2Cal, high2));
          
          Collections.sort(timeAL);
          
          SDF.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));          
          for (TimedValue tv : timeAL)
            System.out.println(tv.getType() + " " + SDF.format(tv.getCalendar().getTime()) + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit());
        }
        if (true)
        {
          List<String[]> hcList = TideUtilities.getStationHarmonicConstituents(ts, constSpeed);
          for (String[] line  : hcList)
            System.out.println(line[0] + " " + line[1] + " " + line[2] + " " + line[3] + " " + line[4]);
        }
        if (false && ts != null)
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
              
              double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
              TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
              System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + cal.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
              TimeZone.setDefault(tz);
            }
          }
          after = System.currentTimeMillis();
          System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
        }
//      else
//        System.out.println(location + " not found...");
      }
      
      if (true)
      {
        System.out.println("-- Building Station Tree");
        TreeMap<String, TideUtilities.StationTreeNode> stationTree = TideUtilities.buildStationTree();
        System.out.println("Done");
      }
      BackEndTideComputer.disconnect();
    }
    if (dbTest)
    {
      System.out.println("DB Tests");
//    BackEndTideComputer.connect(BackEndTideComputer.SQL_OPTION);
      BackEndTideComputer.connect(BackEndTideComputer.SQLITE_OPTION);
      BackEndTideComputer.setVerbose(false);
      
      // Some tests, with SQL
      long before = 0;
      long after = 0;
      if (true)
      {
        TideStation ts = null;
        List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();      
        Calendar now = GregorianCalendar.getInstance();
        
        String location = "Port Townsend";
        if (true)
        {
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            if (true)
            {
              double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
              System.out.println("At " + location + " in " + now.get(Calendar.YEAR) + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
            }
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
            if (false && ts.isTideStation())
              System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(TideUtilities.getWaterHeightIn(wh, ts, TideStation.METERS)) + " " + TideStation.METERS);
          }
        }
        if (true)
        {
          location = "Fare Ute";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
    
          location = "Oyster Point Marina";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
    
          location = "Shediac Bay, New Brunswick";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
          else
            System.out.println("-- " + location + " not found...");
    
          location = "Alcatraz (North Point)";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
    
          location = "Cape Cod Canal, Massachusetts Current";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
          if (ts != null)
          {
            double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
            System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
          }
          
          // Oyster Point for today (every 30 minutes)
          location = "Oyster Point Marina";
          ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
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
                
                double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
                TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
                System.out.println((ts.isTideStation()?"Water Height":"Current Speed") + " in " + location + " at " + cal.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
                TimeZone.setDefault(tz);
              }
            }
            after = System.currentTimeMillis();
            System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
          }
          
          // A test, CSV format, for a spreadsheet
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        // location = "Oyster Point Marina";
        // ts = BackEndSQLTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
          if (ts != null)
          {
         // TimeZone tz = TimeZone.getDefault();
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
                
                double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
             // TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
                System.out.println(sdf.format(cal.getTime()) + ";" + TideUtilities.DF22PLUS.format(wh));
             // TimeZone.setDefault(tz);
              }
            }
            after = System.currentTimeMillis();
            System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
          }
    
          // A test, High and Klow water at Oyster Point
        // location = "Oyster Point Marina";
        // ts = BackEndSQLTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
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
                
                double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
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
    
            List<TimedValue> timeAL = new ArrayList<TimedValue>(4);
            if (low1Cal != null)
              timeAL.add(new TimedValue("LW", low1Cal, low1));
            if (low2Cal != null)
              timeAL.add(new TimedValue("LW", low2Cal, low2));
            if (high1Cal != null)
              timeAL.add(new TimedValue("HW", high1Cal, high1));
            if (high2Cal != null)
              timeAL.add(new TimedValue("HW", high2Cal, high2));
            
            Collections.sort(timeAL);
            
            for (TimedValue tv : timeAL)
              System.out.println(tv.getType() + " " + tv.getCalendar().getTime().toString() + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit());
          }
        }
        
        if (true)
        {
          System.out.println("-- Building Station Tree");
          TreeMap<String, TideUtilities.StationTreeNode> stationTree = BackEndTideComputer.buildStationTree();
          System.out.println(stationTree.size() + " station(s) in the tree (level one)");
//        Utilities.renderTree(stationTree, 0);    
          System.out.println("Done");
        }
      }
      BackEndTideComputer.disconnect();
    }
  }
  
  private static class TimedValue implements Comparable<TimedValue>
  {
    private Calendar cal;
    private double   value;
    private String   type = "";
    
    public TimedValue(String type, Calendar cal, double d)
    {
      this.type = type;
      this.cal = cal;
      this.value = d;
    }
    
    public int compareTo(TimedValue tv)
    {
      return this.cal.compareTo(tv.getCalendar());
    }

    public Calendar getCalendar()
    {
      return cal;
    }

    public double getValue()
    {
      return value;
    }

    public String getType()
    {
      return type;
    }
  }
}
