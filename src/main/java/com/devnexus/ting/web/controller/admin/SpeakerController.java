/*
 * Copyright 2002-2014 the original author or authors.
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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devnexus.ting.core.service.BusinessService;
import com.devnexus.ting.model.Event;
import com.devnexus.ting.model.FileData;
import com.devnexus.ting.model.Speaker;
import com.devnexus.ting.model.SpeakerList;

/**
 * Retrieves all jobs and returns an XML document. The structure conforms to the layout
 * defined by Indeed.com
 *
 * @author Gunnar Hillert
 *
 */
@Controller("adminSpeakerController")
public class SpeakerController {

	@Autowired private BusinessService businessService;
	@Autowired private MultipartProperties multipartProperties;

	@RequestMapping(value="/s/admin/speakers", method=RequestMethod.GET)
	public String getAllSpeakers(ModelMap model, HttpServletRequest request) {
		final List<Speaker> speakers = businessService.getAllSpeakersOrderedByName();
		model.addAttribute("speakers", speakers);
		return "speakers";
	}

	@RequestMapping("/s/admin/{eventKey}/speakers")
	public String getSpeakersForEvent(@PathVariable("eventKey") String eventKey, Model model) {
		final Event event = businessService.getEventByEventKey(eventKey);
		final SpeakerList speakerList = new SpeakerList();
		final List<Speaker> speakers = businessService.getSpeakersForEvent(event.getId());
		speakerList.setSpeakers(speakers);
		model.addAttribute("speakerList", speakerList);
		model.addAttribute("event", event);
		return "/admin/manage-speakers";
	}

	@RequestMapping(value="/s/admin/speaker", method=RequestMethod.GET)
	public String prepareAddSpeaker(ModelMap model) {

		final List<Event> events = businessService.getAllEventsOrderedByName();
		model.addAttribute("events", events);
		final Speaker speakerForm = new Speaker();
		model.addAttribute("speaker", speakerForm);
		return "/admin/add-speaker";
	}

	@RequestMapping(value="/s/admin/{eventKey}/speaker", method=RequestMethod.GET)
	public String prepareAddSpeakerForEvent(ModelMap model) {

		final List<Event> events = businessService.getAllEventsOrderedByName();
		model.addAttribute("events", events);
		model.addAttribute("maxFileSize", multipartProperties.getMaxFileSize());

		final Speaker speakerForm = new Speaker();
		model.addAttribute("speaker", speakerForm);
		return "/admin/add-speaker";
	}

	@RequestMapping(value="/s/admin/{eventKey}/speaker/{speakerId}", method=RequestMethod.GET)
	public String prepareEditSpeaker(@PathVariable("speakerId") Long speakerId, ModelMap model) {

		final List<Event> events = businessService.getAllEventsOrderedByName();

		model.addAttribute("events", events);
		model.addAttribute("maxFileSize", multipartProperties.getMaxFileSize());

		Speaker speakerForm = businessService.getSpeaker(speakerId);

		model.addAttribute("speaker", speakerForm);

		return "/admin/add-speaker";
	}

	@RequestMapping(value="/s/admin/{eventKey}/speaker/{speakerId}", method=RequestMethod.POST)
	public String editSpeaker(
			@PathVariable("eventKey") String eventKey,
			@PathVariable("speakerId") Long speakerId,
							  @RequestParam MultipartFile pictureFile,
							  @Valid Speaker speakerForm,
							  BindingResult result, HttpServletRequest request,
							  ModelMap model,
							  RedirectAttributes redirectAttributes) {

		if (request.getParameter("cancel") != null) {
			return "redirect:/s/admin/{eventKey}/speakers";
		}

		if (result.hasErrors()) {
			return "/admin/add-speaker";
		}

		final Speaker speakerFromDb = businessService.getSpeakerWithPicture(speakerId);

		speakerFromDb.setBio(speakerForm.getBio());
		speakerFromDb.setTwitterId(speakerForm.getTwitterId());
		speakerFromDb.setGooglePlusId(speakerForm.getGooglePlusId());
		speakerFromDb.setLinkedInId(speakerForm.getLinkedInId());
		speakerFromDb.setLanyrdId(speakerForm.getLanyrdId());
		speakerFromDb.setGithubId(speakerForm.getGithubId());

		speakerFromDb.setFirstName(speakerForm.getFirstName());
		speakerFromDb.setLastName(speakerForm.getLastName());
		speakerFromDb.setCompany(speakerForm.getCompany());


		if (pictureFile != null && pictureFile.getSize() > 0) {

			final FileData pictureData;
			if (speakerFromDb.getPicture()==null) {
				pictureData = new FileData();
			} else {
				pictureData = speakerFromDb.getPicture();
			}

			try {

				pictureData.setFileData(IOUtils.toByteArray(pictureFile.getInputStream()));
				pictureData.setFileSize(pictureFile.getSize());
				pictureData.setFileModified(new Date());
				pictureData.setName(pictureFile.getOriginalFilename());
				pictureData.setType(pictureFile.getContentType());

			} catch (IOException e) {
				throw new IllegalStateException(e);
			}

			speakerFromDb.setPicture(pictureData);

		}

		businessService.saveSpeaker(speakerFromDb);

		redirectAttributes.addFlashAttribute("successMessage",
				String.format("The speaker '%s' was edited successfully.", speakerFromDb.getFirstLastName()));

		return "redirect:/s/admin/{eventKey}/speakers";
	}

	@RequestMapping(value="/s/admin/speaker", method=RequestMethod.POST)
	public String addSpeaker(@RequestParam MultipartFile pictureFile, @Valid Speaker speakerForm, BindingResult result, HttpServletRequest request, RedirectAttributes redirectAttributes) {

		Event currentEvent = this.businessService.getCurrentEvent();

		redirectAttributes.addAttribute("eventKey", currentEvent.getEventKey());

		if (request.getParameter("cancel") != null) {
			return "redirect:/s/admin/{eventKey}/speakers";
		}

		if (result.hasErrors()) {
			return "/admin/add-speaker";
		}

		if (pictureFile != null && pictureFile.getSize() > 0) {

			 final FileData pictureData = new FileData();

			 try {

				 pictureData.setFileData(IOUtils.toByteArray(pictureFile.getInputStream()));
				 pictureData.setFileSize(pictureFile.getSize());
				 pictureData.setFileModified(new Date());
				 pictureData.setName(pictureFile.getOriginalFilename());
				 pictureData.setType(pictureFile.getContentType());

			 } catch (IOException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
			 }

			 speakerForm.setPicture(pictureData);

		}

		final Speaker savedSpeaker = businessService.saveSpeaker(speakerForm);

		redirectAttributes.addFlashAttribute("successMessage",
				String.format("The speaker '%s' was added successfully.", savedSpeaker.getFirstLastName()));

		return "redirect:/s/admin/{eventKey}/speakers";
	}

}
