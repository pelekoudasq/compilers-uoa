JC		= javac
JVM		= java
MAIN	= Parser


.SUFFIXES: .java .class

.java.class:
	$(JC) $*.java

CLASSES = \
		Parser.java \
		ParseError.java

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
	$(JVM) $(MAIN)

clean:
	$(RM) *.class