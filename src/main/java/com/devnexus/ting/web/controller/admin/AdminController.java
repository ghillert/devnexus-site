/*
 * Copyright 2002-2013 the original author or authors.
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
package com.devnexus.ting.web.controller.admin;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.IOUtils;

import com.devnexus.ting.core.service.BusinessService;
import com.devnexus.ting.core.service.UserService;
import com.devnexus.ting.model.CfpSpeakerImage;
import com.devnexus.ting.model.CfpSubmission;
import com.devnexus.ting.model.CfpSubmissionSpeaker;
import com.devnexus.ting.model.CfpSubmissionStatusType;
import com.devnexus.ting.model.Event;
import com.devnexus.ting.model.Presentation;
import com.devnexus.ting.model.Speaker;
import com.devnexus.ting.security.SecurityFacade;
import com.devnexus.ting.web.controller.admin.support.CsvRejectedSpeakerBean;
import com.devnexus.ting.web.controller.admin.support.CsvSpeakerBean;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;

/**
 * Main Admin Controller.
 *
 * @author Gunnar Hillert
 *
 */
@Controller
public class AdminController {

	@Autowired private BusinessService businessService;
	@Autowired private UserService userService;
	@Autowired private Validator validator;
	@Autowired private GuavaCacheManager cacheManager;
	@Autowired private SecurityFacade securityFacade;

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

	@RequestMapping({"/s/admin", "/s/admin/"})
	public String redirectToAdmin(ModelMap model) {
		return "redirect:/s/admin/index";
	}

	@RequestMapping({"/s/admin/index"})
	public String execute(RedirectAttributes redirectAttributes, HttpServletRequest r) {
		final Event currentEvent = businessService.getCurrentEvent();
		redirectAttributes.addAttribute("eventKey", currentEvent.getEventKey());
		return "redirect:/s/admin/{eventKey}/index";
	}

	@RequestMapping("/s/admin/{eventKey}/index")
	public String getSpeakersForEvent(@PathVariable("eventKey") String eventKey, Model model) {
		final Event event = businessService.getEventByEventKey(eventKey);
		model.addAttribute("event", event);
		model.addAttribute("events", businessService.getAllEventsOrderedByName());

		final Collection<String> cacheNames = cacheManager.getCacheNames();
		final Map<String, CacheStats> cacheStats = new HashMap<>();

		for (String cacheName : cacheNames) {
			Cache c = (Cache) cacheManager.getCache(cacheName).getNativeCache();
			LOGGER.warn("Cache Stats for '{}' : {}", cacheName, c.stats().toString());
			cacheStats.put(cacheName, c.stats());
		}
		model.addAttribute("cacheStats", cacheStats);
		return "/admin/index";
	}

	@RequestMapping("/s/admin/{eventKey}/download-cfp-speaker-images")
	public void downloadCfpSpeakerImagesForEvent(@PathVariable("eventKey") String eventKey, HttpServletResponse response)
			throws IOException {
		final Event event = businessService.getEventByEventKey(eventKey);
		final String zipFileName = event.getEventKey() + "-accepted-speaker-images.zip";

		final List<CfpSubmission> cfpSubmissions = businessService.getCfpSubmissions(event.getId());

		final Set<CfpSubmissionSpeaker> speakersWithPictures = new HashSet<>();

		for (CfpSubmission cfpSubmission : cfpSubmissions) {

			if (CfpSubmissionStatusType.ACCEPTED.equals(cfpSubmission.getStatus())) {
				for (CfpSubmissionSpeaker cfpSubmissionSpeaker : cfpSubmission.getCfpSubmissionSpeakers()) {
					CfpSubmissionSpeaker cfpSubmissionSpeakerWithPicture = businessService.getCfpSubmissionSpeakerWithPicture(cfpSubmissionSpeaker.getId());
					if (cfpSubmissionSpeakerWithPicture.getCfpSpeakerImage() != null) {
						speakersWithPictures.add(cfpSubmissionSpeakerWithPicture);
					}

				}
			}
		}

		response.setContentType("application/octet-stream");

		final String headerKey = "Content-Disposition";
		final String headerValue = String.format("attachment; filename=\"%s\"", zipFileName);
		response.setHeader(headerKey, headerValue);

		List<ZipEntrySource> entries = new ArrayList<>();

		for (CfpSubmissionSpeaker cfpSubmissionSpeaker : speakersWithPictures) {
			CfpSpeakerImage image = cfpSubmissionSpeaker.getCfpSpeakerImage();

			ByteArrayInputStream bais = new ByteArrayInputStream(image.getFileData());
			BufferedImage bufferedImage = ImageIO.read(bais);

			final String suffix;
			if ("image/png".equals(image.getType())) {
				suffix = ".png";
			}
			else {
				suffix = ".jpg";
			}

			final ZipEntrySource entry = new ByteSource(
					cfpSubmissionSpeaker.getFirstName() + "_" + cfpSubmissionSpeaker.getLastName()
					+ "_" + bufferedImage.getWidth() + "x" + bufferedImage.getWidth() + "_"
					+ cfpSubmissionSpeaker.getId()
					+ suffix, image.getFileData());

			entries.add(entry);
		}

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(response.getOutputStream());
			ZipUtil.pack(entries.toArray(new ZipEntrySource[entries.size()]), out);
		}
		finally {
			IOUtils.closeQuietly(out);
		}
	}

	@RequestMapping("/s/admin/{eventKey}/download-accepted-speakers")
	public void downloadAcceptedSpeakersForEvent(@PathVariable("eventKey") String eventKey, HttpServletResponse response)
		throws IOException {

		final Event event = businessService.getEventByEventKey(eventKey);

		final String csvFileName = event.getEventKey() + "-accepted-speakers.csv";

		response.setContentType("text/csv");

		final String headerKey = "Content-Disposition";
		final String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
		response.setHeader(headerKey, headerValue);

		final List<Speaker> speakers = businessService.getSpeakersForEvent(event.getId());
		final List<CsvSpeakerBean> csvData = new ArrayList<>();

		for (Speaker speaker : speakers) {
			final CsvSpeakerBean csvSpeakerBean = new CsvSpeakerBean();
			csvSpeakerBean.setFirstName(speaker.getFirstName());
			csvSpeakerBean.setLastName(speaker.getLastName());
			csvSpeakerBean.setTwitterId(speaker.getTwitterId());

			final Long cfpSpeakerId = speaker.getCfpSpeakerId();

			if (cfpSpeakerId != null) {
				CfpSubmissionSpeaker cfpSpeaker = businessService.getCfpSubmissionSpeaker(cfpSpeakerId);
				if (cfpSpeaker != null) {
					csvSpeakerBean.setLocation(cfpSpeaker.getLocation());
					csvSpeakerBean.setPhone(cfpSpeaker.getPhone());
					csvSpeakerBean.setReimburseTravel(cfpSpeaker.isMustReimburseTravelCost());
					csvSpeakerBean.setTshirtSize(cfpSpeaker.getTshirtSize());
					csvSpeakerBean.setEmail(cfpSpeaker.getEmail());
				}
			}

			csvData.add(csvSpeakerBean);
		}

		ICsvBeanWriter beanWriter = null;
		try {
				beanWriter = new CsvBeanWriter(response.getWriter(),
						CsvPreference.STANDARD_PREFERENCE);

				final String[] header = new String[] {
						"firstName",
						"lastName",
						"email",
						"twitterId",
						"phone",
						"tshirtSize",
						"reimburseTravel",
						"location"
					};

				final CellProcessor[] processors = CsvSpeakerBean.getProcessors();

				beanWriter.writeHeader(header);

				for( final CsvSpeakerBean speaker : csvData ) {
						beanWriter.write(speaker, header, processors);
				}

		}
		finally {
				if( beanWriter != null ) {
						beanWriter.close();
				}
		}
	}

	private boolean speakerAlreadyAccepted(List<Presentation> presentations, CfpSubmissionSpeaker cfpSubmissionSpeaker) {
		for (Presentation presentation : presentations) {
			for (Speaker speaker : presentation.getSpeakers()) {
				if (speaker.getFirstName().toLowerCase().equals(cfpSubmissionSpeaker.getFirstName().trim().toLowerCase())
					&& speaker.getLastName().toLowerCase().equals(cfpSubmissionSpeaker.getLastName().trim().toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}


	@RequestMapping("/s/admin/{eventKey}/download-rejected-speakers")
	public void downloadRejectedSpeakersForEvent(@PathVariable("eventKey") String eventKey, HttpServletResponse response)
		throws IOException {

		final Event event = businessService.getEventByEventKey(eventKey);

		final String csvFileName = event.getEventKey() + "-rejected-speakers.csv";

		response.setContentType("text/csv");

		final String headerKey = "Content-Disposition";
		final String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
		response.setHeader(headerKey, headerValue);

		final List<CfpSubmission> cfpSubmissions = businessService.getCfpSubmissions(event.getId());
		final SortedSet<CsvRejectedSpeakerBean> rejectedSpeakers = new TreeSet<>();

		final List<Presentation> presentations = businessService.getPresentationsForEventOrderedByName(event.getId());

		for (CfpSubmission cfpSubmission : cfpSubmissions) {

			if (CfpSubmissionStatusType.REJECTED.equals(cfpSubmission.getStatus())) {

				for (CfpSubmissionSpeaker cfpSubmissionSpeaker : cfpSubmission.getCfpSubmissionSpeakers()) {

					boolean speakerAlreadyAccepted = speakerAlreadyAccepted(presentations, cfpSubmissionSpeaker);

					if (!speakerAlreadyAccepted) {

						final CsvRejectedSpeakerBean rejectedSpeaker = new CsvRejectedSpeakerBean();
						rejectedSpeaker.setEmail(cfpSubmissionSpeaker.getEmail().trim().toLowerCase());
						rejectedSpeaker.setFirstName(cfpSubmissionSpeaker.getFirstName().trim().toLowerCase());
						rejectedSpeaker.setLastName(cfpSubmissionSpeaker.getLastName().trim().toLowerCase());
						rejectedSpeaker.setLocation(cfpSubmissionSpeaker.getLocation());
						rejectedSpeaker.setPhone(cfpSubmissionSpeaker.getPhone());
						rejectedSpeaker.setReimburseTravel(cfpSubmissionSpeaker.isMustReimburseTravelCost());
						rejectedSpeaker.setTwitterId(cfpSubmissionSpeaker.getTwitterId());

						rejectedSpeakers.add(rejectedSpeaker);
					}

				}

			}

		}

		ICsvBeanWriter beanWriter = null;
		try {
				beanWriter = new CsvBeanWriter(response.getWriter(),
						CsvPreference.STANDARD_PREFERENCE);

				final String[] header = new String[] {
						"firstName",
						"lastName",
						"email",
						"twitterId",
						"phone",
						"reimburseTravel",
						"location"
					};

				final CellProcessor[] processors = CsvRejectedSpeakerBean.getProcessors();

				beanWriter.writeHeader(header);

				for( final CsvRejectedSpeakerBean rejectedSpeaker : rejectedSpeakers ) {
						beanWriter.write(rejectedSpeaker, header, processors);
				}

		}
		finally {
				if( beanWriter != null ) {
						beanWriter.close();
				}
		}
	}

	@RequestMapping(value="/s/admin/index", method=RequestMethod.POST)
	public String changeEvent(@ModelAttribute("event") Event event,
			BindingResult bindingResult,
			ModelMap model) {
		final String eventKey = businessService.getEvent(event.getId()).getEventKey();
		//model.addAttribute("eventKey",
		//model.put("eventKey", "aaaaa");
		return "redirect:/s/admin/" + eventKey + "/index";
	}

	@RequestMapping({"/s/admin/update-application-cache"})
	public String updateApplicationCache(ModelMap model) {
		businessService.updateApplicationCacheManifest();
		return "redirect:/s/admin/index";
	}

	@RequestMapping({"/s/admin/reset-spring-cache"})
	public String resetSpringCache(ModelMap model) {
		Collection<String> cacheNames = cacheManager.getCacheNames();
		LOGGER.warn("Clearing caches: {}", StringUtils.collectionToCommaDelimitedString(cacheNames));
		for (String cacheName : cacheNames) {
			cacheManager.getCache(cacheName).clear();
		}
		return "redirect:/s/admin/index";
	}

	@RequestMapping("/s/logout")
	public String logout(RedirectAttributes redirectAttributes) {

		final SecurityContext context = SecurityContextHolder.getContext();

		if (context.getAuthentication() != null) {
			LOGGER.info("Logging out user..." + context.getAuthentication().getName());
		} else {
			LOGGER.warn("User not logged in.");
		}

		context.setAuthentication(null);
		redirectAttributes.addFlashAttribute("succesMessage", "You logged out successfully.");

		return "/s/index";
	}

	@RequestMapping("/s/login")
	public String login(ModelMap model) {
		return "/login";
	}

}
