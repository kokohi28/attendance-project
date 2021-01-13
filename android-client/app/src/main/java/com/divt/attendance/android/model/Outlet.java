package com.divt.attendance.android.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Outlet implements Serializable {
  private String id;
  private long idx;
  private String area;
  private String name;
  private String location;
  private String description;

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

  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public static Outlet outletFromJson(JSONObject json) {
    try {
      Outlet o = new Outlet();

      if (json.has("idx")) o.idx = json.getLong("idx");
      if (json.has("outlet")) o.name = json.getString("outlet");
      if (json.has("area")) o.area = json.getString("area");
      if (json.has("location")) o.location = json.getString("location");
      if (json.has("description")) o.description = json.getString("description");

      return o;
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return null;
  }
}
