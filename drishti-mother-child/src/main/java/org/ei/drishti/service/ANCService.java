package org.ei.drishti.service;

import org.ei.drishti.contract.AnteNatalCareCloseInformation;
import org.ei.drishti.contract.AnteNatalCareEnrollmentInformation;
import org.ei.drishti.contract.AnteNatalCareInformation;
import org.ei.drishti.contract.AnteNatalCareOutcomeInformation;
import org.ei.drishti.domain.EligibleCouple;
import org.ei.drishti.domain.Mother;
import org.ei.drishti.repository.AllEligibleCouples;
import org.ei.drishti.repository.AllMothers;
import org.ei.drishti.util.SafeMap;
import org.ei.drishti.service.reporting.MotherReportingService;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.motechproject.model.Time;
import org.motechproject.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.joda.time.LocalTime.now;

@Service
public class ANCService {
    private static Logger logger = LoggerFactory.getLogger(ANCService.class.toString());

    private final AllMothers allMothers;
    private AllEligibleCouples eligibleCouples;
    private ANCSchedulesService ancSchedulesService;
    private ActionService actionService;
    private MotherReportingService reportingService;

    @Autowired
    public ANCService(AllMothers allMothers, AllEligibleCouples eligibleCouples, ANCSchedulesService ancSchedulesService, ActionService actionService, MotherReportingService reportingService) {
        this.allMothers = allMothers;
        this.eligibleCouples = eligibleCouples;
        this.ancSchedulesService = ancSchedulesService;
        this.actionService = actionService;
        this.reportingService = reportingService;
    }

    public void registerANCCase(AnteNatalCareEnrollmentInformation info, Map<String, Map<String, String>> extraData) {
        Map<String, String> details = extraData.get("details");

        EligibleCouple couple = eligibleCouples.findByCaseId(info.ecCaseId());
        Mother mother = new Mother(info.caseId(), info.thaayiCardNumber(), couple.wife()).withAnm(info.anmIdentifier(), info.anmPhoneNumber())
                .withLMP(info.lmpDate()).withECNumber(couple.ecNumber()).withLocation(couple.village(), couple.subCenter(), couple.phc())
                .withFacility(info.deliveryPlace()).withDetails(details).isHighRisk(info.isHighRisk());
        allMothers.register(mother);

        Time preferredAlertTime = new Time(new LocalTime(14, 0));
        LocalDate referenceDate = info.lmpDate() != null ? info.lmpDate() : DateUtil.today();

        reportingService.registerANC(new SafeMap(extraData.get("reporting")), couple.village(), couple.subCenter());
        ancSchedulesService.enrollMother(info.caseId(), referenceDate, new Time(now()), preferredAlertTime);
        actionService.registerPregnancy(info.caseId(), couple.ecNumber(), info.thaayiCardNumber(), info.anmIdentifier(), couple.village(), info.lmpDate(), info.isHighRisk(), info.deliveryPlace(), details);
    }

    public void ancCareHasBeenProvided(AnteNatalCareInformation ancInformation, Map<String, Map<String, String>> extraData) {
        if (!allMothers.motherExists(ancInformation.caseId())) {
            logger.warn("Found care provided without registered mother for case ID: " + ancInformation.caseId());
            return;
        }

        for (Map.Entry<Integer, LocalDate> entry : ancInformation.ancVisits().entrySet()) {
            ancSchedulesService.ancVisitHasHappened(ancInformation.caseId(), entry.getKey(), entry.getValue());
        }

        if (ancInformation.areIFATabletsProvided()) {
            ancSchedulesService.ifaVisitHasHappened(ancInformation.caseId(), DateUtil.today());
        }
    }

    public void updatePregnancyOutcome(AnteNatalCareOutcomeInformation outcomeInformation) {
    }

    public void closeANCCase(AnteNatalCareCloseInformation closeInformation, SafeMap data) {
        if (!allMothers.motherExists(closeInformation.caseId())) {
            logger.warn("Tried to close case without registered mother for case ID: " + closeInformation.caseId());
            return;
        }

        reportingService.closeANC(data);
        ancSchedulesService.closeCase(closeInformation.caseId());
        actionService.updateDeliveryOutcome(closeInformation.caseId(), closeInformation.reason());
    }
}
