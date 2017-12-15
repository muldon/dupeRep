package br.ufu.facom.lascam.dupe;

import java.io.PrintWriter;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.ufu.facom.lascam.dupe.service.ClassifyService;
import br.ufu.facom.lascam.dupe.service.FeaturesService;
import br.ufu.facom.lascam.dupe.util.DupeUtils;

@Component
public class DupeApp {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private long initTime;
	private long generalInitTime;
	
	@Value("${observation}")
	public String observation;
	
	@Value("${maxCreationDate}")
	public String maxCreationDate;
	
	@Value("${lote}")
	public String lote;
	
	@Value("${maxResultSize}")
	public String maxResultSize;
	
	@Value("${percentOfTestedQuestions}")
	public String percentOfTestedQuestions;
	
	@Value("${featureGen}")
	public Boolean featureGen;
	
	
	@Value("${classify}")
	public Boolean classify;
	
	@Value("${tagFilter}")
	public String tagFilter;
	
	@Value("${spring.datasource.url}")
	public String database;
	
	@Autowired
	private ClassifyService classifyService;
	
	
	@Autowired
	private FeaturesService featuresService;
	
	@Autowired
	private DupeUtils dupeUtils;
	
	

	@PostConstruct
	public void init() throws Exception {
		
		logger.info("Inicializando dupe app ................");
		
		PrintWriter pw = new PrintWriter("./dupe.log");
		pw.close();
		
		dupeUtils.initializeConfigs();
		
		logger.info("Variables: "
				+"\n database: "+database
				+"\n observation: "+observation
				+"\n tag: "+tagFilter
				+"\n maxCreationDate: "+maxCreationDate
				+"\n lote="+lote
				+"\n featureGen: "+featureGen
				+"\n classify: "+classify
				+"\n maxResultSize="+maxResultSize
				+"\n percentOfTestedQuestions: "+percentOfTestedQuestions);
		
		generalInitTime =  System.currentTimeMillis();
		
		dupeUtils.generateBuckets();
		
		
		if(featureGen){
			logger.info("features generation...");
			
			initTime = System.currentTimeMillis();
			featuresService.findClosedDuplicatedNonMastersByTag();
			dupeUtils.reportElapsedTime(initTime,"featuresService.findClosedDuplicatedNonMastersByTag()");
			
			initTime = System.currentTimeMillis();
			featuresService.cleanOldData();
			dupeUtils.reportElapsedTime(initTime,"featuresService.cleanOldData()");
			
			
			initTime = System.currentTimeMillis();
			featuresService.generateFeaturesForDuplicatedQuestions();
			dupeUtils.reportElapsedTime(initTime,"featuresService.generateFeaturesForDuplicatedQuestions()");
			
			initTime = System.currentTimeMillis();
			featuresService.findNonDuplicatedQuestions();
			dupeUtils.reportElapsedTime(initTime,"featuresService.findNonDuplicatedQuestions()");
			
			initTime = System.currentTimeMillis();
			featuresService.generateFeaturesForNonDuplicatedQuestions();
			logger.info("End of features generation...");
			dupeUtils.reportElapsedTime(initTime,"featuresService.generateFeaturesForNonDuplicatedQuestions()");
		
			
		}
		if(classify){
			logger.info("Classification...");
			initTime = System.currentTimeMillis();
			classifyService.run();
			dupeUtils.reportElapsedTime(initTime,"classifyService.run(tagFilter)");
			logger.info("End of classification...");
		}
				
		dupeUtils.reportElapsedTime(generalInitTime,"DupeApp fim");
		logger.info("End of experiment...");

	}

	
	
	
	
	
	

}
