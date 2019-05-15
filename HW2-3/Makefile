all: compile

compile:
	java -jar jtb132di.jar minijava.jj
	java -jar javacc5.jar minijava-jtb.jj
	javac ./components/*.java
	javac Main.java

clean:
	rm -f *.class *~ ./components/*.class
