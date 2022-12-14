package com.github.nikyotensai.elf.common.profiler.compact;

import java.util.HashMap;
import java.util.Map;

class TrieNode {

    private final Map<Character, TrieNode> children = new HashMap<>();
    private boolean endOfWord;

    Map<Character, TrieNode> getChildren() {
        return children;
    }

    boolean isEndOfWord() {
        return endOfWord;
    }

    void setEndOfWord(boolean endOfWord) {
        this.endOfWord = endOfWord;
    }

    void addChild(char childChar, TrieNode trieNode) {
        children.put(childChar, trieNode);
    }
}