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

BENCHMARK_SOURCES = \
	$(shell find src/benchmark -name '*.java')

# The default make target entry
default: build

# Run the program
run: default
	$(J) -cp $(CLASSPATH) Main

run-email: default
	$(J) -cp $(CLASSPATH) examples/Email

# Build the program
build:
	$(JC) $(JFLAGS) -d $(CLASSPATH) $(SOURCES) $(TEST_SOURCES) $(BENCHMARK_SOURCES)

test: build
	$(J) -cp $(CLASSPATH) tests/Main

benchmark: build
	$(J) -cp $(CLASSPATH) benchmark/Main

plot:
	python plot.py

# RM is a predefined macro in make (RM = rm -f)
clean:
	$(RM) $(CLASSPATH)/*.class
