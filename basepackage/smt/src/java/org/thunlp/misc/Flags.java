package org.thunlp.misc;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.hadoop.mapred.JobConf;

public class Flags {
	protected Options options;
	protected CommandLine cmdLine;
	protected Hashtable<String, String> optionValues;
	public static String FLAGS_PREFIX = "flags_";

	public Flags() {
		options = new Options();
		optionValues = new Hashtable<String, String>();
	}

	public void add(String name) {
		add(name, "<no description>");
	}

	public void add(String name, String description) {
		add(name, true, true, description);
	}

	public void add(String name, boolean hasValue, boolean required, String description) {
		Option option = new Option(name, hasValue, description);
		option.setRequired(required);
		options.addOption(option);
		optionValues.put(name, "");
	}

	public void addWithDefaultValue(String name, String defaultValue) {
		addWithDefaultValue(name, defaultValue, "");
	}

	public void addWithDefaultValue(String name, String defaultValue, String description) {
		Option option = new Option(name, true, description);
		option.setRequired(false);
		options.addOption(option);
		optionValues.put(name, defaultValue);
	}

	public void parse(String[] args) throws ParseException {
		CommandLineParser parser = new PosixParser();
		cmdLine = parser.parse(options, args);
		for (String optionName : optionValues.keySet()) {
			if (optionName == null)
				continue;
			String value = cmdLine.getOptionValue(optionName);
			if (value != null) {
				optionValues.put(optionName, value);
			}
		}
	}

	public void parseAndCheck(String[] args) {
		try {
			parse(args);
		} catch (ParseException e) {
			printHelp();
			System.exit(0);
		}
	}

	public void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(" ", options);
	}

	public int getInt(String name) {
		return Integer.parseInt(optionValues.get(name));
	}

	public double getDouble(String name) {
		return Double.parseDouble(optionValues.get(name));
	}

	public String getString(String name) {
		return optionValues.get(name);
	}

	public boolean getBoolean(String name) {
		return Boolean.parseBoolean(optionValues.get(name));
	}

	public File getFile(String name) {
		return new File(optionValues.get(name));
	}

	public void saveToJobConf(JobConf job) {
		for (Entry<String, String> entry : optionValues.entrySet()) {
			job.set(FLAGS_PREFIX + entry.getKey(), entry.getValue());
		}
	}

	public void loadFromJobConf(JobConf job) {
		Iterator<Entry<String, String>> iter = job.iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			if (entry.getKey().startsWith(FLAGS_PREFIX)) {
				String optionName = entry.getKey().substring(FLAGS_PREFIX.length());
				optionValues.put(optionName, entry.getValue());
			}
		}
	}

}
