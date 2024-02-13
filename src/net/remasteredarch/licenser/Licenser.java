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
import java.util.ArrayList;
import java.util.HashMap;

public class Licenser {
	private final static String version = "v0.1";

	private final static String reset = "\033[0m";
	private final static String bold = "\033[1m";
	private final static String italic = "\033[3m";

	private static File inputPath;
	private static boolean isDryRun;
	private static boolean actOnHidden;
	private static File copyrightNoticeTemplate = new File("/home/arch/dev/Licenser/templates/java.txt");
	private static boolean skipGitCheck;
	private static Stack<File> files = new Stack<>();
	private static ArrayList<Item> items = new ArrayList<>();
	private static String[] codeFileExtensions = { ".java" };

	public static void main(String[] args) {
		parseOptions(args);

		if (!skipGitCheck)
			checkForGit(inputPath);

		getFileList(inputPath);

		getItems();

		// print(inputPath, copyrightNoticeTemplate);
		// print(inputPath);
	}

	private static void getItems() {
		for (File file : files) {
			ArrayList<Commit> commits = getGitLog(file);
			HashMap<String, String> authors = getAuthors(commits);
			items.add(new Item(file, authors));
		}
	}

	private static HashMap<String, String> getAuthors(ArrayList<Commit> commits) {
		HashMap<String, ArrayList<String>> authors = new HashMap<>();

		for (Commit commit : commits) {
			String author = commit.authorName;
			String year = commit.commitYear;

			if (authors.get(author) == null) {
				ArrayList<String> years = new ArrayList<>();
				years.add(year);
				authors.put(author, years);
			} else {
				authors.get(author).add(year);
			}
		}

		HashMap<String, String> authorsWithYearRange = new HashMap<>();

		for (String author : authors.keySet()) {
			System.out.println("Author: " + author + " Years: ");
		}

		return authorsWithYearRange;

	}

	private static ArrayList<Commit> getGitLog(File file) {
		ArrayList<Commit> commits = new ArrayList<>();

		String path = file.toString();
		String[] command = { "git", "log", "--author-date-order", "--reverse", "--date=short", "--pretty=format:%an\n%as",
				path };

		try {
			Process git = new ProcessBuilder(command).start();
			try {
				git.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			BufferedReader stdout = git.inputReader(); // this is limited 8192 chars, or about 300 commits!!
			while (stdout.ready()) { // until empty, parse ouput
				String authorName = stdout.readLine(); // e.g. RemasteredArch
				String commitYear = stdout.readLine().substring(0, 4); // e.g. 2024-02-12 -> 2024
				Commit commit = new Commit(authorName, commitYear);
				commits.add(commit);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return commits;
	}

	private static boolean checkExtension(File file) {
		for (String extension : codeFileExtensions) {
			int fileLength = file.toString().length();
			int extensionLength = extension.length();
			String inputExtension = file.toString().substring(fileLength - extensionLength, fileLength);
			if (inputExtension.equals(extension))
				return true;
		}
		return false;
	}

	private static void getFileList(File file) {
		if (file.isFile()) {
			if (checkExtension(file))
				files.push(file);
			return;
		}

		for (File item : file.listFiles()) {
			if (!item.isHidden() || actOnHidden) {
				if (file.isDirectory()) {
					getFileList(item);
				} else if (checkExtension(file)) {
					files.push(item);
				}
			}
		}
	}

	private static void checkForGit(File file) {
		String path = file.toString();
		String[] command = { "git", "ls-files", "--error-unmatch", path };
		try {
			Process git = new ProcessBuilder(command).start();
			try {
				Thread.sleep(500); // there has got to be a better way than this
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int exitValue = git.exitValue();
			if (exitValue == 0) // if project is tracked by git
				return;
		} catch (IOException e) {
		}
		String object = file.isDirectory() ? "directory" : "file"; // if project is not tracked by git or git is not
																																// installed
		System.out.println("This " + object
				+ " is not tracked by git. Licenser is not guaranteed to work perfectly, and may make irreversible changes. Are you sure you want to continue? (--ignore-git to ignore this check)");
		String response;
		do {
			System.out.print(bold + "(y/n): " + reset);
			Scanner input = new Scanner(System.in);
			response = input.next();
		} while (!response.equals("y") && !response.equals("n"));
		if (response.equals("n"))
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
			if (arg.equals("--hidden")) {
				actOnHidden = true;
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

class Item {
	public File originalFile;
	// public File tempFile;
	public HashMap<String, String> authors;

	public Item(File originalFile, HashMap<String, String> authors) {
		this.originalFile = originalFile;
		// this.tempFile = tempFile;
		this.authors = authors;
	}
}

class Commit {
	public String authorName;
	public String commitYear;

	public Commit(String authorName, String commitYear) {
		this.authorName = authorName;
		this.commitYear = commitYear;
	}

	@Override
	public String toString() {
		return authorName + ", " + commitYear;
	}
}
