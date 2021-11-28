package com.mercateo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.mercateo.dto.PackageDetails;

public class Analyzer {

	private static final int MAX_WEIGHT_OF_PACKAGE = 100;

	private static final int TOTAL_NOS_OF_ITEMS = 15;

	private static final int MAX_WEIGHT_PER_ITEM = 100;

	private static final int MAX_COST_PER_ITEM = 100;

	// ?? can hold?
	private static final String CONSTRAINT_VIOLATION_MAX_PACKAGE_WEIGHT = "Constraint Voilation- The maximum weight that a package can hold must be <= 100.";

	private static final String CONSTRAINT_VIOLATION_MAX_ITEM = "Constraint Voilation- There may be up to 15 items you can to choose from.";

	private static final String CONSTRAINT_VIOLATION_ITEM_WEIGHT = "Constraint Voilation- The maximum weight of an item should be <= 100";

	private static final String CONSTRAINT_VIOLATION_ITEM_COST = "Constraint Voilation- The maximum cost of an item should be <= €100.";

	private static final String regex = Pattern.quote("(") + "(.*?)" + Pattern.quote(")");

	/**
	 * Method will call get filterinput then call analyseallpackages
	 * 
	 * @param filePath path of file to be read
	 */
	protected void startAnalysis(final String filePath) {
		List<String> input = getFilteredInput(filePath);

		analyseAllPackages(input);
	}

	/**
	 * Method will analyze test cases line by line throws corresponding msg for any
	 * constraint violation
	 * 
	 * @param lines lines is filtered list of string read from input file
	 */
	private void analyseAllPackages(List<String> lines) {
		for (String line : lines) {
			if (!line.contains(":")) {
				// print empty line if line is empty
				System.out.println(line);
				continue;
			}
			String[] splitLine = line.split(":");
			int packageWeightLimit = Integer.parseInt(splitLine[0]);
			if (packageWeightLimit > MAX_WEIGHT_OF_PACKAGE) {
				throw new RuntimeException(CONSTRAINT_VIOLATION_MAX_PACKAGE_WEIGHT);
			}
			Map<Double, List<PackageDetails>> packageDetails = getSortedPackageDetails(line, packageWeightLimit);
			if (packageDetails.isEmpty()) {
				System.out.println("-");
				continue;
			}
			findNoOfPackage(packageDetails, packageWeightLimit);
		}
	}

	/**
	 * Method will iterate over map and nos of items that can be put in package
	 * 
	 * @param packageDetails     map of PackageDetails group by cost sort
	 * @param packageWeightLimit weight limit of package
	 */
	private void findNoOfPackage(Map<Double, List<PackageDetails>> packageDetails, int packageWeightLimit) {
		List<String> result = new ArrayList<>();
		Double totalCost = 0d;
		Double totalWeight = 0d;
		for (Entry<Double, List<PackageDetails>> map : packageDetails.entrySet()) {
			if (map.getValue().size() == 1) {
				PackageDetails packageDetail = map.getValue().get(0);
				if (totalWeight + packageDetail.getWeight() <= packageWeightLimit) {
					if (packageDetail.getWeight() > MAX_WEIGHT_PER_ITEM) {
						throw new RuntimeException(CONSTRAINT_VIOLATION_ITEM_WEIGHT);
					}
					if (packageDetail.getCost() > MAX_COST_PER_ITEM) {
						throw new RuntimeException(CONSTRAINT_VIOLATION_ITEM_COST);
					}

					totalCost += packageDetail.getCost();
					totalWeight += packageDetail.getWeight();
					result.add(packageDetail.getIndex().toString());
				} else {
					break;
				}

			} else {
				for (PackageDetails packageDetail : map.getValue()) {
					if (totalWeight + packageDetail.getWeight() <= packageWeightLimit) {
						if (packageDetail.getWeight() > MAX_WEIGHT_PER_ITEM) {
							throw new RuntimeException(CONSTRAINT_VIOLATION_ITEM_WEIGHT);
						}
						if (packageDetail.getCost() > MAX_COST_PER_ITEM) {
							throw new RuntimeException(CONSTRAINT_VIOLATION_ITEM_COST);
						}
						totalCost += packageDetail.getCost();
						totalWeight += packageDetail.getWeight();
						result.add(packageDetail.getIndex().toString());
					} else {
						continue;
					}
				}

			}
		}
		// if no item is eligible then log - else log with comma separated
		if (result.isEmpty()) {
			System.out.println("-");
		} else {
			System.out.println(result.stream().collect(Collectors.joining(",")));
		}
	}

	/**
	 * validate data and throws constraint violation if any
	 * 
	 * @param line               input from txt file
	 * @param packageWeightLimit packageWeightLimit
	 * @return Packagedetails -> filter with weight-> sort by cost desc-> sorted by
	 *         weight asc-> group by cost -> sorted by cost desc
	 */
	private Map<Double, List<PackageDetails>> getSortedPackageDetails(String line, int packageWeightLimit) {
		List<String> rawData = extractDataBetParenthesis(line);
		List<PackageDetails> packageDetails = new ArrayList<>();
		for (String row : rawData) {
			String[] rowSplit = row.split(",");

			double weight = Double.parseDouble(rowSplit[1]);
			if (weight > MAX_WEIGHT_PER_ITEM) {
				throw new RuntimeException(CONSTRAINT_VIOLATION_ITEM_WEIGHT);
			}
			double cost = Double.parseDouble(rowSplit[2]);
			if (cost > MAX_COST_PER_ITEM) {
				throw new RuntimeException(CONSTRAINT_VIOLATION_ITEM_COST);
			}

			packageDetails.add(new PackageDetails(Integer.parseInt(rowSplit[0]), weight, cost));
		}
		if (packageDetails.size() > TOTAL_NOS_OF_ITEMS) {
			throw new RuntimeException(CONSTRAINT_VIOLATION_MAX_ITEM);
		}
		// <=?
		return new TreeMap<>(packageDetails.stream().filter(x -> (x.getWeight() <= packageWeightLimit))
				.sorted(Comparator.comparing(PackageDetails::getCost).reversed()
						.thenComparing(Comparator.comparing(PackageDetails::getWeight)))
				.collect(Collectors.groupingBy(PackageDetails::getCost))).descendingMap();
	}

	/**
	 * read input from given file and filter it by removing whitespaces and €
	 * 
	 * @param filePath file path to be read
	 * @return line by line in list
	 */
	private List<String> getFilteredInput(String filePath) {
		try {
			return Files.readAllLines(Paths.get(filePath)).stream().map(y -> y.replaceAll(" ", "").replaceAll("€", ""))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException("Unable to read file");
		}
	}

	/**
	 * Regex will find all string between Parenthesis and return in list of string
	 * 
	 * @param str string from which data to be extracted
	 * @return list of string
	 */
	private List<String> extractDataBetParenthesis(String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		List<String> lst = new ArrayList<String>();
		while (matcher.find()) {
			String textInBetween = matcher.group(1);
			lst.add(textInBetween);
		}
		return lst;
	}

}
