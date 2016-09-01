package org.opensrp.register.mcare.repository;

import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;
import org.motechproject.dao.MotechBaseRepository;
import org.opensrp.common.AllConstants;
import org.opensrp.register.mcare.domain.Acl;
import org.opensrp.register.mcare.domain.Privilege;
import org.opensrp.register.mcare.service.PrivilegeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class AllPrivileges  extends MotechBaseRepository<Privilege>{
	private static Logger logger = LoggerFactory.getLogger(AllPrivileges.class);
	
	@Autowired
	public AllPrivileges(@Value("#{opensrp['couchdb.atomfeed-db.revision-limit']}") int revisionLimit, 
			@Qualifier(AllConstants.OPENSRP_DATABASE_CONNECTOR) CouchDbConnector db) {
		super(Privilege.class, db);
		this.db.setRevisionLimit(revisionLimit);
	}
	
	@GenerateView
	public Privilege findByName(String name) {
		List<Privilege> privileges = queryView("by_name", name);
		if (privileges == null || privileges.isEmpty()) {
			return null;
		}
		return privileges.get(0);
	}
	
	@View(name = "all_privileges", map = "function(doc) { if (doc.type === 'Privilege') { emit(doc.name); } }")
	public List<Privilege> privileges() {
		return db.queryView(
				createQuery("all_privileges")
						.includeDocs(true), Privilege.class);
	}
	
	@View(name = "privilege_by_name", map = "function(doc) { if (doc.type === 'Privilege' && doc.name) { emit(doc.name); } }")
	public Privilege privilegeByName(String name) {
		logger.info("inside repository class.");
		List<Privilege> privileges = db.queryView(
				createQuery("privilege_by_name").key(name)
						.includeDocs(true), Privilege.class);
		if (privileges == null || privileges.isEmpty()) {
			return null;
		}
		return privileges.get(0);
	}
	
	/*@View(name = "all_active_role", map = "function(doc) { if (doc.type === 'Acl' && doc.status ==='Active') { emit(doc.roleName); } }")
	public List<Acl> allActiveRoles() {
		return db.queryView(
				createQuery("all_active_role")
						.includeDocs(true), Acl.class);
	}*/
	
}