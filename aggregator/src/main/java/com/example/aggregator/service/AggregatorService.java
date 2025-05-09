package com.example.aggregator.service;

import com.example.aggregator.client.AggregatorRestClient;
import com.example.aggregator.model.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.IntStream;


import java.util.ArrayList;
import java.util.List;

@Service
public class AggregatorService {

    private static final Logger logger = LoggerFactory.getLogger(AggregatorService.class);

    private final AggregatorRestClient aggregatorRestClient;

    public AggregatorService(AggregatorRestClient aggregatorRestClient) {
        this.aggregatorRestClient = aggregatorRestClient;
    }

    public Entry getDefinitionFor(String word) {
        return aggregatorRestClient.getDefinitionFor(word);
    }

    public List<Entry> getWordsThatContainSuccessiveLettersAndStartsWith(String chars) {

        List<Entry> wordsThatStartWith = aggregatorRestClient.getWordsStartingWith(chars);
        List<Entry> wordsThatContainSuccessiveLetters = aggregatorRestClient.getWordsThatContainConsecutiveLetters();

        // stream API version
        List<Entry> common = wordsThatStartWith.stream()
                .filter(wordsThatContainSuccessiveLetters::contains)
                .toList();

        /*List<Entry> common = new ArrayList<>(wordsThatStartWith);
        common.retainAll(wordsThatContainSuccessiveLetters);*/

        return common;

    }

    public List<Entry> getWordsThatContain(String chars) {

        return aggregatorRestClient.getWordsThatContain(chars);

    }
    public List<Entry> getAllPalindromes() {
        final List<Entry> candidates = new ArrayList<>();
        try {
            IntStream.range('a', '{')
                    .mapToObj(i -> Character.toString((char) i))
                    .forEach(c -> {
                        try {
                            List<Entry> startsWith = aggregatorRestClient.getWordsStartingWith(c);
                            List<Entry> endsWith = aggregatorRestClient.getWordsEndingWith(c);
                            List<Entry> startsAndEndsWith = new ArrayList<>(startsWith);
                            startsAndEndsWith.retainAll(endsWith);
                            candidates.addAll(startsAndEndsWith);
                        } catch (Exception innerEx) {
                            logger.error("Error fetching words for letter {}: {}", c, innerEx.getMessage(), innerEx);
                        }
                    });
            List<Entry> palindromes = candidates.stream()
                    .filter(entry -> {
                        String word = entry.getWord();
                        String reverse = new StringBuilder(word).reverse().toString();
                        return word.equals(reverse);
                    })
                    .sorted((e1, e2) -> e1.getWord().compareTo(e2.getWord()))
                    .collect(Collectors.toList());
            logger.info("getAllPalindromes() returning: {}", palindromes);
            return palindromes;
        } catch (Exception mainEx) {
            logger.error("Error in getAllPalindromes: {}", mainEx.getMessage(), mainEx);
            return new ArrayList<>(); // Ensure an empty list is returned on error
        }
    }
}

