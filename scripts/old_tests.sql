-- CREATE TABLE `poststags` (
--  `Id` int(11) NOT NULL,
--  `PostId` int(11) NOT NULL,
--  `TagId` int(11) NOT NULL,
--  PRIMARY KEY (`Id`),
--  KEY `PostId` (`PostId`,`TagId`)
-- ) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- delete from postsQuestions;
CREATE TABLE `postsQuestions` (
  `Id` int(11) NOT NULL,
  `Body` text,
  `Title` varchar(255) DEFAULT NULL,
  `Tags` varchar(200) DEFAULT NULL,
  `RelatedPostId` int(11) DEFAULT 0,
  PRIMARY KEY (`Id`),
  INDEX `RelatedIcx` (`RelatedPostId`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- CREATE INDEX `RelatedIdx`  ON `stackoverflow`.`postsQuestions` (RelatedPostId) COMMENT '' ALGORITHM DEFAULT LOCK EXCLUSIVE;

CREATE TABLE `pair` (
	`Id` int(11) NOT NULL,
	`Question1` int(11) NOT NULL,
    `Question2` int(11) NOT NULL,
    `Duplicated` bit NOT NULL default 0,
    `MainTag` varchar(50) DEFAULT NULL,
    PRIMARY KEY (`Id`)
)ENGINE=MyISAM DEFAULT CHARSET=latin1;

CREATE TABLE `feature` (
	`PairId` int(11) NOT NULL,
	`Type` varchar(2) NOT NULL,
    `Cosine` DOUBLE(10,4) default 0,
    `TermOverlap` DOUBLE(10,4) default 0,
    `EntityOverlap` DOUBLE(10,4) default 0,
    `TypeOverlap` DOUBLE(10,4) default 0,
    `WordNet` DOUBLE default 0
)ENGINE=MyISAM DEFAULT CHARSET=latin1;

drop table feature;
SELECT * FROM pair;
select * from feature;

delete from pair;
delete from feature;

insert into postsQuestions(Id, Body, Title, Tags)
SELECT Id, Body, Title, Tags
FROM   posts
WHERE  PostTypeId = 1
order by Id;


SELECT t.id, t.title, t.body, t.tags, t.relatedPostId FROM (SELECT id FROM postsQuestions ORDER BY id LIMIT 10000, 5000) q JOIN postsQuestions t ON t.id = q.id and t.relatedPostId > 0;


select * from postlinks where LinkTypeId = 3 limit 5;
select * from postsQuestions where id = 26925;


drop table poststags;
drop table `postsQuestions`;


SELECT id, title, body, tags, RelatedPostId 
FROM postsQuestions, (SELECT id as sid FROM postsQuestions where postsQuestions.RelatedPostId = 0 ORDER BY RAND() LIMIT 10000) tmp WHERE postsQuestions.id = tmp.sid;

SELECT id, title, body, tags, RelatedPostId 
  FROM postsQuestions AS r1 JOIN
       (SELECT CEIL(RAND() *
                     (SELECT MAX(id)
                        FROM postsQuestions)) AS sid)
        AS r2
 WHERE r1.id >= r2.sid
 AND r1.RelatedPostId = 0
 ORDER BY r1.id ASC
 LIMIT 1;
 
 SELECT id, title, body, tags, RelatedPostId FROM postsQuestions, 
   (SELECT id as sid FROM postsQuestions ORDER BY RAND()) tmp 
   WHERE postsQuestions.id = tmp.sid
   and postsQuestions.RelatedPostId = 0 and postsQuestions.Tags like '%java%' LIMIT 26227;
   
SELECT id, title, body, tags, RelatedPostId FROM postsQuestions, 
 (SELECT id as sid FROM postsQuestions ORDER BY RAND() ) tmp 
 WHERE postsQuestions.id = tmp.sid and postsQuestions.RelatedPostId = 0 and postsQuestions.tags like '%java%' limit 26227;
