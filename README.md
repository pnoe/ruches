## Web application to manage apiaries

Spring Boot web application running in Tomcat 9 with Postgresql.

In french or english.

"Ruches" is "Hives" in french ! 

GPL-3 license

## Build - deploy

```
git clone https://gitlab.com/ruches/ruches.git
cd ruches
mvn package -DskipTests
cp target/ruches.war.original /.../.../tomcat9/webapps/ruches.war
```

## Documentation

* https://gitlab.com/ruches/ruches-doc/-/tree/master/docker if you want to try "ruches" with Docker
* https://gitlab.com/ruches/ruches-doc/-/tree/master/sql database creation
* https://gitlab.com/ruches/ruches-doc/-/tree/master/docs/install vhhost, context.xml ...
* https://gitlab.com/ruches/ruches-doc for documentation in french.

## Our clients, they trust us !   :-)

https://les-abeilles-de-la-lisette.fr/wordpress/


