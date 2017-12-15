---------------reset e análise qualitativa


delete from postsquestions; --where tags like '%ruby%';

--carrega tabela postsQuestions com perguntas apenas
insert into postsQuestions(Id, Body, Title, Tags, originaltags, closeddate)
SELECT Id, Body, Title, Tags, tags, closeddate
FROM   posts
WHERE  PostTypeId = 1
and tags like '%git%'
order by Id;



--select * from postsquestions where tags like '%<git>%'