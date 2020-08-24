all: clean install

.PHONY: all test clean

clean:
	lein clean

test:
	mvn test

build:
	lein uberjar

install:
	mvn install
