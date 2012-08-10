Validator.nu Standalone
========================

The Validator.nu HTML Validator service

This project packages the Validator.nu HTML Validator so that it can easily be run locally.

How to build the Validator.nu
----------------------------------

    make

You need Python, Java , hg, svn and ant. The script takes the code from the official repository at Validator.nu.

How to run the Validator.nu standalone
----------------------------

    ./sbt run

Then go to [http://localhost:8888](http://localhost:8888).

How to generate a standalone jar
----------------------------

	make jar
	
Then you can run validator following:

	java -jar target/validator-nu-standalone.jar 8888

Licence
-------

This source code is made available under the [W3C Licence](http://opensource.org/licenses/W3C).
