package com.siriusxm.transform;

public class Main {

	public static void main(String[] args) throws Exception {

		DataTransform dt = new DataTransform();

		String filePath = "input.xml";
		if (args != null && args.length > 0) {
			dt.transformXMLToJSON(args[0]);
		} else {
			dt.transformXMLToJSON(filePath);
		}
	}

}
