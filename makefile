JFLAGS = -g
JC = javac
JVM= java
FILE=
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
CLASSES = \
	BootstrapDriver.java \
	BootstrapSocket.java \
	BootstrapWorker.java \
	Message.java \
	Peer.java \
	PeerDriver.java \
	PeerSocket.java \
	PeerWorker.java
    
MAIN = Main

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
	$(JVM) $(MAIN)

clean:
	$(RM) *.class