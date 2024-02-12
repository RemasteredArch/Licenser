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

import java.util.Scanner;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.lang.Process;
import java.lang.Runtime;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Stack;

public class Licenser {
	private final static String version = "v0.1";

	private final static String reset = "\033[0m";
	private final static String bold = "\033[1m";
	private final static String italic = "\033[3m";

	private static File inputPath;
	private static boolean isDryRun;
	private static File copyrightNoticeTemplate = new File("/home/arch/dev/Licenser/license_notice_template.txt");
	private static boolean skipGitCheck;
	private static Stack<File> files;

	public static void main(String[] args) {
		parseOptions(args);
		if (!skipGitCheck)
			checkForGit(inputPath);
		System.out.println("Continuing...");
		// print(inputPath, copyrightNoticeTemplate);
		// print(inputPath);
	}

	private static void checkForGit(File file) {
		String path = file.toString();
		String[] command = { "git", "ls-files", "--error-unmatch", path };
		try {
			Process git = new ProcessBuilder(command).start();
			int exitValue = git.exitValue();
			if (exitValue == 0) // if project is tracked by git
				return;
		} catch (IOException e) {
		}
		String object = file.isDirectory() ? "directory" : "folder"; // if project is not tracked by git or git is not
																																	// installed
		System.out.println("This " + object
				+ " is not tracked by git. Licenser is not guaranteed to work perfectly, and may make irreversible changes. Are you sure you want to continue? (--ignore-git to ignore this check)");
		String response;
		do {
			System.out.print(bold + "(y/n): " + reset);
			Scanner input = new Scanner(System.in);
			response = input.next();
		} while (!response.equals("y") && !response.equals("n"));
		if (response == "n")
			System.exit(0); // this is probably *not* the right way
	}

	private static void print(File path) {
		if (path.isDirectory()) {
			printDirectoryRecursive(path, false, 0);
		} else {
			printFileContents(path);
		}
	}

	private static void print(File path, File copyrightNoticeTemplate) {
		if (path.isDirectory()) {
			printDirectoryRecursive(path, false, 0);
		} else {
			printFileContents(path, copyrightNoticeTemplate);
		}
	}

	private static void printFileContents(File file) {
		try {
			System.out.println(Files.readString(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printFileContents(File file, File copyrightNoticeTemplate) {
		try {
			System.out.println(Files.readString(copyrightNoticeTemplate.toPath()) + Files.readString(file.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printDirectoryRecursive(File directory, boolean showHidden, int depth) {
		String indent = "";
		for (int i = 0; i < depth - 1; i++) {
			indent += "  ";
		}

		System.out.println(indent + directory + ":");
		indent += "  ";

		for (File file : directory.listFiles()) {
			if (!file.isHidden()) {
				if (file.isDirectory()) {
					printDirectoryRecursive(file, showHidden, depth + 1);
				} else {
					System.out.println(indent + file);
				}
			}
		}
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
				System.exit(0); // this might be the *wrong* way to do it
			}
			if (arg.equals("--ignore-git")) {
				skipGitCheck = true;
			}
			if (i == 0) {
				inputPath = new File(arg);
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
		System.out.println("  --ignore-git    " + italic + "Ignores safety check that git is in use.");
		System.out.println("\n\nLicenser is work in progress software. " + bold + "Use at your own risk!" + reset);
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
