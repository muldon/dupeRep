select *
from experiment e, recallrate r
where e.id = r.experiment_id
--and e.lote = 16 
--and e.lote = 17 
--and e.lote = 20
--and e.lote = 19 
--and e.lote = 182 
--and (e.lote = 184 or e.lote = 183)
--and e.lote = 200
--and e.base like '%2017%'
--and origem like '%BM25%'
--and origem like '%Sum%'
--and observacao like '%pesos%'
--and bm25k = 1.50
--and ttweight = 1

order by recallrate_20 desc, recallrate_10 desc
--order by e.id desc

--delete from experiment where id=1
--update experiment set numberoftestedquestions = 1680 where numberoftestedquestions = 1753
--select * from experiment order by lote desc;
--update experiment set base = '2014dupePL2' where base = '2014dupe'
