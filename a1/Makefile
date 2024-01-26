# define compiler and compiler flag variables
JFLAGS = -g
JC = javac
J = java

# Path to the directory where the .class files will be generated
CLASSPATH = bin

# All the source files in the src directory
SOURCES = \
	src/Stack.java \
	src/Main.java 

# The default make target entry
default: build

# Run the program
run: default
	$(J) -cp $(CLASSPATH) Main

# Build the program
build: 
	$(JC) $(JFLAGS) -d $(CLASSPATH) $(SOURCES)

# RM is a predefined macro in make (RM = rm -f)
clean:
	$(RM) $(CLASSPATH)/*.class
