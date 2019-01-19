# DupeRep
A replication package of Dupe original [work](http://ieeexplore.ieee.org/abstract/document/7832919/). If you are using this source code, please cite our [paper](https://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=8330262).

### Prerequisites

Note: all the experiments were conducted over a server equipped with 80 GB RAM, 2.4 GHz on twelve cores and 64-bit Linux Mint Cinnamon operating system. We strongly recommend a similar or better hardware environment. The operating system could be changed. 

Softwares:
1. [Java 1.8] 
2. [Postgres 9.3]
3. [PgAdmin] (we used PgAdmin 3) but feel free to use any DB tool for PostgreSQL. Configure your DB to accept local connections. An example of *pg_hba.conf* configuration:

```
...
# TYPE  DATABASE        USER            ADDRESS                 METHOD
# "local" is for Unix domain socket connections only
local   all             all                                     md5
# IPv4 local connections:
host    all             all             127.0.0.1/32            md5
...
```

4. [Maven 3](https://maven.apache.org/)

### Installing the app.
1. Download the SO Dump of March 2017. We provide two dumps where both contains the main tables we use. They differ only in **posts** table. In [Dump 1](http://lascam.facom.ufu.br/companion/duplicatequestion/backup_so_march_2017_stemmed_stoped_ok.backup), the table is stemmed and had the stop words removed. Also it has the synonyms of tags and code blocks already extracted. In [Dump 2](http://lascam.facom.ufu.br/companion/duplicatequestion/backup_so_2017_raw_basic_tables_ok.backup), the table contains the original raw content. The next steps are described considering the fastest way to reproduce DupPredictor, in other words, for Dump 1. If you desire to simulate the entire process, including the stemming and stop words removal, follow the instructions available in [preprocess](https://github.com/muldon/preprocessor) step, then proceed with the next steps.

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

Obs: restoring this dump would require at least 100 Gb of free space. If your operating system runs in a partition with insufficient free space, create a tablespace pointing to a larger partition and associate the database to it by replacing the "TABLESPACE" value to the new tablespace name: `TABLESPACE = tablespacename`. 

4. Assert the database is sound. Execute the following SQL command: `select title,body,tags,tagssyn,code  from posts where title is not null limit 10`. The return should list the main fields for 10 posts. 

5. Assert Maven is correctly installed. In a Terminal enter with the command: `mvn --version`. This should return the version of Maven. 

## Running the Experiment

1. Edit the file *application.properties* under *src/main/resources* and set the parameters bellow "##### INPUT PARAMETERS #####". The file comes with default values for simulating Dupe original work. You need to fill variable: `spring.datasource.password=YOUR_DB_PASSWORD`. Change `spring.datasource.username` if your db user is not postgres. 

2. In a terminal, go to the Project_folder and build the jar file with the Maven command: `mvn package -Dmaven.test.skip=true`. Assert that dupe.jar is built under target folder. 

3. Go to Project_folder/target and run the command to execute DupeRep: `java -Xms1024M -Xmx40g -jar ./dupe.jar`. The Xmx value may be bigger if you change the "maxCreationDate" parameter to a more recent date. 

## Results

The results are displayed in the terminal but also stored in the database in tables **experiment** and **recallrate**. Each experiment shows recall rates for five classifiers: BM25, DupPredictor, Stanford, Sum of Cosines and Weka. Weka is the default value por DupeRep. 

The following query should return the results:  
```
select e.id, e.tag,e.lote,e.observacao as observation, r.origem as origin, r.recallrate_100,r.recallrate_50, r.recallrate_20,r.recallrate_10,r.recallrate_5
from experiment e, recallrate r
where e.id = r.experiment_id
--and e.lote = 10 
--and origem like '%Weka%'
--and tag='java'
and app = 'Dupe'
order by e.lote, origem 
```
this query shows the considered tag, the observation filled in *application.properties* file, the origin denoting the used classifier and several values for recall rates. You can uncomment `and e.lote=10` to filter your experiment by its id (10 in this case), or `and origem like '%Weka%'` to filter results by the classifier, or `and tag='java'` to filter results by tag. You can also select any column from tables **experiment** and **recallrate**. 


## Authors

* Rodrigo Fernandes  - *Initial work* - [Muldon](https://github.com/muldon)
* Klerisson Paixao - [Klerisson](http://klerisson.github.io/)
* Marcelo Maia - [Marcelo](http://buscatextual.cnpq.br/buscatextual/visualizacv.do?id=K4791753E8)


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details



[Java 1.8]: http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
[Mallet]: http://mallet.cs.umass.edu/
[Postgres 9.3]: https://www.postgresql.org/download/
[PgAdmin]: https://www.pgadmin.org/download/
[Dump of March 2017]: http://lapes.ufu.br/so/
[preprocess]: https://github.com/muldon/preprocessor
