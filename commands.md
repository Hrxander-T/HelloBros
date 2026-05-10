============================================================
            JAVA CHAT APP - QUICK START GUIDE
============================================================

REQUIREMENTS
------------
- Java installed (java.com/download)
- bore installed (host only)

```bash
curl -L https://github.com/ekzhang/bore/releases/download/v0.5.0/bore-v0.5.0-x86_64-unknown-linux-musl.tar.gz | tar xz
```


------------------------------------------------------------
HOST (You) - Do this every session
------------------------------------------------------------

1. Start the chat server:
   java -jar ChatServer.jar

2. Open a new terminal and start bore tunnel:
   ./bore local 4999 --to bore.pub

3. Note the port number from bore output:
   e.g. "listening at bore.pub:26485"  <-- this number changes every session

4. Send your friend:
   - ChatClient.jar
   - The port number (e.g. 26485)


------------------------------------------------------------
FRIEND (Client) - Do this every session
------------------------------------------------------------

1. Install Java if not installed:
   java.com/download

2. Run with the port number host gave you:
   java -jar ChatClient.jar bore.pub PORT_NUMBER

   Example:
   java -jar ChatClient.jar bore.pub 26485


------------------------------------------------------------
COMMANDS DURING CHAT
------------------------------------------------------------

- Type anything and press Enter to send
- Type  bye  to exit


------------------------------------------------------------
REBUILD JARS (after code changes)
------------------------------------------------------------

javac -d out src/ChatServer.java src/ChatClient.java
jar cfe ChatServer.jar ChatServer -C out .
jar cfe ChatClient.jar ChatClient -C out .

============================================================