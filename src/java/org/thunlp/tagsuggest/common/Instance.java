package org.thunlp.tagsuggest.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

public class Instance {
	private int ID;
	private int catID;
	public HashMap<Integer, Double> labelValues = new HashMap<Integer, Double>();
	public HashMap<Integer, Double> featureValues = new HashMap<Integer, Double>();

	public int getID() {
		return ID;
	}

	public void addFeature(int featureID, double featureValue) {
		featureValues.put(featureID, featureValue);
	}

	public void loadFeatures(HashMap<Integer, Double> featureValues) {
		this.featureValues = featureValues;
	}

	public void loadLabels(HashMap<Integer, Double> labelValues) {
		this.labelValues = labelValues;
	}

	public void addLabel(int labelID, double labelValue) {
		featureValues.put(labelID, labelValue);
	}

	public Instance(int ID, int catID, HashMap<Integer, Double> featureValues,
			HashMap<Integer, Double> labelValues) {
		this.ID = ID;
		this.catID = catID;
		loadFeatures(featureValues);
		loadLabels(labelValues);
	}

	@SuppressWarnings("unchecked")
	public Instance(String jsonStr) {
		try {
			JSONObject instance = new JSONObject(jsonStr);
			this.ID = instance.getInt("ID");
			this.catID = instance.getInt("catID");
			JSONObject labels = instance.getJSONObject("labels");
			JSONObject features = instance.getJSONObject("features");

			Iterator<String> labelIter = labels.keys();
			while (labelIter.hasNext()) {
				String label = labelIter.next();
				double value = labels.getDouble(label);
				labelValues.put(Integer.valueOf(label), value);
			}

			Iterator<String> featureIter = features.keys();
			while (featureIter.hasNext()) {
				String feature = featureIter.next();
				double value = features.getDouble(feature);
				featureValues.put(Integer.valueOf(feature), value);
			}
		} catch (JSONException e) {
			System.out.println("Error Str: " + jsonStr);
			e.printStackTrace();
		}
	}

	public String toString() {
		JSONObject instance = new JSONObject();
		try {
			instance.put("ID", ID);
			instance.put("catID", catID);
			JSONObject labels = new JSONObject();
			Iterator<Entry<Integer, Double>> labelIter = labelValues.entrySet()
					.iterator();
			while (labelIter.hasNext()) {
				Entry<Integer, Double> entry = labelIter.next();
				int labelID = entry.getKey();
				double value = entry.getValue();
				labels.put(Integer.toString(labelID), value);
			}
			instance.put("labels", labels);

			JSONObject features = new JSONObject();
			Iterator<Entry<Integer, Double>> featureIter = featureValues
					.entrySet().iterator();
			while (featureIter.hasNext()) {
				Entry<Integer, Double> entry = featureIter.next();
				int featureID = entry.getKey();
				double value = entry.getValue();
				features.put(new Integer(featureID).toString(), value);
			}
			instance.put("features", features);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return instance.toString();
	}
}
