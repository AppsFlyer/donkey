all: clean test build

.PHONY: all test clean

clean:
	lein clean

test:
	mvn test && lein test

build:
	lein uberjar
