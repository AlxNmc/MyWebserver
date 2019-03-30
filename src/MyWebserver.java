/*--------------------------------------------------------

1. Name / Date:
    Alex Niemiec

2. Java version used, if not the official version for the class:
    build 10.0.2+13

3. Precise command-line compilation examples / instructions:

> javac MyWebserver.java

4. Precise examples / instructions to run this program:

> java MyWebserver

connect using firefox web browser with ip address of machine running
the program followed by ":2540"
ex. localhost:2540 in address bar if connecting from same machine

5. List of files needed for running the program.

addnums.html needs to be in same directory as MyWebserver.class

5. Notes:

If client attempts to open a file that does not exist, a blank page is
returned. If a directory that does not exist is attempted to be opened,
a Null Pointer Exception is thrown and the web page never loads.

----------------------------------------------------------*/
import java.io.*;   //I/O libraries
import java.net.*;  //networking libraries
import java.util.Scanner; //for reading files

//inherits all attributes of Thread class
class Worker extends Thread {
    private Socket sock;
    Worker (Socket s) { //constructor assigns passed Socket to sock variable
        sock = s;
    }

    public void run(){
        BufferedReader in;
        PrintStream out;
        try{
            //set up input stream
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            //set up output stream
            out = new PrintStream(sock.getOutputStream());

            try{
                String fileRequest;
                String fileName;

                //read first line of message from client
                fileRequest = in.readLine();
                System.out.println("\nNew request: " + fileRequest);
                //isolate argument containing file/directory request
                fileName = fileRequest.trim().split("\\s+")[1];
                if(fileName.endsWith("/")){
                    //client is requesting access to a directory
                    openDirectory(fileName, out);
                }
                else if(fileName.contains("addnums.fake-cgi")){
                    //client is requesting fake-cgi page addnums
                    addnums(fileName, out);
                }
                else{
                    //client is requesting access to a text or html file
                    sendFile(fileName, out);
                }
            }catch (IOException x){ //look for any exceptions
                System.out.println("Server read error");
                x.printStackTrace();
            }
            sock.close(); //close socket when finished
        }catch (IOException ioe) {System.out.println(ioe);}
    }

    private void openDirectory(String directory, PrintStream out){
        //decode UTF-8 formatting
        try{
            directory = URLDecoder.decode(directory, "UTF-8");
        }catch(UnsupportedEncodingException ex){
            System.out.println("UTF-8 decoding unsuccessful");
        }
        System.out.println("Opening directory - " + directory);
        File dir = new File("." + directory);
        File[] files = dir.listFiles();

        //MIME header
        out.print("HTTP/1.1 200 OK\r\n");
        out.print("Content-Length: " + 300+100*files.length + "\r\n");
        out.print("Content-Type: " + "text/html" + "\r\n\r\n");

        //start of html-formatted directory
        out.println("<pre>");
        out.println("<h1>Index of Alex's server</h1>");

        //if in a sub directory, provide link to parent and home directories
        if(directory.length()>1){
            String[] pDirs = directory.split("/");
            directory = "/";
            for(int i=1; i<pDirs.length-1; i++){
                directory += pDirs[i]+"/";
            }
            out.println("<a href=\"\\\">Home Directory</a><br>");
            out.println("<a href=\"" + directory + "\">Parent Directory</a><br>");
        }

        //list links to all files in current directory
        for(File f: files){
            String name = f.getName();
            if(f.isDirectory()) name = name + "/";
            out.println("   <a href=\"" + name +"\">" + name + "</a><br>");
        }
    }

    private void sendFile(String fileName, PrintStream out){
        //decode UTF-8 formatting
        try{
            fileName = URLDecoder.decode(fileName, "UTF-8");
        }catch(UnsupportedEncodingException ex){
            System.out.println("UTF-8 decoding unsuccessful");
        }
        System.out.println("Sending file - " + fileName);
        File file = new File("." + fileName);

        //get type (html or plain) from fileName
        String type = fileName;
        type = type.split("\\.")[1];
        type = (type.contains("html"))? "text/html" : "text/plain";

        //MIME header
        out.print("HTTP/1.1 200 OK\r\n");
        out.print("Content-Length: " + file.length() + "\r\n");
        out.print("Content-Type: " + type + "\r\n\r\n");

        //send file
        try{
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()){
                out.println(sc.nextLine());
            }
        }catch (FileNotFoundException ex){
            out.println("File does not exit.");
        }
        out.flush();
    }

    private void addnums(String input, PrintStream out){

        //parse the input string and calculate sum for the client
        File file = new File("./addnums.html");
        input = input.split("\\?")[1];
        String[] fields = input.split("&");
        String person = fields[0].split("=")[1];
        int num1 = Integer.parseInt(fields[1].split("=")[1]);
        int num2 = Integer.parseInt(fields[2].split("=")[1]);
        int sum = num1 + num2;
        String result = "<b>Ok " + person + ", the sum of " + num1 + " and " + num2 + " is " + sum + ".</b>";
        System.out.println("Sending addnums result - " + result);

        //MIME header
        out.print("HTTP/1.1 200 OK\r\n");
        out.print("Content-Length: " + result.length()*2 + "\r\n");
        out.print("Content-Type: " + "text/html" + "\r\n\r\n");

        //send formatted result back to client
        out.println("\n" + result);
        out.flush();
    }

}

public class MyWebserver{
    public static void main(String a[]) throws IOException{
        int q_len = 6;
        int port = 2540;
        Socket sock;
        /*socket that will listen for connections from client through port and
          accept as many as q_len connections instantaneously*/
        ServerSocket servsock = new ServerSocket(port, q_len);

        System.out.println
                ("Alex Niemiec's Web Server starting up, listening at port " + port + ".\n");

        while(true){
            //listen for next client connection and store it in sock Socket
            sock = servsock.accept();
            //start new worker thread to handle new connection
            new Worker(sock).start();
        }
    }
}
