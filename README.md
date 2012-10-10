Validator.nu Standalone
========================

This project continues at https://github.com/w3c/validators

The Validator.nu HTML Validator service

This project packages the Validator.nu HTML Validator so that it can easily be run locally.

How to build the Validator.nu
-----------------------------

```bash
make
```

You need Python, Java , hg, svn and ant. The script takes the code from the official repository at [http://validator.nu/](http://validator.nu/).

On some systems, you may have to set the `JAVA_HOME` variable:

```bash
JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 make
```

How to run the Validator.nu standalone
--------------------------------------

```bash
./sbt run
```

Then go to [http://localhost:8888](http://localhost:8888).

How to generate a standalone jar
----------------------------

```bash
make jar
```

Then you can run the validator:

```bash
java -jar target/validator-nu-standalone.jar 8888
```

Licence
-------

This source code is made available under the [W3C Licence](http://opensource.org/licenses/W3C).
