run: build
	cd src && java Main 2>error.log
build: src/Main.class
%.class: %.java
	javac $<