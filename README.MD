# OBJECTIVE

Create a spring boot web application that exposes a RESTful CRUD endpoint for an IP-Hostname loookup table of a PostgreSQL database. 
After each service invocation, the application runs a python script that dumps the database contents on a CSV file.

# TOOLING

Ended up with the following software stack on a 16.04 Ubuntu:
1. PostgreSQL 9.5
2. Java 8 (Spring Boot with JPA
3. Tomcat 8.5
4. Maven 3.3
5. Python 3.5 (with pip and virtualenv installed)

# DATABASE SETUP

Install PostgreSQL and create a user and a database. Connect to the database to make sure everything looks OK.

# PYTHON

Create a python environment that includes the PostreSQL adapter

	virtualenv --python=/usr/bin/python3 venvs/postgrestest
	source ~/venvs/postgrestest/bin/activate
	pip install psycopg2-binary

# SPRING BOOT APPLICATION CONFIGURATION

Open application.properties. Fill the properties mentioned below as appropriate for your setup. 

	custom.db.host=
	custom.db.name=
	custom.fs.csvpath=
	spring.datasource.username=
	spring.datasource.password=

# APACHE TOMCAT SETUP

 If you like to redeploy using maven you have to do sone users and roles configuration. My tomcat-users.xml looks like
 
	<role rolename="manager"/>
    <role rolename="manager-gui"/>
    <role rolename="manager-script"/>
    <user username="admin" password="admin" roles="manager,manager-gui,manager-script"/>
        
If you choose other values, modify the maven-tomcat7-plugin configuration section of the pom.xml.

Important: On the shell that starts Tomcat, we need to have active the virtualenv we created previously for Python.

	source ~/venvs/postgrestest/bin/activate

# MAVEN

Starter mvn project with the required spring boot modules (WEB, REST, JPA). Modified with a couple extra dependencies and the tomcat7-maven-plugin.

	mvn clean tomcat7:deploy|redeploy|undeploy

Application is accessible on 

	http://localhost:8080/bt
	 
The REST api is on 
	
	http://localhost:8080/bt/rs/dnsrecord

# OBSERVATIONS

## The bad

1. Not sure if REST errors (e.g. GET on non existing record) are handled perfectly.
2. Bean validation works, but the underlying error is not propagated properly, a generic HTTP 500 is returned instead.
3. No logging infrastructure.

## The good

1. Minimal codebase size.
2. Used some Java8 features and APIs.
3. Configuration not externalized, but in one place and externalizing the crucial parts is trivial. Demo seems smoother like this. 
3. Python script bundled with the application and does not contain duplicate configuration (db details).
4. Custom validations for IPv4 addresses and host names that delegate to apache commons.

# DEMO

We will use `curl` to demonstrate the REST API. 

## Create

### Happy schenario
	$ curl -i -X POST -H "Content-Type:application/json" http://localhost:8080/bt/rs/dnsrecord/ -d '{"ipAddress":"147.102.3.1","hostName":"springboot.cslab.ece.ntua.gr"}'

	HTTP/1.1 200 
	Location: http://localhost:8080/bt/api/dnsrecord/5
	
	$ tail catalina.out
	sout-1: wrote 1 row to /tmp/dump-1.csv
	script execution with id 1 exited with rc=0

	$ cat /tmp/dump-1.csv
	5,danaos.cslab.ece.ntua.gr,147.102.3.1
	
	springbootdb=# select * from dns_record;
	 id |        host_name         | ip_address  
	----+--------------------------+-------------
	  5 | danaos.cslab.ece.ntua.gr | 147.102.3.1
	(1 row)

### Invalid IP

	$ curl -i -X POST -H "Content-Type:application/json" http://localhost:8080/bt/rs/dnsrecord/ -d '{"ipAddress":"10.0.0.256","hostName":"whatever"}'
	
	HTTP/1.1 500 
	Content-Type: application/json;charset=UTF-8
	...
	{"timestamp":"2018-03-29T20:54:01.698+0000","status":500,"error":"Internal Server Error","message":"Could not commit JPA transaction; nested exception is javax.persistence.RollbackException: Error while committing the transaction","path":"/bt/rs/dnsrecord/"}

## Read

### Existing

	curl http://localhost:8080/bt/rs/dnsrecord/5
	{"id":5,"ipAddress":"147.102.3.1","hostName":"danaos.cslab.ece.ntua.gr"}
	
	$ tail catalina.out
	sout-2: wrote 1 row to /tmp/dump-2.csv
	script execution with id 2 exited with rc=0
	
### Non existing

	curl http://localhost:8080/bt/rs/dnsrecord/999
	Non existing record with id 999
	
	$ tail catalina.out
	sout-3: wrote 1 row to /tmp/dump-3.csv
	script execution with id 3 exited with rc=0
	
## Update

First add one more...

	$ curl -i -X POST -H "Content-Type:application/json" http://localhost:8080/bt/rs/dnsrecord/ -d '{"ipAddress":"10.0.0.1","hostName":"localgw"}'
	HTTP/1.1 200 
	Location: http://localhost:8080/bt/api/dnsrecord/7
	
	$ tail catalina.out
	sout-4: wrote 2 row to /tmp/dump-4.csv
	script execution with id 4 exited with rc=0

	$ cat /tmp/dump-4.csv
	5,danaos.cslab.ece.ntua.gr,147.102.3.1
	7,localgw,10.0.0.1
	
	springbootdb=# select * from dns_record;
	 id |        host_name         | ip_address  
	----+--------------------------+-------------
	  5 | danaos.cslab.ece.ntua.gr | 147.102.3.1
	  7 | localgw                  | 10.0.0.1
	(2 rows)
	
Now update it...

	$ curl -i -X POST -H "Content-Type:application/json" http://localhost:8080/bt/rs/dnsrecord/7 -d '{"ipAddress":"10.0.0.1","hostName":"localgw2"}'
	HTTP/1.1 200 
	Content-Type: application/json;charset=UTF-8
	{"id":7,"ipAddress":"10.0.0.1","hostName":"localgw2"}

	$ tail catalina.out
	sout-5: wrote 2 row to /tmp/dump-5.csv
	script execution with id 5 exited with rc=0
	
	$ cat /tmp/dump-5.csv
	5,danaos.cslab.ece.ntua.gr,147.102.3.1
	7,localgw2,10.0.0.1
	
	springbootdb=# select * from dns_record;
	 id |        host_name         | ip_address  
	----+--------------------------+-------------
	  5 | danaos.cslab.ece.ntua.gr | 147.102.3.1
	  7 | localgw2                 | 10.0.0.1
	(2 rows)

Updating non existing records returns an error like above

## Delete

	$ curl -i -X DELETE http://localhost:8080/bt/rs/dnsrecord/7
	HTTP/1.1 204
	
	$ tail catalina.out
	sout-6: wrote 1 row to /tmp/dump-6.csv
	script execution with id 6 exited with rc=0
	
	$ cat /tmp/dump-6.csv
	5,danaos.cslab.ece.ntua.gr,147.102.3.1
	
	springbootdb=# select * from dns_record;
	 id |        host_name         | ip_address  
	----+--------------------------+-------------
	  5 | danaos.cslab.ece.ntua.gr | 147.102.3.1
	(1 row)
	
Deleting non existing records returns an error like above
