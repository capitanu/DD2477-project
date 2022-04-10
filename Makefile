.PHONY: all

all: build run

build:
	@mvn assembly:assembly -DdescriptorId=jar-with-dependencies
	@echo ""

run:
	@java -cp target/booksrec-1.0-SNAPSHOT-jar-with-dependencies.jar -Xmx4g com.ir22.booksrec.Engine -l logo.png -p patterns.txt

test:
	@mvn test
