/**
 * @author Asifur
 */

package org.opensrp.rest.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.opensrp.common.AllConstants;
import org.opensrp.register.mcare.domain.HouseHold;
import org.opensrp.scheduler.ScheduleLog;
import org.opensrp.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.github.ldriscoll.ektorplucene.CouchDbRepositorySupportWithLucene;
import com.github.ldriscoll.ektorplucene.LuceneAwareCouchDbConnector;
import com.github.ldriscoll.ektorplucene.LuceneQuery;
import com.github.ldriscoll.ektorplucene.LuceneResult;
import com.github.ldriscoll.ektorplucene.LuceneResult.Row;
import com.github.ldriscoll.ektorplucene.designdocument.LuceneDesignDocument;
import com.github.ldriscoll.ektorplucene.designdocument.annotation.FullText;
import com.github.ldriscoll.ektorplucene.designdocument.annotation.Index;
/*index = "function(doc) { " + 
		"    var res = new Document(); " + 
		"    res.add(doc.TODAY,{\"field\":\"TODAY\", \"store\":\"yes\"});"+
		"    res.add(doc.PROVIDERID,{\"field\":\"PROVIDERID\", \"store\":\"yes\"});" +
		"	 res.add(doc.FWUPAZILLA,{\"field\":\"FWUPAZILLA\", \"store\":\"yes\"});" +
		"    return res; " + 
    "}")
*/
@FullText({
    @Index(
        name = "by_provider",
	    index = "function(rec) {" +
	    		" var doc=new Document();" + 
	    		" doc.add(rec.FWDIVISION,{\"field\":\"FWDIVISION\", \"store\":\"yes\"}); " +
	    		" doc.add(rec.FWDISTRICT,{\"field\":\"FWDISTRICT\", \"store\":\"yes\"}); " +
	    		" doc.add(rec.FWUNION,{\"field\":\"FWUNION\", \"store\":\"yes\"});" +  
	    		" doc.add(rec.PROVIDERID,{\"field\":\"PROVIDERID\", \"store\":\"yes\"});" + 
	    		" doc.add(rec.FWUPAZILLA,{\"field\":\"FWUPAZILLA\", \"store\":\"yes\"});" + 
	    		" doc.add(rec.SUBMISSIONDATE,{\"field\":\"SUBMISSIONDATE\", \"store\":\"yes\"});" + 
	    		" doc.add(rec.type,{\"field\":\"type\", \"store\":\"yes\"});" + 
	    		" return doc;" +
	    		"}"),
	@Index(
        name = "by_all_criteria",
        index = "function (doc) { if(doc.type !== 'HouseHold') return null;  var docl = new Array(); var ret = new Document(); if(doc.type){    var led = doc.type;    ret.add(led, {'field' : 'type'}); } docl.push(ret); return docl; }")
})

@Repository
public class LuceneHouseHoldRepository extends CouchDbRepositorySupportWithLucene<HouseHold> {
	private static Logger logger = LoggerFactory.getLogger(LuceneHouseHoldRepository.class);
	@Autowired
	public LuceneHouseHoldRepository(@Value("#{opensrp['couchdb.atomfeed-db.revision-limit']}") int revisionLimit,
			@Qualifier(AllConstants.OPENSRP_DATABASE_LUCENE_CONNECTOR)LuceneAwareCouchDbConnector db) {
		super(HouseHold.class, db);
		this.db.setRevisionLimit(revisionLimit);
		initStandardDesignDocument();
	}
	
	 public LuceneResult findDocsByProvider(String queryString) { 
        LuceneDesignDocument designDoc = db.get(LuceneDesignDocument.class, stdDesignDocumentId); 
        LuceneQuery query = new LuceneQuery(designDoc.getId(), "by_provider"); 
        query.setQuery(queryString); 
        query.setStaleOk(false); 
        logger.info("inside luceneHouseholdRepository.");
        return db.queryLucene(query); 
    } 
	 
	 public LuceneResult getByCriteria(long start,long end,String anmIdentifier) {
			// create a simple query against the view/search function that we've created
			
			Query qf = new Query(FilterType.AND);
			
			/*if(start!=0 && end!=0){
				qf.betwen("SUBMISSIONDATE", start, end);
			}
			
			if(anmIdentifier!= null && !anmIdentifier.isEmpty() && !anmIdentifier.equalsIgnoreCase("")){
				qf.eq("PROVIDERID", anmIdentifier);
			}*/
			
			if(anmIdentifier!= null && !anmIdentifier.isEmpty() && !anmIdentifier.equalsIgnoreCase("")){
				qf.eq("type", "HouseHold");
			}

			if(qf.query() == null || qf.query().isEmpty()){
				throw new RuntimeException("Atleast one search filter must be specified");
			}
				
			/*LuceneDesignDocument designDoc = db.get(LuceneDesignDocument.class, stdDesignDocumentId);
	        LuceneQuery query = new LuceneQuery(designDoc.getId(), "by_all_criteria");*/ 
			
			LuceneQuery query = new LuceneQuery("HouseHold", "by_all_criteria");
			
			System.out.println("Query: "+qf.query());
			
	        query.setQuery(qf.query()); 
	        query.setStaleOk(false); 
	        query.setIncludeDocs(true);
	       
	        LuceneResult result = db.queryLucene(query);
	        List<HouseHold> ol = new ArrayList<>();
			for (Row r : result.getRows()) {
				HashMap<String, Object> doc = r.getDoc();
				//System.out.println("Lucene HH doc: "+doc);
				HouseHold ro = null;
				try {
					ro = new ObjectMapper().readValue(new JSONObject(doc).toString(), type);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//System.out.println("Lucene HH Object: "+ro.getELCODetail("FWWOMFNAME"));
				
				ol.add(ro);
			}
			
			//System.out.println("LuceneResult: "+ol);
		        
	        return result; 
		}

}
