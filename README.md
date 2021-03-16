# My Webserver

## Install

---

This application was written for Java version 10.0.2. There is no guarantee that earlier or later versions of Java will work. If needed, download and install the Java SE Dev Kit 10.0.2 from [here](https://www.oracle.com/java/technologies/java-archive-javase10-downloads.html).

The most recent working version tested is 14.0.2

No real installation is needed. Simply compile with the following command:

```bash
> javac MyWebserver.java
```

/

## Usage

---

This server will offer access to files from the directory in which it is running and any subdirectories. 

Once complied, run the server with the following command:

```bash
> java MyWebserver
```

It will run at port 2540. 

Connect using Firefox web browser with the ip address of machine running the program followed by ":2540". For example, type  [localhost:2540](http://localhost:2540)  in address bar if connecting from the machine running the server.

## Notes

---

If the client attempts to open a file that does not exist, a blank page is returned.
