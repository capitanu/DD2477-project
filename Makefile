.PHONY: all

all: build run

build:
	@mvn package
	@echo ""

run:
	@java -cp target/booksrec-1.0-SNAPSHOT.jar -Xmx4g com.ir22.booksrec.Engine -l logo.png -p patterns.txt

test:
	@mvn test
