run: build
	cd src && java Main 1>../logs/output.log 2>../logs/error.log
build: src/Main.class
%.class: %.java
	javac $<