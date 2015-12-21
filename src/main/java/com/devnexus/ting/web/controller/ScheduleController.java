/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devnexus.ting.web.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.devnexus.ting.core.service.BusinessService;
import com.devnexus.ting.model.Event;
import com.devnexus.ting.model.ScheduleItem;
import com.devnexus.ting.model.ScheduleItemList;

/**
 * @author Gunnar Hillert
 */
@Controller
public class ScheduleController {

	@Autowired private BusinessService businessService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleController.class);

	private String prepareSchedule(Event event, final Model model) {
		model.addAttribute("event", event);
		model.addAttribute("contextEvent", event);

		if (event != null) {
			final ScheduleItemList scheduleItemList = businessService.getScheduleForEvent(event.getId());
			model.addAttribute("scheduleItemList", scheduleItemList);
		}
		else {
			LOGGER.warn("No current event available.");
		}

		return "schedule";
	}

	@RequestMapping("/s/{eventKey}/schedule")
	public String schedule(@PathVariable("eventKey") String eventKey, final Model model) {
		final Event event = businessService.getEventByEventKey(eventKey);
		return prepareSchedule(event, model);
	}

	@RequestMapping("/s/schedule")
	public String scheduleForCurrentEvent(final Model model) {
		final Event event = businessService.getCurrentEvent();
		return prepareSchedule(event, model);
	}

	@RequestMapping("/s/schedule.pdf")
	public void scheduleAsPdfForCurrentEvent(@PathVariable("eventKey") String eventKey, HttpServletResponse response)
		throws IOException {

		final Event event = businessService.getCurrentEvent();
		prepareScheduleAsPdf(event, response);
	}

	@RequestMapping("/s/{eventKey}/schedule.pdf")
	public void scheduleAsPdf(@PathVariable("eventKey") String eventKey, HttpServletResponse response)
		throws IOException {

		final Event event = businessService.getEventByEventKey(eventKey);
		prepareScheduleAsPdf(event, response);

	}

	private void prepareScheduleAsPdf(Event event, HttpServletResponse response)
		throws IOException {

		final String pdfFileName = event.getEventKey() + "-schedule.pdf";

		response.setContentType("application/pdf");

		final String headerKey = "Content-Disposition";
		final String headerValue = String.format("attachment; filename=\"%s\"", pdfFileName);
		response.setHeader(headerKey, headerValue);

		final ScheduleItemList scheduleItemList = businessService.getScheduleForEvent(event.getId());
		final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MMMM d, yyyy");
		final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");

		final PdfUtils pdfUtils = new PdfUtils(72f, event.getTitle());

		try {

			for (Date conferenceDay : scheduleItemList.getDays()) {
				pdfUtils.println();
				pdfUtils.println(dateFormat.format(conferenceDay));
				pdfUtils.println();
				Date fromTimeslot = null;

				for (ScheduleItem scheduleItem : scheduleItemList.findHeaderItemsOnDate(conferenceDay)) {

					if (fromTimeslot == null || !fromTimeslot.equals(scheduleItem.getFromTime())) {
						fromTimeslot = scheduleItem.getFromTime();
						pdfUtils.print(timeFormat.format(fromTimeslot) + " – " + timeFormat.format(scheduleItem.getToTime()));
					}

					final String title = scheduleItem.getPresentation() != null ? scheduleItem.getPresentation().getTitle() : scheduleItem.getTitle();

					pdfUtils.print(150f, scheduleItem.getRoom().getName());
					pdfUtils.print(200, "");
					pdfUtils.println(title);

				}

				for (ScheduleItem scheduleItem : scheduleItemList.findBreakoutItemsOnDate(conferenceDay)) {

					if (fromTimeslot == null || !fromTimeslot.equals(scheduleItem.getFromTime())) {
						fromTimeslot = scheduleItem.getFromTime();
						pdfUtils.println();
						pdfUtils.println(timeFormat.format(fromTimeslot) + " – " + timeFormat.format(scheduleItem.getToTime()));
						pdfUtils.println();
					}

					final String title = scheduleItem.getPresentation() != null ? scheduleItem.getPresentation().getTitle() : "N/A";

					pdfUtils.print(scheduleItem.getRoom().getName(), scheduleItem.getRoom().getColor());
					pdfUtils.print(150, "");
					pdfUtils.println(title);

				}

			}

			pdfUtils.done(response.getOutputStream());
		}

		finally {
			pdfUtils.getDoc().close();
		}
	}

	@RequestMapping("/s/new-schedule")
	public String newscheduleForCurrentEvent(final Model model) {

		final Event event = businessService.getCurrentEvent();
		model.addAttribute("event", event);

		if (event != null) {
			final ScheduleItemList scheduleItemList = businessService.getScheduleForEvent(event.getId());
			model.addAttribute("scheduleItemList", scheduleItemList);
		}
		else {
			LOGGER.warn("No current event available.");
		}

		return "new-schedule";
	}
}
