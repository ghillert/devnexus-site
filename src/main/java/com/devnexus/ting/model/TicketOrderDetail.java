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
package com.devnexus.ting.model;

import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Summers Pittman
 */
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TicketOrderDetail extends BaseModelObject implements Comparable<TicketOrderDetail> {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Size(max = 255)
    private String firstName;
    
    @NotNull
    @Size(max = 255)
    private String lastName;
    
    @NotNull
    @Size(max = 255)
    private String emailAddress;

    @Size(min = 1, max = 255)
    @NotNull
    private String city;
    
    @Size(min = 1, max = 255)
    @NotNull
    private String state;
    
    @Size(min = 1, max = 255)
    @NotNull
    private String country;

    @Size(min = 1, max = 255)
    @NotNull
    private String jobTitle;

    @Size(min = 1, max = 255)
    @NotNull
    private String company;

    @Size(max = 255)
    private String tShirtSize;
    @Size(max = 255)
    private String vegetarian;

    @Size(max = 255)
    private String sponsorMayContact = "True";

    private Long ticketAddOn;
    
    @ManyToOne
    @XmlTransient
    private RegistrationDetails registration;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String gettShirtSize() {
        return tShirtSize;
    }

    public void settShirtSize(String tShirtSize) {
        this.tShirtSize = tShirtSize;
    }

    public String getVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(String vegetarian) {
        this.vegetarian = vegetarian;
    }

    public RegistrationDetails getRegistration() {
        return registration;
    }

    public void setRegistration(RegistrationDetails registration) {
        this.registration = registration;
    }

    @Override
    public int compareTo(TicketOrderDetail o) {
        if (lastName.equals(o.lastName)) {
            return firstName.compareTo(o.firstName);
        }
        return lastName.compareTo(o.lastName);
    }

    public Long getTicketAddOn() {
        return ticketAddOn;
    }

    public void setTicketAddOn(Long ticketAddOn) {
        this.ticketAddOn = ticketAddOn;
    }

    public String getSponsorMayContact() {
        return sponsorMayContact;
    }

    public void setSponsorMayContact(String sponsorMayContact) {
        this.sponsorMayContact = sponsorMayContact;
    }

    
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.firstName);
        hash = 23 * hash + Objects.hashCode(this.lastName);
        hash = 23 * hash + Objects.hashCode(this.emailAddress);
        hash = 23 * hash + Objects.hashCode(this.city);
        hash = 23 * hash + Objects.hashCode(this.state);
        hash = 23 * hash + Objects.hashCode(this.country);
        hash = 23 * hash + Objects.hashCode(this.jobTitle);
        hash = 23 * hash + Objects.hashCode(this.company);
        hash = 23 * hash + Objects.hashCode(this.tShirtSize);
        hash = 23 * hash + Objects.hashCode(this.vegetarian);
        hash = 23 * hash + Objects.hashCode(this.ticketAddOn);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TicketOrderDetail other = (TicketOrderDetail) obj;
        if (!Objects.equals(this.firstName, other.firstName)) {
            return false;
        }
        if (!Objects.equals(this.lastName, other.lastName)) {
            return false;
        }
        if (!Objects.equals(this.emailAddress, other.emailAddress)) {
            return false;
        }
        if (!Objects.equals(this.city, other.city)) {
            return false;
        }
        if (!Objects.equals(this.state, other.state)) {
            return false;
        }
        if (!Objects.equals(this.country, other.country)) {
            return false;
        }
        if (!Objects.equals(this.jobTitle, other.jobTitle)) {
            return false;
        }
        if (!Objects.equals(this.company, other.company)) {
            return false;
        }
        if (!Objects.equals(this.tShirtSize, other.tShirtSize)) {
            return false;
        }
        if (!Objects.equals(this.vegetarian, other.vegetarian)) {
            return false;
        }
        if (!Objects.equals(this.ticketAddOn, other.ticketAddOn)) {
            return false;
        }
        return true;
    }

    
    
}
