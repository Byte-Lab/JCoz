# Get the dependencies from the given shell file
source "$1"

install_deps

# Compile test
javac test/*.java

# Run profiler
java -agentpath:./liblagent.so=pkg=test_progress-point=test/Test:38_warmup=1000_slow-exp test.Test
