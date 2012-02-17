2012.02.15 Trying to get rid of the app.owl protege plugin 
and using jar directly with maven.

I need the following libraries here for an ant build:
1) hypergraphdb.jar
2) hgdbmanagement.jar
3) jars/db.jar 
4) native/ *.dll, other os

Now, with 1.2 its:
\core\target\hgdb-1.2.jar (No dependencies in pom !!!)
/storage/bdb-native/target/hgdbnative-1.2.jar + dependencies com.sleepycat: db : 5.3.15, org.hypergraphdb: hgdb : 1.2

Found in:



