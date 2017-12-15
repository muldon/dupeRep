# DupeRep
A replication package of Dupe original [work](http://ieeexplore.ieee.org/abstract/document/7832919/). 

### Prerequisites

Note: all the experiments were conducted over a server equipped with 80 GB RAM, 2.4 GHz on twelve cores and 64-bit Linux Mint Cinnamon operating system. We strongly recommend a similar or better hardware environment. Operation System however can be changed. 

Softwares:
1. [Java 1.8] 
2. [Postgres 9.3]
3. [PgAdmin] (we used PgAdmin 3) but feel free to use any DB tool for PostgreSQL. 
4. [Maven 3](https://maven.apache.org/)

### Installing the app.
1. Download the SO [Dump of March 2017]. We provide two dumps where both contains the main tables we use. They differs only in **posts** table. In one of them the table contains the original content, while the other is stemmed and had the stop words removed. The next steps are described considered the fastest way to reproduce DupPredictor, in other words, the stemmed/stopped dump. If you desire to simulate the entire process, including the stemming and stop words removal, follow the instructions available in [preprocess] for stemming and removing the stop words, then procceed in next steps.
2. On your DB tool, create a new database named stackoverflow2017. This is a query example:
```
CREATE DATABASE stackoverflow2017
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;
```
3. Restore the downloaded dump to the created database. 

Obs: restoring this dump would require at least 100 Gb of free space. If your Operation System runs in a partition with insufficient free space, create a tablespace pointing to a larger partition and associate the database to it by replacing the "TABLESPACE" value to the new tablespace name: `TABLESPACE = tablespacename`. 

4. Assert that the database is sound. Execute the following SQL command: `select title,body,tags,tagssyn,code  from posts where title is not null limit 10`. The return should list the main fields for 10 posts. 

5. Assert Maven is correctly installed. In a Terminal enter with the command: `mvn --version`. This should return the version of Maven. 

## Running the tests

1. Edit the file *application.properties* under *src/main/resources* and set the parameters bellow "##### INPUT PARAMETERS #####". The file comes with default values for simulating Dupe original work. You need to fill only one variable: `spring.datasource.password=YOUR_DB_PASSWORD`. 

2. In a terminal, go to the Project_folder and build the jar file with the Maven command: `mvn package -Dmaven.test.skip=true`. Assert that dupe.jar is built under target folder. 

3. Go to Project_folder/target and run the command to execute DupeRep: `java -Xms1024M -Xmx40g -jar ./dupe.jar`. The Xmx value may be bigger if you change the "maxCreationDate" parameter to a more recent date. 


### Results

The results are displayed in the terminal but also stored in the database in tables **experiment** and **recallrate** . The following query should return the results:  
`select * from experiment e, recallrate r where e.id = r.experiment_id order by e.lote,e.id desc, origem `


## Authors

* Rodrigo Fernandes  - *Initial work* - [Muldon](https://github.com/muldon)
* Klerisson Paixao - [Klerisson](http://klerisson.github.io/)
* Marcelo Maia - [Marcelo](http://buscatextual.cnpq.br/buscatextual/visualizacv.do?id=K4791753E8)


## License
???
//This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details



[Java 1.8]: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
[Mallet]: http://mallet.cs.umass.edu/
[Postgres 9.3]: https://www.postgresql.org/download/
[PgAdmin]: https://www.pgadmin.org/download/
[Dump of March 2017]: http://lapes.ufu.br/so/
[preprocess]: https://github.com/muldon/preprocessor
