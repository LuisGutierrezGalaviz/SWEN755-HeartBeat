run-monitor: build-monitor
	cd src && java Monitor
build-monitor: src/Monitor.class
run-app: build-app
	cd src && java Main
build-app: src/Main.class
%.class: %.java
	javac $<