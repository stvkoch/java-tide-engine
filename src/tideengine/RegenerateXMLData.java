package tideengine;


public class RegenerateXMLData
{
  public static void main(String[] args) throws Exception
  {
    System.out.println(args.length + " Argument(s)...");

//  String harmonicName = "harmonics_06_14_2004.txt";
    String harmonicName = "harmonics_06_14_2004_fr.txt";
//  String harmonicName1 = "harmonics-dwf-20110410-free.txt";
//  String harmonicName2 = "harmonics-dwf-20110410-nonfree.txt";
    
    BackEndXMLTideComputer.setVerbose(true);
    long before = System.currentTimeMillis();
    try { BackEndXMLTideComputer.generateXML(harmonicName); } catch (Exception ex) { ex.printStackTrace(); }
//  try { BackEndTideComputer.generateXML(new String[] { harmonicName1, 
//                                                       harmonicName2 }); } catch (Exception ex) { ex.printStackTrace(); }
    long after = System.currentTimeMillis();
    System.out.println("It took " + Long.toString(after - before) + " ms to rebuild the XML files.");
  }
}
