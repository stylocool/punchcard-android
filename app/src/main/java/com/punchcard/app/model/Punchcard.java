package com.punchcard.app.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;

public class Punchcard implements Serializable {

	private static final long serialVersionUID = -6504702694005449972L;

    public static String[] STATUS = new String[] {"new", "checkin", "checkout", "sync"};

	private Long id;
    private Long projectId;
    private Long workerId;
    private Date checkin;
    private Date checkout;
    private String checkinLocation;
    private String checkoutLocation;
    private String status;
    private Date syncDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Date getCheckin() {
        return checkin;
    }

    public void setCheckin(Date checkin) {
        this.checkin = checkin;
    }

    public Date getCheckout() {
        return checkout;
    }

    public void setCheckout(Date checkout) {
        this.checkout = checkout;
    }

    public String getCheckinLocation() {
        return checkinLocation;
    }

    public void setCheckinLocation(String checkinLocation) {
        this.checkinLocation = checkinLocation;
    }

    public String getCheckoutLocation() {
        return checkoutLocation;
    }

    public void setCheckoutLocation(String checkoutLocation) {
        this.checkoutLocation = checkoutLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(Date syncDate) {
        this.syncDate = syncDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
        		append("id", id).
                append("projectId", projectId).
                append("workerId", workerId).
                append("checkinLocation", checkinLocation).
                append("checkin", checkin != null ? checkin.toString() : "").
                append("checkoutLocation", checkoutLocation).
                append("checkout", checkout != null ? checkout.toString() : "").
                append("status", checkoutLocation).
                append("syncDate", syncDate != null ? syncDate.toString() : "").
                toString();
        
    }
}
