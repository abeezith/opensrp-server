/**
 * @author julkar nain 
 */
package org.opensrp.register.mcare.service.handler;

import java.util.Map;

import org.opensrp.common.util.EasyMap;
import org.opensrp.form.repository.AllFormSubmissions;
import org.opensrp.service.formSubmission.handler.FormSubmissionHandler;
import org.opensrp.service.formSubmission.handler.IHandlerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static org.opensrp.common.AllConstants.Form.*;

@Service
public class HandlerMapper implements IHandlerMapper {

	private final Map<String, FormSubmissionHandler> handlerMap;
	
	@Autowired
	public HandlerMapper(  
			AllFormSubmissions formSubmissionsRepository,
			HHRegistrationHandler hhRegistrationHandler,
			ANCVisitOneHandler ancVisitOneHandler,
			ANCVisitTwoHandler ancVisitTwoHandler,
			ANCVisitThreeHandler ancVisitThreeHandler,
			ANCVisitFourHandler ancVisitFourHandler,
			TT1_Handler TT1_Handler,
			TT2_Handler TT2_Handler,
			TT3_Handler TT3_Handler,
			TT4_Handler TT4_Handler,
			TT5_Handler TT5_Handler,
			ENCCVisitOneHandler enccVisitOneHandler,
			ENCCVisitTwoHandler enccVisitTwoHandler,
			ENCCVisitThreeHandler enccVisitThreeHandler
			)
	{
		handlerMap = EasyMap.create(HH_REGISTRATION, (FormSubmissionHandler) hhRegistrationHandler)
				     .put(ANC_REMINDER_VISIT_1, ancVisitOneHandler)
				     .put(ANC_REMINDER_VISIT_2, ancVisitTwoHandler)
				     .put(ANC_REMINDER_VISIT_3, ancVisitThreeHandler)
				     .put(ANC_REMINDER_VISIT_4, ancVisitFourHandler)
				     .put(TT1_Visit_Handler, TT1_Handler)
				     .put(TT2_Visit_Handler, TT2_Handler)
				     .put(TT3_Visit_Handler, TT3_Handler)
				     .put(TT4_Visit_Handler, TT4_Handler)
				     .put(TT5_Visit_Handler, TT5_Handler)
				     .put(ENCC_REMINDER_VISIT_1, enccVisitOneHandler)
				     .put(ENCC_REMINDER_VISIT_2, enccVisitTwoHandler)
				     .put(ENCC_REMINDER_VISIT_3, enccVisitThreeHandler)
                     .map();
	}

	@Override
	public Map<String, FormSubmissionHandler> handlerMapper() {
		return handlerMap;
	}

}