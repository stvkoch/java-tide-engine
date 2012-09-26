package tideengine;


import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;


public class SampleTwo
{
  private final static SimpleDateFormat SDF_FULL = new SimpleDateFormat("yyyy-MMM-dd HH:mm z (Z)");
//private final static SimpleDateFormat SDF      = new SimpleDateFormat("yyyy-MMM-dd HH:mm");
  
//private final static SimpleDateFormat TZ_OFFSET = new SimpleDateFormat("z (Z)");
  
  public static void main(String[] args) throws Exception
  {
    System.out.println(args.length + " Argument(s)...");

    System.out.println("XML Tests");
    BackEndTideComputer.connect(BackEndTideComputer.XML_OPTION);
    BackEndTideComputer.setVerbose(false);
    
    List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();
    System.out.println("SpeedCoefficients OK");
    Calendar now = GregorianCalendar.getInstance(); // No need to set a time zone. It's "now" everywhere.

    System.out.println("Now:" + now.getTime());
//  System.out.println("Now:" + SDF.format(now.getTime()));

    tideTest("Kodiak, Women's Bay, Alaska",                        constSpeed, now);
    tideTest("Baltimore (Chesapeake Bay), Maryland",               constSpeed, now);
    tideTest("Oyster Point Marina, San Francisco Bay, California", constSpeed, now);

    BackEndTideComputer.disconnect();
  }
  
  private static void tideTest(String location, List<Coefficient> constSpeed, Calendar now) throws Exception
  {
    TideStation ts = null;
    
    long before = 0;
    long after = 0;

    ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
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
      int nbIteration = 0;
      before = System.currentTimeMillis();
      for (int h=0; h<24; h++)
      {
        for (int m=0; m<60; m++)
        {
          nbIteration++;
          Calendar cal = new GregorianCalendar();
      //  cal.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
          cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
          cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
          cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
          cal.set(Calendar.HOUR_OF_DAY, h);
          cal.set(Calendar.MINUTE, m);
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
      System.out.println("High-Low water Calculation took " + Long.toString(after - before) + " ms (" + nbIteration + " iteration(s))");
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
      
//      TZ_OFFSET.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
//      String tzOffset = TZ_OFFSET.format(now.getTime());

//      for (TimedValue tv : timeAL)
//        System.out.println(tv.getType() + " " + SDF.format(tv.getCalendar().getTime()) + " " + tzOffset + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit());
//      System.out.println("---------------------");      
      SDF_FULL.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
      for (TimedValue tv : timeAL)
        System.out.println(" -- " + tv.getType() + " " + SDF_FULL.format(tv.getCalendar().getTime()) + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit());
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