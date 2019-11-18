package com.siriusxm.transform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class DataTransform {

	public void transformXMLToJSON(String filePath) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
		String line;
		StringBuilder sb = new StringBuilder();
		List<JSONObject> locations = new ArrayList<JSONObject>();
		while ((line = br.readLine()) != null) {
			sb.append(line.trim());
		}

		JSONObject xmlJSONObj = XML.toJSONObject(sb.toString());
		JSONObject pilot = xmlJSONObj.getJSONObject(Config.XML_INCIDENT);
		JSONObject pilot1 = pilot.getJSONObject(Config.XML_TI);
		JSONArray ev = pilot1.getJSONArray(Config.XML_EV);

		if (ev.length() > 0) {
			for (int i = 0; i < ev.length(); i++) {

				JSONObject mainObject = ev.getJSONObject(i);
				JSONObject locationSegment = mainObject.getJSONObject(Config.XML_LOC);

				JSONObject mainJson = new JSONObject();
				JSONObject innerJson = new JSONObject();

				JSONObject valid = mainObject.getJSONObject(Config.XML_VALID);
				JSONObject text = mainObject.getJSONObject(Config.XML_TEXT);
				mainJson.put("_id", mainObject.getInt(Config.XML_ID));
				mainJson.put("description", text.getString("content"));
				mainJson.put("validStart", valid.getString("start"));
				mainJson.put("validEnd", valid.getString("end"));
				mainJson.put("eventCode", mainObject.getInt(Config.XML_EVENT_CODE));
				mainJson.put("severity", mainObject.getInt(Config.XML_SEVERITY));
				mainJson.put("type", "TrafficIncident");
				mainJson.put("lastUpdated", getNewISODate());

				
				// handle loc type: "geo"
				if (locationSegment.has("geo")) {
					JSONObject geo = locationSegment.getJSONObject("geo");
					List<Double> cordinates = new ArrayList<Double>();
					cordinates.add(Double.valueOf(geo.getDouble("lon")));
					cordinates.add(Double.valueOf(geo.getDouble("lat")));
					mainJson.put("roadName", locationSegment.getString("addr"));
					innerJson.put("type", "point");
					innerJson.put("coordinates", cordinates);
					mainJson.put("geo", innerJson);
				}
				
				// handle loc type: "tmc"
				if (locationSegment.has("start")) {
					JSONObject start = locationSegment.getJSONObject("start");
					JSONObject tmcJson = new JSONObject();
					tmcJson.put("table", start.getInt("extent"));
					tmcJson.put("id", start.getInt("id"));
					tmcJson.put("direction", "+");
					mainJson.put("tmc", tmcJson);
				}

				locations.add(mainJson);
			}
		}

		JSONObject location = new JSONObject();
		location.put("locations", locations);

		try (FileWriter file = new FileWriter("output.json")) {
			file.write(location.toString());
			file.flush();
			System.out.println("XML transformed to JSON successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getNewISODate() {
		Date date = new Date();
		String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);
		return formatted;
	}
}
