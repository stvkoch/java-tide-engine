package tideengine.tests;

import tideengine.BackEndTideComputer;

public class SampleFive
{
  public static void main(String[] args) throws Exception
  {
    long[] elapsed = { 0L, 0L, 0L };
    long before = 0L, after = 0L;
    BackEndTideComputer.setVerbose(false);
    for (int i=0; i<5; i++)
    {
      before = System.currentTimeMillis();
      BackEndTideComputer.connect(BackEndTideComputer.XML_OPTION);
      after = System.currentTimeMillis();
      elapsed[0] += (after - before);
      BackEndTideComputer.disconnect();

      before = System.currentTimeMillis();
      BackEndTideComputer.connect(BackEndTideComputer.JAVA_SERIALIZED_OPTION);
      after = System.currentTimeMillis();
      elapsed[1] += (after - before);
      BackEndTideComputer.disconnect();

      before = System.currentTimeMillis();
      BackEndTideComputer.connect(BackEndTideComputer.JSON_SERIALIZED_OPTION);
      after = System.currentTimeMillis();
      elapsed[2] += (after - before);
      BackEndTideComputer.disconnect();
    }
    System.out.println("-------------------------------------");
    System.out.println("XML:" + Long.toString(elapsed[0] / 5) +
                     "  JavaSer:" + Long.toString(elapsed[1] / 5) +
                     "  json:" + Long.toString(elapsed[2] / 5));
  }
}
