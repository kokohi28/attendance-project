package com.divt.attendance.android.model;

import com.divt.attendance.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Attendance implements Serializable {
  private String id;
  private long idx;
  private int type;
  private int shift;
  private long in;
  private long out;
  private long income;
  private String userId;
  private String userName;
  private long outletIdx;
  private String outletName;
  private String outletArea;
  private String ref;
  private String reportId;
  private long inAt;
  private long createdAt;
  private long updatedAt;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public long getIdx() {
    return idx;
  }

  public void setIdx(long idx) {
    this.idx = idx;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getShift() {
    return shift;
  }

  public void setShift(int shift) {
    this.shift = shift;
  }

  public long getIn() {
    return in;
  }

  public void setIn(long in) {
    this.in = in;
  }

  public long getOut() {
    return out;
  }

  public void setOut(long out) {
    this.out = out;
  }

  public long getIncome() {
    return income;
  }

  public void setIncome(long income) {
    this.income = income;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public long getOutletIdx() {
    return outletIdx;
  }

  public void setOutletIdx(long outletIdx) {
    this.outletIdx = outletIdx;
  }

  public String getOutletName() {
    return outletName;
  }

  public void setOutletName(String outletName) {
    this.outletName = outletName;
  }

  public String getOutletArea() {
    return outletArea;
  }

  public void setOutletArea(String outletArea) {
    this.outletArea = outletArea;
  }

  public String getRef() {
    return ref;
  }

  public void setRef(String ref) {
    this.ref = ref;
  }

  public String getReportId() {
    return reportId;
  }

  public void setReportId(String reportId) {
    this.reportId = reportId;
  }

  public long getInAt() {
    return inAt;
  }

  public void setInAt(long inAt) {
    this.inAt = inAt;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }

  public static Attendance attendanceFromJson(JSONObject json) {
    try {
      Attendance a = new Attendance();

      if (json.has("id")) a.id = json.getString("id");
      if (json.has("idx")) a.idx = json.getLong("idx");
      if (json.has("uid")) a.userId = json.getString("uid");
      if (json.has("name")) a.userName = json.getString("name");
      if (json.has("type")) a.type = json.getInt("type");
      if (json.has("outlet")) a.outletIdx = json.getLong("outlet");
      if (json.has("area")) a.outletArea = json.getString("area");
      if (json.has("outlet_name")) a.outletName = json.getString("outlet_name");
      if (json.has("shift")) a.shift = json.getInt("shift");
      if (json.has("ref")) a.ref = json.getString("ref");
      if (json.has("income")) a.income = json.getLong("income");

      if (json.has("updated_at")) {
        a.updatedAt = Utils.timeSQLToTimestamp(json.getString("updated_at"));
      }
      if (json.has("created_at")) {
        a.createdAt = Utils.timeSQLToTimestamp(json.getString("created_at"));
      }

      return a;
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return null;
  }
}
