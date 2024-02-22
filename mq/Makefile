# define compiler and compiler flag variables
JFLAGS = -g -Xlint:deprecation
JC = javac
J = java

# Path to the directory where the .class files will be generated
CLASSPATH = bin

# All the source files in the src directory
SOURCES = \
	$(shell find src -name '*.java' -not -path 'src/tests/*')

TEST_SOURCES = \
	$(shell find src/tests -name '*.java')

# The default make target entry
default: build

# Run the program
run: default
	$(J) -cp $(CLASSPATH) Main

# Build the program
build:
	$(JC) $(JFLAGS) -d $(CLASSPATH) $(SOURCES) $(TEST_SOURCES)

test: build
	$(J) -cp $(CLASSPATH) tests/Main

# RM is a predefined macro in make (RM = rm -f)
clean:
	$(RM) $(CLASSPATH)/*.class
