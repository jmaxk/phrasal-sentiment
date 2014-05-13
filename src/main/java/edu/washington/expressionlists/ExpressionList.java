package edu.washington.expressionlists;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.washington.expressionlists.types.BaseExpression;
import edu.washington.expressionlists.types.Expression;
import edu.washington.util.StanfordAnnotator;

public class ExpressionList {

	private List<Expression> expressions = new ArrayList<Expression>();

	public List<Expression> getExpressions() {
		return expressions;
	}

	public ExpressionList(List<Expression> expressions) {
		this.expressions = expressions;
	}

	public ExpressionList(Path listPath) {
		load(listPath);
	}

	private void load(Path list) {
		try {
			BufferedReader b = Files.newBufferedReader(list,
					StandardCharsets.UTF_8);
			String line = b.readLine();
			String currentCategory = line;
			List<String> wordsForCategory = new ArrayList<String>();

			while ((line = b.readLine()) != null) {
				if (line.startsWith("Category:")) {
					for (String word : wordsForCategory) {
						expressions.add(new BaseExpression(word,
								currentCategory));
					}
					currentCategory = line;
				} else {
					// "	Adam's apple" --> "Adam's apple"
					line = line.replaceAll("\t", "");
					wordsForCategory.add(line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void save(Path output) {
		try {

			BufferedWriter w = Files.newBufferedWriter(output,
					StandardCharsets.UTF_8);

			Map<String, List<Expression>> map = expressions.parallelStream()
					.collect(Collectors.groupingBy((e) -> e.getCategory()));
			for (Entry<String, List<Expression>> expressionsForCategory : map
					.entrySet()) {
				w.append(expressionsForCategory.getKey() + "\n");
				for (Expression expression : expressionsForCategory.getValue()) {
					w.append("\t" + expression.getText() + "\n");
				}
			}
			w.flush();
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveAsSemcor(Path output) {
		try {
			StanfordAnnotator annotator = StanfordAnnotator.getInstance();
			BufferedWriter w = Files.newBufferedWriter(output,
					StandardCharsets.UTF_8);
			for (Expression e : expressions) {
				String text = e.getText();
				String suffix = findSuffix(annotator.annotate(text));
				w.append(text.replaceAll(" ", "_") + suffix + "\n");
			}
			w.flush();
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String findSuffix(List<CoreLabel> tokens) {
		int numVerbs = 0;
		for (int i = 0; i < tokens.size(); i++) {
			CoreLabel token = tokens.get(i);
			String pos = token.get(PartOfSpeechAnnotation.class);
			if (pos.startsWith("V")) {
				numVerbs++;
			}
		}
		if (numVerbs != 0)
			return "_V 1,1,1,1,1";
		else
			return "_N 1,1,1,1,1";
	}

	

}
