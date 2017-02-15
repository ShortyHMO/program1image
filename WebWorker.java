/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.text.*;

public class WebWorker implements Runnable
{

private Socket socket;


/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;

}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      // Start of changed code created string for file path
	  //and sending to different methods
	  String location = readHTTPRequest(is);
	  String type = parse(location);
	  System.out.println(type);
	  if(type.toLowerCase() == "png")writeHTTPHeader(os,"image/png",location);
	  else if(type.toLowerCase() == "jpeg")writeHTTPHeader(os,"image/jpeg",location);
	  else if(type.toLowerCase() == "gif")writeHTTPHeader(os,"image/gif",location);
	  else if(type.toLowerCase() == "jpg")writeHTTPHeader(os,"image/jpg",location);
      else writeHTTPHeader(os,"text/html",location);
      writeContent(os,location,type);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
// changed to return string path
private String readHTTPRequest(InputStream is)
{
   String local= "";
   String line;
   int count = 0;
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
		// change to parse out the line.
   		 if (count == 0){
			
			local = line.substring(4);
			local = local.substring(1,local.indexOf(" "));
			count++;
		 }
         System.err.println("Request line: ("+line+")");
         if (line.length()==0) break;
      } catch (Exception e) {
         System.err.println("Request error: "+e);
         break;
      }
   }
   //return path
   return local;
}
private String parse(String li){
	
	File file = new File(li);
	String type = file.getName();
	if(type.lastIndexOf(".") != -1 && type.lastIndexOf(".") != 0 ) return type.substring(type.lastIndexOf(".") + 1);
	else return " ";
}
/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType,String path) throws Exception
{
	System.out.println(contentType);
   Date d = new Date();
   File file = new File(path);
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   if(file.exists() &&! file.isDirectory()){os.write("HTTP/1.1 200 OK\n".getBytes());}
   else{os.write("HTTP/1.1 404 Not Found \n".getBytes()); }
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Jon's very own server\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String path, String type) throws Exception
{
	System.out.println(type);
	if (type.equals("gif")||type.equals("png")||type.equals("jpeg")|| type.equals("jpg")){
		File imageF = new File(path); 
		FileInputStream IF = new FileInputStream(imageF);
		byte[] image = new byte[(int) imageF.length()];
		IF.read(image);
		IF.read(image);
		System.err.println("show");
		DataOutputStream dd = new DataOutputStream(os);
		dd.write(image);
		dd.close();
	}
	else{
		File file = new File(path);
		System.err.println("read");
		if(file.exists() && !file.isDirectory()){
			FileInputStream fileS = new FileInputStream(path);
			BufferedReader fl = new BufferedReader(new InputStreamReader(fileS));
			String reader = "";
			while((reader = fl.readLine()) !=null){
				if(reader.toLowerCase().equals(("<cs371date>").toLowerCase())== true){
				Date date = new Date();
				System.err.println(date);
				String dadate = date.toString();
				System.err.println(dadate);
				os.write(dadate.getBytes());
				os.write("<br>".getBytes());
				os.write("<fl>".getBytes());
				}
				else if(reader.toLowerCase().equals(("<cs371server>").toLowerCase())== true){
					os.write("Server's identification string : Heather's Server\n".getBytes());
					os.write("<br>".getBytes());
				}
				else{
				os.write(reader.getBytes());
				os.write("<br>".getBytes());
				}
			
			}
	
	
		}
		else{
			os.write("<html><head></head><body>\n".getBytes());
			os.write("<h3>Error: 404 Not Found</h3>\n".getBytes());
			os.write("</body></html>\n".getBytes());
		}
	}
}

} // end class
