package com.learnmate.ai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// 简单的本地向量实现：中文单字/二元组和英文词哈希到 256 维。
@Service
public class EmbeddingService {

    public static final int DIMENSION = 256;

    public double[] embed(String text) {
        double[] vector = new double[DIMENSION];
        for (String token : tokenize(text)) {
            int hash = token.hashCode();
            int index = Math.floorMod(hash, DIMENSION);
            double sign = ((hash >>> 8) & 1) == 0 ? 1.0 : -1.0;
            vector[index] += sign;
        }
        normalize(vector);
        return vector;
    }

    public String toCsv(double[] vector) {
        StringBuilder builder = new StringBuilder(vector.length * 6);
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(String.format(Locale.ROOT, "%.5f", vector[i]));
        }
        return builder.toString();
    }

    public double[] fromCsv(String csv) {
        String[] parts = csv.split(",");
        double[] vector = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Double.parseDouble(parts[i]);
        }
        return vector;
    }

    public double cosine(double[] left, double[] right) {
        int length = Math.min(left.length, right.length);
        double dot = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (int i = 0; i < length; i++) {
            dot += left[i] * right[i];
            leftNorm += left[i] * left[i];
            rightNorm += right[i] * right[i];
        }
        if (leftNorm == 0 || rightNorm == 0) {
            return 0;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    // 查询词在标题/章节中的覆盖率，用于简单重排。
    public double overlap(String query, String target) {
        Set<String> queryTokens = new HashSet<>(tokenize(query));
        Set<String> targetTokens = new HashSet<>(tokenize(target));
        if (queryTokens.isEmpty() || targetTokens.isEmpty()) {
            return 0;
        }
        int hit = 0;
        for (String token : queryTokens) {
            if (targetTokens.contains(token)) {
                hit++;
            }
        }
        return (double) hit / queryTokens.size();
    }

    private List<String> tokenize(String text) {
        String normalized = text == null ? "" : text.toLowerCase(Locale.ROOT)
                .replaceAll("[\\u200B-\\u200D\\uFEFF]", " ");
        List<String> tokens = new ArrayList<>();
        StringBuilder hanRun = new StringBuilder();
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.UnicodeScript.of(ch) == Character.UnicodeScript.HAN) {
                flushWord(word, tokens);
                hanRun.append(ch);
            } else if (Character.isLetterOrDigit(ch)) {
                flushHan(hanRun, tokens);
                word.append(ch);
            } else {
                flushHan(hanRun, tokens);
                flushWord(word, tokens);
            }
        }
        flushHan(hanRun, tokens);
        flushWord(word, tokens);
        return tokens;
    }

    private void flushHan(StringBuilder run, List<String> tokens) {
        if (run.isEmpty()) {
            return;
        }
        for (int i = 0; i < run.length(); i++) {
            tokens.add(String.valueOf(run.charAt(i)));
            if (i + 1 < run.length()) {
                tokens.add(run.substring(i, i + 2));
            }
        }
        run.setLength(0);
    }

    private void flushWord(StringBuilder word, List<String> tokens) {
        if (!word.isEmpty()) {
            tokens.add(word.toString());
            word.setLength(0);
        }
    }

    private void normalize(double[] vector) {
        double norm = 0;
        for (double value : vector) {
            norm += value * value;
        }
        if (norm == 0) {
            return;
        }
        double denominator = Math.sqrt(norm);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= denominator;
        }
    }
}
