package com.mercateo;

public class Application {

	public static void main(String[] args) {
		if (args.length != 1) {
			throw new RuntimeException("Only one args is expected");
		}

		Analyzer application = new Analyzer();
		application.startAnalysis(args[0]);
	}

}
