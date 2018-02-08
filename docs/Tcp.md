### com.esri.simulator.Tcp

$ java -cp rttest.jar com.esri.rttest.producers.Tcp 

Usage: Tcp &lt;server&gt; &lt;port&gt; &lt;file&gt; &lt;rate&gt; &lt;numrecords&gt;
- Sends lines from file to the specified server and port.  
- The simulator tries to send numrecords at rate requested.
- During the run the simulator counts the records and actual rate it was able to send and outputs that to the screen.
- There is a limit to the rate Tcp can achieve; which depends on hardware and network speed; Memmory seems to be a big limiter on max speed (on i7 computer with 16G of memory max rate was around 150,000/s)
- There is also a limit on the size of the file. I was able to run with a file containing 5 Million (~100 byte) lines; however, it would not load a 10 Million line file.
