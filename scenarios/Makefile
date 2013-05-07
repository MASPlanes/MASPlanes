GEN=java -cp ../dist/planes.jar es.csic.iiia.planes.generator.Cli -s

%.json:	%.settings
	$(GEN) $^ $@

all:	*.json
