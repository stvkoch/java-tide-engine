Written in Java, and based on XML Data (SQL available too, through Hypersonic SQL and SQLite. And also serialized java classes, and json files).
<br>
Provides all the required API to calculate current speed or water height at any time for a given tide station.<br>
<br>
Comes with several samples illustrating the usage of the APIs.<br>
<br>
Originally using the data text file provided by <a href='http://www.arachnoid.com/JTides/'>JTides</a>, turned into XML with a utility provided in the project.<br>
<br>
The goal was to isolate the Core of a Tide application, so you can plug it anywhere.<br>
<br>
Sample outputs:<br>
<pre><code>High-Low water Calculation took 118 ms<br>
-- Oyster Point Marina --<br>
LW Wed Jul 13 06:06:00 PDT 2011 : -1.13 feet<br>
HW Wed Jul 13 12:52:00 PDT 2011 : +5.88 feet<br>
LW Wed Jul 13 17:44:00 PDT 2011 : +2.87 feet<br>
HW Wed Jul 13 23:37:00 PDT 2011 : +7.85 feet<br>
</code></pre>

Coefficients can be stored in<br>
<ul><li>an XML Document<br>
</li><li>a relational database (SQLite or HypersonicSQL)<br>
</li><li>a json file<br>
</li><li>a ser file</li></ul>

See an example of implementation at <a href='http://code.google.com/p/tide-engine-implementation/'>TideEngineImplementation</a>
<br><br>
This project is part of the Navigation Desktop project. Build it from <a href='http://code.google.com/p/oliv-soft-project-builder/'>there</a>.