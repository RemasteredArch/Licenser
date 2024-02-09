package net.remasteredarch.licenser;

import java.util.*;

public class Licenser {
	static String reset_text = "\033[0m";
	static String bold_text = "\033[1m";

	public static void main(String[] args) {
		parseOptions(args);
	}

	private static void parseOptions(String[] args) {
		for (String arg : args) {
			if (arg == "--help" || arg == "-h") {
				helpMessage();
			}
		}
	}

	private static void helpMessage() {
		System.out.println(bold_text + "Licenser:" + reset_text);
	}
}
