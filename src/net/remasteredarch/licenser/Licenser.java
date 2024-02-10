/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Copyright © 2024 RemasteredArch
 *
 * Licenser is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Licenser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Licenser. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package net.remasteredarch.licenser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Licenser {
	private final static String version = "v0.1";

	private final static String reset = "\033[0m";
	private final static String bold = "\033[1m";
	private final static String italic = "\033[3m";

	private static File path;
	private static boolean isDryRun;

	public static void main(String[] args) {
		parseOptions(args);
		System.out.println(path + " " + isDryRun);
	}

	private static void parseOptions(String[] args) {
		if (args.length == 0)
			helpMessage();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("--help") || arg.equals("-h")) {
				helpMessage();
			}
			if (arg.equals("--version") || arg.equals("-v")) {
				System.out.println(version);
				System.exit(0);
			}
			if (i == 0) {
				path = new File(arg);
			} else if (arg.equals("--dry-run") || arg.equals("-d")) {
				isDryRun = true;
			}
		}
	}

	private static void helpMessage() {
		System.out.println(bold + "Licenser " + italic + version + ":" + reset
				+ " Recursively apply a license and copyright header to an existing codebase for GPL compliance");
		System.out.println(bold + "\nUsage: " + reset + "java Licenser.java " + italic + "[path] [options]" + reset);
		System.out.println(bold + "\nPath:" + reset);
		System.out.println("  Provide a directory to recursively apply to all files within the given directory");
		System.out.println("  Provide a file to only apply to the given file");
		System.out.println(bold + "\nOptions:" + reset);
		System.out.println("  -h | --help     " + italic + "Displays this help message" + reset);
		System.out.println("  -v | --version  " + italic + "Displays the version of the program" + reset);
		System.out.println(
				"  -d | --dry-run  " + italic + "Displays the effects of a given input without actually running." + reset);
		System.out.println(italic + "\n\nLicenser is work in progress software. " + bold + "Use at your own risk!" + reset);
		System.out.println(italic
				+ "\n\nLicenser is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.");
		System.out.println(
				"\nLicenser is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.");
		System.out.println(
				"\nYou should have received a copy of the GNU General Public License along with Licenser. If not, see <https://www.gnu.org/licenses/>."
						+ reset);
		System.out.println("\n\nThis Licenser has spider powers 🕷️ 🕸️"); // spiders 🕷️ 🕸️
		System.exit(0);
	}
}
